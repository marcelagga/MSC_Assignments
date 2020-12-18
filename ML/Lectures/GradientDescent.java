/**
 * This is a very simple but correct implementation of the Gradient Descent algorithm for
 * Univariate Linear Regression as discussed in class.
 * Note that theta0 and theta1 are updated simultaneously, as required for correctness of the algorithm:
 * this means that we compute new values for them using their old values before changing either value.
 *
 * By Michael Madden, Oct 2011, updated Jan 2014 & Jan 2015.
 *
 * This code is may be used freely without restriction, though attribution of my authorship would be appreciated.
 */

import java.io.IOException;

import javax.swing.JFrame;

// This program uses JMathPlot, a package for producing Matlab-style graphs
//   Get it at http://code.google.com/p/jmathplot/
//   or just delete the blocks of code that use it.
import org.math.plot.*;


public class GradientDescent 
{
	// parameters
	static double theta0;
	static double theta1;

	// data taken from one of the worked examples
	// Note: for this data, correct answers are theta0=0.5, theta1=0.9.
//	static double[] x = {2, 4, 6, 8};
//	static double[] y = {2, 5, 5, 8};
	
	
	static double[] x = {2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 14, 15, 17, 18, 19, 20};
	static double[] y = {2, 4, 5, 8, 5, 7, 8, 10, 12, 12, 14, 14, 15, 16, 16, 19, 18, 19};

	
	public static void main(String[] args) throws IOException
	{	
		// Algorithm settings
		double alpha = 0.01;      // learning rate 
		double tol = 1e-6;        // tolerance to determine convergence
		int maxiter = 12000;      // maximum number of iterations (in case convergence is not reached)
		
		// initial guesses for parameters
		// 0,0 are typical; 15,-1 are particularly bad for the sample data here, but good for illustration.
		theta0 = 1000;
		theta1 = -1000;
		
		// other variables needed
		double delta0, delta1;
		int iter = 0;
		
		// plot the data
		initPlot(maxiter);
		
		do {
			// Simultaneous updates of thetas: compute all changes first, then update all thetas with them
			delta0 = alpha * dJ_dtheta0();
			delta1 = alpha * dJ_dtheta1();

			updatePlot(iter);
			
//			if (iter == 0)
//			{
//				System.out.println("\nPress return to start ...");
//				System.in.read();
//			}
			
			iter++;
			theta0 -= delta0;
			theta1 -= delta1;

			if (iter %100 == 0)
			   System.out.println("Iteration " + iter + ": theta_0=" + theta0 + " - " + delta0 + ", theta_1=" + theta1 + " - "+ delta1);

			if (iter > maxiter) break;
		} 
		while (Math.abs(delta1) > tol || Math.abs(delta0) > tol);

		System.out.println("\nConvergence after " + iter + " iterations: theta_0=" + theta0 + ", theta_1=" + theta1);

		// Update the plotting
		addTrendline(plot, false);
		plotConvergence(iter);
		
		// All finished
		System.out.println("\nPress return to end ...");
		System.in.read();
		System.exit(0);
	}

	/** Computes partial derivative of J wrt theta0 */
	public static double dJ_dtheta0()
	{
		double sum = 0;
		
		for (int i=0; i<x.length; i++) {
			sum += h(x[i]) - y[i];
		}
		return sum / x.length;
	}

	/** Computes partial derivative of J wrt theta1 */
	public static double dJ_dtheta1()
	{
		double sum = 0;
		
		for (int i=0; i<x.length; i++) {
			sum += (h(x[i]) - y[i]) * x[i];
		}
		return sum / x.length;
	}


	/** Computes h(x) = theta0 + theta1 . x */
	public static double h(double x) 
	{
		 return theta0 + theta1*x;
	}

	//////////////////////////////////////////////////////////////////////
	// The remainder of the code in this class is just needed for plotting
	//////////////////////////////////////////////////////////////////////

