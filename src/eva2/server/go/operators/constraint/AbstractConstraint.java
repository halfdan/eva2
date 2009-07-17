package eva2.server.go.operators.constraint;

import java.io.Serializable;

import eva2.gui.GenericObjectEditor;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.paramcontrol.ConstantParameters;
import eva2.server.go.operators.paramcontrol.GenericParamAdaption;
import eva2.server.go.operators.paramcontrol.InterfaceParameterControl;
import eva2.server.go.operators.paramcontrol.NoParamAdaption;
import eva2.server.go.operators.paramcontrol.ParamAdaption;
import eva2.server.go.operators.paramcontrol.ParameterControlManager;
import eva2.tools.EVAERROR;

/**
 * An abstract constraint contains a penalty factor with control strategy (for dynamic penalties)
 * and a relation.
 * 
 * @author mkron
 *
 */
public abstract class AbstractConstraint implements InterfaceDoubleConstraint, Serializable {
//	private transient GPFunctionProblem func = null;
	protected ConstraintRelationEnum relation = ConstraintRelationEnum.lessEqZero;
	protected ConstraintHandlingEnum handling = ConstraintHandlingEnum.specificTag;
	protected static boolean TRACE = false;
	protected double equalityEpsilon = 0.0001; // threshold below which equality constraints are seen as satisfied 
	
	private double penaltyFactor = 1.;
//	protected ParamAdaption	 penaltyFactAdaption = new NoParamAdaption();
	protected ParameterControlManager paramCtrl = new ParameterControlManager(new NoParamAdaption());
	private static String penaltyPropName = "penaltyFactor";
	
	public AbstractConstraint() {
		relation = ConstraintRelationEnum.lessEqZero;
		penaltyFactor = 1.;
		paramCtrl = new ParameterControlManager(new NoParamAdaption());
//		penaltyFactAdaption = new NoParamAdaption();		
	}
	
	public AbstractConstraint(AbstractConstraint o) {
//		penaltyFactAdaption = (ParamAdaption)o.penaltyFactAdaption.clone();
		paramCtrl = new ParameterControlManager(o.paramCtrl);
		penaltyFactor = o.penaltyFactor;
		relation = o.relation;
	}
	
	public void hideHideable() {
		setRelation(getRelation());
	}
	
	public abstract Object clone();
	
	public InterfaceParameterControl getParamControl() {
		return paramCtrl;
	}

	/**
	 * Return the raw degree of violation - usually the function value of the constraint function.
	 */
	protected abstract double getRawViolationValue(double[] indyX);
	
	/**
	 * Return the absolute (positive) degree of violation or zero if the constraint is fulfilled.
	 * The penalty factor is included here.
	 */
	public double getViolation(double[] indyX) {
		double viol = getRawViolationValue(indyX);
		return getViolationConsideringRelation(viol);
	}

	/**
	 * Check whether the given individual violates the constraint and immediately add
	 * the violation if it is the case. Expect that the fitness has already been set.
	 * This regards the handling strategy and adds the violation to the fitness (in each dimension) or 
	 * sets the individual constraint violation.
	 * 
	 * @param indy the individual to check for constraint violation.
	 */
	public void addViolation(AbstractEAIndividual indy, double[] indyX) {
		double v = getViolation(indyX);
		switch (handling) {
		case penaltyAdditive:
			if (v>0) {
				indy.SetMarkPenalized(true);
				for (int i=0; i<indy.getFitness().length; i++) {
					indy.SetFitness(i, indy.getFitness(i)+v+penaltyFactor);
				}
			}
			break;
		case penaltyMultiplicative:
			if (v>0) {
				indy.SetMarkPenalized(true);
				for (int i=0; i<indy.getFitness().length; i++) {
					indy.SetFitness(i, indy.getFitness(i)*(1+v));
				}
			}
		case specificTag:
			if (v>0) indy.addConstraintViolation(v);
			break;
		}
	}
	
	private double getViolationConsideringRelation(double val) {
//		System.out.println("Penalty is " + penaltyFactor);
		val *= penaltyFactor;
		switch (relation) {
//		case linearLessEqZero:
		case lessEqZero:
			 return (val <= 0.) ? 0 : val;
		case eqZero:
			val = Math.abs(val);
			if (val<=equalityEpsilon) return 0.;
			else return val;
		case greaterEqZero:
			return (val >= 0.) ? 0. : -val;
		}
		System.err.println("Unknown relation!");
		return 0.;
	}

	public boolean isViolated(double[] pos) {
		return (getViolation(pos)>0);
	}

	public boolean isSatisfied(double[] pos) {
		return (getViolation(pos)==0.);
	}
	
	public ConstraintRelationEnum getRelation() {
		return relation;
	}

	public void setRelation(ConstraintRelationEnum relation) {
		this.relation = relation;
		GenericObjectEditor.setShowProperty(this.getClass(), "equalityEpsilon", relation==ConstraintRelationEnum.eqZero);
	}

	public ParamAdaption getPenaltyFactControl() {
		return paramCtrl.getSingleAdapters()[0];
	}

	public void setPenaltyFactControl(ParamAdaption penaltyAdaption) {
//		this.penaltyFactAdaption = penaltyFact;
		if (!(penaltyAdaption instanceof NoParamAdaption)) {
			if (penaltyAdaption instanceof GenericParamAdaption) {
				((GenericParamAdaption)penaltyAdaption).setControlledParam(penaltyPropName);
			} else {
				if (!penaltyPropName.equals(penaltyAdaption.getControlledParam())) System.err.println("Warning: penalty factor control may have different target");
			}
		}
		paramCtrl.setSingleAdapters(new ParamAdaption[]{penaltyAdaption});
	}
	
	public String penaltyFactControlTipText() {
		return "Adaptive penalty may used. For generic adaption mechanisms, the target string will be set automatically.";
	}

	public double getPenaltyFactor() {
		return penaltyFactor;
	}

	public void setPenaltyFactor(double penaltyFactor) {
		if (penaltyFactor<0) {
			EVAERROR.errorMsgOnce("Error: a negative penalty factor is not allowed!");
		} else this.penaltyFactor = penaltyFactor;
	}
	
	public String penaltyFactorTipText() {
		return "Penalty factor by which a constraint violation is multiplied."; 
	}

	public ConstraintHandlingEnum getHandlingMethod() {
		return handling;
	}

	public void setHandlingMethod(ConstraintHandlingEnum handling) {
		this.handling = handling;
	}
	
	public String handlingMethodTipText() {
		return "Select the method the constraint is handled.";
	}

	public double getEqualityEpsilon() {
		return equalityEpsilon;
	}

	public void setEqualityEpsilon(double equalityEpsilon) {
		this.equalityEpsilon = equalityEpsilon;
	}
	
	public String equalityEpsilonTipText() {
		return "The threshold below which equality constraints are said to be satisfied.";
	}
}
