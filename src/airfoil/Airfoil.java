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


import json.Json;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Basic airfoil geometry
 * 
 * @see NACA
 * @see UIUC
 * @see sagittarius.panel.Airfoil
 * @see sagittarius.panel.Panel
 */
public abstract class Airfoil
    extends MathAbstract
{
    /**
     * {@link Airfoil} instance keys for cache
     */
    public final static class Key
        extends Object
        implements Comparable<Key>
    {

        public final float y;

        public final float lex, tex, chord;

        public final int hashCode;



        public Key(float lex, float tex, float y){
            super();
            if (lex > tex){

                this.lex = lex;
                this.tex = tex;
                this.chord = (lex-tex);
                this.y = y;

                this.hashCode = Float.floatToIntBits( (float)this.lex);
            }
            else
                throw new Error();
        }



        public Airfoil transform(Airfoil foil){

            AirfoilVertices av = foil.getSectionVertices();

            av.translate( this.y);

            av.transform( this.tex, this.chord);

            return foil;
        }
        public int hashCode(){
            return this.hashCode;
        }
        public boolean equals(Object that){
            if (this == that)
                return true;
            else if (that instanceof Key)
                return this.equals((Key)that);
            else
                return false;
        }
        public boolean equals(Key that){
            if (this == that)
                return true;
            else if (null == that)
                return false;
            else {
                return (this.lex == that.lex);
            }
        }
        public int compareTo(Key that){

            if (null == that)
                return 1;
            else if (this.lex == that.lex)
                return 0;
            else if (this.lex < that.lex)
                return -1;
            else
                return 1;
        }


        public final static Key[] Add(Key[] list, Key item){
            if (null == item)
                return list;
            else if (null == list)
                return new Key[]{item};
            else {
                final int len = list.length;
                Key[] copier = new Key[len+1];
                System.arraycopy(list,0,copier,0,len);
                copier[len] = item;
                return copier;
            }
        }
        public final static Key[] Insert(Key[] list, Key item){
            if (null == item)
                return list;
            else if (null == list)
                return new Key[]{item};
            else {
                final int len = list.length;
                Key[] copier = new Key[len+1];
                System.arraycopy(list,0,copier,1,len);
                copier[0] = item;
                return copier;
            }
        }
        public final static Key[] Cat(Key[] a, Key[] b){
            if (null == b)
                return a;
            else if (null == a)
                return b;
            else {
                final int alen = a.length;
                final int blen = b.length;

                Key[] copier = new Key[alen+blen];

                System.arraycopy(a,0,copier,0,alen);
                System.arraycopy(b,0,copier,alen,blen);

                return copier;
            }
        }
    }

    public interface Properties {

        public interface Names
            extends Properties
        {
            public final static String Configuration = "airfoil.Airfoil";
            public final static String Identifier = "airfoil.Airfoil.Identifier";
        }
        public interface Defaults
            extends Properties
        {
            public final static String Configuration = "airfoil.NACA";
            public final static String Identifier = "2509";
        }
    }
    /**
     * System property "airfoil.Airfoil.Identifier" is
     * employed via {@link #Identifier()} as the principal design
     * parameter within an Airfoil subclass.
     */
    public final static String Identifier(){

        String identifier = System.getProperty(Properties.Names.Identifier);
        if (null == identifier)
            identifier = Properties.Defaults.Identifier;

        return identifier;
    }
    public final static void Identifier(String value){
        if (null != value){
            value = value.trim();
            if (0 != value.length()){
                System.setProperty(Properties.Names.Identifier,value);
            }
        }
    }
    /**
     * System property "airfoil.Airfoil" is employed via
     * {@link #Constructor()} as a classname for a class with a public
     * constructor with one parameter {@link Visualization}.
     * 
     * @see java.lang.Class#forName(String)
     */
    public final static String Configuration(){

        String classname = System.getProperty(Properties.Names.Configuration);
        if (null == classname)
            classname = Properties.Defaults.Configuration;

        return classname;
    }
    public final static void Configuration(String classname){
        try {
            Class<?> oclas = (Class<?>)Class.forName(classname);
            if (Airfoil.class.isAssignableFrom(oclas)){

                System.setProperty(Properties.Names.Configuration,classname);
            }
            else
                throw new IllegalArgumentException(classname);
        }
        catch (ClassNotFoundException exc){
            throw new IllegalArgumentException(classname,exc);
        }
    }
    /**
     * Dynamic configuration function used by {@link Body} to
     * construct airfoil.
     */
    public final static Airfoil Constructor(){

        return Airfoil.Constructor(Airfoil.Configuration());
    }
    public final static Airfoil Constructor(String classname){
        if (null != classname){
            try {
                Class<?> oclas = (Class<?>)Class.forName(classname);
                if (Airfoil.class.isAssignableFrom(oclas)){
                    Class<Airfoil> clas = (Class<Airfoil>)oclas;
                    Constructor<Airfoil> ctor = clas.getConstructor();
                    return ctor.newInstance();
                }
                else
                    throw new IllegalArgumentException(classname);
            }
            catch (NoSuchMethodException exc){
                throw new IllegalStateException(classname,exc);
            }
            catch (ClassNotFoundException exc){
                throw new IllegalStateException(classname,exc);
            }
            catch (SecurityException exc){
                throw new IllegalStateException(classname,exc);
            }
            catch (InstantiationException exc){
                throw new IllegalStateException(classname,exc);
            }
            catch (IllegalAccessException exc){
                throw new IllegalStateException(classname,exc);
            }
            catch (InvocationTargetException exc){
                Throwable cause = exc.getCause();
                if (cause instanceof ThreadDeath)
                    throw (ThreadDeath)cause;
                else
                    throw new IllegalStateException(classname,cause);
            }
        }
        else
            throw new IllegalArgumentException(classname);
    }



    public Airfoil(){
        super();
    }



    /**
     * Model construction parameters definition
     */
    protected AirfoilVertices define(int np){

        return this.setModelVertices(new AirfoilVertices(Geometry.Model,np));
    }
    /**
     * Construct model before transformation to objective location and
     * dimension
     */
    public abstract void init(int np);

    /**
     * Model coordinates between zero and one, ordered from the
     * trailing edge at (1,0) to the leading edge at (0,0) -- in
     * clockwise order bottom then top.
     *
     * @return Vertices in (X,Z)+ order, cloned from the array stored
     * internally.
     */
    public final AirfoilVertices getModelVertices(){

        return this.getModelVertices(Geometry.Model);
    }
    public final int countModelVertices(){

        return this.countModelVertices(Geometry.Model);
    }
    public float[] getTriangulationVertices3(){

        return this.getSectionVertices().getSectionVertices();
    }
    /**
     * Section coordinates between zero and one, ordered from the
     * leading edge at (1,0) to the trailing edge at (0,0) -- in
     * counter-clockwise order top then bottom.
     *
     * @return Vertices in (X,Z)+ order, cloned from the array stored
     * internally.
     */
    public final AirfoilVertices getSectionVertices(){

        return this.getModelVertices(Geometry.Section);
    }
    public final AirfoilVertices setSectionVertices(AirfoilVertices section){

        return this.setModelVertices(section);
    }
    public final int countSectionVertices(){

        return this.countModelVertices(Geometry.Section);
    }
    public void list(){
        if (this.list(Geometry.Section))
            return;
        else
            this.list(Geometry.Model);
    }
    public boolean list(Geometry g){
        AirfoilVertices av = this.getModelVertices(g);
        if (null != av){
            av.list();
            return true;
        }
        else
            return false;
    }
    /**
     * @return GL_LINES
     */
    public Mesh getMesh(Geometry geometry){

        final AirfoilVertices af = this.getModelVertices(geometry);

        final float[] points;
        if (af.hasSection() && Geometry.TB3 == geometry)
            points = af.getSectionMeshLines();
        else
            points = af.getModelMeshLines();

        final int count = (points.length/geometry.dimension.number);

        final Mesh mesh = new Mesh(true,count,0,VertexAttribute.Position());

        mesh.setVertices(points);

        return mesh;
    }
    @Override
    public Airfoil clone(){

        return (Airfoil)super.clone();
    }
    public String getAirfoilConfiguration(){
        return Airfoil.Configuration();
    }
    public String getAirfoilIdentifier(){
        return Airfoil.Identifier();
    }
    public void generate(){

        this.generate(Geometry.Model,Geometry.Section);
    }
    public void terminal(){
    }
    public boolean waitfor(){
        return true;
    }
    public void dispose(){
        final Iterable<AirfoilVertices> it = this.iterable(); // type coersion

        for (AirfoilVertices vertices: it){

            vertices.dispose();
        }
    }
    public Json toJson(){
        throw new UnsupportedOperationException();
    }
    public boolean fromJson(Json json){
        throw new UnsupportedOperationException();
    }


    public boolean hasModelVertices(Geometry geometry){
        return (null != this.get(geometry));
    }
    public boolean isInitializedModelVertices(Geometry geometry){
        AirfoilVertices foil = this.getModelVertices(geometry);
        if (null != foil)
            return foil.isInitialized();
        else
            return false;
    }
    public boolean isTransformedModelVertices(Geometry geometry){
        AirfoilVertices foil = this.getModelVertices(geometry);
        if (null != foil)
            return foil.hasSection();
        else
            return false;
    }
    public int countModelVertices(Geometry geometry){

        final AirfoilVertices list = this.getModelVertices(geometry);
        if (null == list)
            return 0;
        else
            return list.np;
    }
    public AirfoilVertices getModelVertices(Geometry geometry){

        return (AirfoilVertices)this.get(geometry);
    }
    public AirfoilVertices copyModelVertices(Geometry geometry){

        AirfoilVertices list = this.getModelVertices(geometry);
        if (null != list){
            list = list.clone();
        }
        return list;
    }
    public AirfoilVertices setModelVertices(AirfoilVertices vary){

        return (AirfoilVertices)this.put(vary.geometry,vary);
    }
    public AirfoilVertices copyModelVertices(AirfoilVertices vary){

        if (null != vary)
            return this.setModelVertices(vary.clone());
        else
            return vary;
    }
    public AirfoilVertices generate(Geometry from, Geometry to){

        AirfoilVertices list = this.getModelVertices(from);

        if (null == list)
            throw new IllegalArgumentException(String.format("Missing vertex set from '%s'",from.name()));

        else if (from != to)

            return this.setModelVertices(new AirfoilVertices(list,to));
        else
            return list;
    }
}
