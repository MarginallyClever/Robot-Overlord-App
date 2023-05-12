#version 330 core

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec3 aNormal;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

out vec3 FragPos;
out vec3 Normal;

void main() {
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(aPosition, 1.0);
    FragPos = vec3(modelMatrix * vec4(aPosition, 1.0));
    Normal = mat3(transpose(inverse(modelMatrix))) * aNormal;

}
