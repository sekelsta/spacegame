#version 330 core
out vec4 fragColor;

in vec3 normal;
in vec2 texture_coord;
in vec3 frag_pos;

uniform vec3 light_pos;

uniform sampler2D texture_sampler;
uniform sampler2D emission_sampler;

uniform float reflectance;
// Range: 0 < scattering <= 1
uniform float scattering = 0.05;
uniform float shininess = 16;

void main()
{
    vec3 light_dir = normalize(light_pos - frag_pos);
    float diffuse_str = max(dot(normal, light_dir), 0.0);

    float specular_str = 0;
    if (dot(normal, light_dir) > 0) {
        // in view space the view pos is at the origin
        vec3 view_dir = normalize(-frag_pos);
        vec3 halfway_dir = normalize(light_dir + view_dir);

        specular_str = pow(max(dot(normal, halfway_dir), 0.0), shininess) * reflectance
                         * min(1.0, dot(normal, light_dir) / scattering);
    }

    float ambient_str = 0.005;
    vec4 color = texture(texture_sampler, texture_coord);
    vec4 emissive = texture(emission_sampler, texture_coord);
    float alpha = color.a + emissive.a;
    if (alpha < 0.01) {
        discard;
    }
    vec3 lit = color.rgb * (ambient_str + diffuse_str) + vec3(1, 1, 1) * specular_str;
    // OpenGL automatically clamps color components to the range [0, 1]
    fragColor = vec4(color.a * lit + emissive.rgb, alpha);
}
