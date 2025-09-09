#version 330 core

uniform vec4 outlineColor = vec4(0.0, 1.0, 0.0, 1.0);
uniform float outlineSize = 1.0;
uniform vec2 canvasSize;  // Size of the texture/screen

out vec4 finalColor;  // Output fragment color

// Sampler for the stencil texture (or depth-stencil data)
uniform sampler2D stencilTexture;

// the screen and the stencilTexture have dimensions from (-1,-1) to (1,1)
// the canvasSize, outlineSize, and gl_FragCoord are in pixels from (0,0) to (width,height)
void main() {
    // Size of a fragment in texture coordinates
    vec2 texelSize = 1.0 / canvasSize;
    // Current pixel position in the stencil texture
    vec2 textureCoord = gl_FragCoord.xy / canvasSize;
    vec4 stencilValue = texture(stencilTexture, textureCoord);

    // If the stencil value is not zero we're inside the stencil area so skip.
    if (stencilValue.r > 0.0) discard;

    int outInt = int(ceil(outlineSize));
    float o2 = outlineSize * outlineSize;
    // loop over all pixels within +/-outline size
    int smallest = 999;
    for (int y = -outInt; y <= outInt; y++) {
        for (int x = -outInt; x <= outInt; x++) {
            if(x*x + y*y > o2) continue; // Skip pixels outside the circle
            // convert pixel offset to texture coordinate offset
            vec2 offset = vec2(x, y) * texelSize;
            // Sample the stencil texture at the offset position
            vec4 neighbor = texture(stencilTexture, textureCoord + offset);
            if (neighbor.r > 0.0) {
                finalColor = outlineColor;
                return;
            }
        }
    }
    // nothing nearby, do nothing.
    //finalColor = vec4(0,0,1,1);  // make the background blue for testing
    //finalColor = vec4(0,(textureCoord.x+1)/2,(textureCoord.y+1)/2,1);  // gradient for testing
}