#version 330 core

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec3 aNormal;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform float outlineSize;

void main() {
    vec3 adj = aPosition + aNormal * outlineSize;
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(adj, 1.0);
}
