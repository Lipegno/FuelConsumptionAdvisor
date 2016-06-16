package com.fuel.advisor.ui.vehicle_info.openGLStuff;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.PointF;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.Log;


@SuppressWarnings("unused")
public class OpenGLRenderer implements Renderer{
	private final Group root;
    private PointF offset;
    private static String MODULE = "Renderer";
	public float _zoom = -2;
	public float[] _clearColor =  new float[]{0,0,0,0};
	public OpenGLRenderer() {
		// Initialize our root.
		Group group = new Group();
		offset = new PointF();
		offset.x=0;
		offset.y=0;
		root = group;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.opengl.GLSurfaceView.Renderer#onSurfaceCreated(javax.microedition
	 * .khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig)
	 */
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// Set the background color to black ( rgba ).
		gl.glClearColor(_clearColor[0], _clearColor[0], _clearColor[0], _clearColor[0]);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	//	Log.e(MODULE,"Surface Created");
		}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.opengl.GLSurfaceView.Renderer#onDrawFrame(javax.microedition.
	 * khronos.opengles.GL10)
	 */
	public void onDrawFrame(GL10 gl) {
		// Clears the screen and depth buffer.
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		// Replace the current matrix with the identity matrix
		gl.glLoadIdentity();
		// Translates ... units into the screen.
		gl.glTranslatef(offset.x, offset.y, _zoom);
		// Draw our scene.
		gl.glDisable(GL10.GL_TEXTURE_2D);
		root.draw(gl);
		//Log.e(MODULE,"Draw");
	}
	
	
	public void move(float dx, float dy){
		
		offset.x+=dx;
		offset.y+=dy;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.opengl.GLSurfaceView.Renderer#onSurfaceChanged(javax.microedition
	 * .khronos.opengles.GL10, int, int)
	 */
	public void onSurfaceChanged(GL10 gl, int width, int height) {

		// Sets the current view port to the new size.
		gl.glViewport(0, 0, width, height);
		// Select the projection matrix
		gl.glMatrixMode(GL10.GL_PROJECTION);
		// Reset the projection matrix
		gl.glLoadIdentity();
		// Calculate the aspect ratio of the window
		GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f,
				1000.0f);
		// Select the modelview matrix
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		// Reset the modelview matrix
		gl.glLoadIdentity();
		//Log.e(MODULE,"Surface Changed");
	}
	/**
	 * Adds a mesh to the root.
	 * 
	 * @param mesh
	 *            the mesh to add.
	 */
	public void addMesh(Mesh mesh) {
		
		root.add(mesh);
	}

	public void removeLast() {
		root.remove(1);
	}
	
	public void clean() {
		root.clean();
	}
}
