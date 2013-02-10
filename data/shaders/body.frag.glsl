attribute vec4 a_position;
attribute vec4 a_normal;

uniform mat4 u_camera;
uniform vec4 u_color;

varying float intensity;

void main(){

    gl_FragColor = intensity * u_color;
}
