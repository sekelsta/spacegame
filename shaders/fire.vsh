#version 330 core
layout (location = 0) in vec3 position;
layout (location = 1) in vec3 in_normal;
layout (location = 2) in vec2 in_texture;
layout (location = 3) in vec4 bone_weights;
layout (location = 4) in ivec4 boneIDs;

uniform mat4 modelview;
uniform mat4 projection;

out vec2 texture_coord;

void main()
{
    texture_coord = in_texture;
    gl_Position = projection * modelview * vec4(position, 1.0);
}
