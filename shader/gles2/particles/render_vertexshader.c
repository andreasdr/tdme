// layouts
attribute vec3 inVertex;
attribute vec4 inColor;

// uniforms
uniform mat4 mvpMatrix;
uniform float pointSize;

// will be passed to fragment shader
varying vec4 fragColor;
  
void main(void) {
	gl_PointSize = pointSize;

	//
	fragColor = inColor;

	// compute gl position
	gl_Position = mvpMatrix * vec4(inVertex, 1.0);
}
