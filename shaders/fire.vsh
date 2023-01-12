#version 330 core
layout (location = 0) in vec3 vertex_pos;
layout (location = 1) in vec2 in_texture;
layout (location = 2) in vec3 instance_pos;
layout (location = 3) in float in_data;

uniform mat4 billboard;
uniform mat4 modelview;
uniform mat4 projection;

out vec2 texture_coord;
out float data;

void main()
{
    data = in_data;
    texture_coord = in_texture;
    mat4 translation = mat4(
        1, 0, 0, 0, // First column
        0, 1, 0, 0,
        0, 0, 1, 0,
        instance_pos, 1  // Last column
    );
    float s = 0.3 + 0.7 * (1.0 - data);
    mat4 scale = mat4(
        s, 0, 0, 0,
        0, s, 0, 0,
        0, 0, s, 0,
        0, 0, 0, 1
    );
    gl_Position = projection * modelview * translation * scale * billboard * vec4(vertex_pos, 1.0);
}
