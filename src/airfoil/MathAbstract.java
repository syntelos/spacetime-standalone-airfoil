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

import fv3.font.GlyphVector;
import static fv3.math.Abstract.*;
import fv3.math.VertexArray;

/**
 * 
 */
public abstract class MathAbstract
    extends spacetime.standalone.Spacetime
    implements fv3.math.Notation
{
    protected final static fv3.font.Font Font;
    static {
        try {
            Font = new fv3.font.HersheyFont.Futural();
        }
        catch (java.io.IOException exc){

            exc.printStackTrace();
            throw new Error();
        }
    }


    /**
     * Linear interpolation (not projection) of Y over X (flips
     * inverted domain points order) -- the argument 'x' is assumed to
     * lie within the segment of the domain bounded by 'x0' and 'x1':
     * <i>x0 &lt;= x &lt;= x1</i>.
     */
    public final static float LIY(float x0, float y0, float x1, float y1, float x){
        if (EEQ(x0,x))
            return y0;
        else if (EEQ(x1,x))
            return y1;
        else if (x0 < x1){
            if (x < x0 || x > x1)
                throw new Geometry.Interpolation.DomainException(x0,x,x1);
            else {
                float y = (y0+((y1-y0)*((x-x0)/(x1-x0))));
                if (y != y)
                    throw new Geometry.Interpolation.NanException(x0,x,x1);
                else
                    return y;
            }
        }
        else {
            if (x < x1 || x > x0)
                throw new Geometry.Interpolation.DomainException(x1,x,x0);
            else {
                float y = (y1+((y0-y1)*((x-x1)/(x0-x1))));
                if (y != y)
                    throw new Geometry.Interpolation.NanException(x1,x,x0);
                else
                    return y;
            }
        }
    }

    /**
     * Subroutine spline is the Numerical Recipes cubic spline
     * routine. It computes the second derivatives at each node for
     * the data points x and y, real vectors of length n.  yp1 and ypn
     * are the endpoint slopes of the spline. If they are set to
     * 1.0e30 or greater then a natural spline is formed.
     */
    public final static void Spline(float[] x, float[] y, int n, float yp1, float ypn, float[] y2){

        float u[] = new float[n];

        if (yp1 > 0.99e30){
            y2[0] = 0.0f;
            u[0] = 0.0f;
        }
        else {
            y2[0] = -0.5f;
            u[0] =(3.0f/(x[1]-x[0]))*((y[1]-y[0])/(x[1]-x[0])-yp1);
        }

        float sig, p;

        for (int i = 1, trm = (n-1); i < trm; i++){

            sig = (x[i]-x[i-1])/(x[i+1]-x[i-1]);
            p = sig*y2[i-1]+2.0f;
            y2[i] = (sig-1.0f)/p;
            u[i] = (6.0f*((y[i+1]-y[i])/(x[i+1]-x[i])-(y[i]-y[i-1])
                         /(x[i]-x[i-1]))/(x[i+1]-x[i-1])-sig*u[i-1])/p;
        }
        float qn, un;

        if (ypn > 0.99e30f){
            qn = 0.0f;
            un = 0.0f;
        }
        else {
            qn = 0.5f;
            un = (3.0f/(x[n-1]-x[n-2]))*(ypn-(y[n-1]-y[n-2])/(x[n-1]-x[n-2]));
        }

        y2[n-1] = (un-qn*u[n-2])/(qn*y2[n-2]+1.0f);

        for (int k = (n-2); -1 < k; k--){

            y2[k] = y2[k]*y2[k+1]+u[k];
        }
    }
    /**
     * Given the arrays xa[1..n] and ya[1..n], which tabulate a
     * function (with the xai's in order), and given the array
     * y2a[1..n], which is the output from spline above, and given a
     * value of x, this routine returns a cubic-spline interpolated
     * value y.
     */
    public final static float Splint(float xa[], float ya[], float y2a[], int n, float x)
    {
        int klo = 1, khi = n, k;
        float h, b, a;

        while (khi-klo > 1) {
            k=(khi+klo) >> 1;
            if (xa[k] > x)
                khi=k;
            else
                klo=k;
        }
        h = xa[khi]-xa[klo];
        if (h == 0.0f) 
            throw new IllegalStateException("Bad xa input to routine splint"); 
        else {
            a = (xa[khi]-x)/h;
            b = (x-xa[klo])/h; 
            return (a*ya[klo]+b*ya[khi]+((a*a*a-a)*y2a[klo]+(b*b*b-b)*y2a[khi])*(h*h)/6.0f);
        }
    }
    public final static void Smooth(float[] xm, float[] ym, float[] xb, float[] yb, int n){
        final int trm = (n-1);
        for (int lc = 0, idx; lc < 2; lc++){
            for (idx = 1; idx < trm; idx++){
                xb[idx] = ((xm[idx-1] + xm[idx] + xm[idx+1]) / 3.0f);
                yb[idx] = ((ym[idx-1] + ym[idx] + ym[idx+1]) / 3.0f);
            }
            System.arraycopy(xb,1,xm,1,trm);
            System.arraycopy(yb,1,ym,1,trm);
        }
    }


    public final static void Redistribute(float[] s1, int nb1, int ne1, 
                                           float[] s2, int nb2, int ne2, float ds)
    {
        float ct[] = new float[10], c[] = new float[10];
        int ict[] = new int[10], ipt[] = new int[10];

        /*
         *  load polynomial vars.
         */
        ipt[0] = (nb2+1);
        ict[0] = 0;
        ct[0] = s1[nb1];

        ipt[1] = (nb2+1);
        ict[1] = 1;
        ct[1] = ds;

        ipt[2] = (ne2+1);
        ict[2] = 0;
        ct[2] = s1[ne1];

        ipt[3] = (ne2+1);
        ict[3] = 1;
        ct[3] = ds;

        if (Airfoil.Polynomial(ct,ict,ipt,4,c))
            throw new IllegalStateException("Singular s-redistribution");
        else {
            s2[nb2] = 0.0f;
            for (int j = (nb2+1); j < ne2; j++){

                s2[j] = (c[0] +
                         c[1] * (j+1) +
                         c[2] * (float)Math.pow((j+1),2) +
                         c[3] * (float)Math.pow((j+1),3));
            }

            s2[ne2] = s1[ne1];
        }
    }
    /**
     * Subroutine polynm is a library routine used to compute the
     * coefficients of an nth degree polynomial for a curve fit.
     * 
     * @param ct is the location or slope of the curve.
     * @param ict is an array with switching values. ict = 0 for a
     * location control.  ict = 1 for a derivative (slope) control.
     * @param ipt is an array of the indicies that are used for control.
     * @param n is the number of control points.
     * @param c is the array of coefficients found.
     */
    public final static boolean Polynomial(float[] ct, int[] ict, int[] ipt, 
                                            final int n, float[] c)
    {
        float a[][] = new float[10][11];
        
        for (int irow = 0; irow < n; irow++){
            for (int icol = 0; icol < n; icol++){
              if ( 0 == ict[irow])
                  /*
                   * Position control
                   */
                  a[irow][icol] = (float)Math.pow( ipt[irow], icol);
              else
                  /*
                   * Slope control
                   */
                  a[irow][icol] = (icol)*(float)Math.pow(ipt[irow],(icol-1));
            }
            /*
             * RHS loaded
             */
            a[irow][n] = ct[irow];
        }
        return Airfoil.Inverse(n,a,c);
    }
    /**
     * Solve a system of linearly independent equations.
     * 
     * @param n is the number of equations.
     * @param a is the n*n matrix of coefficients with the rhs values
     *     appended to the right side of the matrix.
     * @param f is the solution set for the system.
     * 
     * The matrix a is over written and no attempt is made at this
     * time to handle singular matrices.
     */
    public final static boolean Inverse(final int n, float[][] a, float[] c){

        final int trm = (n-1);
        /*
         * Forward sweep
         */
        outer:
        for (int j = 0; j < trm; j++){
            /*
             * Row normalization and stack shifting
             */
            inner:
            for (int i = j; i < n; i++){

                if ( 0.0 != a[i][j]){

                    for (int k = n; k >= j; k--){

                        a[i][k] = a[i][k] / a[i][j];
                    }
                }
                else if ( trm != i){

                    for (int l = (i+1); l < n; l++){

                        if ( 0.0 != a[l][j]){

                            for (int k = j; k <= n; k++){

                                float atemp = a[i][k];
                                a[i][k] = a[l][k];
                                a[l][k] = atemp;
                            }
                        }
                        else if ( l == trm ){
                            break inner;
                        }
                    }
                }
            }
            /*
             * Upper triangularization
             */
            for (int i = (j+1); i < n; i++){

                if ( 0.0 != a[i][j]){

                    for (int k = j; k <= n; k++){

                        a[i][k] = a[i][k] - a[j][k];
                    }
                }
            }
        }
        /*
         * Singularity check
         */
        if ( 0.0 == a[trm][trm]){

           return true;
        }
        /*
         * Back substitution
         */
        else {
            for (int j = trm; -1 < j; j--){

                c[j] = a[j][n] / a[j][j];

                for (int i = (j+1); i < n; i++){

                    c[j] = c[j] - c[i] * a[j][i] / a[j][j];
                }
            }
            return false;
        }
    }

    public static void main(String[] argv){
        if (5 == argv.length){
            final float x0 = Float.parseFloat(argv[0]);
            final float y0 = Float.parseFloat(argv[1]);
            final float x1 = Float.parseFloat(argv[2]);
            final float y1 = Float.parseFloat(argv[3]);
            final float x = Float.parseFloat(argv[4]);

            final float y = LIY(x0,y0,x1,y1,x);

            System.out.printf("%f = LIY(%f,%f,%f,%f,%f)%n",y,x0,y0,x1,y1,x);

            System.exit(0);
        }
        else {
            System.err.println("LIY x0 y0 x1 y1 x");
            System.exit(1);
        }
    }
}
