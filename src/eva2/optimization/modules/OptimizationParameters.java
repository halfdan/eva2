package eva2.optimization.modules;

import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.go.InterfaceTerminator;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.problems.F1Problem;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.optimization.strategies.GeneticAlgorithm;
import eva2.optimization.strategies.InterfaceOptimizer;
import eva2.tools.Serializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.logging.Level;


/**
 * Created by IntelliJ IDEA.
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 *
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version: $Revision: 306 $
 * $Date: 2007-12-04 14:22:52 +0100 (Tue, 04 Dec 2007) $
 * $Author: mkron $
 */
public class OptimizationParameters extends AbstractOptimizationParameters implements InterfaceOptimizationParameters, Serializable {

    public static OptimizationParameters getInstance() {
        return getInstance("OptimizationParameters.ser", true);
    }

    /**
     * Create an instance from a given serialized parameter file.
     *
     * @param serParamFile
     * @param casually     if true, standard parameters are used quietly if the params cannot be loaded
     * @return a OptimizationParameters instance
     */
    public static OptimizationParameters getInstance(String serParamFile, final boolean casually) {
        OptimizationParameters instance = null;
        try {
            FileInputStream fileStream = new FileInputStream(serParamFile);
            instance = (OptimizationParameters) Serializer.loadObject(fileStream, casually);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Could not load instance object.", ex);
        }

        if (instance == null) {
            instance = new OptimizationParameters();
        }
        return instance;
    }

    public OptimizationParameters() {
        super(new GeneticAlgorithm(), new F1Problem(), new EvaluationTerminator(1000));
    }

    public OptimizationParameters(InterfaceOptimizer opt, InterfaceOptimizationProblem prob, InterfaceTerminator term) {
        super(opt, prob, term);
    }

    /**
     *
     */
    private OptimizationParameters(OptimizationParameters parameters) {
        super(parameters);
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
        return new OptimizationParameters(this);
    }

    /**
     * This method returns a global info string.
     *
     * @return description
     */
    public static String globalInfo() {
        return "Select the optimization parameters.";
    }
}
