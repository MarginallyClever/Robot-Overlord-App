#version 450

uniform sampler2D silhouette;

in int borderThickness;
in vec3 borderColor;

in FragData {
    smooth vec2 coords;
} frag;

out vec4 PixelColor;

void main() {

    // if the pixel is black (we are on the silhouette)
    if (texture(silhouette, frag.coords).xyz == vec3(0.0f)) {
        vec2 size = 1.0f / textureSize(silhouette, 0);

        for (int i = -borderThickness; i <=borderThickness; i++) {
            for (int j = -borderThickness; j <= borderThickness; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }

                vec2 offset = vec2(i, j) * size;

                // and if one of the pixel-neighbor is white (we are on the border)
                if (texture(silhouette, frag.coords + offset).xyz == vec3(1.0f)) {
                    PixelColor = vec4(borderColor, 1.0f);
                    return;
                }
            }
        }
    }

    discard;
}