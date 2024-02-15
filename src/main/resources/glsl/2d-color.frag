#version 330

uniform vec4 color;
uniform bool invertColor;
uniform float opacity;

in vec2 vTexCoord;
out vec4 fragColor;

void main() {
    float alpha = color.a * opacity;
    vec3 rgb = color.rgb;
    if (invertColor) {
        rgb = vec3(1.0) - rgb;
    }
    fragColor = vec4(rgb, alpha);
}
