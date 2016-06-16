package com.fuel.advisor.ui.vehicle_info.openGLStuff;

import java.util.Vector;
import com.fuel.advisor.processing.custom.CoordinateCalculator;

/**
 * The MapMeshCreator Class is responsible for ,based on a given set of gps coordinates (in decimal), building a path
 * to be renderer, the path is made of Custom planes aligned according to the correct path
 * @author filipe
 *
 */
public class MapMeshCreator {

	private double[] _gps_coords;
	private int[] _sectionCons;
	private double[] _landmarks;

	// constructor without the gps coordinates (initializes the _gps_coords array with dummy values)
	public MapMeshCreator(){   
		set_gps_coords(new double[]{ -16.92443236751885,32.65843034363206, 
				-16.92443506395765,32.65854523153747, 
				-16.92409275429451,32.65840951117475, 
				-16.92400750716982,32.65846040135879, 
				-16.92368961832862,32.65885401877454, 
				-16.92371761199567,32.65915619420166, 
				-16.92414626410086,32.65938863010496, 
				-16.92434373571659,32.65941542283683, 
				-16.92453196175955,32.65938052477192, 
				-16.92501392178415,32.65954197992621, 
				-16.92539456950765,32.65981449087167, 
				-16.92565744143833,32.6601167248436, 
				-16.92615394589685,32.66038481080994, 
				-16.92654521658413,32.66058865932246, 
				-16.92681037075264,32.66055349370711, 
				-16.92702400886383,32.66053034046416, 
				-16.92714159108218,32.66061634091643, 
				-16.92727647105147,32.66061747204062, 
				-16.92751061223332,32.66022992280224, 
				-16.92748817304601,32.65993621094967, 
				-16.92737120795807,32.65974039022728, 
				-16.92647453324502,32.65926812334877, 
				-16.92584058239336,32.65896851516963, 
				-16.92547105344538,32.65867183888886, 
				-16.92510382497403,32.65815582824744, 
				-16.92464320832799,32.65760709113146, 
				-16.92402598422288,32.65724609573834, 
				-16.92326081302996,32.65699226392509, 
				-16.92281955949297,32.65668322622025, 
				-16.92254340485642,32.65629495821207, 
				-16.92259685585014,32.65605337390822, 
				-16.92242862076527,32.65566583827998, 
				-16.92227450478592,32.65518548894799, 
				-16.92223035360675,32.6545255081675, 
				-16.92235222582461,32.65415492552131, 
				-16.92262109457035,32.65392738135265, 
				-16.92288051451579,32.65368615243204, 
				-16.923327915337,32.65403647654686, 
				-16.92356555040774,32.65426835400223, 
				-16.92356636075671,32.65437748476092, 
				-16.92342071554206,32.65485871522904, 
				-16.92362078344857,32.65537303981161, 
				-16.92400302067506,32.65576854494397, 
				-16.92450716974358,32.65617741341688, 
				-16.92512883349346,32.65665978942271, 
				-16.92555762583153,32.65698958180338, 
				-16.92562868566232,32.65702385570511, 
				-16.926214881782,32.65642148755471, 
				-16.92579666086219,32.65608630035694, 
				-16.92540153247311,32.65576055769795, 
				-16.92511125121571,32.6553896949374, 
				-16.92486727108835,32.65478441765021, 
				-16.92478638510272,32.65440629437133, 
				-16.92419126613193,32.65385636942106, 
				-16.92385194580705,32.65330859953769, 
				-16.92372033130373,32.65293583138475, 
				-16.92348222461621,32.65208782309952, 
				-16.92337950325978,32.65140934350575, 
				-16.92334251278826,32.65110637655076, 
				-16.92318765579326,32.65069122097395, 
				-16.92285300028544,32.65006602386144, 
				-16.92250822935119,32.64971623772233, 
				-16.92210139799154,32.64936801710091, 
				-16.92188158876061,32.6488611351489, 
				-16.92144117613985,32.64844499886892, 
				-16.92132842622449,32.6480641959531, 
				-16.92146552927014,32.6480701560562, 
				-16.92269369584166,32.64875744826942, 
				-16.92317425665005,32.64903039217502, 
				-16.92371701046191,32.64929511630538, 
				-16.92382095922241,32.64931263487525, 
				-16.92394244769172,32.64936176221455, 
				-16.92403192330203,32.64943758356453, 
				-16.92417632095508,32.6495299800149, 
				-16.92433100520634,32.64954458439379, 
				-16.92438311203153,32.64946325175827, 
				-16.92435087060015,32.64932158957676, 
				-16.92421338118041,32.64926099512075, 
				-16.92408082655868,32.64914214354289, 
				-16.9238265543684,32.64887805732354, 
				-16.92345513440806,32.648268600245, 
				-16.92312895194129,32.64761875851477, 
				-16.92296129646611,32.64688366075234, 
				-16.92311954685215,32.64687398184283,
		});
		
		set_landmarks(new double[]{-16.92835186651518,32.6454288472506,
				-16.90843950350724,32.6481385112626,
				-16.92467379878823,32.65915189295517,
				-16.91401311604363,32.64779897585986,
				-16.91293912938938,32.64664162792603,
				-16.90875882383527,32.6504507220911
		});
	}	

	public MapMeshCreator( double[] gps_coords){
		set_gps_coords(gps_coords);

	}


	public void set_sectionCons(int[] _sectionCons) {
		this._sectionCons = _sectionCons;
	}

	public int[] get_sectionCons() {
		return _sectionCons;
	}

	public void set_gps_coords(double[] _gps_coords) {
		this._gps_coords = _gps_coords;

	}

	public double[] get_gps_coords() {
		return _gps_coords;
	}

