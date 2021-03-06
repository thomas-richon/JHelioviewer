package org.helioviewer.jhv.opengl;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.swing.SwingUtilities;

import org.helioviewer.jhv.gui.IconBank;

import com.jogamp.common.nio.Buffers;

public class CenterLoadingScreen implements RenderAnimation {

	private int texture;
	private Dimension dimension;
	private final double TOTAL_SEC_4_ONE_ROTATION = 2;
	private final int NUMBER_OF_CIRCLE = 32;
	private final int NUMBER_OF_VISIBLE_CIRCLE = 12;
	private final int POINT_OF_CIRCLE = 36;

	private final float RADIUS = 99;
	private final float CIRCLE_RADIUS = 8;
	private final float DEFAULT_X_OFFSET = -3;
	private final float DEFAULT_Y_OFFSET = 27;
	private int vertices;
	private int indices;
	private int color;
	private float CIRCLE_COLOR = 192 / 255f;
	private int indicesSize;

	private OpenGLHelper openGLHelper;
	
	public CenterLoadingScreen() {
		openGLHelper = new OpenGLHelper();
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					BufferedImage image = IconBank
							.getImage(IconBank.JHVIcon.LOADING_BIG);
					texture = openGLHelper.createTextureID(); 
					openGLHelper.bindBufferedImageToGLTexture(image);
					dimension = new Dimension(image.getWidth(), image
							.getHeight());
				} catch (Exception e) {
					e.printStackTrace();
				}
				initCircleVBO(OpenGLHelper.glContext.getGL().getGL2());
			}
		});
	}

	@Override
	public void render(GL2 gl, double canvasWidth, double canvasHeight) {

		gl.glUseProgram(0);
		
		gl.glDisable(GL2.GL_FRAGMENT_PROGRAM_ARB);
		gl.glDisable(GL2.GL_VERTEX_PROGRAM_ARB);

		
		double imageWidth = dimension.getWidth() / 2.0;
		double imageHeight = dimension.getHeight() / 2.0;
		gl.glDisable(GL2.GL_DEPTH_TEST);

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(-imageWidth, imageWidth, -imageHeight, imageHeight, 10, -10);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glColor3f(1, 1, 1);

		
		gl.glEnable(GL2.GL_BLEND);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
		gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, texture);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER,
				GL2.GL_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER,
				GL2.GL_LINEAR);
		// gl.glColor3f(1, 0, 0);
		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2f(0, 0);
		gl.glVertex2d(-imageWidth, imageHeight);
		gl.glTexCoord2f(1, 0);
		gl.glVertex2d(imageWidth, imageHeight);
		gl.glTexCoord2f(1, 1);
		gl.glVertex2d(imageWidth, -imageHeight);
		gl.glTexCoord2f(0, 1);
		gl.glVertex2d(-imageWidth, -imageHeight);
		gl.glEnd();
		
		gl.glDisable(GL2.GL_DEPTH_TEST);
		gl.glDisable(GL2.GL_BLEND);
		gl.glDisable(GL2.GL_TEXTURE_2D);

		renderCircles(gl);

	}

	private void renderCircles(GL2 gl) {
		// gl.glPushMatrix();
		double t = System.currentTimeMillis() / (TOTAL_SEC_4_ONE_ROTATION*1000.0);
		System.out.println("t1: " + t);
		t = t - (int) t;
		//t /= FPS;
		t = t - (int) t;
		gl.glTranslated(DEFAULT_X_OFFSET, DEFAULT_Y_OFFSET, 0);
		gl.glRotated(t*360, 0, 0, -1);
		gl.glDisable(GL2.GL_FRAGMENT_PROGRAM_ARB);
		gl.glDisable(GL2.GL_VERTEX_PROGRAM_ARB);
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glDisable(GL2.GL_DEPTH_TEST);
		gl.glDisable(GL2.GL_COLOR_MATERIAL);
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
		gl.glEnable(GL2.GL_COLOR_MATERIAL);		
		//gl.glCullFace(GL2.GL_BACK);

		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertices);
		gl.glVertexPointer(2, GL2.GL_FLOAT, 0, 0);
		
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, color);
		gl.glColorPointer(4, GL2.GL_FLOAT, 0, 0);
				
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, indices);
		gl.glDrawElements(GL2.GL_TRIANGLES, indicesSize, GL2.GL_UNSIGNED_INT, 0);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);

		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
}

	@Override
	public void isFinish() {
		// TODO Auto-generated method stub

	}

	public void initCircleVBO(GL2 gl) {
		System.out.println("initCircle");

		IntBuffer indices = Buffers
				.newDirectIntBuffer((POINT_OF_CIRCLE) * 3 * NUMBER_OF_VISIBLE_CIRCLE);
		FloatBuffer vertices = Buffers.newDirectFloatBuffer(((POINT_OF_CIRCLE) * 2 + 2)
				* NUMBER_OF_VISIBLE_CIRCLE);
		FloatBuffer colors = Buffers.newDirectFloatBuffer(((POINT_OF_CIRCLE) * 4 + 4)
				* NUMBER_OF_VISIBLE_CIRCLE);



		for (int j = 0; j  < NUMBER_OF_VISIBLE_CIRCLE; j ++){
			float alpha = (192 - (192 * ((float) j / NUMBER_OF_VISIBLE_CIRCLE))) / 255.f;
			double test = j / (double) NUMBER_OF_CIRCLE;
			
			float x = (float) Math.cos(test * 2 * Math.PI) * RADIUS;
			float y = (float) Math.sin(test * 2 * Math.PI) * RADIUS;
			vertices.put(x);
			vertices.put(y);
			
			colors.put(CIRCLE_COLOR);
			colors.put(CIRCLE_COLOR);
			colors.put(CIRCLE_COLOR);
			colors.put(alpha);
			int middle = (POINT_OF_CIRCLE + 1) * j;
			for (int i = 0; i < POINT_OF_CIRCLE; i++){
				vertices.put((float) (Math.cos(i/(double)POINT_OF_CIRCLE*2*Math.PI))*CIRCLE_RADIUS + x);
				vertices.put((float) (Math.sin(i/(double)POINT_OF_CIRCLE*2*Math.PI))*CIRCLE_RADIUS + y);
				colors.put(CIRCLE_COLOR);
				colors.put(CIRCLE_COLOR);
				colors.put(CIRCLE_COLOR);
				colors.put(alpha);
				System.out.println("alpha : " + alpha);
			}
			
			for (int i = 1; i <= POINT_OF_CIRCLE; i++) {
				int idx1 = i + j * (POINT_OF_CIRCLE + 1);
				int idx2 = i+1 > POINT_OF_CIRCLE ? 1 + j * (POINT_OF_CIRCLE+1) : i+1 + j * (POINT_OF_CIRCLE+1);
				indices.put(idx1);
				indices.put(middle);
				indices.put(idx2);
				}
		}
		vertices.flip();
		colors.flip();
		indices.flip();

		
		int[] buffer = new int[4];
		gl.glGenBuffers(4, buffer, 0);

		this.vertices = buffer[0];
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, this.vertices);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, vertices.limit() * Buffers.SIZEOF_FLOAT, vertices,
				GL.GL_STATIC_DRAW);

		this.indices = buffer[1];
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, this.indices);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, indices.limit() * Buffers.SIZEOF_INT, indices,
				GL.GL_STATIC_DRAW);
		this.indicesSize = indices.limit();

		this.color = buffer[2];
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, this.color);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, colors.limit() * Buffers.SIZEOF_FLOAT, colors,
				GL.GL_STATIC_DRAW);
		
	}

}
