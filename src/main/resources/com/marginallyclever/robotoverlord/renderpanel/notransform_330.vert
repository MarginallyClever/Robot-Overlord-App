// vertex shader
// set position, no change, pass color to fragment shader
#version 330 core

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec4 aColor;

out vec4 fragmentColor;

void main() {
    gl_Position = vec4(aPosition,1);
    fragmentColor = aColor;
}
