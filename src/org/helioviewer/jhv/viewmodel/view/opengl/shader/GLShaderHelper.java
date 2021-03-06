package org.helioviewer.jhv.viewmodel.view.opengl.shader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import org.helioviewer.jhv.base.FileUtils;
import org.helioviewer.jhv.viewmodel.renderer.GLCommonRenderGraphics;

/**
 * Helper class to handle OpenGL shaders.
 * 
 * <p>
 * This class provides a lot of useful functions to handle shaders in OpenGL,
 * including compiling them. Therefore, it uses the Cg stand-alone compiler.
 * 
 * <p>
 * For further information about how to build shaders, see
 * {@link GLShaderBuilder} as well as the Cg User Manual.
 * 
 * @author Markus Langenberg
 */
public class GLShaderHelper {
    private static LinkedList<Integer> allShaders = new LinkedList<Integer>();
    
    private static File tmpAsm;
    private static File tmpCg;

    /**
     * Initializes the helper.
     * 
     * This function has to be called before using any other helper function.
     * 
     * @param _tmpPath
     *            Location where to put temporary files.
     */
    public static void initHelper(GL2 gl) throws IOException {
        System.out.println(">> GLShaderHelper.initHelper(GL gl, String _tmpPath) > Initialize helper functions");
        
        Path tmpDir=Files.createTempDirectory("jhv-cg");
        tmpDir.toFile().deleteOnExit();
        
        tmpCg = new File(tmpDir.toFile(), "tmp.cg");
        tmpAsm = new File(tmpDir.toFile(), "tmp.asm");
        if (tmpAsm.exists()) {
            tmpAsm.delete();
        }
        tmpAsm.deleteOnExit();
        tmpCg.deleteOnExit();


        System.out.println(">> GLShaderHelper.initHelper(GL gl, String _tmpPath) > temp path: " + tmpDir.toString());
    }

    /**
     * Generates a new shader.
     * 
     * @param gl
     *            Valid reference to the current gl object
     * @return new shader id
     */
    public int genShaderID(GL2 gl) {
        int id = genStandaloneShaderID(gl);
        allShaders.add(id);
        return id;
    }

    public int genStandaloneShaderID(GL2 gl) {
        int[] tmp = new int[1];
        gl.glGenProgramsARB(1, tmp, 0);
        return tmp[0];
    }

    /**
     * Deletes an existing shader.
     * 
     * It is not possible to delete a shader that has not been generated by this
     * helper.
     * 
     * @param gl
     *            Valid reference to the current gl object
     * @param shaderID
     *            Shader id to delete
     */
    public void delShaderID(GL2 gl, int shaderID) {
        if (!allShaders.contains(shaderID))
            return;

        if (gl == null) {
            gl = GLU.getCurrentGL().getGL2();
        }

        allShaders.remove(allShaders.indexOf(shaderID));

        int[] tmp = new int[1];
        tmp[0] = shaderID;
        gl.glDeleteProgramsARB(1, tmp, 0);
    }

    /**
     * Deletes all textures generates by this helper.
     * 
     * This might be necessary to clean up after not using OpenGL any more.
     * 
     * @param gl
     *            Valid reference to the current gl object
     */
    public void delAllShaderIDs(GL2 gl) {
        GLCommonRenderGraphics.clearShader();
        for (int i = allShaders.size() - 1; i >= 0; i--) {
            delShaderID(gl, allShaders.get(i));
        }
    }

    /**
     * Compiles a program and loads the result to a given shader.
     * 
     * <p>
     * Note, that the only mechanism to display errors is the general check for
     * OpenGL errors during the rendering process. If the given program contains
     * errors, "invalid operation" should be displayed on the console. To verify
     * the exact error, try calling the Cg stand- alone compiler from the
     * console.
     * 
     * @param gl
     *            Valid reference to the current gl object
     * @param programType
     *            Shader type, has to be GL_VERTEX_PROGRAM_ARB or
     *            GL_FRAGMENT_PROGRAM_ARB
     * @param source
     *            Complete program code, given in Cg.
     * @param target
     *            Shader id to put the compiled program
     */
    public synchronized void compileProgram(GL2 gl, int programType, String source, int target) {
        
        String cacheKey=programType+"@"+source;
        
        String compiledProgram = shaderCache.get(cacheKey);
        if(compiledProgram == null)
        {
            putContents(tmpCg, source);
    
            String profile = programType == GL2.GL_FRAGMENT_PROGRAM_ARB ? "arbfp1" : "arbvp1";
            List<String> args = new ArrayList<String>();
            args.add("-profile");
            args.add(profile);
            args.add("-o");
            args.add(tmpAsm.getPath());
            args.add(tmpCg.getPath());
            
            try {
                Process p = FileUtils.invokeExecutable("cgc", args);
                FileUtils.logProcessOutput(p, "cgc", true);
                p.waitFor();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    
            if (!tmpAsm.exists()) {
                System.err.println("Error while compiling shader program:");
                System.err.println(source);
                return;
            }
            
            //Log.debug("GLShaderHelper.compile Source: "+source);
            compiledProgram = getContents(tmpAsm);
            // Log.debug("GLShaderHelper.compile Compiled Code: "+compiledProgram);
            
            shaderCache.put(cacheKey,compiledProgram);
        }
        
        gl.glBindProgramARB(programType, target);
        
        CharBuffer programBuffer = CharBuffer.wrap(compiledProgram);
        gl.glProgramStringARB(programType, GL2.GL_PROGRAM_FORMAT_ASCII_ARB, compiledProgram.length(), programBuffer.toString());
    }
    
    private static final HashMap<String,String> shaderCache=new HashMap<>();

    /**
     * Reads the contents of a file and puts them to a String.
     * 
     * @param aFile
     *            File to read
     * @return contents of the file
     */
    private String getContents(File aFile) {

        StringBuilder contents = new StringBuilder();

        try {
            BufferedReader input = new BufferedReader(new FileReader(aFile));
            try {
                String line = null; // not declared within while loop

                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return contents.toString();
    }

    /**
     * Writes String to a File.
     * 
     * @param aFile
     *            Output file
     * @param content
     *            Data to write
     */
    private void putContents(File aFile, String content) {
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(aFile));
            output.write(content);
            output.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
