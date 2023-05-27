// display debug texture frag shader
#version 330 core

in vec2 TexCoord;

out vec4 FragColor;

uniform sampler2D debugTexture;

void main() {
    if(texture(debugTexture, TexCoord).r!=0) FragColor=vec4(1.0,0.0,0.0,0.5);
    else                                     FragColor=vec4(1.0,1.0,1.0,0.5);
}
