// based on http://fabiensanglard.net/shadowmapping/index.php, modified by me

#version 100

attribute vec3 inVertex;
attribute vec3 inNormal;
attribute vec2 inTextureUV;

uniform mat4 depthBiasMVPMatrix;
uniform mat4 mvpMatrix;
uniform mat4 mvMatrix;
uniform mat3 normalMatrix;
uniform vec4 lightPosition;

varying vec4 shadowCoord;
varying float shadowIntensity;



void main() {
	shadowCoord = depthBiasMVPMatrix * vec4(inVertex, 1.0);
	shadowCoord = shadowCoord / shadowCoord.w;

	vec4 position = mvMatrix * vec4(0.0, 0.0, 0.0, 1.0);
	position = position / position.w;
	vec4 lightPositionTransformed = mvMatrix * lightPosition;
	lightPositionTransformed = lightPositionTransformed / lightPositionTransformed.w;
	vec3 normal = normalMatrix * inNormal;

	shadowIntensity = 1.0 - clamp(dot(normalize(position.xyz - lightPositionTransformed.xyz), normal), 0.0, 1.0);

	// compute gl position
	gl_Position = mvpMatrix * vec4(inVertex, 1.0);
}
