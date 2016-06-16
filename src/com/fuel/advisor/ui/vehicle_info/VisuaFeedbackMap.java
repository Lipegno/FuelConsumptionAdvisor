package com.fuel.advisor.ui.vehicle_info;

import java.util.Vector;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

import com.fuel.advisor.R;
import com.fuel.advisor.ui.vehicle_info.openGLStuff.OpenGLRenderer;
import com.fuel.advisor.ui.vehicle_info.openGLStuff.SimplePlane;

public class VisuaFeedbackMap extends Activity{
	
	private static OpenGLRenderer _map_renderer;
	private GLSurfaceView _map_view;
	private Vector<SimplePlane> _backgroundElements;

	private Button _zoomMinus;
	private Button _zoomPlus;

	
	
	@Override
	 public void onCreate(Bundle savedInstanceState){
		 super.onCreate(savedInstanceState);
		 requestWindowFeature(Window.FEATURE_NO_TITLE);
		// this.setContentView(R.layout.visual_feedback_map);
			
		 SimplePlane map = new SimplePlane(4f,2f);
		 map.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.trip_example2));
		 map.x=1.5f;
		 map.y=0.5f;
	 
//		 _zoomMinus = (Button)findViewById(R.id.zoom_minus);
//		 _zoomPlus  = (Button) findViewById(R.id.zoom_plus);
		 
		 _zoomMinus.getBackground().setAlpha(160);
		 _zoomPlus.getBackground().setAlpha(160);

		 _map_renderer  = new OpenGLRenderer();
		 _map_renderer._clearColor = new float[]{0.86f,0.86f,0.86f,1};
		// _backgroundElements = buildBack();

		 for(int i=0;i<_backgroundElements.size();i++)				// ads the background elements to the renderer
			 _map_renderer.addMesh(_backgroundElements.elementAt(i));
		 
		 _map_renderer.addMesh(map);								// ads the map to the renderer
		 _map_renderer._zoom = -1.5f;
		// _map_view = (GLSurfaceView) findViewById(R.id.map_gl_view);
		
		 _map_view.setRenderer(_map_renderer);
		 _map_view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		// _map_view.setOnTouchListener(this);
	}
