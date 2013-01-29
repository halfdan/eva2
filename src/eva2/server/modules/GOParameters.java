package eva2.server.modules;

import eva2.server.go.InterfaceGOParameters;
import eva2.server.go.InterfaceTerminator;
import eva2.server.go.operators.terminators.EvaluationTerminator;
import eva2.server.go.problems.F1Problem;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.go.strategies.GeneticAlgorithm;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.tools.Serializer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.logging.Level;


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
    public static GOParameters getInstance(String serParamFile, final boolean casually) {
        GOParameters instance = null;
        try {
            FileInputStream fileStream = new FileInputStream(serParamFile);
            instance = (GOParameters) Serializer.loadObject(fileStream, casually);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Could not load instance object.", ex);
        }

        if (instance == null) {
            instance = new GOParameters();
        }
        return instance;
    }

    public GOParameters() {
    	super(new GeneticAlgorithm(), new F1Problem(), new EvaluationTerminator(1000));
    }
    
    public GOParameters(InterfaceOptimizer opt, InterfaceOptimizationProblem prob, InterfaceTerminator term) {
    	super(opt, prob, term);
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
    @Override
    public String getName() {
        return "Optimization parameters";
    }
    /**
     *
     */
    @Override
    public Object clone() {
        return new GOParameters(this);
    }
    
    /** This method returns a global info string.
     * @return description
     */
    public static String globalInfo() {
        return "Select the optimization parameters.";
    }
}
