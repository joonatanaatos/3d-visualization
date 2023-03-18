#version 330

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 textureCoordinate;

uniform mat4 modelMatrix;
uniform vec2 translate;

out vec2 vTexCoord;

void main() {
    vTexCoord = textureCoordinate;
    vec4 result = modelMatrix * vec4(position, 1.0);
    result += vec4(translate, 0.0f, 0.0f);
    gl_Position = result;
}
