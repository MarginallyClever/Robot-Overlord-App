// fragment shader
// draw only the color of the vertex
#version 330 core

in vec4 fragmentColor;

out vec4 finalColor;

void main() {
    finalColor = fragmentColor;
}
