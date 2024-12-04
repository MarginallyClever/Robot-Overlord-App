#version 330 core

layout(location = 0) in vec3 aPosition;

uniform mat4 lightProjectionMatrix;
uniform mat4 lightViewMatrix;
uniform mat4 lightSpaceMatrix;
uniform mat4 modelMatrix;

void main() {
    //gl_Position = lightSpaceMatrix * modelMatrix * vec4(aPosition, 1.0);

    vec4 worldPose = modelMatrix * vec4(aPosition, 1.0);
    gl_Position = lightProjectionMatrix * lightViewMatrix * worldPose;
}