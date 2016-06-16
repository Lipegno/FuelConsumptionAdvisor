package com.fuel.advisor.ui.vehicle_info;

import java.util.Vector;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ZoomControls;

import com.fuel.advisor.R;
import com.fuel.advisor.ui.custom.popupwindow.RoadSectionInfoPopup;
import com.fuel.advisor.ui.vehicle_info.openGLStuff.CustomPlane;
import com.fuel.advisor.ui.vehicle_info.openGLStuff.MapMeshCreator;
import com.fuel.advisor.ui.vehicle_info.openGLStuff.Mesh;
import com.fuel.advisor.ui.vehicle_info.openGLStuff.OpenGLRenderer;
import com.fuel.advisor.ui.vehicle_info.openGLStuff.SimplePlane;

public class HistoricalFeedbackMap extends Activity implements OnTouchListener {
	
	private final String MODULE = "Historical Feedback Map";
	
	private OpenGLRenderer _map_renderer;
	private GLSurfaceView _map_view;

	private int[] _sectionsCons;// = new int[] {0,0,0,0,0,0,0,0};
	private float _totalX=0;
	private float _totalY=0;
	private static int _lastTouchAction;
	private int _clickedSection;
	
	private PointF _glsViewOffset;
	private float _pointRatio = 1f;
	private PointF touchStart;
	private int _width=460;
	private float _var;
	private Vector<Mesh>  bars;
	private Vector<Mesh>  landmarks;

	private ZoomControls _zoomControls;
	
