// JFA fragment shader
#version 330 core

in vec2 TexCoords;

out vec4 FragColor;

uniform vec2 screenSize;
uniform sampler2D stencilBuffer;
uniform int step;

void main() {
    vec2 uv = gl_FragCoord.xy / screenSize;
    vec4 data = texture(stencilBuffer, uv);

    for (int y = -1; y <= 1; ++y) {
        for (int x = -1; x <= 1; ++x) {
            vec2 offset = vec2(x, y) * step / screenSize;
            vec4 neighbor = texture(stencilBuffer, uv + offset);

            if (neighbor.a > 0.0) {
                vec2 delta = neighbor.xy - gl_FragCoord.xy;
                float distance = dot(delta, delta);

                if (data.a == 0.0 || distance < data.z) {
                    data.xy = neighbor.xy;
                    data.z = distance;
                    data.a = 1.0;
                }
            }
        }
    }

    FragColor = data;
}