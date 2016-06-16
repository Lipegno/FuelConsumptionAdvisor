package com.fuel.advisor.processing.custom;

@SuppressWarnings("unused")
public class CoordinateCalculator {

    private static final double ERADM = 6378135.0;	// Earth equatorial radius in meters (Semi-major)
    private static final double EPRADM = 6356752.0;	// Earth polar radius in meters (Semi-minor)
    private static final double FLATENNING = 1 / 298.257223563;	// Flattening of the earth (WSG 84)
    private static final double EPS = 0.000000000005;
    private static final double ECCENTRICITY = 0.0167;
	
	/**
	 * Convert a point in decimal degrees and meters in height to cartesian representation
	 * {@link http://www.ordnancesurvey.co.uk/oswebsite/gps/docs/convertingcoordinates3D.pdf}
	 * @param latitude - The latitude value in DD
	 * @param longitude - The longitude value in DD
	 * @param altitude - The heigh in meters
	 * @return - A three-dimensional vector that holds the representation in cartesian coordinates
	 */
    public static double[] convertToCartesian(double latitude, double longitude, double altitude)	{
		
		double[] coord = new double[3];
		double ec_2 = 2 * FLATENNING - Math.pow(FLATENNING, 2);
		double lat = Math.toRadians(latitude);
		double lon = Math.toRadians(longitude);
		double v = ERADM / (Math.sqrt(1 - ec_2 * Math.pow(Math.sin(lat), 2)));

		coord[0] = (v + altitude) * Math.cos(lat) * Math.cos(lon);
		coord[1] = (v + altitude) * Math.cos(lat) * Math.sin(lon);
		coord[2] = ((1 - ec_2) * v + altitude) * Math.sin(lat); 
		
		return coord;
	}
	
	/**
	 * Convert a point in cartesian coordinates to Ellipsoidal coordinates
	 * in decimal degrees and meters in height
	 * @param x - The value of the x coordinate
	 * @param y - The value of the y coordinate
	 * @param z - The value of the z coordinate
	 * @return - A three-dimensional vector that holds the representation of this location in Latitude, Longitude, and Altitude
	 */
	public static double[] convertToEllipsoidal(double x, double y, double z)	{
		
		double[] coord = new double[3];
		double p = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		double ec_2 = 2 * FLATENNING - Math.pow(FLATENNING, 2);
		double lat = Math.atan2(z, p*(1 - ec_2));
		double v = 0.0;
		
		// Use an iterative approach to calculate reasonable latitude value
		for (int i = 0; i < 10; i++)	{
			v = ERADM / Math.sqrt(1 - ec_2 * Math.pow(Math.sin(lat), 2));
			lat = Math.atan2(z + ec_2 * v * Math.sin(lat), p);
			if (Math.toDegrees(Math.abs(v - lat)) <= 0.000001)
				break;
		}
		coord[0] = Math.toDegrees(lat);
		coord[1] = Math.toDegrees(Math.atan2(y, x));
		coord[2] = p / Math.cos(lat) - v;
		
		return coord;
	}
	
	/**
	 * Calculates the ellipsoidal distance between two points
	 * @param latitude_1 - The latitude of the first point
	 * @param longitude_1 - The longitude of the first point
	 * @param latitude_2 - The latitude of the second point
	 * @param longitude_2 - The longitude of the second point
	 * @return - The distance in meters between the two points
	 */
	public static double calculateEllipsoidalDistance(double latitude_1, double longitude_1,
						double latitude_2, double longitude_2)	{
		
		if (latitude_1 == latitude_2 && longitude_1 == longitude_2)
			return 0.0;
		
		latitude_1 = Math.toRadians(latitude_1);
		latitude_2 = Math.toRadians(latitude_2);
		longitude_1 = Math.toRadians(longitude_1);
		longitude_2 = Math.toRadians(longitude_2);
		double sinG = Math.sin((latitude_1 - latitude_2) / 2);
		double cosL = Math.cos((longitude_1 - longitude_2) / 2);
		double cosF = Math.cos((latitude_1 + latitude_2) / 2);
		double sinL = Math.sin((longitude_1 - longitude_2) / 2);
		double sinF = Math.sin((latitude_1 + latitude_2) / 2);
		double cosG = Math.cos((latitude_1 - latitude_2) / 2);
		
		double S = Math.pow(sinG, 2) * Math.pow(cosL, 2) + Math.pow(cosF, 2) * Math.pow(sinL, 2);
		double C = Math.pow(cosG, 2) * Math.pow(cosL, 2) + Math.pow(sinF, 2) * Math.pow(sinL, 2);
		double W = Math.atan2(Math.sqrt(S), Math.sqrt(C));
		double R = Math.sqrt(S*C) / W;

		return (2 * W * ERADM * (1 + FLATENNING * (3 * R - 1) / (2 * C) * Math.pow(sinF, 2)
				* Math.pow(cosG, 2) - FLATENNING * (3 * R + 1) / (2 * S) 
				* Math.pow(cosF, 2) * Math.pow(sinG, 2)));
	}
}