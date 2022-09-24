#version 330 core
layout (location = 0) in vec2 position;
layout (location = 1) in vec2 in_texture;

uniform vec2 dimensions;

out vec2 texture_coord;

void main()
{
    texture_coord = in_texture;
    gl_Position = vec4(position.x / dimensions.x * 2 - 1, position.y / dimensions.y * 2 - 1, 0.0, 1.0);
}
