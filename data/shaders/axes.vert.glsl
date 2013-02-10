attribute vec4 a_position;
attribute vec4 a_normal;

uniform mat4 u_camera;
uniform vec4 u_color;

void main(){

  gl_Position = u_camera * a_position;
}
