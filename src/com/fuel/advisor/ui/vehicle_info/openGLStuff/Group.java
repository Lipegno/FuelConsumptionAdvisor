package com.fuel.advisor.ui.vehicle_info.openGLStuff;

import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.RectF;
import android.util.Log;

/**
 * Group is a mesh with no mesh data itself but can contain one or more other
 * meshes.
 * 
 * @author based on an example from Per-Erik Bergman (per-erik.bergman@jayway.com)
 * 
 */
public class Group extends Mesh {
	private final Vector<Mesh> mChildren = new Vector<Mesh>();

	@Override
	public void draw(GL10 gl) {
		for (int i = 0; i < mChildren.size(); i++)
			mChildren.get(i).draw(gl);
	}

	/**
	 * @param location
	 * @param object
	 * @see java.util.Vector#add(int, java.lang.Object)
	 */
	public void add(int location, Mesh object) {
		mChildren.add(location, object);
	}

	/**
	 * @param object
	 * @return
	 * @see java.util.Vector#add(java.lang.Object)
	 */
	public boolean add(Mesh object) {
		return mChildren.add(object);
	}

	/**
	 * 
	 * @see java.util.Vector#clear()
	 */

	/**
	 * @param location
	 * @return
	 * @see java.util.Vector#get(int)
	 */
	public Mesh get(int location) {
		return mChildren.get(location);
	}

	/**
	 * @param location
	 * @return
	 * @see java.util.Vector#remove(int)
	 */
	public Mesh remove(int location) {
		return mChildren.remove(location);
	}

	/**
	 * @param object
	 * @return
	 * @see java.util.Vector#remove(java.lang.Object)
	 */
	public boolean remove(Object object) {
		return mChildren.remove(object);
	}

	public void clean(){
		 mChildren.clear();
		
	}
	/**
	 * @return
	 * @see java.util.Vector#size()
	 */
	public int size() {
		return mChildren.size();
	}

	@Override
	public RectF get_touchArea() {
		// TODO Auto-generated method stub
		return null;
	}

}
