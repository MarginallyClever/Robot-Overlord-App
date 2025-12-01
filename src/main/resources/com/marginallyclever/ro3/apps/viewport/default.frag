#version 330 core

in vec4 fragmentColor;
in vec3 normalVector;
in vec3 fragmentPosition;
in vec2 textureCoord;

out vec4 finalColor;

uniform vec4 specularColor = vec4(0.5, 0.5, 0.5,1);
uniform vec4 ambientColor = vec4(0.2, 0.2, 0.2,1);
uniform vec4 diffuseColor = vec4(1,1,1,1);
uniform vec4 emissionColor = vec4(0,0,0,1);
uniform vec4 lightColor = vec4(1,1,1,1);
uniform int shininess = 32;
uniform float specularStrength = 0.5;

uniform vec3 lightPos; // Light position in world space
uniform vec3 cameraPos;  // Camera position in world space

uniform sampler2D Albedo;
uniform sampler2D Normal;
uniform sampler2D Metallic;
uniform sampler2D Roughness;
uniform sampler2D AO;

uniform bool useLighting;
uniform bool useVertexColor;  // per-vertex color

void main() {
    vec4 myColor = diffuseColor;
    if(useVertexColor) myColor *= fragmentColor;
    myColor *= texture(Albedo, textureCoord);
    vec4 result = myColor;

    if(useLighting) {
        vec3 norm = normalize(normalVector);
        vec3 lightDir = normalize(lightPos - fragmentPosition);

        // Diffuse
        float diff = max(dot(norm, lightDir), 0.0);
        vec4 diffuseLight = diff * lightColor;

        // Specular
        vec3 viewDir = normalize(cameraPos - fragmentPosition);
        vec3 reflectDir = reflect(-lightDir, norm);
        float spec = pow(max(dot(viewDir, reflectDir), 0.0), shininess);
        vec4 specWithTexture = specularStrength * spec * specularColor;
        vec4 specularLight = specWithTexture * lightColor;

        // these are unused currently, but here so the shader compiles
        float temp = specularStrength;
        vec4 metallicMap = texture(Metallic, textureCoord);
        vec4 normalMap = texture(Normal, textureCoord);
        vec4 roughnessMap = texture(Roughness, textureCoord);
        vec4 aoMap = texture(AO, textureCoord);

        // put it all together.
        result *= ambientColor + diffuseLight + specularLight;
        result += emissionColor;
    }

    //finalColor = vec4(textureCoord.x,textureCoord.y,0,1);  // for testing texture coordinates
    finalColor = result;
    finalColor.a = myColor.a;

    // log depth for more accuracy at far distances
    float far = 1e9;
    gl_FragDepth = log2(gl_FragCoord.z * far) / log2(far+1.0);
}
