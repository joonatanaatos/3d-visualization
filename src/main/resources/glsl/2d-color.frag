#version 330

uniform vec4 color;
uniform bool invertColor;
uniform float opacity;

in vec2 vTexCoord;

void main() {
    float alpha = color.a * opacity;
    vec3 rgb = color.rgb;
    if (invertColor) {
        rgb = vec3(1.0) - rgb;
    }
    gl_FragColor = vec4(rgb, alpha);
}
