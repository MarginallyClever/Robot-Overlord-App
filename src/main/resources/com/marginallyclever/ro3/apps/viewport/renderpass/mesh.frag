#version 330 core

out vec4 finalColor;

in VS_OUT {
    vec4 fragmentColor;
    vec3 normalVector;
    vec3 fragmentPosition;
    vec2 textureCoord;
    vec4 fragPosLightSpace;
} fs_in;

uniform vec4 ambientColor = vec4(0, 0, 0, 1);
uniform vec4 diffuseColor = vec4(1,1,1,1);
uniform vec4 emissionColor = vec4(0,0,0,1);
uniform vec4 specularColor = vec4(0, 0, 0, 1);
uniform vec4 lightColor = vec4(1,1,1,1);
uniform int shininess = 0;
uniform float specularStrength = 0.5;

uniform vec3 lightPos; // Light position in world space
uniform vec3 cameraPos;  // Camera position in world space

uniform sampler2D Albedo;
uniform sampler2D Normal;
uniform sampler2D Metallic;
uniform sampler2D Roughness;
uniform sampler2D AO;

uniform sampler2D shadowMap;

uniform bool useLighting;
uniform bool useVertexColor;  // per-vertex color

float shadowCalculation(vec4 fragPosLightSpace,vec3 normal,vec3 lightDir) {
    // perform perspective divide
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    // transform to [0,1] range
    projCoords = projCoords * 0.5 + 0.5;
    // get closest depth value from light's perspective (using [0,1] range fragPosLight as coords)
    float closestDepth = texture(shadowMap, projCoords.xy).r;
    // get depth of current fragment from light's perspective
    float currentDepth = projCoords.z;

    float bias = max(0.0005 * (1.0 - dot(normal, lightDir)), 0.00005);
    // check whether current frag pos is in shadow

    if(projCoords.z > 1.0) return 0.0f;

    float shadow = 0.0;
    vec2 texelSize = 1.0 / textureSize(shadowMap, 0);
    for(int x = -1; x <= 1; ++x) {
        for(int y = -1; y <= 1; ++y) {
            float pcfDepth = texture(shadowMap, projCoords.xy + vec2(x, y) * texelSize).r;
            shadow += currentDepth - bias > pcfDepth ? 1.0 : 0.0;
        }
    }
    return shadow / 9.0f;
}

void main() {
    vec4 normalMap = texture(Normal, fs_in.textureCoord);
    vec4 roughnessMap = texture(Roughness, fs_in.textureCoord);
    vec4 aoMap = texture(AO, fs_in.textureCoord);
    vec4 metallicMap = texture(Metallic, fs_in.textureCoord);

    vec4 result = diffuseColor;
    if(useVertexColor) result *= fs_in.fragmentColor;
    result *= texture(Albedo, fs_in.textureCoord);

    if(useLighting) {
        // Apply normal map (simple version: blend with vertex normal)
        vec3 norm = normalize(fs_in.normalVector);
        if (length(normalMap.rgb) > 0.0) {
            vec3 tangentNormal = normalMap.rgb * 2.0 - 1.0;
            norm = normalize(norm + tangentNormal * 0.5); // Blended for stability without full TBN
        }
        vec3 lightDir = normalize(lightPos - fs_in.fragmentPosition);

        // Diffuse
        float diff = max(dot(norm, lightDir), 0.0);
        vec4 diffuseLight = diff * lightColor;

        // Specular (Roughness affects the exponent)
        vec3 viewDir = normalize(cameraPos - fs_in.fragmentPosition);
        vec3 reflectDir = reflect(-lightDir, norm);
        float roughness = roughnessMap.r;
        float currentShininess = shininess * (1.0 - roughness);
        float spec = pow(max(dot(viewDir, reflectDir), 0.0), max(1.0, currentShininess));

        // Metallic affects the specular tint
        vec4 specWithTexture = specularStrength * spec * metallicMap * specularColor;
        vec4 specularLight = specWithTexture * lightColor;

        // Shadow
        float shadow = shadowCalculation(fs_in.fragPosLightSpace,norm,lightDir);

        // put it all together.
        result *= (ambientColor * aoMap) + (diffuseLight + specularLight) * (1.0 - shadow);
        result += emissionColor;
    }

    //finalColor = vec4(fs_in.textureCoord.x,fs_in.textureCoord.y,0,1);  // for testing texture coordinates
    finalColor = result;
    finalColor.a = diffuseColor.a;

    // log depth for more accuracy at far distances
    float far = 1e9;
    gl_FragDepth = log2(gl_FragCoord.z * far) / log2(far+1.0);
}
