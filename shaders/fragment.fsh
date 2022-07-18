#version 330 core
out vec4 fragColor;

in vec3 normal;
in vec2 texture_coord;
in vec3 frag_pos;

uniform sampler2D texture_sampler;
uniform sampler2D specular_sampler;
uniform sampler2D emission_sampler;

const vec3 light_pos = vec3(1, 1, 1);
const float shininess = 4;

void main()
{
    vec3 light_dir = normalize(light_pos - frag_pos);
    // in view space the view pos is at the origin
    // frag_pos should already be normalized
    vec3 view_dir = -frag_pos;
    vec3 halfway_dir = normalize(light_dir + view_dir);
    float diffuse_str = max(dot(normal, light_dir), 0.0);

    vec4 specular_sample = texture(specular_sampler, texture_coord);
    float specular_str = pow(max(dot(normal, halfway_dir), 0.0), shininess) * specular_sample.a;
    vec4 color = texture(texture_sampler, texture_coord);
    vec4 emissive = texture(emission_sampler, texture_coord);
    if (color.a + emissive.a < 0.1) {
        discard;
    }
    fragColor = vec4(color.xyz * (diffuse_str + specular_str) + emissive.xyz, 1.0);
}
