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

import static fv3.math.Abstract.*;


/**
 * Instances of this class are produced by {@link ChordIterator} to
 * represent the state of a single chord iteration cycle.
 * 
 * Each chord iteration cycle has one or two vertices.  The interior
 * and exterior edges have one vertex, while all other points in the
 * chord are represented by a pair of (two) vertices.
 * 
 * Each chord pair iteration cycle has two vertices.  These two
 * vertices differ in their range coordinates (as members of top or
 * bottom surfaces), and may differ in their domain coordinates.
 * 
 * The geometry of the subject airfoil, including its domain and
 * range, is defined by the {@link ModelVertices$Kind Geometric Kind}
 * structure.
 * 
 * @see ModelVertices$Kind
 * @see AirfoilVertices
 * @see ChordIterator
 */
public final class ChordVertex
    extends MathAbstract
{
    public enum In {
        Domain, Current, None;
    }


    public final AirfoilVertices airfoil;

    public final Geometry geometry;

    public final int dimension;

    private final float[] a, b;

    public final int chordIndex;
    /**
     * This point in the chord represents a single edge vertex,
     * otherwise a pair of chord vertices.  
     */
    public final boolean edge;

    private ChordIterator chord;


    public ChordVertex(ChordIterator chord, int chordIndex, float[] a, float[] b){
        super();
        this.chord = chord;
        this.airfoil = chord.airfoil;
        this.geometry = chord.geometry;
        this.dimension = this.geometry.dimension.number;
        this.chordIndex = chordIndex;
        this.edge = (0 == chordIndex || chordIndex == this.airfoil.ne);
        this.a = a;
        this.b = b;
    }


    /**
     * Search for a point between the current and previous chord
     * points, over the first vertex in each pair.
     * 
     * @param prev In the first cycle of the iteration, the previous
     * member of the iteration is null.  After the first cycle of the
     * iteration, the previous member of the iteration is not null.
     * This argument may be null in the first cycle of the iteration.
     * 
     * @param search Airfoil (chord) domain search point
     */
    public In in(float search){
        ChordVertex prev = this.peek(-1);

        if (null == prev){

            final float dom = this.getX();

            if (EEQ(dom,search))
                return In.Current;
            else
                return In.None;
        }
        else {

            final float tDom = this.getX();

            if (EEQ(tDom,search)){

                return In.Current;
            }
            else {
                final float pDom = prev.getX();

                if (pDom < tDom){

                    if ((pDom < search)&&(search < tDom))
                        return In.Domain;
                    else
                        return In.None;
                }
                else {
                    if ((pDom > search)&&(search > tDom))
                        return In.Domain;
                    else
                        return In.None;
                }
            }
        }
    }
    public float[] getVertices(){

        return Cat(this.a,this.b);
    }
    public float getVertexZ(In in, float x, float y, float z){
        switch(in){
        case Current:
            {
                if (this.edge)
                    return this.getZ();
                else {
                    if (0.0f <= z)
                        return this.getTopZ();
                    else
                        return this.getBottomZ();
                }
            }
        case Domain:
            {
                final ChordVertex p = this.peek(-1);

                if (0.0f <= z){

                    final Geometry.Interpolation top = this.geometry.interpolate(p,this,x,Geometry.SurfaceOrder.Top);

                    return top.getZ();
                }
                else {

                    final Geometry.Interpolation bot = this.geometry.interpolate(p,this,x,Geometry.SurfaceOrder.Bottom);

                    return bot.getZ();
                }
            }
        default:
            throw new Error();
        }
    }
    /**
     * @return Interior edge vertex for chord iteration
     * 
     * @see #edge
     * 
     * @exception java.lang.IllegalStateException Calling this method on pair point
     */
    public float[] getEdge()
        throws java.lang.IllegalStateException
    {
        if (this.edge)

            return this.a.clone();
        else 
            throw new IllegalStateException();
    }
    /**
     * @return Pair of vertices for chord iteration
     * 
     * @exception java.lang.IllegalStateException Calling this method on edge point
     */
    public float[] getPair()
        throws java.lang.IllegalStateException
    {
        if (this.edge)

            throw new IllegalStateException();
        else 
            return Cat(this.a,this.b);
    }
    public float getX(){

        return this.a[X];
    }
    public float getY(){

        return this.a[Y];
    }
    public float getZ(){

        return this.a[Z];
    }
    public float getZ(Geometry.SurfaceOrder s){

        if (s == this.geometry.path.surface)

            return this.a[Z];
        else
            return this.b[Z];
    }
    public float getTopZ(){
        if (this.edge)

            throw new IllegalStateException();

        else if (this.geometry.isTop)
            return this.a[Z];
        else
            return this.b[Z];
    }
    public float getBottomZ(){
        if (this.edge)

            throw new IllegalStateException();

        else if (this.geometry.isBottom)
            return this.a[Z];
        else
            return this.b[Z];
    }
    public float[] getTop()
        throws java.lang.IllegalStateException
    {
        if (this.edge)

            throw new IllegalStateException();

        else if (this.geometry.isTop)
            return this.a.clone();
        else
            return this.b.clone();
    }
    public float[] getBottom()
        throws java.lang.IllegalStateException
    {
        if (this.edge)

            throw new IllegalStateException();

        else if (this.geometry.isBottom)
            return this.a.clone();
        else
            return this.b.clone();
    }
    public void destroy(){
        ChordIterator chord = this.chord;
        if (null != chord){
            this.chord = null;
            chord.destroy(this.chordIndex);
        }
    }
    public ChordVertex peek(int r){

        return this.chord.peek(this.chordIndex+r);
    }
}
