
import java.io.File;
import java.util.regex.Pattern;

/**
 * Main argument lib dirname
 * Output manifest style space separated listing of files.
 */
public class Libclasspath {

    public enum Type {
        jogl, tools;
    }
    /**
     * Includes
     */
    public enum Lib {
        GdxAudio("gdx-audio.jar"),
        GdxBackendAndroid("gdx-backend-android.jar"),
        GdxBackendJogl("gdx-backend-jogl.jar"),
        GdxBackendLwjgl("gdx-backend-lwjgl.jar"),
        GdxFreetype("gdx-freetype.jar"),
        GdxImage("gdx-image.jar"),
        Gdx("gdx.jar"),
        GdxJnigen("gdx-jnigen.jar"),
        GdxOpenal("gdx-openal.jar"),
        GdxStbTruetype("gdx-stb-truetype.jar"),
        GdxTiledPreprocessor("gdx-tiled-preprocessor.jar"),
        GdxTools("gdx-tools.jar"),
        GdxAudioNatives("gdx-audio-natives.jar"),
        GdxBackendJoglNatives("gdx-backend-jogl-natives.jar"),
        GdxBackendLwjglNatives("gdx-backend-lwjgl-natives.jar"),
        GdxFreetypeNatives("gdx-freetype-natives.jar"),
        GdxImageNatives("gdx-image-natives.jar"),
        GdxNatives("gdx-natives.jar"),
        GdxStbTruetypeNatives("gdx-stb-truetype-natives.jar"),
        Lxl("lxl-[0-9]+.[0-9]+.[0-9]+.jar"),
        Fv3("fv3-[0-9]+.[0-9]+.[0-9]+.jar"),
        JodaTime("joda-time-[.0-9]+.jar"),
        Path("path-[.0-9]+.jar"),
        Spacetime("spacetime-[.0-9]+.jar"),
        SpacetimeStandalone("spacetime-standalone-[.0-9]+.jar"),
        Json("json-[.0-9]+.jar");



        public final Pattern filename;


        Lib(String filename){
            this.filename = Pattern.compile(filename);
        }


        public boolean matches(String name){
            return this.filename.matcher(name).matches();
        }


        public final static Lib For(File file){
            final String name = file.getName();
            for (Lib lib: Lib.values()){
                if (lib.matches(name)){
                    return lib;
                }
            }
            return null;
        }
    }

    public static void main(String[] argv){
        if (2 == argv.length){
            Type type = Type.valueOf(argv[0]);
            File lib = new File(Basename(argv[1]));
            if (lib.isDirectory()){
                File[] listing = lib.listFiles();
                if (null != listing){
                    final int count = listing.length;
                    final int term = (count-1);
                    switch(type){
                    case jogl:
                        for (int cc = 0; cc < count; cc++){
                            File file = listing[cc];
                            Lib spec = Lib.For(file);
                            if (null != spec){
                                switch(spec){
                                case GdxBackendJogl:
                                case Gdx:
                                case GdxBackendJoglNatives:
                                case GdxNatives:
                                case Lxl:
                                case Fv3:
                                case JodaTime:
                                case Path:
                                case Spacetime:
                                case SpacetimeStandalone:
                                case Json:
                                    if (cc < term)
                                        System.out.printf("lib/%s ",file.getName());
                                    else
                                        System.out.printf("lib/%s",file.getName());
                                    break;
                                default:
                                    break;
                                }
                            }
                        }
                        break;
                    case tools:
                        for (int cc = 0; cc < count; cc++){
                            File file = listing[cc];
                            Lib spec = Lib.For(file);
                            if (null != spec){
                                switch(spec){
                                case GdxFreetype:
                                case GdxImage:
                                case Gdx:
                                case GdxJnigen:
                                case GdxStbTruetype:
                                case GdxTiledPreprocessor:
                                case GdxTools:
                                    if (cc < term)
                                        System.out.printf("lib/%s ",file.getName());
                                    else
                                        System.out.printf("lib/%s",file.getName());
                                    break;
                                default:
                                    break;
                                }
                            }
                        }
                        break;
                    default:
                        throw new Error(type.name());
                    }
                }
                System.exit(0);
            }
            else {
                System.err.printf("Error: directory not found, %s%n",lib.getPath());
                System.exit(1);
            }
        }
        else {
            System.err.println("Usage: Libclasspath Libclasspath.Type Lib.Dir");
            System.exit(1);
        }
    }

    public final static String Basename(String path){
        /*
         * Tail
         */
        while (0 < path.length() && '/' == path.charAt(path.length()-1))
            path = path.substring(0,path.length()-1);
        /*
         * Base
         */
        int idx = path.lastIndexOf('/');
        if (-1 < idx)
            return path.substring(idx+1);
        else
            return path;
    }
    public final static String Suffix(File file){
        String name = file.getName();
        int idx = name.lastIndexOf('.');
        if (0 < idx)
            return name.substring(idx+1);
        else
            return "";
    }
    public final static boolean IsJar(File file){

        return (file.isFile() && Suffix(file).equalsIgnoreCase("jar"));
    }
}
