package eva2.server.go.operators.constraint;

/**
 * To handle a set of constraints with a single parameter adaption mechanism. 
 * Single constraints are 
 * 
 * @author mkron
 *
 */
public class ConstraintCollection extends AbstractConstraint {
	private AbstractConstraint[] constraintArray = new AbstractConstraint[]{};

	public ConstraintCollection() {
		super();
		constraintArray = new AbstractConstraint[]{};
	}
	
	public ConstraintCollection(AbstractConstraint[] constrArr) {
		super();
		constraintArray = constrArr;
	}
	
	public ConstraintCollection(AbstractConstraint[] constrArr, ConstraintHandlingEnum handling, double penaltyFact) {
		this(constrArr);
		setHandlingMethod(handling);
		setPenaltyFactor(penaltyFact);
	}
	
	public ConstraintCollection(ConstraintCollection o) {
		super(o);
		constraintArray = o.constraintArray.clone();
		for (int i=0; i<constraintArray.length; i++) {
			constraintArray[i]=(AbstractConstraint)o.constraintArray[i].clone();
		}
	}

	@Override
	public Object clone() {
		return new ConstraintCollection(this);
	}

	@Override
	protected double getRawViolationValue(double[] indyX) {
		double v, sum=0;
		if (TRACE) System.out.println("Viol (pen "+getPenaltyFactor()+")");
		for (AbstractConstraint constr : constraintArray) {
			v=constr.getViolation(indyX);
			sum += v; 
			if (TRACE) System.out.println(constr.getClass().getSimpleName() + " " + v);
		}
		return sum;
	}

	public AbstractConstraint[] getConstraints() {
		return constraintArray;
	}

	public void setConstraints(AbstractConstraint[] constrArray) {
		this.constraintArray = constrArray;
	}
	
	public String constraintsTipText() {
		return "A set of constraints which is handled uniformly";
	}
	
	public String getName() {
		return constraintArray.length + " constr./" + getPenaltyFactor() + "/" + getHandlingMethod() + "/" + getPenaltyFactControl().getClass().getSimpleName();
	}
	
	public static String globalInfo() {
		return "A set of constraints with a single parameter adaption mechanism.";
	}
}
