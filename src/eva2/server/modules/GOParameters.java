package eva2.server.modules;

import java.io.Serializable;

import eva2.server.go.InterfaceGOParameters;
import eva2.server.go.individuals.GAIndividualDoubleData;
import eva2.server.go.operators.terminators.EvaluationTerminator;
import eva2.server.go.problems.F1Problem;
import eva2.server.go.strategies.GeneticAlgorithm;
import eva2.tools.Serializer;


/**
 * Created by IntelliJ IDEA.
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 306 $
 *            $Date: 2007-12-04 14:22:52 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */
public class GOParameters extends AbstractGOParameters implements InterfaceGOParameters, Serializable {

    public static boolean   TRACE   = false;

    public static GOParameters getInstance() {
    	return getInstance("GOParameters.ser", true);
    }

    /**
     * Create an instance from a given serialized parameter file.
     *  
     * @param serParamFile
     * @param casually if true, standard parameters are used quietly if the params cannot be loaded
     * @return a GOParameters instance
     */
    public static GOParameters getInstance(String serParamFile, boolean casually) {
    	if (TRACE) System.out.println("GOParameters getInstance 1 - " + serParamFile + " , " + casually);
    	GOParameters Instance = null;
    	if (serParamFile!=null) {
	    	try {
	    		Instance = (GOParameters) Serializer.loadObject(serParamFile, casually);
	    		if (TRACE) System.out.println("Loading succeded.");
	    	} catch(Exception e) {
	    		System.err.println("Error loading GOParameters from " + serParamFile);
	    		Instance = null;
	    	}
    	} else if (!casually) System.err.println("Error: null argument for noncasual param file loading! (GOParameters)");
    	if (TRACE) System.out.println("GOParameters getInstance 2");
    	if (Instance == null) Instance = new GOParameters();
    	return Instance;
    }
    
    public void saveInstance(String serParamFile) {
    	if (TRACE) System.out.println("GOParameters: saveInstance to " + serParamFile);
    	Serializer.storeObject(serParamFile,this);
    }
    
    public void saveInstance() {
    	saveInstance("GOParameters.ser");
    }

    public GOParameters() {
    	super(new GeneticAlgorithm(), new F1Problem(), new EvaluationTerminator(1000));
//    	((F1Problem)m_Problem).setEAIndividual(new GAIndividualDoubleData());
    }

    /**
     *
     */
    private GOParameters(GOParameters Source) {
    	super(Source);
    }
    /**
     *
     */
    public String getName() {
        return "Optimization parameters";
    }
    /**
     *
     */
    public Object clone() {
        return new GOParameters(this);
    }
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "Select the optimization parameters.";
    }
}
