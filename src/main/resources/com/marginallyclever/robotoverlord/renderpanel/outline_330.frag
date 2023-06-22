#version 330 core

out vec4 fragColor;

uniform vec4 outlineColor;
uniform vec4 objectColor;

void main() {
    fragColor = outlineColor;
}
