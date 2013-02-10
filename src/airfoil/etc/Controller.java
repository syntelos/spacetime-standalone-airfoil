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
package airfoil.etc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * 
 * @author jdp
 */
public abstract class Controller
    extends InputAdapter
{
    private final static Vector3 XA = new Vector3(1,0,0);
    private final static Vector3 YA = new Vector3(0,1,0);
    private final static Vector3 ZA = new Vector3(0,0,1);
    /**
     * Camera scale
     */
    public final static float H = 2.0f;
    /**
     * Model scale
     * @param a Maximal axis dimension
     * @param b Maximal axis dimension
     * @param c Maximal axis dimension
     * @return Scale for common fit display
     */
    public final static float Scale(float a, float b, float c){

        return (H/Math.max(Math.max(a,b),c));
    }




    public int width, height;

    private Matrix4 camera = new Matrix4();

    private Vector3 last = new Vector3();

    private Matrix4 orientation;

    private Matrix4 rotation = new Matrix4();

    private float scale = 1.0f, hor, ver;

    private boolean button;



    public Controller(Viewport viewport){
        super();
        this.width = (int)viewport.width;
        this.height = (int)viewport.height;
        this.orientation = new Matrix4(new Quaternion(XA,180));
    }


    public void resize (int width, int height){
        this.width = width;
        this.height = height;

        final float aspect = ((float)width/(float)height);
        this.hor = (aspect*H);
        this.ver = (H);

        this.reinit();
    }
    public Matrix4 getCamera(){
        return this.camera;
    }
    private void reinit(){

        this.camera.setToOrtho(-this.hor,+this.hor,-this.ver,+this.ver,-this.hor,+this.hor);

        /*
         */
        if (1.0f != this.scale){

            this.camera.scl(this.scale);
        }
        /*
         * Flip default orientation from viewer at Z- to Z+
         */
        this.camera.mul(this.orientation);

        /*
         */
        if (null != this.rotation){

            this.camera.mul(this.rotation);
        }
    }
    @Override
    public boolean touchDown (int x, int y, int pointer, int button) {

        this.button = (0 == button);

        if (this.button)
            this.last.set(x, y, 0);
        else
            this.last.set(0, x, y);

        return true;
    }
    @Override
    public boolean touchDragged (int x, int y, int pointer) {

        final Vector3 delta;

        if (this.button){

            delta = new Vector3(x,y,0).sub(this.last).mul(0.5f);

            this.last.set(x,y,0);
        }
        else {

            delta = new Vector3(0,x,y).sub(this.last).mul(0.5f);

            this.last.set(0,x,y);
        }

        final Quaternion rotation;


        if (this.button){

            rotation = new Quaternion(YA,-delta.x).mul(new Quaternion(XA,delta.y));
        }
        else {

            rotation = new Quaternion(ZA,delta.z).mul(new Quaternion(YA,-delta.y));
        }

        this.rotation.mul(new Matrix4(rotation));

        this.reinit();

        return true;
    }
    @Override
    public boolean scrolled(int amount){

        if (0 < amount)
            this.scale *= 0.9f;
        else 
            this.scale *= 1.1f;

        this.reinit();

        return true;
    }
}
