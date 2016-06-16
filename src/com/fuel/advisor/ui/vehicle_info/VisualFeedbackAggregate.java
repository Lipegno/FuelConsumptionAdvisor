package com.fuel.advisor.ui.vehicle_info;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.fuel.advisor.R;

public class VisualFeedbackAggregate extends Activity {

	 public static final String TYPE = "type";

	  private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	  private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	  private GraphicalView mChartView;
	  private List<double[]> values = new ArrayList<double[]>(); // values to be represented in the graph
	  private  String[] titles = new String[] { "DSP Binning" };


	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.visual_feedback_aggregate);
	   
	    values.add(new double[] { 5230, 7300, 9240, 10540, 7900, 9200, 12030});
	    
	    mDataset = buildBarDataset(titles, values);
	    
        int colors =  Color.LTGRAY ;
        
        mRenderer = buildBarRenderer(colors);
        mRenderer.setOrientation(Orientation.HORIZONTAL);
        setChartSettings(mRenderer, "Consumo nos ultimos XX minutos", "", "", 0.5,
            7.5, 0, 14000, Color.GRAY, Color.LTGRAY, 12f);
        mRenderer.setXLabels(1);
        mRenderer.setYLabels(10);
        mRenderer.addTextLabel(1, "DSP1");
        mRenderer.addTextLabel(2, "DSP2");
        mRenderer.addTextLabel(3, "DSP3");
        mRenderer.addTextLabel(4, "DSP4");
        mRenderer.addTextLabel(5, "DSP5");
        mRenderer.addTextLabel(6, "DSP6");
        mRenderer.addTextLabel(7, "DSP7");
        mRenderer.setDisplayChartValues(true);
     
        
        LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
	      mChartView = ChartFactory.getBarChartView(this, mDataset, mRenderer,Type.DEFAULT );
	      layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
	          LayoutParams.FILL_PARENT));
        
      	  }

	  /**
	   * Builds an XY multiple time dataset using the provided values.
	   * 
	   * @param titles the series titles
	   * @param xValues the values for the X axis
	   * @param yValues the values for the Y axis
	   * @return the XY multiple time dataset
	   */
	  private XYMultipleSeriesDataset buildBarDataset(String[] titles, List<double[]> values) {
		    
		  XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		    CategorySeries series = new CategorySeries(titles[0]);
		    
		    double[] v = values.get(0);
		      int seriesLength = v.length;
		      for (int k = 0; k < seriesLength; k++) {
		        series.add(v[k]);
		      }
		      dataset.addSeries(series.toXYSeries());
		    
		    return dataset;
		}
	  
	  /**
	   * Sets up the graph
	   * @param renderer - renderer of the graph
	   * @param title - title of the graph
	   * @param xTitle - title of the x axis
	   * @param yTitle - title of y axis
	   * @param xMin - min value possible o the x axis
	   * @param xMax - max values possible on the x axis
	   * @param yMin - min value possible o the y axis
	   * @param yMax - max value possible o the y axis
	   * @param axesColor - color for the axis
	   * @param labelsColor - color for the labels
	   * @param labelsTextSize - text size for the labels
	   */
	  private void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
			  						String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
			  						int labelsColor, float labelsTextSize) {
		  
		    renderer.setChartTitle(title);
		    renderer.setXTitle(xTitle);
		    renderer.setYTitle(yTitle);
		    renderer.setXAxisMin(xMin);
		    renderer.setXAxisMax(xMax);
		    renderer.setYAxisMin(yMin);
		    renderer.setYAxisMax(yMax);
		    renderer.setAxesColor(axesColor);
		    renderer.setLabelsColor(labelsColor);
	        renderer.setLabelsTextSize(labelsTextSize);

		  }
	  
	  /**
	   * sets ups the renderer for the bar chart
	   * @param colors - colors for the bars
	   * @return the renderer with the right color the series
	   */
	  private XYMultipleSeriesRenderer buildBarRenderer(int color) {
		  	
		  	  XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		      SimpleSeriesRenderer r = new SimpleSeriesRenderer();
		      r.setColor(color);
		      renderer.addSeriesRenderer(r);
		 
		    return renderer;
	  }
}