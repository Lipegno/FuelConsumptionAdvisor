package com.fuel.advisor.ui.vehicle_info.openGLStuff;

import android.graphics.RectF;
import android.util.Log;
/**
 * Custom Plane class
 * This class represents a plane to be renderer, it extends 
 * the Mesh class (which is responsible of for all the operations) related to the rendering itself
 * the constructor for the customPlane class receives the 4 vertices of the quad plane to be rendered
 * in addiction the class has a RectF variable that holds the left,top,botom, and right borders of the plane
 * those values are used to map the coordinates for the touch events (on the main activity)
 * @author FILIPE	
 *
 */
public class CustomPlane extends Mesh {

	private RectF _touchArea;

	/**
	 * Creates a custome plane with the given coordinates
	 *                           
	 * @param x1 
	 * @param y1  
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 */
	public CustomPlane(float x1, float y1, float z1, float x2, float y2, float z2) {
	
		float dx =.1f; //.1f;
		float dy = .1f; //.1f;
		short[] indices = new short[] { 0, 1, 2, 1, 3, 2 };

		float textureCoordinates[] = { 0.0f, 1.0f, //
				1.0f, 1.0f, //
				0.0f, 0.0f, //
				1.0f, 0.0f, //
		};
		
		float angle = (float) Math.atan((y2-y1)/(x2-x1));
		angle =  (float)(Math.PI/2)-angle;
		dx = Math.abs((float) (0.1f*Math.cos(angle)));
		dy = Math.abs((float) (0.1f*Math.sin(angle)));

		if(x1<=x2)
			dy*=-1;
		
		if(y1>=y2)
			dx*=-1;

		
		Log.e("CustomPlaneAnge",angle+"");
	
		if(x1>x2){
			if(y1>y2){
				_touchArea=new RectF(x2-.1f+Math.abs(dx),y1+.1f+Math.abs(dy),x1+.1f+Math.abs(dx),y2-.1f-Math.abs(dy));
			}
			else{
				_touchArea=new RectF(x2-.1f+Math.abs(dx),y2+.1f+Math.abs(dy),x1+.1f+Math.abs(dx),y1-.1f-Math.abs(dy));
			}
		}
		else{
			if(y1>y2){
				_touchArea=new RectF(x1-.1f-Math.abs(dx),y1+.1f+Math.abs(dy),x2+.1f+Math.abs(dx),y2-.1f-Math.abs(dy));
			}
			else{
				_touchArea=new RectF(x1-.1f-Math.abs(dx),y2+.1f+Math.abs(dy),x2+1f+Math.abs(dx),y1-.1f-Math.abs(dy));
			}
		}

		float[] vertices = new float[] { x1, y1, z1, x1+dx, y1+dy, z1,
				 x2, y2, z2,  x2+dx,y2+dy, z2 };
		setIndices(indices);
		setVertices(vertices);
		setTextureCoordinates(textureCoordinates);

	}

	public void set_touchArea(RectF _touchArea) {
		this._touchArea = _touchArea;
	}

	public RectF get_touchArea() {
		return _touchArea;
	}
}
