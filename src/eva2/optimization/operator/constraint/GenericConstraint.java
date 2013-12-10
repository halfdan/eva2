package eva2.optimization.operator.constraint;

import eva2.optimization.individuals.codings.gp.AbstractGPNode;
import eva2.optimization.problems.GPFunctionProblem;
import eva2.tools.EVAERROR;

import java.io.Serializable;

/**
 * A generic constraint is defined by a String describing a function of the x0...xn values of potential solutions.
 * The function String is parsed to a GP function tree using AbstractGPNode and GPFunctionProblem.
 *
 * @author mkron
 */
public class GenericConstraint extends AbstractConstraint implements InterfaceDoubleConstraint, Serializable {
    private String constraintString = "+(x0,x1)";
    private transient AbstractGPNode constraintProgram = null;
    GPFunctionProblem func = null;

    public GenericConstraint() {
        super();
        constraintProgram = null;
//		compileConstraint(constraintString);
    }

    public GenericConstraint(String str) {
        this();
        setConstraintString(str);
        compileConstraint();
    }

    public GenericConstraint(String str, ConstraintRelationEnum relation) {
        this(str);
        setRelation(relation);
    }

    public GenericConstraint(String str, ConstraintRelationEnum relation, ConstraintHandlingEnum method) {
        this(str, relation);
        setHandlingMethod(method);
    }

    public GenericConstraint(String str, ConstraintRelationEnum relation, ConstraintHandlingEnum method, double penFact) {
        this(str, relation, method);
        setPenaltyFactor(penFact);
    }

    public GenericConstraint(GenericConstraint o) {
        super(o);
        constraintString = o.constraintString;
        constraintProgram = null;
//		compileConstraint(constraintString);
    }

    @Override
    public Object clone() {
        return new GenericConstraint(this);
    }

    private void compileConstraint() {
        func = null;
        constraintProgram = AbstractGPNode.parseFromString(constraintString);
        if (constraintProgram == null) {
            System.err.println("Error: invalid expression: " + constraintString);
        }
        if (TRACE) {
            System.out.println("Compiled constraint " + constraintString);
            System.out.println("Program: " + constraintProgram.getStringRepresentation());
        }
    }

    /**
     * Return true if there is a valid generic function represented within the instance.
     *
     * @return
     */
    public boolean checkValid() {
        if (constraintProgram == null) {
            compileConstraint();
        }
        return (constraintProgram != null);
    }

    @Override
    public double getRawViolationValue(double[] indyX) {
        switch (relation) {
            case eqZero:
            case greaterEqZero:
            case lessEqZero:
                if (constraintProgram == null) {
                    compileConstraint();
                }
                if (constraintProgram != null) {
                    if (func == null) {
                        func = new GPFunctionProblem(constraintProgram, null, indyX.length, 0., 0.);
                    }
                    return func.evaluate(indyX)[0];
                } else {
                    return 0.;
                }
//		case linearLessEqZero:
//			return getViolation(evalLinearConstr(indy));
        }
        EVAERROR.errorMsgOnce("Error: unknown relation for GenericConstraint!");
        return 0.;
    }

    public String getConstraintString() {
        return constraintString;
    }

    public void setConstraintString(String constraintString) {
        this.constraintString = constraintString;
        constraintProgram = null;
        if (TRACE) {
            System.out.println(" NEW CONSTRAINT STRING SET! in " + this);
        }
    }

    public String getName() {
        return this.getClass().getSimpleName() + " " + constraintString;
    }

    public static String globalInfo() {
        return "A generic constraint which is parsed from a String; n is dimension, x0..xn are solution components. Use prefix notation as in \"+(-(sum(x),n),sqrt(*(pi,x0)))\".";
    }

}
