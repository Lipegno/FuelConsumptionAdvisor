package com.fuel.advisor.processing.filter;

import Jama.Matrix;

/**
 *	Based on the definitions by Welch, G. and Bishop, G. and based on the work
 *	of Abdelkader, F., this class defines a Kalman Filter using the
 *	concept of the <i>Predictor-Corrector</i> algorithm, where the prediction step
 *	tries to calculate the <i>a priori</i> state of the process, and the
 *	correction step tries to estimate the <i>a posteriori</i> state using
 *	the given measurement vector <i>Z</i>
 * 
 * @author Tiago Camacho
 */
@SuppressWarnings("unused")
public class KalmanFilter  {

	private static final String MODULE = "KalmanFilter";
	
	// The a priori and a posteriori state estimation matrixes
	// X belongs to R^n
	private Matrix X_minus_hat, X_hat;
	
	// The a priori and a posteriori estimation of error covariance
	private Matrix P_minus, P;
	
	// F - The matrix that relates state at time k with state at time k-1 (n x n)
	// G - The matrix that relates to optional control input U (nxl)
	// U - The control input matrix (U belongs to R^l)
	// Q - The process noise covariance matrix. Relates to w, the random variable that
	// represents the process noise (p(w) ~ N(0,Q))
	// R - The measurement noise covariance matrix. Relates to v, the random variable
	// that represents the measurement noise (p(v) ~ N(0,R)) 
	// H - Relates the state to the measurement matrix Z ( m x n)
	// K - The Kalman Gain matrix (n x m)
	// I - The identity matrix (m x m)
	private Matrix F, G, U, Q, R, H, K, I;
	
	/**
	 * Default constructor to set up the Kalman Filter
	 * @param X0 - The initial state of the system (X_hat)
	 * @param P - The error covariance matrix (P)  
	 * @param F - The state transition matrix (F)
	 * @param U - The control input matrix (U)
	 * @param G - The control input gain matrix (G)
	 * @param Q - The covariance matrix for the process noise (Q)
	 * @param H - The matrix that relates state with measurement (H)
	 * @param R -The covariance matrix for the measurement noise (R)
	 * @throws - {@link DimensionsNotCorrectException} if matrices dimensions do not agree
	 */
	public KalmanFilter(
			Matrix X0,
			Matrix P,
			Matrix F,
			Matrix U,
			Matrix G,
			Matrix Q, 
			Matrix H, 
			Matrix R)
		throws DimensionsNotCorrectException	{
		
		this.X_hat = X0;
		this.P = P;
		this.F = F;
		this.U = U;
		this.G = G;
		this.Q = Q;
		this.H = H;
		this.R = R;
	}
	
	/**
	 * Apply the prediction step to estimate the <i>a priori</i> state
	 */
	public Matrix predict()	{
		
		// Estimation of the a priori state of the process. Notice that
		// the X_hat is our state at step k-1 and that X_minus_hat is 
		// our prediction for step k
		X_minus_hat = F.times(X_hat).plus(G.times(U));
		
		// Estimation of the a priori error covariance
		P_minus = F.times(P).times(F.transpose()).plus(Q);
		
		return X_minus_hat;
	}
	
	/**
	 * Apply the prediction step using the optional control input
	 * @param U - The control input matrix
	 */
	public Matrix predict(Matrix U)	{
		
		this.U = U;
		return predict();
	}
	
	/**
	 * Apply the correction step given the measurement at time <i>k</i>
	 * @param Z - The measurement matrix at the current time <i>k</i>
	 */
	public Matrix correct(Matrix Z)	{
		
		// First we must compute the Kalman Gain matrix
		K = P_minus.times(H.transpose()).times(
				(H.times(P_minus).times(H.transpose()).plus(R)).inverse());
		
		// Update the a posteriori state given the measurement matrix
		// received
		X_hat = X_minus_hat.plus(K.times(Z.minus(H.times(X_minus_hat))));
		
		// Finally update the measurement error covariance matrix
		if (I == null)
			I = Matrix.identity(P_minus.getRowDimension(), P_minus.getColumnDimension());
		P = (I.minus(K.times(H))).times(P_minus);
		
		return X_hat;
	}
	
	/**
	 * Fetch the state estimation matrix
	 * @return - The state estimation matrix (X_hat)
	 */
	public Matrix getStateEstimationMatrix()	{
		
		return this.X_hat;
	}
	
	/**
	 * Force the state estimation vector update. This is done
	 * so we can update the state estimation even when no measurements
	 * are made available
	 * @param estimation - The estimation vector (usually the prior)
	 */
	public void setStateEstimationMatrix(Matrix estimation)	{
		
		this.X_hat = estimation;
	}
	
	/**
	 * Fetch the estimated error covariance matrix
	 * @return - The estimated error covariance matrix (P)
	 */
	public Matrix getErrorCovarianceMatrix()	{
		
		return this.P;
	}
	
	/**
	 * Thrown when an attempt to constructor a Kalman Filter with improper matrices dimensions
	 */
	public class DimensionsNotCorrectException extends Exception	{

		private static final long serialVersionUID = -3911437839108817979L;
		
		public DimensionsNotCorrectException() {	}
		
		public DimensionsNotCorrectException(String message)	{
			
			super(message);
		}
	}
}