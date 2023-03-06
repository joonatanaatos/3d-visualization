#version 330

uniform vec4 color;

in float cameraDistance;

float getColorFactor(float distance) {
    float steepness = 0.3f;
    return 1.0f / (steepness * distance * distance + 1);
}

void main() {
    float alpha = color.a;
    vec3 originalRGB = vec3(color.r, color.g, color.b);
    vec3 finalRGB = originalRGB * getColorFactor(cameraDistance);
    gl_FragColor = vec4(finalRGB, alpha);
}
