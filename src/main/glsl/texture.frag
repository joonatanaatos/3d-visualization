#version 330

uniform sampler2D texture;

in float cameraDistance;
in vec2 vTexCoord;

float getColorFactor(float distance) {
    float steepness = 0.1f;
    return 1.0f / (steepness * distance + 1);
}

void main() {
    vec4 color = texture2D(texture, vTexCoord);
    float alpha = color.a;
    vec3 originalRGB = vec3(color.x, color.y, color.z);
    vec3 finalRGB = originalRGB * getColorFactor(cameraDistance);
    gl_FragColor = vec4(finalRGB, alpha);
}
