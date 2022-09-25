#version 330 core
out vec4 fragColor;

in vec2 texture_coord;

uniform sampler2D texture_sampler;

void main()
{
    fragColor = texture(texture_sampler, texture_coord);
}
