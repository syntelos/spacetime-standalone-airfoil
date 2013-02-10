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

/**
 *
 */
public enum Geometry
    implements fv3.math.Notation
{
    /**
     * Conventional Model Geometry
     */
    TB2(PathOrder.TE_B2T_CW,VertexDimension.XY, Axis.X,Axis.Y),

    /**
     * Objective Section Geometry
     */
    TB3(PathOrder.TE_B2T_CCW,VertexDimension.XYZ,Axis.X,Axis.Z);


    public final static int Count = Geometry.values().length;

    public final static Geometry Model = Geometry.TB2;
    public final static Geometry Section = Geometry.TB3;

    /**
     * @see Geometry#For(java.lang.String)
     */
    public enum Defaults {

        Model(Geometry.Model), 
        Section(Geometry.Section);


        public final Geometry geometry;


        private Defaults(Geometry geometry){
            this.geometry = geometry;
        }
    }
    /**
     * @param name One of Defaults or Geometry (case sensitive)
     */
    public final static Geometry For(String name){
        try {
            return Defaults.valueOf(name).geometry;
        }
        catch (RuntimeException exc){

            return Geometry.valueOf(name);
        }
    }

    /**
     *
     */
    public abstract static class Interpolation {

        /**
         *
         */
        public static class Exception
            extends IllegalArgumentException
        {
            public Exception(String m){
                super(m);
            }
            public Exception(){
                super();
            }
        }
        /**
         *
         */
        public final static class NanException
            extends Interpolation.Exception
        {

            public final float x0, x, x1;


            public NanException(float x0, float x, float x1){
                super(String.format("%f <= %f <= %f",x0,x,x1));
                this.x0 = x0;
                this.x = x;
                this.x1 = x1;
            }
        }
        /**
         *
         */
        public final static class DomainException
            extends Interpolation.Exception
        {

            public final float x0, x, x1;


            public DomainException(float x0, float x, float x1){
                super(String.format("%f <= %f <= %f",x0,x,x1));
                this.x0 = x0;
                this.x = x;
                this.x1 = x1;
            }
        }
        /**
         *
         */
        public final static class Interpolation2
            extends Interpolation
        {
            public final float domain, range;


            protected Interpolation2(Geometry geometry, float d, float r){
                super(geometry);
                this.domain = d;
                this.range = r;
            }


            public boolean is2(){
                return true;
            }
            public float[] toArray(){

                final float[] vertices = new float[2];

                final int xD = this.geometry.domain();
                final int xR = this.geometry.range();

                vertices[xD] = this.domain;
                vertices[xR] = this.range;

                return vertices;
            }
            public Float getDomain(){
                return this.domain;
            }
            public Float getRange(){
                return this.range;
            }
            public Float getIndependent(){
                return null;
            }
        }
        /**
         *
         */
        public final static class Interpolation3
            extends Interpolation
        {
            public final float domain, range, independent;


            protected Interpolation3(Geometry geometry, float d, float r, float i){
                super(geometry);
                this.domain = d;
                this.range = r;
                this.independent = i;
            }


            public boolean is3(){
                return true;
            }
            public float[] toArray(){

                final float[] vertices = new float[3];

                final int xD = this.domain();
                final int xR = this.range();
                final int xI = this.independent();

                vertices[xD] = this.domain;
                vertices[xR] = this.range;
                vertices[xI] = this.independent;

                return vertices;
            }
            public Float getDomain(){
                return this.domain;
            }
            public Float getRange(){
                return this.range;
            }
            public Float getIndependent(){
                return this.independent;
            }
        }


        public final Geometry geometry;


        protected Interpolation(Geometry geometry){
            super();
            this.geometry = geometry;
        }


        public abstract float[] toArray();

        public abstract Float getDomain();
        public abstract Float getRange();
        public abstract Float getIndependent();

        public final Float getX(){

            return this.geometry.getX(this);
        }
        public final Float getY(){

            return this.geometry.getY(this);
        }
        public final Float getZ(){

            return this.geometry.getZ(this);
        }
        public boolean is2(){
            return false;
        }
        public boolean is3(){
            return false;
        }
        public final int domain(){
            return this.geometry.domain();
        }
        public final int range(){
            return this.geometry.range();
        }
        public final int independent(){
            return this.geometry.independent();
        }
    }
    public enum GeomOrder {
        CW, CCW;


        public int peek(int delta){
            switch(this){
            case CW:
                return delta;
            default:
                return -(delta);
            }
        }
    }
    public enum EdgeOrder {
        LE, TE;
    }
    public enum SurfaceOrder {
        Top, Bottom;


        public SurfaceOrder next(){
            switch(this){
            case Top:
                return Bottom;
            case Bottom:
                return Top;
            default:
                throw new Error(this.name());
            }
        }
    }
    public enum PathOrder {
        /**
         * Model (model 2d) coordinates between zero and one, ordered
         * from the trailing edge at (1,0) to the leading edge at
         * (0,0) -- in clockwise order bottom then top.
         */
        TE_B2T_CW(EdgeOrder.TE,SurfaceOrder.Bottom,GeomOrder.CW),

        /**
         * Section (model 3d) coordinates between zero and one,
         * ordered from the trailing edge at (-0.5,0,0) to the leading
         * edge at (0.5,0,0) -- in counter-clockwise order bottom to
         * top.
         */
        TE_B2T_CCW(EdgeOrder.TE,SurfaceOrder.Bottom,GeomOrder.CCW);


        public final EdgeOrder edge;

        public final SurfaceOrder surface;

        public final GeomOrder order;


        private PathOrder(EdgeOrder e, SurfaceOrder s, GeomOrder g){
            this.edge = e;
            this.surface = s;
            this.order = g;
        }
    }
    public enum Axis {
        X, Y, Z;


        public final static Axis Independent(Axis domain, Axis range){
            switch (domain){
            case X:
                switch (range){
                case Y:
                    return Z;
                case Z:
                    return Y;
                default:
                    throw new Error();
                }
            case Y:
                switch (range){
                case X:
                    return Z;
                case Z:
                    return X;
                default:
                    throw new Error();
                }
            case Z:
                switch (range){
                case X:
                    return Y;
                case Y:
                    return X;
                default:
                    throw new Error();
                }
            default:
                throw new Error();
            }
        }
    }
    public enum VertexDimension {
        XY(2), XYZ(3);


        public final int number;


        private VertexDimension(int number){
            this.number = number;
        }
    }



    public final PathOrder path;

    public final VertexDimension dimension;

    public final Axis domain;

    public final Axis range;

    public final Axis independent;

    public final boolean isTop, isBottom;


    private Geometry(PathOrder p, VertexDimension d, 
                     Axis dd, Axis rr)
    {
        this.path = p;
        this.dimension = d;
        this.domain = dd;
        this.range = rr;
        this.independent = Axis.Independent(dd,rr);

        this.isTop = (SurfaceOrder.Top == this.path.surface);
        this.isBottom = (SurfaceOrder.Bottom == this.path.surface);
    }


    public Float getX(Interpolation in){
        if (Axis.X == this.domain)
            return in.getDomain();
        else if (Axis.X == this.range)
            return in.getRange();
        else
            return in.getIndependent();
    }
    public Float getY(Interpolation in){
        if (Axis.Y == this.domain)
            return in.getDomain();
        else if (Axis.Y == this.range)
            return in.getRange();
        else
            return in.getIndependent();
    }
    public Float getZ(Interpolation in){
        if (Axis.Z == this.domain)
            return in.getDomain();
        else if (Axis.Z == this.range)
            return in.getRange();
        else
            return in.getIndependent();
    }
    public boolean in(float p, float x, float n){

        switch(this.path.order){
        case CW:
            return (p >= x && n <= x);

        case CCW:
            return (n >= x && p <= x);

        default:
            throw new Error();
        }
    }
    public String inf(float p, float x, float n){
        switch(this.path.order){
        case CW:
            return String.format("(p % 4.4f) >= (x % 4.4f) && (n % 4.4f) <= (x % 4.4f)",p,x,n,x);

        case CCW:
            return String.format("(n % 4.4f) >= (x % 4.4f) && (p % 4.4f) <= (x % 4.4f)",n,x,p,x);

        default:
            throw new Error();
        }
    }
    public boolean is(PathOrder p){
        return (p == this.path);
    }
    public boolean is(VertexDimension d){
        return (d == this.dimension);
    }
    public boolean is(GeomOrder g){
        return (g == this.path.order);
    }
    public int domain(){
        return this.offset(this.domain);
    }
    public int range(){
        return this.offset(this.range);
    }
    public int independent(){
        return this.offset(this.independent);
    }
    public int offset(Axis a){
        /*
         * Switching on Axis to return fv3.math.Notation
         */
        switch(this.dimension){
        case XY:
            switch(a){
            case X:
                return X;
            case Y:
                return Y;
            default:
                throw new Error(a.name());
            }
        case XYZ:
            switch(a){
            case X:
                return X;
            case Y:
                return Y;
            case Z:
                return Z;
            default:
                throw new Error(a.name());
            }
        default:
            throw new Error(a.name());
        }
    }
    public Interpolation interpolate(ChordVertex p, ChordVertex n, float x, SurfaceOrder s){

        if (3 == this.dimension.number){

            final float pX = p.getX();
            final float nX = n.getX();

            final float pY = p.getY();
            final float nY = n.getY();

            final float pZ = p.getZ(s);
            final float nZ = n.getZ(s);

            final float y = MathAbstract.LIY(pX,pY,nX,nY,x);

            final float z = MathAbstract.LIY(pX,pZ,nX,nZ,x);

            return new Interpolation.Interpolation3(this,x,y,z);
        }
        else {
            throw new UnsupportedOperationException("TODO");
        }
    }
    public float[] toArray(Interpolation a, 
                           Interpolation b)
    {
        final int n = this.dimension.number;

        float[] vertices = new float[n<<1];

        System.arraycopy(a.toArray(),0,vertices,0,n);

        System.arraycopy(b.toArray(),0,vertices,n,n);

        return vertices;
    }
}
