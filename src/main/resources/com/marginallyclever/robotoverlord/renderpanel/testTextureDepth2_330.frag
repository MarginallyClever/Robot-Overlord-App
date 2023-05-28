// display debug texture frag shader
#version 330 core

in vec2 TexCoord;

out vec4 FragColor;

uniform sampler2D debugTexture;

void main() {
    float scale = 1.0;
    FragColor = vec4(vec3(texture(debugTexture, TexCoord).r * scale), 1.0);
}
