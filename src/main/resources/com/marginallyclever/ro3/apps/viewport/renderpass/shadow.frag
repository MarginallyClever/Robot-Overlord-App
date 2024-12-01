#version 330 core

void main() {
    // gl_FragDepth = gl_FragCoord.z;

    // log depth for more accuracy at far distances... does not work with shadow mapping.
    // we're not going to that scale, anyhow.
    //float far = 1e9;
    //gl_FragDepth = log2(gl_FragCoord.z * far) / log2(far+1.0);
}