// based on http://fabiensanglard.net/shadowmapping/index.php, modified by me

#define SHADOWMAP_LOOKUPS	4

precision mediump float;

uniform sampler2D textureUnit;
uniform float texturePixelWidth;
uniform float texturePixelHeight;

varying vec4 shadowCoord;
varying float shadowIntensity;



void main() {
	// do not process samples out of frustum
	if (shadowCoord.w == 0.0) {
		// return color to be blended with framebuffer
		gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
	} else {
		// depth bias
		float depthBias = 0.0;

		// determine visibility
		float visibility = 0.0;
		for (float y = (-SHADOWMAP_LOOKUPS + 0.5) / 2; y <= (+SHADOWMAP_LOOKUPS - 0.5) / 2; y+=1.0)
		for (float x = (-SHADOWMAP_LOOKUPS + 0.5) / 2; x <= (+SHADOWMAP_LOOKUPS - 0.5) / 2; x+=1.0) {
			visibility+= texture2D(textureUnit, shadowCoord.xy + vec2(x * texturePixelWidth, y * texturePixelHeight)).x < shadowCoord.z + depthBias?0.3:0.0;
		}
		visibility = visibility / (SHADOWMAP_LOOKUPS * SHADOWMAP_LOOKUPS);

		// return color to be blended with framebuffer
		gl_FragColor = vec4(0.0, 0.0, 0.0, visibility * shadowIntensity);
	}
}
