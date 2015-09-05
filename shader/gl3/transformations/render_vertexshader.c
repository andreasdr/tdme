#version 330

uniform mat4 mvpMatrix;

layout (location = 0) in vec3 inVertex;
layout (location = 1) in vec3 inNormal;
layout (location = 2) in vec2 inTextureUV;

out vec2 fragTextureUV;
out vec4 fragColor;

void main() {
   gl_Position = mvpMatrix * vec4(inVertex, 1.0);
   fragTextureUV = inTextureUV;
   fragColor = vec4(1.0f,1.0f,1.0f,1.0f);
}
