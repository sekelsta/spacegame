#version 330 core
out vec4 fragColor;

in vec2 texture_coord;
in float data;

uniform sampler2D texture_sampler;
void main()
{
    vec4 color = texture(texture_sampler, texture_coord);
    if (color.a < 1.0) {
        discard;
    }
    float f = 1.0 - data;
    color.r = sqrt(f);
    color.g = f * sqrt(f);
    color.b = 0.5 * f * f;
    fragColor = color;
}
