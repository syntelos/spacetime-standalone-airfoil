attribute vec4 a_position;
attribute vec4 a_normal;

uniform mat4 u_camera;
uniform vec4 u_color;
uniform vec3 u_light;
uniform float u_mat;

varying float intensity;

void main(){

    vec3 n = u_camera * a_normal;

    intensity = dot(n, u_light) * u_mat;

    gl_Position = u_camera * a_position;
}
