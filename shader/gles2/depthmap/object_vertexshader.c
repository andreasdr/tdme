#version 100

attribute vec3 inVertex;
attribute vec3 inNormal;
attribute vec2 inTextureUV;

uniform mat4 mvpMatrix;

varying vec3 vsPosition;

void main(){
	vsPosition = mvpMatrix * vec4(inVertex, 1.0);
	gl_Position = vsPosition;
}
