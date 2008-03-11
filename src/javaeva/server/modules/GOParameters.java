package javaeva.server.modules;

import java.io.Serializable;

import javaeva.gui.BeanInspector;
import javaeva.server.go.InterfaceGOParameters;
import javaeva.server.go.InterfacePopulationChangedEventListener;
import javaeva.server.go.operators.terminators.EvaluationTerminator;
import javaeva.server.go.problems.B1Problem;
import javaeva.server.go.strategies.GeneticAlgorithm;
import javaeva.tools.Serializer;

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

    /**
     *
     */
    public static GOParameters getInstance() {
        if (TRACE) System.out.println("GOParameters getInstance 1");
        GOParameters Instance = (GOParameters) Serializer.loadObject("GOParameters.ser");
        if (TRACE) System.out.println("GOParameters getInstance 2");
        if (Instance == null) Instance = new GOParameters();
        return Instance;
    }
    
    /**
     *
     */
    public void saveInstance() {
        Serializer.storeObject("GOParameters.ser",this);
    }
    /**
     *
     */
    public GOParameters() {
    	super(new GeneticAlgorithm(), new B1Problem(), new EvaluationTerminator(1000));
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
    public String globalInfo() {
        return "Select the optimization parameters.";
    }
}