//	/**
//	 * Builds the sprites to be used in the background of the map
//	 */
//	public Vector<SimplePlane> buildBack(){
//		Vector<SimplePlane> result = new Vector<SimplePlane>();
//			float[] rgba= new float[]{0,0,0,0};
//			
//			SimplePlane temp = new SimplePlane(1,1);
//			temp.x = -1.5f;
//			temp.y =0.5f;
//			rgba= calculatesColor(_sectionsCons[0]);
//			temp.setColor(rgba[0],rgba[1],rgba[2],rgba[3]);
//			
//			SimplePlane temp2 = new SimplePlane(1,1);
//			temp2.x = 1;
//			temp2.y = 0;
//			rgba= calculatesColor(_sectionsCons[1]);
//			temp2.setColor(rgba[0],rgba[1],rgba[2],rgba[3]);
//			
//			SimplePlane temp3 = new SimplePlane(1,1);
//			temp3.x = 1;
//			temp3.y = 0;
//			rgba= calculatesColor(_sectionsCons[2]);
//			temp3.setColor(rgba[0],rgba[1],rgba[2],rgba[3]);
//			
//			SimplePlane temp4 = new SimplePlane(1,1);
//			temp4.x = 1f;
//			temp4.y = 0;
//			rgba= calculatesColor(_sectionsCons[3]);
//			temp4.setColor(rgba[0],rgba[1],rgba[2],rgba[3]);
//			
//			SimplePlane temp5 = new SimplePlane(1,1);
//			temp5.x = 0;
//			temp5.y = -1f;
//			rgba= calculatesColor(_sectionsCons[4]);
//			temp5.setColor(rgba[0],rgba[1],rgba[2],rgba[3]);
//			
//			SimplePlane temp6 = new SimplePlane(1,1);
//			temp6.x = -1;
//			temp6.y = 0;
//			rgba= calculatesColor(_sectionsCons[5]);
//			temp6.setColor(rgba[0],rgba[1],rgba[2],rgba[3]);
//			
//			SimplePlane temp7 = new SimplePlane(1,1);
//			temp7.x = -1;
//			temp7.y = 0;
//			rgba= calculatesColor(_sectionsCons[6]);
//			temp7.setColor(rgba[0],rgba[1],rgba[2],rgba[3]);
//			
//			SimplePlane temp8 = new SimplePlane(1,1);
//			temp8.x = -1f;
//			temp8.y = 0;
//			rgba= calculatesColor(_sectionsCons[7]);
//			temp8.setColor(rgba[0],rgba[1],rgba[2],rgba[3]);
//			
//		result.add(temp);
//		result.add(temp2);
//		result.add(temp3);
//		result.add(temp4);
//		result.add(temp5);
//		result.add(temp6);
//		result.add(temp7);
//		result.add(temp8);
//		populateLabels();
//		return result;
//	}
//	/**
//	 * Build the presentation for the several information
//	 */
//	public void populateLabels(){
//		int val=0;
//		for(int i=0;i<_sectionsCons.length;i++)
//			val+=_sectionsCons[i];
//	}
//	
//	public void handleButtonClick(View v){
//		
//		switch(v.getId()){
//			
//		case (R.id.finish_vehicle_info_aggregate):
//			try {
//				this.finalize();
//			} catch (Throwable e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			this.finish();
//			break;
//		
//		}
//	}
//	/**
//	 * Handles the click on the zoom buttons
//	 * @param v - the view that has been pressed
//	 */
//	public void handleZoomClick(View v){
//		float temp = _map_renderer._zoom;
//			if(v.getId()==R.id.zoom_plus){
//				_map_renderer._zoom= temp+.2f;  // calls the zoom function of the renderer (performs a translation in the Z axis)
//	   			_map_view.requestRender();
//			}
//			else{
//				_map_renderer._zoom= temp-.2f; // calls the zoom function of the renderer (performs a translation in the Z axis)
//	   			_map_view.requestRender();
//			}
//			float dz  = 1.5f-(1.5f-_map_renderer._zoom);
//			float var = (float) (dz*(Math.tan(Math.PI/4)));
//			_pointRatio = Math.abs(2/var);
//			Log.e(MODULE,""+_pointRatio);
//	}
//	/**
//	 * associates a click with a road section in the map to show info about it
//	 * @param screenX - x screen coordinates
//	 * @param screenY - y screen coordinates
//	 */
//	public void handleMapClick(float screenX, float screenY, View v){
//		int val=0;
//		float ratio = 512f/_width;
//		float x=((_width/2)+screenX-_glsViewOffset.x)*_pointRatio*ratio; // adjusts the points accordint to the transformation made to the map
//		float y=(screenY+_glsViewOffset.y)*_pointRatio*ratio;
//
//		Log.e(MODULE, "PointX"+x);
//		Log.e(MODULE, "PointY"+y);
//		
//		if(x <( (1024/2)*_pointRatio)){
//			if(x <( (1024/4)*_pointRatio)){
//				if(y < ((1024/4)*_pointRatio))
//					val=1;
//					else
//						val=5;
//			}
//			else{
//				if(y < ((512/2)*_pointRatio))
//					val=2;
//					else
//						val=6;
//			}
//		}
//			else{
//				if(x <( 3*(1024/4)*_pointRatio)){
//					if(y<((1024/4)*_pointRatio))
//						val=3;
//						else
//							val=7;
//				}
//			else{
//				if(y < ((1024/4)*_pointRatio))
//					val=4;
//					else
//						val=8;
//		}
//			
//		}
//		_clickedSection=val;
//	}
//	
//	/**
//	 * Calcultes a color to display in the map according to the consumption of the vehicle
//	 * @param cons - consumption on the section
//	 * @return
//	 */
//	public float[] calculatesColor(int cons){
//		
//		float[] rgba = new float[]{0,0,0,1};
//		float max_cons = 40;
//		if(cons >= max_cons*0.7){
//			rgba[0]	  = (cons/max_cons);
//		}
//		if((max_cons*0.4 < cons) && (cons <max_cons*0.7)){
//			rgba[0] = 1f;
//			rgba[1]  = 1.5f- (cons/max_cons);
//		}
//		
//		if(cons<=max_cons*0.4) {
//			rgba[0] = .4f;
//			rgba[1] = 1-(cons/max_cons);	
//		}
//		return rgba;
//	}
//	
//	
//	public void checkBorder(float x, float y){
//		Log.e(MODULE,"ratio_ "+_pointRatio);
//		
//		if(_totalX<-1.3f*_pointRatio){
//			_map_renderer.move(-(x - touchStart.x)/256,0);
//			_totalX-=(x - touchStart.x)/256;
//			_glsViewOffset.x-= x - touchStart.x;
//			 }
//		if(_totalX>1.3f*_pointRatio){
//			_map_renderer.move(-(x - touchStart.x)/256,0);
//			_totalX-=(x - touchStart.x)/256;
//			_glsViewOffset.x-= x - touchStart.x;
//		 }
//		if(_totalY<-0.4f*_pointRatio){
//			_map_renderer.move(0,-(touchStart.y-y)/256);
//			_totalY-=(touchStart.y-y)/256;
//			_glsViewOffset.y-= touchStart.y-y;
//			 }
//		if(_totalY>0.4*_pointRatio){
//			_map_renderer.move(0,-(touchStart.y-y)/256);
//			_totalY-=(touchStart.y-y)/256;
//			_glsViewOffset.y-= touchStart.y-y;
//		 }
//		
//	}
//	
//	/**
//	 * Handles the touches on the screen, its used to perform the scrolling of the map with the fingers
//	 */
//	@Override
//	public boolean onTouch(View v, MotionEvent event) {
//		// TODO Auto-generated method stub
//
//		   switch (event.getAction()) {
//		   		case MotionEvent.ACTION_MOVE:
//		   			_map_renderer.move((event.getX() - touchStart.x)/256,(touchStart.y-event.getY())/256);
//		   			_glsViewOffset.x+= event.getX() - touchStart.x;
//		   			_glsViewOffset.y+= touchStart.y-event.getY();
//		   			_totalX+=(event.getX() - touchStart.x)/256;
//		   			_totalY+=(touchStart.y-event.getY())/256;
//		   			checkBorder(event.getX(),event.getY());
//		   			touchStart.set(event.getX(), event.getY());
//		   			_map_view.requestRender();
//			   		_lastTouchAction=MotionEvent.ACTION_MOVE;
//		   			return true;
//		   		
//		   		case MotionEvent.ACTION_UP:
//		   			if(_lastTouchAction==MotionEvent.ACTION_DOWN){
//		   			new RoadSectionInfoPopup(v,"SECTION: "+_clickedSection,_sectionsCons[_clickedSection-1]+"","","");
//		   			}
//		   			return true;
//		   			
//		   		case MotionEvent.ACTION_DOWN:
//			   		touchStart.set(event.getX(), event.getY());
//			   		Log.e(MODULE,"aqui down  "+event.getX()+"  "+event.getY());
//			   		handleMapClick(event.getX(),event.getY(),v);
//			   		_lastTouchAction=MotionEvent.ACTION_DOWN;
//		   			return true;
//		   	
//		   		default:
//		         return super.onTouchEvent(event);
//		   	}
//		   }
	
}
