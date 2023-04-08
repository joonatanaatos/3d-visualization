#version 330

#define POINT_LIGHTS 10

struct PointLight {
    vec3 position;
    float brightness;
};

uniform vec4 color;
uniform PointLight pointLights[POINT_LIGHTS];
uniform float ambientLightBrightness;
uniform vec3 normal;

in vec3 viewPos;
in float cameraDistance;
in vec2 vTexCoord;

float lightSteepness = 0.6f;
float distanceSteepness = 10.0f;

float distanceFactor() {
    return pow(distanceSteepness, 2.0f) / pow(cameraDistance + distanceSteepness, 2.0f);
}

vec3 getAmbientLight(vec3 rgb) {
    return rgb * ambientLightBrightness;
}

vec3 getDiffuseLight(vec3 rgb, PointLight pointLight) {
    vec3 lightDir = pointLight.position - viewPos;
    float angleFactor = max(0.0f, dot(normalize(normal), normalize(lightDir)));
    float brightnessFactor = pointLight.brightness;
    float lightDistance = length(lightDir);
    float distanceFactor = pow(lightSteepness, 2.0f) / pow(lightDistance + lightSteepness, 2.0f);
    return rgb * brightnessFactor * angleFactor * distanceFactor;
}

vec3 getSpecularLight(vec3 rgb, PointLight pointLight) {
    vec3 lightDir = pointLight.position - viewPos;
    vec3 viewDir = viewPos;
    vec3 reflectDir = reflect(lightDir, normal);
    float angleFactor = pow(max(0.0f, dot(normalize(viewDir), normalize(reflectDir))), 8.0f);
    float brightnessFactor = pointLight.brightness;
    float lightDistance = length(lightDir);
    float distanceFactor = pow(lightSteepness, 2.0f) / pow(lightDistance + lightSteepness, 2.0f);
    return rgb * brightnessFactor * angleFactor * distanceFactor;
}

void main() {
    float alpha = color.a;
    vec3 originalRGB = color.rgb;

    vec3 light = getAmbientLight(originalRGB);
    for (int i = 0; i < POINT_LIGHTS; i++) {
        light += getDiffuseLight(originalRGB, pointLights[i]);
        light += getSpecularLight(originalRGB, pointLights[i]);
    }

    vec3 finalRGB = distanceFactor() * light;
    gl_FragColor = vec4(finalRGB, alpha);
}
