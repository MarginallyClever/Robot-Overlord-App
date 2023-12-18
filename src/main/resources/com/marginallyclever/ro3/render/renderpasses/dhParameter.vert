#version 330 core

uniform mat4 modelMatrix;
uniform float d;
uniform float theta;
uniform float r;
uniform float alpha;

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
    gl_Position = modelMatrix * vec4(newPosition, 1.0);
}