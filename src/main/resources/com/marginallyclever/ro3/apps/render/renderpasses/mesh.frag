#version 330 core

out vec4 finalColor;

in VS_OUT {
    vec4 fragmentColor;
    vec3 normalVector;
    vec3 fragmentPosition;
    vec2 textureCoord;
    vec4 fragPosLightSpace;
} fs_in;

uniform vec4 specularColor = vec4(0.5, 0.5, 0.5,1);
uniform vec4 ambientColor = vec4(0.2, 0.2, 0.2,1);
uniform vec4 objectColor = vec4(1,1,1,1);
uniform vec4 lightColor = vec4(1,1,1,1);
uniform int shininess = 32;

uniform vec3 lightPos; // Light position in world space
uniform vec3 cameraPos;  // Camera position in world space

uniform sampler2D diffuseTexture;
uniform sampler2D shadowMap;

uniform bool useTexture;
uniform bool useLighting;
uniform bool useVertexColor;  // per-vertex color

float ShadowCalculation(vec4 fragPosLightSpace,vec3 normal,vec3 lightDir) {
    // perform perspective divide
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    // transform to [0,1] range
    projCoords = projCoords * 0.5 + 0.5;
    // get closest depth value from light's perspective (using [0,1] range fragPosLight as coords)
    float closestDepth = texture(shadowMap, projCoords.xy).r;
    // get depth of current fragment from light's perspective
    float currentDepth = projCoords.z;

    float bias = max(0.05 * (1.0 - dot(normal, lightDir)), 0.005);
    // check whether current frag pos is in shadow
    float shadow = currentDepth - bias > closestDepth  ? 1.0 : 0.0;

    return shadow;
}

void main() {
    vec4 diffuseColor = objectColor;
    if(useVertexColor) diffuseColor *= fs_in.fragmentColor;
    if(useTexture) diffuseColor *= texture(diffuseTexture, fs_in.textureCoord);

    vec4 result = diffuseColor;

    if(useLighting) {
        vec3 norm = normalize(fs_in.normalVector);
        vec3 lightDir = normalize(lightPos - fs_in.fragmentPosition);

        // Diffuse
        float diff = max(dot(norm, lightDir), 0.0);
        vec4 diffuseLight = diff * lightColor;

        // Specular
        vec3 viewDir = normalize(cameraPos - fs_in.fragmentPosition);
        vec3 reflectDir = reflect(-lightDir, norm);
        float spec = pow(max(dot(viewDir, reflectDir), 0.0), shininess);
        vec4 specularLight = spec * specularColor * lightColor;

        // Shadow
        float shadow = ShadowCalculation(fs_in.fragPosLightSpace,norm,lightDir);

        // put it all together.
        result *= ambientColor + (diffuseLight + specularLight) * (1.0 - shadow);
    }

    //finalColor = vec4(fs_in.textureCoord.x,fs_in.textureCoord.y,0,1);  // for testing texture coordinates
    finalColor = result;
    finalColor.a = diffuseColor.a;
}
