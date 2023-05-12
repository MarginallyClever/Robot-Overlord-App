// fragment shader
// ignore all data, just output red
#version 330 core

in vec3 FragPos;
in vec3 Normal;

out vec4 FragColor;

uniform vec3 lightPos; // Light position in world space
uniform vec3 cameraPos;  // Camera position in world space
uniform vec3 objectColor;
uniform vec3 lightColor;

void main() {
    FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
