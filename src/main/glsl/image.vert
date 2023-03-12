#version 330

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 textureCoordinate;

uniform mat4 modelMatrix;

out vec2 vTexCoord;

void main() {
    vTexCoord = textureCoordinate;
    gl_Position = modelMatrix * vec4(position, 1.0);
}
