#version 330

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 textureCoordinate;

uniform mat4 mvpMatrix;

out float cameraDistance;
out vec2 vTexCoord;

void main() {
    vec4 result = mvpMatrix * vec4(position, 1.0f);
    cameraDistance = result.z;
    vTexCoord = textureCoordinate;
    gl_Position = result;
}
