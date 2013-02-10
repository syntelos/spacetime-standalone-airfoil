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

import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

/**
 * 
 */
public final class Viewport
    extends Dimension2D
{
    /**
     * Screen fractions preserving aspect ratio
     */
    public enum Screen {

        Sixteenth, Twelfth, Eighth, Sixth, Quarter, ThreeEighths, Half, ThreeQuarters, Unity;

        public Viewport viewport(){
            return Screen.For(this);
        }


        private final static Viewport[] Ratios;
        static {
            final Dimension2D screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            final float width = ((float)screen.getWidth())/4.0f;
            final float height = ((float)screen.getHeight())/4.0f;

            Ratios = new Viewport[]{

                new Viewport(width/4.0f,height/4.0f),
                new Viewport(width/3.0f,height/3.0f),
                new Viewport(width/2.0f,height/2.0f),
                new Viewport(width/1.5f,height/1.5f),
                new Viewport(width,height),
                new Viewport(width*1.5f,height*1.5f),
                new Viewport(width*2.0f,height*2.0f),
                new Viewport(width*3.0f,height*3.0f),
                new Viewport(width*4.0f,height*4.0f),
            };
        }

        public final static Viewport For(Screen ratio){
            return Screen.Ratios[ratio.ordinal()].clone();
        }
    }



    public final float width, height;


    public Viewport(double width, double height){
        this( (float)width, (float)height);
    }
    public Viewport(float width, float height){
        super();
        if (0.0f < width && 0.0f < height){
            this.width = width;
            this.height = height;
        }
        else
            throw new IllegalArgumentException();
    }


    public double getWidth(){
        return this.width;
    }
    public double getHeight(){
        return this.height;
    }
    public float min(){
        return Math.min(this.width,this.height);
    }
    public float max(){
        return Math.max(this.width,this.height);
    }
    public void setSize(double width, double height){
        throw new UnsupportedOperationException();
    }
    public Viewport clone(){
        return (Viewport)super.clone();
    }
    public java.awt.Dimension toDimension(){
        return new java.awt.Dimension((int)this.width,(int)this.height);
    }
    public java.awt.Dimension toHalfDimension(){
        return new java.awt.Dimension((int)(this.width/2.0f),(int)(this.height/2.0f));
    }
    public boolean equals(Object that){
        if (that instanceof Viewport)
            return this.equals( (Viewport)that);
        else
            return false;
    }
    public boolean equals(Viewport that){
        if (this == that)
            return true;
        else if (null == that)
            return false;
        else
            return (this.width == that.width && this.height == that.height);
    }
}
