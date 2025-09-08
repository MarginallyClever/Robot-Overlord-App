#version 330 core

uniform vec4 outlineColor = vec4(0.0, 1.0, 0.0, 1.0); // The color of the outline
uniform float outlineSize = 1.0;                      // Thickness of the outline
uniform vec2 textureSize;                             // Size of the texture/screen

// Output fragment color
out vec4 finalColor;

// Sampler for the stencil texture (or depth-stencil data)
uniform sampler2D stencilTexture;

void main() {
    // Size of a texel (to sample neighboring pixels)
    vec2 textureCoord = gl_FragCoord.xy / textureSize;
    // Detect neighboring pixels with non-zero stencil values (for the outline)
    vec2 texelSize = 1.0 / textureSize;

    // Current pixel position in the stencil texture
    vec4 stencilValue = texture(stencilTexture, textureCoord);
    // If the stencil value is not zero we're inside the stencil area so skip.
    if (stencilValue.r > 0.0) discard;

    // loop over all pixels within half outline size
    for (int y = int(-outlineSize); y <= int(outlineSize); y++) {
        for (int x = int(-outlineSize); x <= int(outlineSize); x++) {
            if(x*x+y*y > outlineSize*outlineSize) continue; // Skip pixels outside the circle

            vec2 offset = vec2(x, y) * texelSize;
            vec4 neighbor = texture(stencilTexture, textureCoord + offset);

            // Look for the edge where neighboring pixels have a difference in stencil value
            if (neighbor.r > 0.0) {
                finalColor = outlineColor;
                return;
            }
        }
    }

    // If no neighboring pixels are found with a stencil value do nothing.

    //finalColor = vec4(0,0,1,1);
    //finalColor = vec4(0,(textureCoord.x+1)/2,(textureCoord.y+1)/2,1);
}