// JFA vertex shader
#version 330 core

layout(location = 0) in vec3 aPosition;

out vec2 TexCoords;

void main() {
    gl_Position = vec4(aPosition, 1.0);
    TexCoords = aPosition.xy;
}