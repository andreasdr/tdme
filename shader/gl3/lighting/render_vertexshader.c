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

#version 330

#define MAX_JOINTS	60

// standard layouts
layout (location = 0) in vec3 inVertex;
layout (location = 1) in vec3 inNormal;
layout (location = 2) in vec2 inTextureUV;

// skinning layouts
layout (location = 4) in float inSkinningVertexJoints;
layout (location = 5) in vec4 inSkinningVertexJointIdxs;
layout (location = 6) in vec4 inSkinningVertexJointWeights;

// normal mapping
layout (location = 7) in vec3 inTangent;
layout (location = 8) in vec3 inBitangent;

// uniforms
uniform sampler2D displacementTextureUnit;
uniform int displacementTextureAvailable;

uniform int normalTextureAvailable;

uniform mat4 mvpMatrix;
uniform mat4 mvMatrix;
uniform mat4 normalMatrix;

uniform mat4 skinningJointsTransformationsMatrices[MAX_JOINTS];
uniform int skinningEnabled;

// will be passed to fragment shader
out vec2 vsFragTextureUV;
out vec3 vsPosition;
out vec3 vsNormal;
out vec3 vsTangent;
out vec3 vsBitangent;

void main(void) {
	// pass texture uv to fragment shader
	vsFragTextureUV = inTextureUV;

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
	if (displacementTextureAvailable == 1) {
		vec3 displacementVector = texture(displacementTextureUnit, inTextureUV).rgb * 2.0 - 1.0;
		/*
		float displacementLength = (displacementVector.x + displacementVector.y + displacementVector.z) / 3.0;
		skinnedInVertex-=
			vec4(normalize(skinnedInNormal) * displacementLength, 0.0);
		*/
	}

	//
	gl_Position = mvpMatrix * skinnedInVertex;

	// Eye-coordinate position of vertex, needed in various calculations
	vec4 vsPosition4 = mvMatrix * skinnedInVertex;
	vsPosition = vsPosition4.xyz / vsPosition4.w;

	// compute the normal
	vsNormal = normalize(vec3(normalMatrix * vec4(skinnedInNormal, 0.0)));

	//
	if (normalTextureAvailable == 1) {
		vsTangent = normalize(vec3(normalMatrix * vec4(inTangent, 0.0)));
		vsBitangent = normalize(vec3(normalMatrix * vec4(inBitangent, 0.0)));
	}

}
