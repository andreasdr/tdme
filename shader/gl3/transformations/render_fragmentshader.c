#version 330

uniform sampler2D diffuseTextureUnit;

in vec2 fragTextureUV;
in vec4 fragColor;
out vec4 outColor;

void main() {
	outColor = texture(diffuseTextureUnit, fragTextureUV) * fragColor;
}
