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

import static airfoil.Geometry.*;

import fv3.font.GlyphVector;
import static fv3.math.Abstract.*;
import fv3.math.Matrix;
import fv3.math.VertexArray;


/**
 * A kind of {@link Airfoil} {@link Geometry}
 * 
 * Compares on the Y coordinate
 * 
 * @see ModelVertices
 * @see ChordIterator
 */
public final class AirfoilVertices
    extends MathAbstract
{


    public final Geometry geometry;
    /**
     * Number of vertices in vertex list (N[V]) -- exclusive index
     * number.
     */
    public final int np;
    /**
     * Vertex number of interior edge vertex.  This is an inclusive
     * index in (0 &lt; n &lt; N[V]).
     * 
     * @see NACA#init(int)
     */
    public final int ne;
    /**
     * Length of vertex array (N[A])
     */
    public final int vp;
    /**
     * Index of vertex for interior edge (index; 0 &lt;= v &lt; N[A])
     */
    public final int ve;
    /**
     * An abstract vertex set is transformed into a practical section
     * vertex set.  The model is in NACA-490 order (X- forward) and
     * the section is in the opposite orientation for X+ forward (see
     * {@link sagittarius.design.Model}).
     */
    private float[] model;

    private float tex, lex, y, chord, section[];

    private boolean initialized;

    private float[] modelMesh, modelMeshDescription, sectionMesh;

    private String description;

    private GlyphVector description3;


    public AirfoilVertices(Geometry geometry, int count){
        super();
        this.geometry = geometry;

        final int n = ((count/geometry.dimension.number)*geometry.dimension.number);//(even)

        this.np = (n+1);
        this.ne = (n/2)-1;
        this.vp = (this.np*geometry.dimension.number);
        this.ve = (this.ne*geometry.dimension.number);

        this.model = new float[this.vp];
    }
    public AirfoilVertices(AirfoilVertices from, Geometry to){
        super();
        this.geometry = to;
        this.np = from.np;
        this.ne = from.ne;
        this.vp = (this.np*to.dimension.number);
        this.ve = (this.ne*to.dimension.number);

        this.model = new float[this.vp];

        VertexArray vary = new VertexArray(this.np);


        float domMin = Float.MAX_VALUE, domMax = Float.MIN_VALUE;
        {
            /*
             * Copy In
             */
            final float[] frArray = from.getModelVertices();
            final float[] toArray = vary.array();

            final int frDom = from.geometry.domain();
            final int frRan = from.geometry.range();

            final int toDom = to.domain();
            final int toRan = to.range();

            final int frCC = from.geometry.dimension.number;
            final int toCC = to.dimension.number;


            for (int frC = 0, toC = 0; toC < this.vp; frC += frCC, toC += toCC){

                final float dom = frArray[frC+frDom];
                toArray[toC+toDom] = dom;
                toArray[toC+toRan] = frArray[frC+frRan];

                domMin = Math.min(dom,domMin);
                domMax = Math.max(dom,domMax);
            }
        }
        final float domMid = Z((domMin+domMax)/2.0f);
        {
            /*
             * Each geometric conversion may be defined by a rotation,
             * and the dimensional copy-out that follows.
             */
            final Matrix conversion = new Matrix();
            switch(from.geometry){

            case TB2:

                switch(to){

                case TB2:
                    break;

                case TB3:
                    /*
                     * Origin centered airfoil
                     * See #transform(float,float)
                     */
                    if (0.0f != domMid)
                        conversion.translate(from.geometry.domain.ordinal(),-domMid);

                    conversion.rotate(from.geometry.independent.ordinal(),PI);

                    break;

                default:
                    throw new IllegalArgumentException(String.format("Undefined conversion from '%s' to '%s'",from.geometry.name(),to.name()));
                }
                break;

            case TB3:
                switch(to){

                case TB3:
                    break;

                default:
                    throw new IllegalArgumentException(String.format("Undefined conversion from '%s' to '%s'",from.geometry.name(),to.name()));
                }
                break;

            default:
                throw new IllegalArgumentException(String.format("Undefined conversion from '%s' to '%s'",from.geometry.name(),to.name()));
            }

            vary.transform(conversion);
        }

        if (Geometry.VertexDimension.XYZ == this.geometry.dimension){

            vary.copy(this.model);
        }
        else {
            /*
             * Copy Out
             */
            final float[] frArray = vary.array();
            final float[] toArray = this.model;

            final int frDom = from.geometry.domain();
            final int frRan = from.geometry.range();

            final int toDom = to.domain();
            final int toRan = to.range();

            final int frCC = 3;
            final int toCC = to.dimension.number;


            for (int frC = 0, toC = 0; toC < this.vp; frC += frCC, toC += toCC){

                toArray[toC+toDom] = frArray[frC+frDom];
                toArray[toC+toRan] = frArray[frC+frRan];
            }
        }
    }


    public float x0(){

        if (null != this.section)
            return this.section[X];
        else
            return this.model[X];
    }
    public float xe(){

        if (null != this.section)
            return this.section[this.ve+X];
        else
            return this.model[this.ve+X];
    }
    public float z0(){

        if (null != this.section)
            return this.section[Y];
        else
            return this.model[Y];
    }
    public float ze(){

        if (null != this.section)
            return this.section[this.ve+Y];
        else
            return this.model[this.ve+Y];
    }
    public float y(){

        return this.y;
    }
    public final float tex(){
        return this.tex;
    }
    public final float lex(){
        return this.lex;
    }
    public final float chord(){
        return this.chord;
    }
    public int oppositeN(int n){
        if (n <= this.ne){
            final int d = (this.ne - n);
            return (this.ne + d);
        }
        else
            throw new IllegalArgumentException(String.valueOf(n));
    }
    public int oppositeV(int v){
        if (v <= this.ve){
            final int d = (this.ve - v);
            return (this.ve + d);
        }
        else
            throw new IllegalArgumentException(String.valueOf(v));
    }
    public String getDescription(){
        return this.description;
    }
    public AirfoilVertices setDescription(String d){
        this.description = d;
        return this;
    }
    public GlyphVector getDescription3(){
        GlyphVector description3 = this.description3;
        if (null == description3){
            description3 = Font.toString(this.toString());
            this.description3 = description3;
        }
        return description3;
    }
    public String toString(){
        return this.description;
    }
    public boolean isInitialized(){
        return this.initialized;
    }
    public boolean hasSection(){
        return (this.initialized && (null != this.section) && (0.0f != this.y));
    }
    public final float getModelVerticesArea(){
        final float[] vertices = this.getModelVertices();
        final int count = (vertices.length>>1);

        float A = 0.0f;
        for (int cc = 0; cc < count; cc++){
            int xx = (cc*2);
            int zz = (xx+1);
            float x0 = vertices[xx];
            float z0 = vertices[zz];
            xx += 2;
            zz += 2;
            float x1 = vertices[xx];
            float z1 = vertices[zz];

            A += (x0*z1 - x1*z0);
        }
        return (A / 2.0f);
    }
    public float[] getModelVertices(){
        return this.model;
    }
    public float[] copyModelVertices(){

        if (null != this.model)
            return this.model.clone();
        else
            throw new Error();
    }
    public AirfoilVertices setModelVertices(float[] model){
        if (model.length == vp){
            this.initialized = true;
            this.model = model;
            return this;
        }
        else
            throw new IllegalArgumentException(String.format("%d/%d",model.length,this.vp));
    }
    public AirfoilVertices copyModelVertices(float[] model){
        if (model.length == vp){
            this.model = model.clone();
            return this;
        }
        else
            throw new IllegalArgumentException(String.format("%d/%d",model.length,this.vp));
    }
    public ChordIterator chordIteratorModel(){

        return new ChordIterator(this,this.model.clone());
    }
    public ChordIterator chordIteratorSection(){

        return new ChordIterator(this,this.section.clone());
    }
    public float[] getSectionVertices(){
        return this.section;
    }
    public float[] copySectionVertices(){

        if (null != this.section)
            return this.section.clone();
        else
            throw new Error();
    }
    /**
     * Before transform, translate the objective (lateral foil)
     * section location in the Y dimension.
     *
     * @param y Scaled Y coordinate for this section
     */
    public final void translate(double y){

        this.y = (float)y;
    }
    /**
     * Transform a model to the objective (lateral foil) section
     * location and dimension.
     *
     * Because the model chord is 1.0, the scaled chord is also the
     * scale factor.
     * 
     * @param tex Scaled X coordinate of trailing edge
     * @param chord Scaled chord of this section
     *
     * @see #setModelVertices(float[])
     * @see #copyModelVertices(float[])
     * @see #translate(float)
     */
    public final void transform(float tex, float chord){
        /*
         * Origin centered airfoil
         * See #ctor()
         */
        this.tex = tex;
        this.lex = -tex;
        this.chord = chord;

        final float s = chord;

        final float[] vertices = this.copyModelVertices();
        switch(this.geometry.dimension.number){

        case 2:
            {
                for (int xx = 0, yy = 1; xx < this.vp; xx += 2, yy += 2){

                    vertices[xx] *= s;
                    vertices[yy] *= s;
                }
            }
            break;
        case 3:
            {
                for (int xx = 0, yy = 1, zz = 2; xx < this.vp; xx += 3, yy += 3, zz += 3){

                    vertices[xx] *= s;
                    vertices[yy] = this.y;
                    vertices[zz] *= s;
                }
            }
            break;
        default:
            throw new Error(this.geometry.dimension.name());
        }

        this.section = vertices;
    }
    public float getSectionY(){

        return this.y;
    }
    /**
     * Intersection
     * 
     * @param x Section coordinate in X
     * @param y Section coordinate in Y
     * @param z Section coordinate in Z
     *
     * @return Coordinate in Z
     * 
     * @see #init(int)
     * @see #transform(float,float)
     * @see #translate(float)
     */
    public final float getSectionZ(float x, float y, float z){

        final ChordIterator it = this.chordIteratorSection();
        try {
            if (it.in(x)){

                for (ChordVertex cv: it){

                    final ChordVertex.In in = cv.in(x);

                    switch (in){
                    case Current:
                    case Domain:
                        return cv.getVertexZ(in,x,y,z);

                    case None:
                        break;
                    }
                }

                throw new Geometry.Interpolation.Exception(String.format("x % 4.4f y % 4.4f z % 4.4f",x,y,z));
            }
            else
                throw new Geometry.Interpolation.Exception(String.format("!(%s) x % 4.4f y % 4.4f z % 4.4f",it.inf(x),x,y,z));
        }
        finally {
            it.destroy();
        }
    }
    public AirfoilVertices clone(){

        AirfoilVertices clone = (AirfoilVertices)super.clone();
        clone.model = clone.model.clone();
        if (null != clone.section){
            clone.section = clone.section.clone();
        }
        return clone;
    }
    public VertexArray createModelMeshLines(){

        return this.createModelMeshLines(new VertexArray(VertexArray.Type.Lines));
    }
    public VertexArray createModelMeshLines(VertexArray array){

        switch(this.geometry.dimension.number){

        case 2:
            return array.addLinesXY(this.model);

        case 3:
            return array.addLinesXYZ(this.model);

        default:
            throw new Error(this.geometry.dimension.name());
        }
    }
    public VertexArray createSectionMeshLines(){

        return this.createSectionMeshLines(new VertexArray(VertexArray.Type.Lines));
    }
    public VertexArray createSectionMeshLines(VertexArray array){

        switch(this.geometry.dimension.number){

        case 2:
            return array.addLinesXY(this.section);

        case 3:
            return array.addLinesXYZ(this.section);

        default:
            throw new Error(this.geometry.dimension.name());
        }
    }
    /**
     * @return GL Lines
     */
    public float[] getModelMeshLines(){
        float[] mesh = this.modelMesh;
        if (null == mesh && null != this.model){

            final VertexArray array = this.createModelMeshLines();

            mesh = array.array();

            this.modelMesh = mesh;
        }
        return mesh;
    }
    /**
     * @return GL Lines
     */
    public float[] getModelMeshLinesDescription(){
        float[] mesh = this.modelMesh;
        if (null == mesh && null != this.model){

            final VertexArray array = this.getDescription3();

            final float s = (1.0f/400.0f);

            array.scale(s,s,s);

            array.translate(0.4f,-0.2f,0.0f);

            mesh = array.array();

            this.modelMeshDescription = mesh;
        }
        return mesh;
    }
    /**
     * @return GL Lines
     */
    public float[] getSectionMeshLines(){
        float[] mesh = this.sectionMesh;
        if (null == mesh){

            if (null != this.section){

                final VertexArray array = this.createSectionMeshLines();

                mesh = array.array();

                this.sectionMesh = mesh;
            }
        }
        return mesh;
    }
    public void dispose(){
    }
    public void list(){
        System.out.println("#");
        System.out.println("# Airfoil data listing");
        System.out.println("#");

        if (null != this.description){
            System.out.printf( "# Airfoil: %s%n",this.description);
            System.out.println("#");
        }

        System.out.printf( "# Geometry: %s%n",this.geometry);
        System.out.println("#");
        System.out.println("# [vertex_index] (a_position)");
        System.out.println("#");

        final float[] vertices;
        if (null != this.section)
            vertices = this.section;
        else
            vertices = this.model;

        switch(this.geometry.dimension.number){

        case 2:
            {
                final int verticesCount = vertices.length/2;
                float[] a_position = new float[2];

                for (int vv = 0, ofs; vv < verticesCount; vv++){
                    ofs = (vv*2);
                    System.arraycopy(vertices,ofs,a_position,0,2);

                    if (this.ve == ofs)
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

                    if (this.ve == ofs)
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
    public int compareTo(AirfoilVertices that){

        return this.compareTo(that.y);
    }
    public int compareTo(float y){
        if (EEQ(this.y,y))
            return 0;
        else if (this.y < y)
            return -1;
        else
            return +1;
    }

    /**
     * Iteration over sparse list skips null elements in {@link
     * #hasNext()}.
     */
    public static class Iterator 
        extends Object
        implements java.util.Iterator<AirfoilVertices>
    {

        private final int length;
        private final AirfoilVertices[] list;
        private int index;

        public Iterator(AirfoilVertices[] list){
            super();
            if (null == list){
                this.list = null;
                this.length = 0;
            }
            else {
                this.list = list.clone();
                this.length = this.list.length;
            }
        }


        public boolean hasNext(){

            while (this.index < this.length && null == this.list[this.index]){

                this.index += 1;
            }
            return (this.index < this.length);
        }
        public AirfoilVertices next(){

            if (this.index < this.length){

                return this.list[this.index++];
            }
            else
                throw new java.util.NoSuchElementException();
        }
        public void remove(){
            throw new UnsupportedOperationException();
        }
    }

    public final static AirfoilVertices[] Add(AirfoilVertices[] list, AirfoilVertices item){
        if (null == item)
            return list;
        else if (null == list)
            return new AirfoilVertices[]{item};
        else {
            final int len = list.length;
            AirfoilVertices[] copier = new AirfoilVertices[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
}
