#version 330 core

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec4 aColor;
layout(location = 3) in vec2 aTexture;


out vec2 TexCoord;

void main() {
    gl_Position = vec4(aPosition, 1.0);
    TexCoord = aTexture;
}
