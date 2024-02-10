#version 330

uniform sampler2D textureSampler;
uniform bool invertColor;
uniform float opacity;

in vec2 vTexCoord;
out vec4 fragColor;
void main() {
    vec4 color = texture(textureSampler, vTexCoord);
    float alpha = color.a * opacity;
    vec3 rgb = color.rgb;
    if (invertColor) {
        rgb = vec3(1.0) - rgb;
    }
    fragColor = vec4(rgb, alpha);
}
