#version 330 core

in vec4 fragmentColor;
in vec3 normalVector;
in vec3 fragmentPosition;

out vec4 finalColor;

uniform vec3 lightPos; // Light position in world space
uniform vec3 cameraPos;  // Camera position in world space
uniform vec3 objectColor;
uniform vec3 lightColor;

void main() {
    vec3 norm = normalize(normalVector);
    vec3 lightDir = normalize(lightPos - fragmentPosition);

    // Ambient
    float ambientStrength = 0.1;
    vec3 ambient = ambientStrength * lightColor;

    // Diffuse
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColor;

    // Specular
    float specularStrength = 0.5;
    vec3 viewDir = normalize(cameraPos - fragmentPosition);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
    vec3 specular = specularStrength * spec * lightColor;

    vec3 result = (ambient + diffuse + specular) * objectColor;
    finalColor = vec4(result, 1.0);
}
