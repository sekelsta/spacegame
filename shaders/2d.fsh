#version 330 core
out vec4 fragColor;

in vec2 texture_coord;

uniform sampler2D texture_sampler;

void main()
{
    vec4 color = texture(texture_sampler, texture_coord);
    if (color.a < 0.1) {
        discard;
    }
    fragColor = vec4(color.xyz, 1.0);
}
