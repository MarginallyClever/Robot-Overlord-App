// display debug texture frag shader
#version 330 core

in vec2 TexCoord;

out vec4 FragColor;

uniform sampler2D debugTexture;

void main() {
    // check the texture coordinates.
    // should display black, red, yellow, and green in the four corners, blending between each.
    FragColor = vec4(TexCoord.x,TexCoord.y, 0, 1.0);
}
