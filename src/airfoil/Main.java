/*
 * Spacetime Standalone Airfoil
 * Copyright (C) 2013, John Pritchard.
 * 
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package airfoil;

import json.Json;
import json.Reader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.jogl.JoglApplication;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/**
 * 
 * @author jdp
 */
public class Main
    extends airfoil.etc.Controller
    implements com.badlogic.gdx.ApplicationListener
{
    private final static Color ColorClear = new Color(0.5f,0.5f,0.6f,1.0f);
    private final static Color ColorBody  = new Color(0.9f,0.9f,1.0f,1.0f);

    private final static Vector3 LightNormal = new Vector3(0,0,1);

    private final static Matrix4 UCam = new Matrix4();

    private final static float Material = 2.4f;


    public final static String Title = "Spacetime Airfoil";

    public final static airfoil.etc.Viewport Viewport = airfoil.etc.Viewport.Screen.Half.viewport();



    private ShaderProgram bodyShader, axesShader;

    private boolean alive = false;

    private int naca_number = 2509;

    private int resolution = 1000;

    private Geometry geometry = Geometry.Model;

    private Airfoil database;


    public Main(){
        super(Main.Viewport);
        this.database = new NACA(this.naca_number);
        this.database.init(this.resolution);
        this.database.generate(Geometry.Model,this.geometry);
        this.scale = 3.0f;
    }



    @Override
    public void create(){

        Gdx.input.setInputProcessor(this);

        ShaderProgram.pedantic = false;
        this.bodyShader = new ShaderProgram(Gdx.files.internal("data/shaders/body.vert.glsl").readString(),
                                        Gdx.files.internal("data/shaders/body.frag.glsl").readString());

        this.alive = this.bodyShader.isCompiled();
        if (!this.alive) {
            Gdx.app.error(Main.Title,"Error compiling body shader " + this.bodyShader.getLog());
            Gdx.app.exit();
        }
        else {
            this.axesShader = new ShaderProgram(Gdx.files.internal("data/shaders/axes.vert.glsl").readString(),
                                                Gdx.files.internal("data/shaders/axes.frag.glsl").readString());

            this.alive = this.axesShader.isCompiled();
            if (!this.alive) {
                Gdx.app.error(Main.Title,"Error compiling axes shader " + this.axesShader.getLog());
                Gdx.app.exit();
            }
        }
    }
    @Override
    public void render(){
        if (this.alive){
            GLCommon gl = Gdx.gl;
            gl.glViewport(0, 0, this.width, this.height);
            gl.glClearColor(ColorClear.r,ColorClear.g,ColorClear.b,ColorClear.a);
            gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            gl.glEnable(GL20.GL_DEPTH_TEST);
            gl.glDepthFunc(GL20.GL_LESS);

            /*
             */

            this.renderMeshLines(this.database.getMesh(this.geometry));

        }
    }
    protected final void renderMeshTriangles(Mesh mesh){
        final ShaderProgram bodyShader = this.bodyShader;
        if (null != bodyShader){

            if (null != mesh){
                final Matrix4 camera = this.getCamera();

                bodyShader.begin();

                bodyShader.setUniformMatrix("u_camera", camera);
                bodyShader.setUniformf("u_color", Color.YELLOW);
                bodyShader.setUniformf("u_light", LightNormal);
                bodyShader.setUniformf("u_mat", Material);

                mesh.render(bodyShader, GL20.GL_TRIANGLES);

                bodyShader.end();
            }
        }
    }
    protected final void renderMeshLines(Mesh mesh){
        final ShaderProgram axesShader = this.axesShader;
        if (null != axesShader){

            if (null != mesh){
                final Matrix4 camera = this.getCamera();

                axesShader.begin();

                axesShader.setUniformMatrix("u_camera", camera);
                axesShader.setUniformf("u_color", Color.YELLOW);
                axesShader.setUniformf("u_light", LightNormal);
                axesShader.setUniformf("u_mat", Material);

                mesh.render(axesShader, GL20.GL_LINES);

                axesShader.end();
            }
        }
    }
    @Override
    public void pause(){
        this.alive = false;
    }
    @Override
    public void resume(){
        this.alive = true;
    }
    @Override
    public void dispose(){

        ShaderProgram bodyShader = this.bodyShader;
        if (null != bodyShader){
            this.bodyShader = null;
            bodyShader.dispose();
        }

        ShaderProgram axesShader = this.axesShader;
        if (null != axesShader){
            this.axesShader = null;
            axesShader.dispose();
        }
    }

    public static void main(String[] argv){


        Main application = new Main();

        new JoglApplication(application,Main.Title,application.width,application.height,true);

    }
}
