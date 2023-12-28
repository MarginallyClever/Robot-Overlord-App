#version 330 core

layout(location = 0) in vec3 aPosition;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 lightSpaceMatrix;
uniform mat4 modelMatrix;

void main() {
    gl_Position = lightSpaceMatrix * modelMatrix * vec4(aPosition, 1.0);

    //vec4 worldPose = modelMatrix * vec4(aPosition, 1.0);
    //gl_Position = projectionMatrix * viewMatrix * worldPose;
}