#version 330 core
out vec4 fragColor;

in vec2 texture_coord;

uniform sampler2D texture_sampler;
void main()
{
    vec4 color = texture(texture_sampler, texture_coord);
    if (color.a < 1.0) {
        discard;
    }
    fragColor = color;
}
