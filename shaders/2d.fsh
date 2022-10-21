#version 330 core
out vec4 fragColor;

in vec2 texture_coord;
in vec3 color;

uniform sampler2D texture_sampler;

void main()
{
    fragColor = vec4(color, 1) * texture(texture_sampler, texture_coord);
}
