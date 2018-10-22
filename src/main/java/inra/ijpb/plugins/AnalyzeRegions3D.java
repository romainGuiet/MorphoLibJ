/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package inra.ijpb.plugins;


import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.geometry.Ellipsoid;
import inra.ijpb.geometry.Point3D;
import inra.ijpb.geometry.Sphere;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.IntrinsicVolumes3D;
import inra.ijpb.measure.region3d.Centroid3D;
import inra.ijpb.measure.region3d.InertiaEllipsoid;
import inra.ijpb.measure.region3d.IntrinsicVolumesAnalyzer3D;
import inra.ijpb.measure.region3d.LargestInscribedBall;

/**
 * Plugin for measuring geometric quantities such as volume, surface area,
 * and eventually sphericity index.
 * 
 * Volume is obtained by counting the number of voxels. Surface area is
 * computed using discretization of Crofton formula. Sphericity is obtained as
 * the ratio of V^2 by S^3, multiplied by 36*pi.
 * 
 * If the input image is calibrated, the spatial resolution is taken into 
 * account for computing geometric features.
 * 
 * @see inra.ijpb.measure.GeometricMeasures3D
 * 
 * @author David Legland
 *
 */
public class AnalyzeRegions3D implements PlugIn
{
    // ====================================================
    // Global Constants
    
	/**
	 * The list of connectivity names.
	 */
	private final static String[] connectivityNames = {
		"C6", "C26"
	};
	
	private final static int[] connectivityValues = new int[]{6, 26};
	
    /**
     * List of available numbers of directions
     */
    private final static String[] surfaceAreaMethods = {
            "Crofton  (3 dirs.)", 
            "Crofton (13 dirs.)" 
    }; 
    
    /**
     *  Array of weights, in the same order than the array of names.
     */
    private final static int[] dirNumbers = {
        3, 13
    };
    
    
    // ====================================================
    // Class variables
    
   /**
     * When this options is set to true, information messages are displayed on
     * the console, and the number of counts for each direction is included in
     * results table. 
     */
    public boolean debug  = false;
    
	boolean computeVolume 		= true;
	boolean computeSurface 		= true;
	boolean computeMeanBreadth  = true;
	boolean computeEulerNumber	= true;
	boolean computeSphericity 	= true;
    boolean computeCentroid     = true;
    boolean computeEllipsoid    = true;
	boolean computeElongations 	= true;
	boolean computeInscribedBall = true;
	
    String surfaceAreaMethod = surfaceAreaMethods[1];
    int surfaceAreaDirs = 13;
    int meanBreadthDirs = 13;
    int connectivity = 6;
    
    // ====================================================
    // Calling functions 
   

