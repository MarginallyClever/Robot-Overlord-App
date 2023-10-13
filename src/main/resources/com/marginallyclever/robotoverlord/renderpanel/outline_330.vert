#version 330 core

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec4 aColor;
layout(location = 3) in vec2 aTexture;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

uniform float outlineSize;

void main() {
    vec3 offsetPosition = aPosition + aNormal * outlineSize;
    vec4 worldPose = modelMatrix * vec4(offsetPosition, 1.0);
    gl_Position = projectionMatrix * viewMatrix * worldPose;
}
