#version 330

// layout
layout (location = 0) in vec3 inVertex;
layout (location = 3) in vec4 inColor;

// uniforms
uniform mat4 mvpMatrix;

// will be passed to fragment shader
out vec4 fragColor;
  
void main(void) {
	//
	fragColor = inColor;

	// compute gl position
	gl_Position = mvpMatrix * vec4(inVertex, 1.0);
}
