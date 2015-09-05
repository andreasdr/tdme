// Based on:
//	of some code of 3Dlabs Inc. Ltd.
//	and http://stackoverflow.com/questions/11365399/opengl-shader-a-spotlight-and-a-directional-light?answertab=active#tab-top
/************************************************************************
*                                                                       *                                                                       
*                                                                       *
*        Copyright (C) 2002-2004  3Dlabs Inc. Ltd.                      *
*                                                                       *
*                        All rights reserved.                           *
*                                                                       *
* Redistribution and use in source and binary forms, with or without    *
* modification, are permitted provided that the following conditions    *
* are met:                                                              *
*                                                                       *
*     Redistributions of source code must retain the above copyright    *
*     notice, this list of conditions and the following disclaimer.     *
*                                                                       *
*     Redistributions in binary form must reproduce the above           *
*     copyright notice, this list of conditions and the following       *
*     disclaimer in the documentation and/or other materials provided   *
*     with the distribution.                                            *
*                                                                       *
*     Neither the name of 3Dlabs Inc. Ltd. nor the names of its         *
*     contributors may be used to endorse or promote products derived   *
*     from this software without specific prior written permission.     *
*                                                                       *
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS   *
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT     *
* LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS     *
* FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE        *
* COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, *
* INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,  *
* BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;      *
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER      *
* CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT    *
* LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN     *
* ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE       *
* POSSIBILITY OF SUCH DAMAGE.                                           *
*                                                                       *
************************************************************************/

#define FALSE		0
#define MAX_LIGHTS	8
#define MAX_JOINTS	60

struct Material {
	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	vec4 emission;
	float shininess;
};

struct Light {
	int enabled;
	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	vec4 position;
	vec3 spotDirection;
	float spotExponent;
	float spotCosCutoff;
	float constantAttenuation;
	float linearAttenuation;
	float quadraticAttenuation;
};

// standard layouts
attribute vec3 inVertex;
attribute vec3 inNormal;
attribute vec2 inTextureUV;

// skinning layouts
attribute float inSkinningVertexJoints;
attribute vec4 inSkinningVertexJointIdxs;
attribute vec4 inSkinningVertexJointWeights;

// uniforms
uniform mat4 mvpMatrix;
uniform mat4 mvMatrix;
uniform mat4 normalMatrix;

uniform vec4 sceneColor;
uniform vec4 effectColorMul;
uniform Material material;
uniform Light lights[MAX_LIGHTS];

uniform mat4 skinningJointsTransformationsMatrices[MAX_JOINTS];
uniform int skinningEnabled;

// will be passed to fragment shader
varying vec2 fragTextureUV;
varying vec4 fragColor;

void computeLight(in int i, in vec3 normal, in vec3 position) {
	vec3 L = lights[i].position.xyz - position;
	float d = length(L);
	L = normalize(L);
	vec3 E = normalize(-position);
	vec3 R = normalize(-reflect(L,normal));

	// compute attenuation
	float attenuation =
		1.0 /
		(
			lights[i].constantAttenuation +
			lights[i].linearAttenuation * d +
			lights[i].quadraticAttenuation * d * d
		);
 
	// see if point on surface is inside cone of illumination
	float spotDot = dot(-L, normalize(lights[i].spotDirection));
	float spotAttenuation = 0.0;
	if (spotDot >= lights[i].spotCosCutoff) {
		spotAttenuation = pow(spotDot, lights[i].spotExponent);
	}

	// Combine the spotlight and distance attenuation.
	attenuation *= spotAttenuation;

	// add color components to fragment color
	fragColor+=
		clamp(lights[i].ambient * material.ambient, 0.0, 1.0) +
		clamp(lights[i].diffuse * material.diffuse * max(dot(normal,L), 0.0) * attenuation, 0.0, 1.0) +
		clamp(lights[i].specular * material.specular * pow(max(dot(R,E), 0.0), 0.3 * material.shininess) * attenuation, 0.0, 1.0);
}

void computeLights(in vec3 normal, in vec3 position) {
	// process each light
	for (int i = 0; i < MAX_LIGHTS; i++) {
		// skip on disabled lights
		if (lights[i].enabled == FALSE) continue;

		// compute light
		computeLight(i, normal, position);
	}
}
 
 
void main(void) {
	// pass texture uv to fragment shader
	fragTextureUV = inTextureUV;

	//
	fragColor = vec4(0.0, 0.0, 0.0, 0.0);
	fragColor+= clamp(sceneColor, 0.0, 1.0);
	fragColor+= clamp(material.emission, 0.0, 1.0);

	// do skinning
	vec4 skinnedInVertex = vec4(0.0, 0.0, 0.0, 0.0);
	vec3 skinnedInNormal = vec3(0.0, 0.0, 0.0);
	if (skinningEnabled == 1) {
		float totalWeights = 0.0;
		int _inSkinningVertexJoints = int(inSkinningVertexJoints);
		for (int i = 0; i < 4; i++) {
			if (_inSkinningVertexJoints > i) {
				int inSkinningVertexJointIdx = int(inSkinningVertexJointIdxs[i]);
				mat4 transformationsMatrix =
					skinningJointsTransformationsMatrices[inSkinningVertexJointIdx];
				skinnedInVertex+=
						(transformationsMatrix * vec4(inVertex, 1.0)) * inSkinningVertexJointWeights[i];
				skinnedInNormal+=
						(mat3(transformationsMatrix) * inNormal) * inSkinningVertexJointWeights[i];
				totalWeights+= inSkinningVertexJointWeights[i];
			}
		}

		// scale to full weight
		if (totalWeights != 1.0) {
			float weightNormalized = totalWeights != 0.0?1.0 / totalWeights:0.0;

			// vertex
			skinnedInVertex*= weightNormalized;
			skinnedInNormal*= weightNormalized;
		}

		// this is a vertex now
		skinnedInVertex.w = 1.0;
	} else {
		skinnedInVertex = vec4(inVertex, 1.0);
		skinnedInNormal = vec3(inNormal);
	}

	// compute gl position
	gl_Position = mvpMatrix * skinnedInVertex;

	// Eye-coordinate position of vertex, needed in various calculations
	vec4 position4 = mvMatrix * skinnedInVertex;
	vec3 position = position4.xyz / position4.w;

	// compute the normal
	vec3 normal = normalize(vec3(normalMatrix * vec4(skinnedInNormal, 0.0)));
 
	// compute lights
	computeLights(normal, position);

	// take effect colors into account
	fragColor = fragColor * effectColorMul;
	fragColor.a = material.diffuse.a * effectColorMul.a;
}