	public  Vector<double[]> createCartesianValues(){
		Vector<double[]> returnValue = new Vector<double[]>();

		for(int i=0;i<_gps_coords.length;i=i+2){
			returnValue.add(CoordinateCalculator.convertToCartesian(_gps_coords[i+1], _gps_coords[i], 1));
		}
		return returnValue;
	}

	public  Vector<double[]> normalizeValues(Vector<double[]> values){
		Vector<double[]> returnValue = new Vector<double[]>();
		double[] test = values.elementAt(0);
		for(int i=0;i<values.size();i++){
			returnValue.add(new double[]{(values.elementAt(i)[0]-test[0])/200,(values.elementAt(i)[1]-test[1])/200,0});

		}
		return returnValue;
	}

	public  Vector<Double> calculateDistances(){
		Vector<Double> returnValue = new Vector<Double>();

		for(int i=0;i<(_gps_coords.length)/2;i=i+2){
			returnValue.add(CoordinateCalculator.calculateEllipsoidalDistance(_gps_coords[i], _gps_coords[i+1],_gps_coords[i+2], _gps_coords[i+3]));
		}
		return returnValue;
	}

	public float calculate2Pdistance(float x1, float y1, float z1, float x2, float y2, float z2){

		return (float)Math.sqrt(Math.pow((x1-x2),2)+Math.pow((y1-y2),2)+Math.pow((z1-z2),2));
	}

	/**
	 * based on a consumption calculates the color of that will map that consumption for  a section of the road
	 * j- the number of the section  
	 * @return
	 */
	public float[] calculateColor(int j){

		float avgCons=0;
		for(int i=0; i<j+5;i++){
			avgCons+=_sectionCons[i+j];
		}
		avgCons = avgCons/j;

		float[] rgba = new float[]{0,0,0,1};
		float max_cons = 60;
		if(avgCons >= max_cons*0.7){
			rgba[0]	  = (avgCons/max_cons);
		}
		if((max_cons*0.4 < avgCons) && (avgCons <max_cons*0.7)){
			rgba[0] = 1f;
			rgba[1]  = 1.5f- (avgCons/max_cons);
		}

		if(avgCons<=max_cons*0.4) {
			rgba[0] = .4f;
			rgba[1] = 1-(avgCons/max_cons);	
		}

		return rgba;
	}
	/**
	 * Creates a Mesh representation of the path 
	 * @return a set of custom planes that all together made the map
	 */
	public Vector<Mesh> createMesh(){

		Vector<double[]> coordinates =createCartesianValues();
		Vector<double[]> normalized_coordinates = normalizeValues(coordinates);
		Vector<Mesh> sections = new Vector<Mesh>();
		float x1=0;
		float x2=0;
		float y1=0;
		float y2=0;
		float z1=0;
		float z2=0;
		float []color={0,0,0,0};

		for(int j=1; j<normalized_coordinates.size();j++){
			x1= (float)normalized_coordinates.elementAt(j-1)[0];
			y1= (float)normalized_coordinates.elementAt(j-1)[1];
			z1=(float)normalized_coordinates.elementAt(j-1)[2];
			x2=(float)normalized_coordinates.elementAt(j)[0];
			y2=(float)normalized_coordinates.elementAt(j)[1];
			z2=(float)normalized_coordinates.elementAt(j)[2];

			float x_1 = (float) (x1*Math.cos(Math.toRadians(-70))-y1*Math.sin(Math.toRadians(-70)));
			float y_1 = (float) (x1*Math.sin(Math.toRadians(-70))+y1*Math.cos(Math.toRadians(-70)));
			float x_2 = (float) (x2*Math.cos(Math.toRadians(-70))-y2*Math.sin(Math.toRadians(-70)));
			float y_2 = (float) (x2*Math.sin(Math.toRadians(-70))+y2*Math.cos(Math.toRadians(-70)));

			if(j%5==0){
				CustomPlane test = new CustomPlane(x_1-.03f, y_1-.03f, z1,x_1+.03f, y_1+.03f, z2);
				test.setColor(0,0,1,1);
				color = calculateColor(j);
				sections.add(test);
			}

			CustomPlane test3;
			test3 = new CustomPlane(x_1, y_1, z1, x_2, y_2, z2);
			test3.setColor(color[0],color[1],color[2],1f);
			sections.add(test3);
		}

		return sections;
	}
	
	public Vector<Mesh> buildLandmarks(){

		float total_offset_x=0;
		float total_offset_y=0;
		Vector<double[]> coordinates =createCartesianValues();
		Vector<Mesh> sections = new Vector<Mesh>();
		float x=0;
		float y=0;
		float x_l=0;
		float y_l=0;
		for(int i=0;i<12;i=i+2){
			
			x = (float)CoordinateCalculator.convertToCartesian(_landmarks[i+1], _landmarks[i],1)[0];
			y = (float)CoordinateCalculator.convertToCartesian(_landmarks[i+1], _landmarks[i],1)[1];
			SimplePlane test = new SimplePlane(.6f,.6f);
			x_l =	((float)(x-coordinates.elementAt(0)[0])/200);
		    y_l =	((float)(y-coordinates.elementAt(0)[1])/200);
			x = (float) (x_l*Math.cos(Math.toRadians(-70))-y_l*Math.sin(Math.toRadians(-70)));
			y = (float) (x_l*Math.sin(Math.toRadians(-70))+y_l*Math.cos(Math.toRadians(-70)));
			
			test.x= (-total_offset_x)+x;
			test.y= (-total_offset_y)+y;
			total_offset_x+=test.x;
			total_offset_y+=test.y;
			sections.add(test);
		}
		
		return sections;
	}

	public void set_landmarks(double[] _landmarks) {
		this._landmarks = _landmarks;
	}

	public double[] get_landmarks() {
		return _landmarks;
	}

}
