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

import static airfoil.MathAbstract.*;

import fv3.font.GlyphVector;
import static fv3.math.Abstract.*;
import fv3.math.VertexArray;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;

/**
 * Iterate over the chord of an airfoil geometry.  The domain of the
 * chord iteration is one set of X coordinates in the first surface,
 * from the first edge, in the geometry.
 * 
 * This class implements this behavior by reinterpolating the second
 * surface in the domain coordinates of the first.
 * 
 * Furthermore, the number of points in the chord differs from the
 * number of vertices in the input.
 * 
 * Running the main function in this class will illustrate, by first
 * printing the input and then printing the iteration point set.  The
 * input is ordered as a continuous geometric path, while the set of
 * points in the chord iterator is roughly half of the input path,
 * listed in twice as two surfaces from the first edge to the second
 * edge.
 * 
 * @see ChordVertex
 * @see AirfoilVertices
 */
public final class ChordIterator
    extends Object
    implements Cloneable,
               fv3.math.Notation,
               java.lang.Iterable<ChordVertex>,
               java.util.Iterator<ChordVertex>
{


    public final AirfoilVertices airfoil;

    public final Geometry geometry;

    public final int dimension;

    public final int length;

    private final float[] top, bot;

    private final ChordVertex[] target;

    private int index;


    public ChordIterator(AirfoilVertices airfoil, float[] source){
        super();
        if (null == airfoil)
            throw new IllegalArgumentException();
        else {
            this.airfoil = airfoil;
            this.geometry = airfoil.geometry;
            this.dimension = this.geometry.dimension.number;
            if (null == source)
                throw new IllegalArgumentException();
            else {

                this.length = (airfoil.ne+1);
                this.target = new ChordVertex[this.length];

                final int vlen = (this.length*this.dimension);

                final float[] top = new float[vlen];
                final float[] bot = new float[vlen];

                if (this.geometry.isBottom){
                    /*
                     * Copy first surface
                     */
                    System.arraycopy(source,0,bot,0,vlen);

                    /*
                     * Interpolate second surface for identical domain
                     * coordinates and ordering
                     */
                    if (3 == this.dimension){

                        int check;

                        for (int bc = 0, tc = 0; tc < vlen; ){

                            final float bx = bot[bc+X];
                            final float by = bot[bc+Y];
                            final float bz = bot[bc+Z];

                            if (0.0f == bz){

                                bc += 3;

                                top[tc++] = bx;
                                top[tc++] = by;
                                top[tc++] = bz;
                            }
                            else {
                                check = bc;

                                float sx0 = source[X], sx1;
                                float sy0 = source[Y], sy1;
                                float sz0 = source[Z], sz1;

                                search:
                                for (int sc = (source.length-3); sc >= airfoil.ve; sc -= 3){

                                    sx1 = source[sc+X];
                                    sy1 = source[sc+Y];
                                    sz1 = source[sc+Z];

                                    if ((sx0 <= bx && bx <= sx1)||(sx0 >= bx && bx >= sx1)){

                                        bc += 3;

                                        top[tc++] = bx;
                                        top[tc++] = by;
                                        top[tc++] = LIY(sx0,sz0,sx1,sz1,bx);

                                        break search;
                                    }
                                    
                                    sx0 = sx1;
                                    sy0 = sy1;
                                    sz0 = sz1;
                                }
                                /*
                                 */
                                if (check == bc){

                                    throw new Error(String.valueOf(bc));
                                }
                            }
                        }

                        this.top = top;
                        this.bot = bot;
                    }
                    else {
                        throw new UnsupportedOperationException("TODO");
                    }
                }
                else {
                    throw new UnsupportedOperationException("TODO");
                }
            }
        }
    }


    public float x0(){

        if (this.geometry.isTop)
            return this.top[X];
        else
            return this.bot[X];
    }
    public float xe(){

        if (this.geometry.isTop)
            return this.top[this.airfoil.ve+X];
        else
            return this.bot[this.airfoil.ve+X];
    }
    public boolean in(float x){

        return this.geometry.in(this.x0(),x,this.xe());
    }
    public String inf(float x){

        return this.geometry.inf(this.x0(),x,this.xe());
    }
    public float[] getVertices(){

        if (this.geometry.isTop)
            return Cat(this.top,this.bot);
        else
            return Cat(this.bot,this.top);
    }
    public void reset(){

        this.index = 0;
    }
    public ChordVertex peek(int idx){
        if (-1 < idx && idx < this.length){
            ChordVertex cv = this.target[idx];
            if (null == cv){
                if (this.geometry.isTop)
                    cv = new ChordVertex(this,idx,this.top,this.bot);
                else
                    cv = new ChordVertex(this,idx,this.bot,this.top);

                this.target[idx] = cv;
            }
            return cv;
        }
        else
            return null;
    }
    public java.util.Iterator<ChordVertex> iterator(){
        return this;
    }
    public boolean hasNext(){

        return (this.index < this.length);
    }
    public ChordVertex peek(){

        if (this.index < this.length){

            return this.peek(this.index);
        }
        else
            throw new java.util.NoSuchElementException();
    }
    public ChordVertex next(){

        if (this.index < this.length){

            return this.peek(this.index++);
        }
        else
            throw new java.util.NoSuchElementException();
    }
    public void remove(){
        throw new UnsupportedOperationException();
    }
    public void destroy(){

        for (int idx = 0; idx < this.length; idx++){

            ChordVertex cv = this.target[idx];
            if (null != cv)
                cv.destroy();
        }
    }
    /**
     * Called from {@link ChordVertex#destroy()}
     */
    protected void destroy(int idx){
        if (-1 < idx && idx < this.length)

            this.target[idx] = null;
        else
            throw new java.util.NoSuchElementException(String.valueOf(idx));
    }
    public void list(){

        System.out.println("#");
        System.out.println("# ChordIterator data listing");
        System.out.println("#");
        System.out.printf( "# Geometry: %s%n",this.geometry);
        System.out.println("#");
        System.out.println("# [vertex_index] (a_position)");
        System.out.println("#");

        final float[] vertices = this.getVertices();


        switch(this.geometry.dimension.number){

        case 2:
            {
                final int verticesCount = vertices.length/2;
                float[] a_position = new float[2];

                for (int vv = 0, ofs; vv < verticesCount; vv++){
                    ofs = (vv*2);
                    System.arraycopy(vertices,ofs,a_position,0,2);

                    if (this.airfoil.ve == ofs)
                        System.out.printf("[%02d] (% 5.4g, % 5.4g) [ve]%n",vv,a_position[0],a_position[1]);
                    else
                        System.out.printf("[%02d] (% 5.4g, % 5.4g)%n",vv,a_position[0],a_position[1]);
                }
            }
            break;
        case 3:
            {
                final int verticesCount = vertices.length/3;
                float[] a_position = new float[3];

                for (int vv = 0, ofs; vv < verticesCount; vv++){
                    ofs = (vv*3);
                    System.arraycopy(vertices,ofs,a_position,0,3);

                    if (this.airfoil.ve == ofs)
                        System.out.printf("[%02d] (% 5.4g, % 5.4g, % 5.4g) [ve]%n",vv,a_position[0],a_position[1],a_position[2]);
                    else
                        System.out.printf("[%02d] (% 5.4g, % 5.4g, % 5.4g)%n",vv,a_position[0],a_position[1],a_position[2]);
                }
            }
            break;
        default:
            throw new Error(this.geometry.dimension.name());
        }
    }

    /**
     * Print iterator
     */
    public final static void main(String[] argv){

        int np = 20;

        if (0 < argv.length){

            np = Integer.parseInt(argv[0]);
        }

        final NACA naca = new NACA();
        {
            naca.init(np);

            final AirfoilVertices section = naca.generate(Geometry.Model,Geometry.Section);
            {
                section.list();
            }
            {
                final ChordIterator iterator = section.chordIteratorModel();

                iterator.list();
            }
            System.exit(0);
        }
    }
}
