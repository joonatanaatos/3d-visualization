#version 330

#define POINT_LIGHTS 10

struct PointLight {
    vec3 position;
    float brightness;
};

uniform sampler2D texture;
uniform PointLight pointLights[POINT_LIGHTS];
uniform float ambientLightBrightness;
uniform vec3 normal;

in vec3 viewPos;
in float cameraDistance;
in vec2 vTexCoord;

float distanceFactor() {
    float steepness = 10.0f;
    return pow(steepness, 2.0f) / pow(cameraDistance + steepness, 2.0f);
}

vec3 getAmbientLight(vec3 rgb) {
    return rgb * ambientLightBrightness;
}

vec3 getDiffuseLight(vec3 rgb, PointLight pointLight) {
    float steepness = 0.8f;
    vec3 lightDir = pointLight.position - viewPos;
    float angleFactor = max(0.0f, dot(normalize(normal), normalize(lightDir)));
    float brightnessFactor = pointLight.brightness;
    float lightDistance = length(lightDir);
    float distanceFactor = pow(steepness, 2.0f) / pow(lightDistance + steepness, 2.0f);
    return rgb * brightnessFactor * angleFactor * distanceFactor;
}

vec3 getSpecularLight(vec3 rgb, PointLight pointLight) {
    float steepness = 0.8f;
    vec3 lightDir = pointLight.position - viewPos;
    vec3 viewDir = viewPos;
    vec3 reflectDir = reflect(lightDir, normal);
    float angleFactor = pow(max(0.0f, dot(normalize(viewDir), normalize(reflectDir))), 4.0f);
    float brightnessFactor = pointLight.brightness;
    float lightDistance = length(lightDir);
    float distanceFactor = pow(steepness, 2.0f) / pow(lightDistance + steepness, 2.0f);
    return rgb * brightnessFactor * angleFactor * distanceFactor;
}

void main() {
    vec4 color = texture2D(texture, vTexCoord);
    float alpha = color.a;
    vec3 originalRGB = vec3(color.r, color.g, color.b);

    vec3 light = getAmbientLight(originalRGB);
    for (int i = 0; i < POINT_LIGHTS; i++) {
        light += getDiffuseLight(originalRGB, pointLights[i]);
        light += getSpecularLight(originalRGB, pointLights[i]);
    }

    vec3 finalRGB = distanceFactor() * light;
    gl_FragColor = vec4(finalRGB, alpha);
}