	// All of the following member variables are just to support plotting
	static int dispiter = 50;        // interval for displaying results during iterations 
	static int pausems = 100;         // pause in milliseconds after displaying each plot: set to 0 if  not wanted
	static boolean replacetrendlines = true; // if false, new trendlines are added to old ones rather than replacing old ones as algorithm proceeds
	static boolean plotcost = true;  // This is diagnostic: runs faster if false.
	static int trendline; 			 // handle used for adding/removing trendline
	static double[] costplot, theta0plot, theta1plot, timestepplot;  // Will store data for plotting
	static Plot2DPanel plot;		 // Plot displayed in this panel
	static double cost, prevcost;	 // For the cost plots


	
	/** J(theta0, theta1) is the squared error cost function. Only needed here to plot the change in cost. */
	public static double J()
	{
		double sum = 0;
		
		for (int i=0; i<x.length; i++) {
			sum += Math.pow((h(x[i]) - y[i]), 2);
		}
		return sum / (2 * x.length);
	}

	/** Initate the main plot */ 
	public static void initPlot(int maxiter)
	{
		// These arrays store results for plotting
		costplot = new double[maxiter+1];
		theta0plot = new double[maxiter+1];
		theta1plot = new double[maxiter+1];
		timestepplot = new double[maxiter+1];

		// Initiate the cost
		prevcost = 1e50;

		// create a PlotPanel
		plot = new Plot2DPanel();

		// add a the initial data to the PlotPanel
		plot.addScatterPlot("X-Y", x, y);
		
		// show the initial trendline
		addTrendline(plot, false);
		
		// put the PlotPanel in a JFrame, as a JPanel
		JFrame frame = new JFrame("Original X-Y Data & Regression Line");
		frame.setContentPane(plot);
		frame.setSize(600, 600);
		frame.setVisible(true);
	}

	
	public static void updatePlot(int iter)
	{
		// Store data for plotting
		timestepplot[iter] = iter;
		theta0plot[iter] = theta0;
		theta1plot[iter] = theta1;
		
		// If tracking cost, check for failure to converge and store data for plotting
		if (plotcost)
		{
			cost = J();
			if (cost > prevcost) {
				System.err.println("ERROR at iteration " + iter + ": not converging, re-run with a lower value for alpha.");
				System.err.println("The cost should decrease but prev > current: " + prevcost + " > " + cost);
				System.exit(0);
				
			}
			costplot[iter] = cost; // store data for plotting
			prevcost = cost;
		}
		
		if (iter % dispiter == 0) 
		{
			if (pausems > 0) {
				try {
					Thread.sleep(pausems); // pause before updating so that first plot can be seen
				} catch (InterruptedException e) {
					// Nothing to do here
				} 
			}
			
			addTrendline(plot, replacetrendlines);
		}
	}

	
	public static void addTrendline(Plot2DPanel plot, boolean removePrev)
	{
		if (removePrev)
			plot.removePlot(trendline);
		
		double[] yEnd = new double[x.length];
		for (int i=0; i<x.length; i++)
			yEnd[i] = h(x[i]);
		trendline = plot.addLinePlot("final", x, yEnd);
	}
	
	public static void plotConvergence(int iter)
	{
		// Before plotting the convergence plots, extract arrays of the right size from them
		double[] theta0plot2 = new double[iter];
		double[] theta1plot2 = new double[iter];
		double[] tsplot2 = new double[iter];
		System.arraycopy(theta0plot, 0, theta0plot2, 0, iter);
		System.arraycopy(theta1plot, 0, theta1plot2, 0, iter);
		System.arraycopy(timestepplot, 0, tsplot2, 0, iter);
		
		// Plot the convergence of data
		Plot2DPanel convPlot = new Plot2DPanel();

		// add a line plot to the PlotPanel
		convPlot.addLinePlot("theta_0", tsplot2, theta0plot2);
		convPlot.addLinePlot("theta_1", tsplot2, theta1plot2);
		convPlot.addLegend("NORTH");

		// put the PlotPanel in a JFrame, as a JPanel
		JFrame frame2 = new JFrame("Convergence of parameters over time");
		frame2.setContentPane(convPlot);
		frame2.setSize(600, 600);
		frame2.setVisible(true);

		// Plot the cost function if chosen to
		if (plotcost)
		{
			double[] costplot2 = new double[iter];
			System.arraycopy(costplot, 0, costplot2, 0, iter);
			Plot2DPanel costPlot = new Plot2DPanel();
			costPlot.addLinePlot("J(theta)", tsplot2, costplot2);
			costPlot.addLegend("NORTH");
			JFrame frame3 = new JFrame("Reduction in cost over time");
			frame3.setContentPane(costPlot);
			frame3.setSize(600, 600);
			frame3.setVisible(true);
		}
		
	}
}
