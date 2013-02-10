/*
 * Spacetime Standalone Airfoil
 * Copyright (C) 2002, Kevin Jones.
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
 * NACA airfoil geometry
 */
public class NACA
    extends Airfoil
{


    /**
     * NACA airfoil series parameters
     */
    public final static class Series {
        /**
         * XY45 function tuple
         */
        public final static class Foil {

            float xc, tc, yc, beta;
        }


        public final int naca, naca_45, naca_3, naca_12;

        public final boolean not5;

        public final float c_max, x_cam, t_max;

        public final String string;


        /**
         * @param number NACA Airfoil Number (base ten unsigned
         * integer, e.g. "2509")
         */
        public Series(String number){
            this(Integer.parseInt(number));
        }
        /**
         * @param number NACA Airfoil Number, e.g. 2509
         */
        public Series(int number){
            super();
            this.naca = number;
            this.naca_45 = this.naca / 1000;
            this.naca_3  = this.naca / 100 - 10 * this.naca_45;
            this.naca_12 = this.naca - 1000 * this.naca_45 - 100 * this.naca_3;
            this.not5 = (number < 9999);

            float c_max = this.naca_45 * 0.01f;
            float x_cam = this.naca_3  * 0.1f;
            float t_max = this.naca_12 * 0.01f;
            if (10 <= this.naca_45){
                switch (this.naca_45){
                case 21:
                    c_max = 0.0580f;
                    x_cam = 361.4f * (float)Math.pow(c_max,3) / 6;
                    break;
                case 22:
                    c_max = 0.1260f;
                    x_cam = 51.64f * (float)Math.pow(c_max,3) / 6;
                    break;
                case 23:
                    c_max = 0.2025f;
                    x_cam = 15.957f * (float)Math.pow(c_max,3) / 6;
                    break;
                case 24:
                    c_max = 0.2900f;
                    x_cam = 6.643f * (float)Math.pow(c_max,3) / 6;
                    break;
                default:
                    c_max = 0.3910f;
                    x_cam = 3.230f * (float)Math.pow(c_max,3) / 6;
                    break;
                }
            }
            this.c_max = c_max;
            this.x_cam = x_cam;
            this.t_max = t_max;

            this.string = String.format("NACA %d",this.naca);
        }


        public String toString(){
            return this.string;
        }
        public void xy45(Foil foil){

            if ( foil.xc < 1.0e-10 ){
                foil.tc = 0.0f;
            }
            else {
                foil.tc = this.t_max * 5 * ( 0.2969f * (float)Math.sqrt(foil.xc)
                                        - foil.xc * ( 0.1260f
                                                      + foil.xc * ( 0.3537f
                                                                    - foil.xc * ( 0.2843f
                                                                                  - foil.xc * 0.1015f))));
            }

            if ( 0.0 == this.c_max){
                foil.yc = 0.0f;
                foil.beta = 0.0f;
            }
            else {
                float r;
                if (this.not5){
                    r = foil.xc / this.x_cam;
                    if ( foil.xc < this.x_cam ){
                        foil.yc   = 2 * this.c_max * r - this.c_max * (r*r);
                        foil.beta = (float)Math.atan( 2 * this.c_max * ( 1 - r ) / this.x_cam );
                    }
                    else {
                        foil.yc   = this.c_max*(this.x_cam*this.x_cam)/(float)Math.pow((1-this.x_cam),2) * ((1-2*this.x_cam)/(this.x_cam*this.x_cam) + 2*r - (r*r) );
                        foil.beta = (float)Math.atan( 2*this.x_cam*this.c_max/Math.pow((1-this.x_cam),2) * ( 1-r));
                    }
                }
                else {
                    r = foil.xc / this.c_max;
                    if ( foil.xc < this.c_max ){
                        foil.yc   = this.x_cam * ( (float)Math.pow(r,3) - 3*(r*r) + (3-this.c_max)*r );
                        foil.beta = (float)Math.atan( this.x_cam/this.c_max * ( 3*(r*r) - 6*r + 3 - this.c_max ));
                    }
                    else {
                        foil.yc   = this.x_cam * ( 1.0f - foil.xc );
                        foil.beta = (float)Math.atan( -this.x_cam );
                    }
                }
            }
        }
    }



    public final Series number;



    public NACA(){
        super();
        this.number = new NACA.Series(Airfoil.Identifier());
    }
    public NACA(String number){
        super();
        this.number = new NACA.Series(number);
    }
    public NACA(int number){
        super();
        this.number = new NACA.Series(number);
    }


    /**
     * Construct {@link Geometry#Model model} ({@link Geometry#TB2})
     */
    public void init(final int np){

        AirfoilVertices model = this.define(np);

        model.setDescription(this.number.toString());

        final int ve = model.ve;

        float[] vertices = model.getModelVertices();
        {
            int lx, ly, ux, uy;
            float dtx, dty;
            Series.Foil foil = new Series.Foil();
            /*
             * Model: (2 == geometry.dimension)
             */
            for (int n = 1; n < model.ne; n++){

                lx = (n-1)<<1;
                ly = (lx + 1);

                ux = (model.np-n)<<1;
                uy = (ux + 1);

                foil.xc = (float)(( 1.0 + Math.cos( Math.PI * lx/ve )) / 2.0);

                this.number.xy45(foil);

                dtx = (float)(foil.tc * Math.sin( foil.beta ));
                dty = (float)(foil.tc * Math.cos( foil.beta ));
                vertices[lx] = foil.xc + dtx;
                vertices[ly] = foil.yc - dty;
                vertices[ux] = foil.xc - dtx;
                vertices[uy] = foil.yc + dty;
            }
        }
        {
            vertices[0] = 1.0f;
            vertices[1] = 0.0f;
        }
        {
            int e = ve;

            vertices[e] = 0.0f;

            e += 1;
            
            vertices[e] = 0.0f;
        }
        {
            final int nx = (model.np-1)<<1;
            final int ny = (nx+1);

            vertices[nx] = vertices[0];
            vertices[ny] = vertices[1];
        }
        model.setModelVertices(vertices);
    }
    public String toString(){
        return this.number.toString();
    }

    /**
     * Print section
     */
    public final static void main(String[] argv){

        Geometry geometry = Geometry.Model;

        int np = 20;

        if (0 < argv.length){

            geometry = Geometry.For(argv[0]);

            if (1 < argv.length){

                np = Integer.parseInt(argv[1]);
            }
        }

        final NACA naca = new NACA();
        {
            naca.init(np);
            naca.generate(Geometry.Model,geometry);

            if (naca.list(geometry)){

                System.exit(0);
            }
            else {
                System.err.printf("Geometry not found '%s'%n",geometry);
                System.exit(1);
            }
        }
    }
}