	@Override
	 public void onCreate(Bundle savedInstanceState){
		 super.onCreate(savedInstanceState);
		 requestWindowFeature(Window.FEATURE_NO_TITLE);
		 this.setContentView(R.layout.historical_feedback_map);
		 _zoomControls=(ZoomControls) findViewById(R.id.zoomControlsMap);
		 
		 ZoomClickListenter zoom_in_lst= new ZoomClickListenter(.2f);
		 _zoomControls.setOnZoomInClickListener(zoom_in_lst);
		
		 ZoomClickListenter zoom_out_lst= new ZoomClickListenter(-.2f);
		 _zoomControls.setOnZoomOutClickListener(zoom_out_lst);
		 
		 touchStart		= new PointF();
		 _glsViewOffset = new PointF();
		 _map_renderer  = new OpenGLRenderer();
		 _sectionsCons = new int[] {20,10,50,22,15,85,10,18,20,10,50,22,15,85,10,    // SAMPLE CONSUMPTION ARRAY WITH THE CONSUMPTION VALUES OVER SEVERAL SECTION OF THE ROAD
				 					18,20,10,50,22,15,85,10,18,20,10,50,22,15,85,
				 					10,18,20,10,50,22,15,22,10,18,20,10,50,22,15,
				 					22,10,18,10,50,22,15,22,10,18,20,10,50,22,15,
				 					10,50,22,15,85,10,18,20,10,50,22,15,50,22,15,
				 					10,50,22,15,85,10,18,20,10,50,22,15,50,22,15,
				 					50,22,15,50,22,15,50,22,15,50,22,15,50,22,15,
				 					20,10,50,22,15,22,10,18,20,10,50,22,15,85,10,    // SAMPLE CONSUMPTION ARRAY WITH THE CONSUMPTION VALUES OVER SEVERAL SECTION OF THE ROAD
				 					18,20,10,50,22,15,85,10,18,20,10,50,22,15,85,
				 					10,18,20,10,50,22,15,22,10,18,20,10,50,22,15,
				 					22,10,18,10,50,22,15,22,10,18,20,10,50,22,15,
				 					10,50,22,15,85,10,18,20,10,50,22,15,50,22,15,
				 					10,50,22,15,85,10,18,20,10,50,22,15,50,22,15,
				 					50,22,15,50,22,15,22,22,15,50,22,15,50,22,15};
		 _map_renderer._zoom = -4f;
		 _map_renderer.move(0,0f);
		 _map_renderer._clearColor=new float[]{.94f,.93f,.94f,1f};
		 setDistanceRatio(_map_renderer._zoom);
		 
		 MapMeshCreator test = new MapMeshCreator();
		 test.set_sectionCons(_sectionsCons);
		 bars = test.createMesh();

		 SimplePlane background = new SimplePlane(15,15);
		 background.y=-2;
		 background.loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.historical_map_background));
		 
		 SimplePlane start = new SimplePlane(.2f,.4f);
		 start.loadBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.semaforo));
		 		 
		 SimplePlane end = new SimplePlane(.4f,.4f);
		 end.loadBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.checkered_flag));
		 end.x=bars.elementAt(bars.size()-1).get_touchArea().left;
		 end.y=bars.elementAt(bars.size()-1).get_touchArea().top;
		 bars.add(start);
		 bars.add(end);
		 
		 landmarks = test.buildLandmarks();
		 landmarks.elementAt(0).loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.barreiros));
		 landmarks.elementAt(1).loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.igreja));
		 landmarks.elementAt(2).loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.university));
		 landmarks.elementAt(3).loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.dolce_vita));
		 landmarks.elementAt(4).loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.rotunda_infante));
		 landmarks.elementAt(5).loadBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.tribunal));

		 for(int i=0; i<bars.size();i++)
			 _map_renderer.addMesh(bars.elementAt(i));
		 
		 landmarks.elementAt(0).y=-1f;
		 landmarks.elementAt(0).x-=end.x;
		 
		 for(int i=0; i<landmarks.size();i++)
			 _map_renderer.addMesh(landmarks.elementAt(i));
		 
 		 _map_view = (GLSurfaceView) findViewById(R.id.historical_map_gl_view);
		 _map_view.setRenderer(_map_renderer);
		 _map_view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		 _map_view.setOnTouchListener(this);
	}

	/**
	 * handles the click on the start and end buttons on the map
	 * @param v - the view being pressed
	 */
	public void handleStartEndClick(View v){
	
		
		Log.e(MODULE, "aqui handle start end x"+_glsViewOffset.x+" y"+_glsViewOffset.y);
		if(v.getId()==R.id.go_to_start){
		
			float x_motion=convertCoordinates(-_glsViewOffset.x,-_glsViewOffset.y)[0];
			float y_motion=convertCoordinates(-_glsViewOffset.x,-_glsViewOffset.y)[1];
				_map_renderer.move(x_motion,y_motion);
				_glsViewOffset.x=0;// event.getX() - touchStart.x;
				_glsViewOffset.y=0;// touchStart.y-event.getY();
				_totalX=0;
				_totalY=0;
		}
		else{
			float x_motion= (convertCoordinates(_glsViewOffset.x,_glsViewOffset.y)[0])+bars.elementAt(bars.size()-1).x;
			float y_motion= (convertCoordinates(_glsViewOffset.x,_glsViewOffset.y)[1])+bars.elementAt(bars.size()-1).y;
			Log.i(MODULE,"x Offset "+_glsViewOffset.x+"  y Offset "+_glsViewOffset.y);
			Log.i(MODULE,"x lastBar"+bars.elementAt(bars.size()-1).x+" y lastBar"+bars.elementAt(bars.size()-1).y);
			Log.i(MODULE, "x offset in GL "+(convertCoordinates(_glsViewOffset.x,_glsViewOffset.y)[0])+"y offset in GL "+(convertCoordinates(_glsViewOffset.x,_glsViewOffset.y)[1]));
			Log.i(MODULE, "x motion"+x_motion+ "y motion"+y_motion);
			_map_renderer.move(-x_motion ,-y_motion);
			
			_glsViewOffset.x=-((bars.elementAt(bars.size()-1).x*_width)/_var);
			_glsViewOffset.y=-((bars.elementAt(bars.size()-1).y*_width)/_var);

			_totalX=-((bars.elementAt(bars.size()-1).x*_width)/_var);;
			_totalY=-((bars.elementAt(bars.size()-1).y*_width)/_var);; 
		}

		
		_map_view.requestRender();

	}
	
	/**
	 * Changes the ratio that maps the screen coordinates to the openGL view coordinates
	 * @param zoom - current zoom of the view
	 */
	public void setDistanceRatio(float zoom){
		
		float tg = Math.abs(zoom)*(float) Math.tan(Math.PI/8);
		 _var = 2*tg;
		
	}
	/**
	 * associates a click with a road section in the map to show info about it
	 * @param screenX - x screen coordinates
	 * @param screenY - y screen coordinates
	 */
	public void handleClickSection(float x,float y){
		//y=y-20;
		float openGLClick_x = ((((x-_totalX)/_width)*_var)-(_var/2));
		float openGLClick_y = ((((_width-(y+_totalY))/_width)*_var)-(_var/2));
		Log.e(MODULE, "TotalX:"+_totalX);
		Log.e(MODULE, "TotalY:"+_totalY);
		Log.e(MODULE, "x: "+x+ "  y: "+y);
		Log.e(MODULE, "x: "+openGLClick_x+ "  y: "+openGLClick_y);
	
		RectF rect;
		CustomPlane temp;
		_clickedSection=-1;
		for(int i=1; i<bars.size();i++){
			if(bars.elementAt(i) instanceof CustomPlane){
				temp = (CustomPlane) bars.elementAt(i);
				rect = temp.get_touchArea();
				if(((rect.left<openGLClick_x) && (rect.right>openGLClick_x))){
					if(((rect.bottom<openGLClick_y) && (rect.top>openGLClick_y))){
						Log.e(MODULE,"SECTION"+i);
						_clickedSection=i-1;
					}
				}
			  }
		}
	}
	
	private float[] convertCoordinates(float x, float y){
		
		float openGLClick_x = ((x/_width)*_var);
		float openGLClick_y = ((y/_width)*_var);
		
		return new float[]{openGLClick_x,openGLClick_y};
	}
	
	private void createPopUpwindow(View v){
		
		int section = Math.round(_clickedSection/5);
		new RoadSectionInfoPopup(v,"SECTION: "+section,_sectionsCons[_clickedSection]+"","","");
		
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {

		float x_motion,y_motion;
		   switch (event.getAction()) {
		   		case MotionEvent.ACTION_MOVE:
		   			x_motion=convertCoordinates(((event.getX() - touchStart.x)),((touchStart.y-event.getY())))[0];
		   			y_motion=convertCoordinates(((event.getX() - touchStart.x)),((touchStart.y-event.getY())))[1];
		   			_map_renderer.move(x_motion,y_motion);
		   			_glsViewOffset.x+= event.getX() - touchStart.x;
		   			_glsViewOffset.y+= touchStart.y-event.getY();
		   			_totalX+=(event.getX() - touchStart.x);
		   			_totalY+=(touchStart.y-event.getY());
		   			touchStart.set(event.getX(), event.getY());
		   			_map_view.requestRender();
			   		_lastTouchAction=MotionEvent.ACTION_MOVE;
		   			return true;
		   		
		   		case MotionEvent.ACTION_UP:
		   			if((_lastTouchAction==MotionEvent.ACTION_DOWN)&&_clickedSection>=0){
		   				createPopUpwindow(v);
		   			}
		   			return true;
		   			
		   		case MotionEvent.ACTION_DOWN:
			   		touchStart.set(event.getX(), event.getY());
			   		handleClickSection(event.getX(),event.getY());
			   		_lastTouchAction=MotionEvent.ACTION_DOWN;
		   			return true;
		   	
		   		default:
		         return super.onTouchEvent(event);
		   	}
		   }
	/**
	 * Class that handles the ZoomInClickListener
	 * 
	 * The constructor receives the value for the zoom increment 
	 * positive for a zoom in handler and negative for a zoom out handler
	 * @author admin
	 *
	 */
	private class ZoomClickListenter implements OnClickListener{

		private float _zoomIncrement;
		
		public ZoomClickListenter(float zoomIncrement){
			_zoomIncrement=zoomIncrement;
		}
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Log.i("ZoomList","ZoomClicked");
			float temp = _map_renderer._zoom;
			_map_renderer._zoom= temp+_zoomIncrement;  // calls the zoom function of the renderer (performs a translation in the Z axis)
			
				
			float x_motion=convertCoordinates(-_glsViewOffset.x,-_glsViewOffset.y)[0];
			float y_motion=convertCoordinates(-_glsViewOffset.x,-_glsViewOffset.y)[1];
   			_map_renderer.move(x_motion,y_motion);		 // moves the renderer to the center before the zoom
			 setDistanceRatio(_map_renderer._zoom);		 // resets the variables with the zooming value

			 x_motion=convertCoordinates(_totalX,_totalY)[0];
   			 y_motion=convertCoordinates(_totalX,_totalY)[1];
   			 
   			_map_renderer.move(x_motion,y_motion);		 // moves the renderer back to his position after the zoom
   			_glsViewOffset.x= _totalX;
   			_glsViewOffset.y= _totalY;

			_map_view.requestRender();

		}
		
	}
	
}