	/* (non-Javadoc)
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    public void run(String args) 
    {
        ImagePlus imagePlus = IJ.getImage();
        
		if (imagePlus.getStackSize() == 1) 
		{
			IJ.error("Requires a Stack");
			return;
		}
		
        // create the dialog, with operator options
        GenericDialog gd = new GenericDialog("Particles Analysis 3D");
        gd.addCheckbox("Volume", true);
        gd.addCheckbox("Surface_Area", true);
        gd.addCheckbox("Mean_Breadth", true);
        gd.addCheckbox("Sphericity", true);
        gd.addCheckbox("Euler_Number", true);
        gd.addCheckbox("Centroid", true);
        gd.addCheckbox("Inertia_Ellipsoid", true);
        gd.addCheckbox("Ellipsoid_Elongations", true);
        gd.addCheckbox("Max._Inscribed Ball", true);
        gd.addMessage("");
        gd.addChoice("Surface_area_method:", surfaceAreaMethods, surfaceAreaMethods[1]);
        gd.addChoice("Euler_Connectivity:", connectivityNames, connectivityNames[1]);
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;

        // Extract features to extract from image
        computeVolume 		= gd.getNextBoolean();
        computeSurface 		= gd.getNextBoolean();
        computeMeanBreadth 	= gd.getNextBoolean();
        computeSphericity 	= gd.getNextBoolean();
        computeEulerNumber	= gd.getNextBoolean();
        computeCentroid     = gd.getNextBoolean();
        computeEllipsoid    = gd.getNextBoolean();
        computeElongations 	= gd.getNextBoolean();
        computeInscribedBall = gd.getNextBoolean();
        
        
        // extract analysis options
        surfaceAreaDirs = dirNumbers[gd.getNextChoiceIndex()];
        connectivity = connectivityValues[gd.getNextChoiceIndex()];
        
        // Execute the plugin
        ResultsTable table = process(imagePlus);
        
 		// create string for indexing results
		String tableName = imagePlus.getShortTitle() + "-morpho"; 
    
		// show result
		table.show(tableName);
    }
    
    /**
     * Computes features from an ImagePlus object. 
     * Spatial resolution is read from image Calibration.
     * 
     * @param imagePlus the label image to analyze
     * @return the results in a new ResultsTable
     */
    public ResultsTable process(ImagePlus imagePlus)
    {
    	// Check validity of parameters
        if (imagePlus==null) 
            return null;

        // Extract Image Stack and its calibration
        ImageStack image = imagePlus.getStack();
        Calibration calib = imagePlus.getCalibration();
        
        return process(image, calib);
    }
    
