package com.fuel.advisor.ui.custom.popupwindow;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.fuel.advisor.R;

/**
 * Class responsible  for showing the consumption of a given road segment in the map_view 
 * @author Filipe Quintal
 *
 */
public class RoadSectionInfoPopup extends PopupWindowScreen {

	@SuppressWarnings("unused")
	private static final String MODULE = "RoadSectionPopup";
	/**
	 * Constructor for the popup window that shows the details of a road segment
	 * @param view
	 * @param label - id of the section
	 * @param consump	- consumption for the section
	 * @param comparison	- comparision between the consumption versus previous trips
	 * @param average - average consumption for this trip
	 */
	public RoadSectionInfoPopup(View view,String label,String consump,String comparison, String average) {
		super(view, R.layout.road_section_info);
		populateLabels(label ,consump);
		// TODO Auto-generated constructor stub
	}

	private void populateLabels(String label, String consump){
		 TextView _section_info_title = (TextView) findPopupWidget(R.id.section_info_title);
		 TextView _section_info_consump = (TextView) findPopupWidget(R.id.section_info_consump);
		 _section_info_title.setText(label);
		 _section_info_consump.setText(consump);
		
	}
	@Override
	protected void setButtonsClickListeners() {
		// TODO Auto-generated method stub
		Button b = (Button)findPopupWidget(R.id.section_info_ok_btn);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					dismissPopupWindow();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
