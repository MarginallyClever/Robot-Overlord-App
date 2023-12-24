// Outline fragment shader
#version 330 core

in vec2 TexCoords;

out vec4 FragColor;

uniform sampler2D jfaOutput;
uniform vec4 outlineColor;
uniform float outlineSize;
uniform vec2 screenSize;

void main() {
    // Fetch the distance from the nearest seed
    float distance = texture(jfaOutput, TexCoords / screenSize).z;

    // Outline if the distance is within the thickness threshold
    if (distance < outlineSize) {
        FragColor = outlineColor;
    } else {
        // Make the fragment transparent if it's outside the outline
        FragColor = vec4(0.0, 0.0, 0.0, 0.0);
    }
}