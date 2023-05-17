#version 330

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 textureCoordinate;

uniform mat4 mvMatrix;
uniform mat4 pMatrix;
uniform vec2 translate;

out vec3 viewPos;
out float cameraDistance;
out vec2 vTexCoord;

void main() {
    vec4 viewCoordinates = mvMatrix * vec4(position, 1.0f);
    vec4 result = pMatrix * viewCoordinates;

    viewPos = vec3(viewCoordinates);
    cameraDistance = result.z;
    result += vec4(translate, 0.0f, 0.0f);
    vTexCoord = textureCoordinate;
    gl_Position = result;
}
