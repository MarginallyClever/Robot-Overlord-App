#version 330 core

in vec4 fragmentColor;
in vec3 normalVector;
in vec3 fragmentPosition;
in vec2 textureCoord;

out vec4 finalColor;

uniform vec3 specularColor = vec3(0.5, 0.5, 0.5);
uniform vec3 ambientLightColor = vec3(0.2, 0.2, 0.2);
uniform vec3 lightPos; // Light position in world space
uniform vec3 cameraPos;  // Camera position in world space
uniform vec4 objectColor;
uniform vec3 lightColor;
uniform sampler2D diffuseTexture;

uniform bool useTexture;
uniform bool useLighting;
uniform bool useVertexColor;  // per-vertex color

void main() {
    vec4 diffuseColor = objectColor;
    if(useVertexColor) diffuseColor *= fragmentColor;
    if(useTexture) diffuseColor *= texture(diffuseTexture, textureCoord);

    vec3 result = vec3(diffuseColor);

    if(useLighting) {
        vec3 norm = normalize(normalVector);
        vec3 lightDir = normalize(lightPos - fragmentPosition);

        // Diffuse
        float diff = max(dot(norm, lightDir), 0.0);
        vec3 diffuseLight = diff * lightColor;

        // Specular
        vec3 viewDir = normalize(cameraPos - fragmentPosition);
        vec3 reflectDir = reflect(-lightDir, norm);
        float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
        vec3 specularLight = spec * specularColor * lightColor;

        // put it all together.
        result *= ambientLightColor + diffuseLight + specularLight;
    }

    //finalColor = vec4(textureCoord.x,textureCoord.y,0,1);  // for testing texture coordinates
    finalColor = vec4(result, diffuseColor.a);
}
