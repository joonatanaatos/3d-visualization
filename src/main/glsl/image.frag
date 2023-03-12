#version 330

uniform sampler2D texture;
uniform bool invertColor;

in vec2 vTexCoord;

void main() {
    vec4 color = texture2D(texture, vTexCoord);
    float alpha = color.a;
    vec3 rgb = color.rgb;
    if (invertColor) {
        rgb = vec3(1.0) - rgb;
    }
    gl_FragColor = vec4(rgb, alpha);
}
