#version 100

precision mediump float;

varying vec3 vsPosition;

// from Fabien Sangalard's DEngine
vec4 pack (float depth)
{
    const vec4 bitSh = vec4(256.0 * 256.0 * 256.0,
                            256.0 * 256.0,
                            256.0,
                            1.0);
    const vec4 bitMsk = vec4(0,
                             1.0 / 256.0,
                             1.0 / 256.0,
                             1.0 / 256.0);
    vec4 comp = fract(depth * bitSh);
    comp -= comp.xxyz * bitMsk;
    return comp;
}

void main() {
	// the depth
	float normalizedDistance  = vsPosition.z;

	// scale -1.0;1.0 to 0.0;1.0
	normalizedDistance = (normalizedDistance + 1.0) / 2.0;

	// pack value into 32-bit RGBA texture
	gl_FragColor = pack(normalizedDistance);
}
