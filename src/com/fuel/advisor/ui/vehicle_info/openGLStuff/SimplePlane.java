package com.fuel.advisor.ui.vehicle_info.openGLStuff;

import android.graphics.RectF;

/**
 * SimplePlane is a setup class for Mesh that creates a plane mesh.
 * 
 * @author based on an example from Per-Erik Bergman (per-erik.bergman@jayway.com)
 * 
 */
public class SimplePlane extends Mesh {
	/**
	 * Create a plane with a default with and height of 1 unit.
	 */
	public SimplePlane() {
		this(1, 1);
	}

	public RectF get_touchArea(){
		return null;
	}
	/**
	 * Create a plane.
	 * 
	 * @param width
	 *            the width of the plane.
	 * @param height
	 *            the height of the plane.
	 */
	public SimplePlane(float width, float height) {
		// Mapping coordinates for the vertices
		float textureCoordinates[] = { 0.0f, 1.0f, //
				1.0f, 1.0f, //
				0.0f, 0.0f, //
				1.0f, 0.0f, //
		};

		short[] indices = new short[] { 0, 1, 2, 1, 3, 2 };

		float[] vertices = new float[] { -width/2, -height/2, 0.0f,width/2, -height/2, 0.0f,
				 -width/2, height/2, 0.0f, width/2, height/2, 0.0f };
		setIndices(indices);
		setVertices(vertices);
		setTextureCoordinates(textureCoordinates);
	}
}
