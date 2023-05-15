#version 330 core

out vec4 fragColor;

uniform vec4 outlineColor;

void main() {
    fragColor = outlineColor;
}
