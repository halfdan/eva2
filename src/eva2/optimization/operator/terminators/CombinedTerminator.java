package eva2.optimization.operator.terminators;

import eva2.optimization.population.InterfaceSolutionSet;
import eva2.optimization.population.PopulationInterface;
import eva2.problems.InterfaceOptimizationProblem;
import eva2.util.annotation.Description;
import eva2.util.annotation.Parameter;

import java.io.Serializable;

@Description("Boolean combination of two terminators.")
public class CombinedTerminator implements InterfaceTerminator, Serializable {

    public enum LogicalOperator { AND, OR }
    /**
     *
     */
    private static final long serialVersionUID = -4748749151972645021L;
    private InterfaceTerminator t1 = new FitnessConvergenceTerminator();
    private InterfaceTerminator t2 = new EvaluationTerminator();
    private LogicalOperator logicalOperator = LogicalOperator.AND;
    private String msg = null;

    /**
     *
     */
    public CombinedTerminator() { }

    /**
     * Convenience constructor combining the given terminators in the expected way.
     */
    public CombinedTerminator(InterfaceTerminator t1, InterfaceTerminator t2, boolean bAnd) {
        this.t1 = t1;
        this.t2 = t2;
        logicalOperator = bAnd ? LogicalOperator.AND : LogicalOperator.OR;
    }

    @Override
    public void initialize(InterfaceOptimizationProblem prob) {
        if (t1 != null) {
            t1.initialize(prob);
        }
        if (t2 != null) {
            t2.initialize(prob);
        }
        msg = "Not terminated.";
    }

    @Override
    public boolean isTerminated(InterfaceSolutionSet solSet) {
        return isTerm(solSet);
    }

    @Override
    public boolean isTerminated(PopulationInterface pop) {
        return isTerm(pop);
    }

    private boolean getTermState(InterfaceTerminator term, Object curPopOrSols) {
        if (curPopOrSols instanceof InterfaceSolutionSet) {
            return term.isTerminated((InterfaceSolutionSet) curPopOrSols);
        } else {
            return term.isTerminated((PopulationInterface) curPopOrSols);
        }
    }

    /**
     * As we want to react to both Population and SolutionSet in the according way, this method is an abstraction
     * over both and uses getTermState to distinct the dereferenced calls.
     *
     * @param curPopOrSols
     * @return
     */
    private boolean isTerm(Object curPopOrSols) {
        boolean ret;
        if ((t1 == null) && (t2 == null)) {
            System.err.println("Error: No terminator set in CombinedTerminator");
            return true;
        }
        if (t1 == null) {
            ret = getTermState(t2, curPopOrSols);
            msg = t2.lastTerminationMessage();
            return ret;
        }
        if (t2 == null) {
            ret = getTermState(t1, curPopOrSols);
            msg = t1.lastTerminationMessage();
            return ret;
        }

        if (logicalOperator == LogicalOperator.AND) {
            // make sure that both terminators are triggered by every call, because some judge
            // time-dependently and store information on the population.
            ret = getTermState(t1, curPopOrSols);
            boolean ret2 = getTermState(t2, curPopOrSols);
            ret = ret && ret2;
            if (ret) {
                msg = "Terminated because both: " + t1.lastTerminationMessage() + " And " + t2.lastTerminationMessage();
            }
        } else { // OR
            // make sure that both terminators are triggered on every call, because some judge
            // time-dependently and store information on the population.
            ret = getTermState(t1, curPopOrSols);
            if (ret) {
                msg = t1.lastTerminationMessage();
                getTermState(t2, curPopOrSols); // just so that the second one is triggered, result is ignored.
            } else {
                ret = getTermState(t2, curPopOrSols);  // trigger and really look at the result
                if (ret) {
                    msg = t2.lastTerminationMessage();
                }
            }
        }
        return ret;
    }

    @Override
    public String lastTerminationMessage() {
        return msg;
    }

    /**
     * @return the logicalOperator
     */
    public LogicalOperator getLogicalOperator() {
        return logicalOperator;
    }

    /**
     * @param logicalOperator the logicalOperator to set
     */
    @Parameter(description = "Set the boolean operator to be used to combine the two terminators.")
    public void setLogicalOperator(LogicalOperator logicalOperator) {
        this.logicalOperator = logicalOperator;
    }

    /**
     * @return the t1
     */
    public InterfaceTerminator getTerminatorOne() {
        return t1;
    }

    /**
     * @param t1 the t1 to set
     */
    @Parameter(description = "The first terminator to be combined.")
    public void setTerminatorOne(InterfaceTerminator t1) {
        this.t1 = t1;
    }

    /**
     * @return the t2
     */
    public InterfaceTerminator getTerminatorTwo() {
        return t2;
    }

    /**
     * @param t2 the t2 to set
     */
    @Parameter(description = "The second terminator to be combined.")
    public void setTerminatorTwo(InterfaceTerminator t2) {
        this.t2 = t2;
    }
}