    /**
	 * Computes features from an ImageStack object, specifying the calibration.
	 * 
	 * @param image
	 *            the 3D label image to analyze
	 * @param calib
	 *            the spatial calibration of the image
	 * @return the results in a new ResultsTable
	 */
    public ResultsTable process(ImageStack image, Calibration calib)
    {
    	// Extract spatial calibration
        double[] resol = new double[]{1, 1, 1};
        if (calib != null && calib.scaled()) 
        {
        	resol[0] = calib.pixelWidth;
        	resol[1] = calib.pixelHeight;
        	resol[2] = calib.pixelDepth;
        }

        // declare arrays for results
        IntrinsicVolumesAnalyzer3D.Result[] intrinsicVolumes = null; 
        Point3D[] centroids = null;
        Ellipsoid[] ellipsoids = null;
        double[][] elongations = null;
        Sphere[] inscribedBalls = null;
        
        
        // Identifies labels within image
        int[] labels = LabelImages.findAllLabels(image);

        // compute intrinsic volumes
        if (computeVolume || computeSurface || computeEulerNumber || computeMeanBreadth || computeSphericity)
        {
            IJ.showStatus("Intrinsic Volumes");
            
            long tic = System.nanoTime();
            // Create ans setup computation class
            IntrinsicVolumesAnalyzer3D algo = new IntrinsicVolumesAnalyzer3D();
            algo.setDirectionNumber(this.surfaceAreaDirs);
            algo.setConnectivity(this.connectivity);
            DefaultAlgoListener.monitor(algo);
            
            // run analysis
            intrinsicVolumes = algo.analyzeRegions(image, labels, calib);
            long toc = System.nanoTime();
            IJ.log(String.format("Intrinsic volumes: %7.2f ms", (toc - tic) / 1000000.0));
        }
        
        // compute inertia ellipsoids and their elongations
        if (computeEllipsoid)
        {
        	IJ.showStatus("Inertia Ellipsoids");
            long tic = System.nanoTime();
        	InertiaEllipsoid algo = new InertiaEllipsoid();
        	DefaultAlgoListener.monitor(algo);
            ellipsoids = algo.analyzeRegions(image, labels, calib);
            long toc = System.nanoTime();
            IJ.log(String.format("inertia ellipsoids: %7.2f ms", (toc - tic) / 1000000.0));

            if (computeCentroid)
            {
                // initialize centroid array from ellipsoid array
                centroids = Ellipsoid.centers(ellipsoids);
            }
        } 
        else if (computeCentroid)
        {
            // Compute centroids if not computed from inertia ellipsoid
            IJ.showStatus("Centroid");
            Centroid3D algo = new Centroid3D();
            DefaultAlgoListener.monitor(algo);
            centroids = algo.analyzeRegions(image, labels, calib);
        }
        
        if (computeElongations)
        {
            IJ.showStatus("Ellipsoid elongations");
        	elongations = Ellipsoid.elongations(ellipsoids);
        }
        
        // compute position and radius of maximal inscribed ball
        if (computeInscribedBall)
        {
        	IJ.showStatus("Inscribed Balls");
            long tic = System.nanoTime();
        	LargestInscribedBall algo = new LargestInscribedBall();
        	DefaultAlgoListener.monitor(algo);
        	inscribedBalls = algo.analyzeRegions(image, labels, calib);
            long toc = System.nanoTime();
            IJ.log(String.format("inscribed balls: %7.2f ms", (toc - tic) / 1000000.0));
        }
        
        // Convert to ResultsTable object
        IJ.showStatus("Create Table");
        ResultsTable table = new ResultsTable();
        for (int i = 0; i < labels.length; i++) 
        {
            IJ.showProgress(i, labels.length);
            
        	table.incrementCounter();
        	table.addLabel(Integer.toString(labels[i]));
        	
        	// geometrical quantities
        	if (computeVolume)
        		table.addValue("Volume", intrinsicVolumes[i].volume);
        	if (computeSurface)
        		table.addValue("SurfaceArea", intrinsicVolumes[i].surfaceArea);
        	if (computeMeanBreadth)
        		table.addValue("MeanBreadth", intrinsicVolumes[i].meanBreadth);
        	if (computeSphericity)
        	{
                double vol =  intrinsicVolumes[i].volume;
                double surf =  intrinsicVolumes[i].surfaceArea;
        		table.addValue("Sphericity", IntrinsicVolumes3D.sphericity(vol, surf));
        	}
        	if (computeEulerNumber)
        		table.addValue("EulerNumber", intrinsicVolumes[i].eulerNumber);

            if (computeCentroid)
            {
                Point3D center = centroids[i];
                table.addValue("Centroid.X", center.getX());
                table.addValue("Centroid.Y", center.getY());
                table.addValue("Centroid.Z", center.getZ());
            }

        	// inertia ellipsoids
        	if (computeEllipsoid)
        	{
                // add coordinates of origin pixel (IJ coordinate system) 
        		Ellipsoid elli = ellipsoids[i];
        		Point3D center = elli.center();
                table.addValue("Elli.Center.X", center.getX());
            	table.addValue("Elli.Center.Y", center.getY());
            	table.addValue("Elli.Center.Z", center.getZ());
            	// add scaling parameters 
                table.addValue("Elli.R1", elli.radius1());
            	table.addValue("Elli.R2", elli.radius2());
            	table.addValue("Elli.R3", elli.radius3());
            	// add orientation info
                table.addValue("Elli.Azim", elli.phi());
            	table.addValue("Elli.Elev", elli.theta());
            	table.addValue("Elli.Roll", elli.psi());
        	}
        	if (computeElongations)
        	{
        		table.addValue("Elli.R1/R2", elongations[i][0]);
        		table.addValue("Elli.R1/R3", elongations[i][1]);
        		table.addValue("Elli.R2/R3", elongations[i][2]);            	
        	}
        	
        	if (computeInscribedBall)
        	{
        		Sphere ball = inscribedBalls[i];
        		Point3D center = ball.center();
                
        		table.addValue("InscrBall.Center.X", center.getX());
        		table.addValue("InscrBall.Center.Y", center.getY());
        		table.addValue("InscrBall.Center.Z", center.getZ());
        		table.addValue("InscrBall.Radius", ball.radius());
        	}
        }
        
        // cleanup algo display
        IJ.showProgress(1, 1);
        IJ.showStatus("");
                
        return table;
    }
}
