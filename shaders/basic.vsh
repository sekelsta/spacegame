#version 330 core
layout (location = 0) in vec3 position;
layout (location = 1) in vec3 in_normal;
layout (location = 2) in vec2 in_texture;
layout (location = 3) in vec4 bone_weights;
layout (location = 4) in ivec4 boneIDs;

uniform mat4 modelview;
uniform mat4 projection;
uniform mat3 normal_transform;

const int MAX_BONES = 64;
const int MAX_BONE_INFLUENCE = 4;
uniform mat4 bone_matrices[MAX_BONES];

out vec3 normal;
out vec2 texture_coord;
out vec3 frag_pos;

void main()
{
    vec4 skeletal_pos = vec4(0.0);
    vec3 skeletal_norm = vec3(0.0);
    for (int i = 0; i < MAX_BONE_INFLUENCE; ++i) {
        if (boneIDs[i] == -1) {
            continue;
        }
        if (boneIDs[i] >= MAX_BONES) {
            skeletal_pos = vec4(position, 1.0);
            skeletal_norm = in_normal;
            break;
        }
        vec4 local_pos = bone_matrices[boneIDs[i]] * vec4(position, 1.0);
        skeletal_pos += local_pos * bone_weights[i];
        vec3 local_norm = mat3(bone_matrices[boneIDs[i]]) * in_normal;
        skeletal_norm += local_norm * bone_weights[i];
    }

    normal = normalize(normal_transform * skeletal_norm);
    texture_coord = in_texture;
    gl_Position = projection * modelview * skeletal_pos;
    frag_pos = vec3(modelview * skeletal_pos);
}
