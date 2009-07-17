package eva2.server.go.problems;

import java.io.Serializable;
import java.util.Vector;

import eva2.server.go.operators.constraint.AbstractConstraint;
import eva2.server.go.operators.constraint.ConstraintCollection;
import eva2.server.go.operators.constraint.IntervalConstraint;

public class ConstrHimmelblauProblem extends AbstractProblemDouble implements Serializable {
	private static double yOffset = 31025.5602425; // moving the optimum close to zero
	private boolean useYOffset = true;
	
	public ConstrHimmelblauProblem() {
		setWithConstraints(true);
		setDefaultRange(100);
		setConstraints(new AbstractConstraint[]{new ConstraintCollection(makeDefaultConstraints())});
	}

	public ConstrHimmelblauProblem(
			ConstrHimmelblauProblem o) {
		super();
		super.cloneObjects(o);
		useYOffset=o.useYOffset;
	}

	@Override
	public Object clone() {
		return new ConstrHimmelblauProblem(this);
	}
	
//	@Override
//	public void initProblem() {
//		super.initProblem();
//		setConstraints(new AbstractConstraint[]{new ConstraintCollection(makeDefaultConstraints())});
//	}

	public static AbstractConstraint[] makeDefaultConstraints() {
		Vector<AbstractConstraint> constraints = new Vector<AbstractConstraint>();
		constraints.add(new IntervalConstraint("+(+(85.334407,*(0.0056858,*(x1,x4))), +(*(0.00026,*(x0,x3)),*(-0.0022053,*(x2,x4))))", 0, 92));
		constraints.add(new IntervalConstraint("+(+(80.51249,*(0.0071317,*(x1,x4))), +(*(0.0029955,*(x0,x1)),*(0.0021813,*(x2,x2))))", 90, 110));
		constraints.add(new IntervalConstraint("+(+(9.300961,*(0.0047026,*(x2,x4))), +(*(0.0012547,*(x0,x2)),*(0.0019085,*(x2,x3))))", 20, 25));
		
		constraints.add(new IntervalConstraint(0, 78, 102));
		constraints.add(new IntervalConstraint(1, 33, 45));
		constraints.add(new IntervalConstraint(2, 27, 45));
		constraints.add(new IntervalConstraint(3, 27, 45));
		constraints.add(new IntervalConstraint(4, 27, 45));
		return constraints.toArray(new AbstractConstraint[constraints.size()]);
	}

	@Override
	public double[] eval(double[] x) {
		double v=5.3578547*x[2]*x[2]+0.8356891*x[0]*x[4]+37.293239*x[0]-40792.141;
		if (useYOffset) v+=yOffset;
		return new double[]{v};
	}

	@Override
	public int getProblemDimension() {
		return 5;
	}
	
	public String getName() {
		return "Constrained Himmelblau Problem";
	}
	
	public String globalInfo() {
		return "Himmelblau's nonlinear optimization problem with 5 simple boundary constraints and 3 nonlinear boundary constraints.";
	}

	public boolean isUseYOffset() {
		return useYOffset;
	}

	public void setUseYOffset(boolean useYOffset) {
		this.useYOffset = useYOffset;
	}
	
	public String useYOffsetTipText() {
		return "Activate offset moving the optimum (close) to zero.";
	}
}
