#version 330

layout (location = 0) in vec3 position;

uniform mat4 mvpMatrix;

void main() {
    vec4 result = mvpMatrix * vec4(position, 1.0f);
    gl_Position = result;
}
