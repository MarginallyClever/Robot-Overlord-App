// vertex shader
// set position, no change, pass color to fragment shader
#version 330 core

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec4 aColor;
layout(location = 3) in vec2 aTexture;

out vec4 fragmentColor;
out vec3 normalVector;
out vec3 fragmentPosition;
out vec2 textureCoord;

void main() {
    gl_Position = vec4(aPosition,1);
    fragmentColor = aColor;
    normalVector = vec3(0,0,1);
    fragmentPosition = aPosition;
    textureCoord = aTexture;
}
