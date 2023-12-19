#version 330 core

uniform float d;
uniform float theta;
uniform float r;
uniform float alpha;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

in vec3 position;

void main() {
    // Calculate the new position
    vec3 newPosition = position;
    newPosition.z += d;
    newPosition = vec3(
        newPosition.x * cos(theta) - newPosition.y * sin(theta),
        newPosition.x * sin(theta) + newPosition.y * cos(theta),
        newPosition.z);
    newPosition.x += r;

    // Apply the world matrix
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(newPosition, 1.0);
}