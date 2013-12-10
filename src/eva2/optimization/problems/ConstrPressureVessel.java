package eva2.optimization.problems;

import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.constraint.*;

import java.util.Vector;

public class ConstrPressureVessel extends AbstractProblemDouble {
    private boolean discreteThickness = true;
    private double minThickness = 0.0625;
    private double maxThickness = 2;
    private double minRad = 10, maxRad = 300;
    private double minLen = 10, maxLen = 300;
//	L=200;
//	R=40.3239;
//	thickS=0.8125;
//	thickH=0.4375;

    public ConstrPressureVessel() {
        setWithConstraints(true);
        setConstraints(new AbstractConstraint[]{new ConstraintCollection(makeDefaultConstraints(), ConstraintHandlingEnum.penaltyAdditive, 1000)});
//		setConstraints(makeDefaultConstraints());
    }

    public ConstrPressureVessel(ConstrPressureVessel o) {
        super();
        super.cloneObjects(o);
    }

    public static AbstractConstraint[] makeDefaultConstraints() {
        Vector<AbstractConstraint> constraints = new Vector<AbstractConstraint>();
//		constraints.add(new IntervalConstraint("+(+(85.334407,*(0.0056858,*(x1,x4))), +(*(0.00026,*(x0,x3)),*(-0.0022053,*(x2,x4))))", 0, 92));
        constraints.add(new GenericConstraint("-(*(0.0193,x2),x0)", ConstraintRelationEnum.lessEqZero));
        constraints.add(new GenericConstraint("-(*(0.00954,x2),x1)", ConstraintRelationEnum.lessEqZero));
        constraints.add(new GenericConstraint("-(1296000, +(*(pi, *(pow2(x2),x3)),*(/(4,3),*(pi,pow3(x2))))))", ConstraintRelationEnum.lessEqZero));
        constraints.add(new GenericConstraint("-(x3,240)", ConstraintRelationEnum.lessEqZero));

//		for (Iterator<AbstractConstraint> iterator = constraints.iterator(); iterator.hasNext();) {
//			AbstractConstraint constr = iterator.next();
//			constr.setHandlingMethod(ConstraintHandlingEnum.penaltyAdditive);
//			constr.setPenaltyFactor(10000);
//		}
        return constraints.toArray(new AbstractConstraint[constraints.size()]);
    }

    @Override
    protected double[] getEvalArray(AbstractEAIndividual individual) {
        double[] x = super.getEvalArray(individual);
        if (discreteThickness) {// integer multiple of minimal thickness
            int n = (int) (x[0] / minThickness);
            x[0] = n * minThickness;
            n = (int) (x[1] / minThickness);
            x[1] = n * minThickness;
        }
        //double thickS=x[0], thickH=x[1], R=x[2], L=x[3];
//		x[0]=0.8125;	//thickS=0.8125;
//		x[1]=0.4375;	//thickH=0.4375;
//		x[2]=40.3239;	//R=40.3239;
//		x[3]=200; 	//L=200;
        return x;
    }

    @Override
    public double[] evaluate(double[] x) {
        double v, thickS = x[0], thickH = x[1], R = x[2], L = x[3];

        v = 0.6224 * thickS * R * L + 1.7781 * thickH * R * R + 3.1661 * thickS * thickS * L + 19.84 * thickS * thickS * R;
//		v = 0.6224*x(0)*x(2)*x(3)+1.7781*x(1)*x(2)*x(2)+3.1661*x(0)*x(0)*x(3)+19.84*x(0)*x(0)*x(2);
        return new double[]{v};
    }

    @Override
    public int getProblemDimension() {
        return 4;
    }

    @Override
    public Object clone() {
        return new ConstrPressureVessel(this);
    }

    @Override
    public double getRangeLowerBound(int dim) {
        switch (dim) {
            case 0:
            case 1:
                return minThickness / 2;
            case 2:
                return minRad;
            case 3:
                return minLen;
        }
        System.err.println("Invalid dimension for lower bound (ConstrPressureVessel)");
        return 0.;
//    	if (dim<=1) return minThickness/2;
//    	else return minLen;
    }

    @Override
    public double getRangeUpperBound(int dim) {
        switch (dim) {
            case 0:
            case 1:
                return maxThickness;
            case 2:
                return maxRad;
            case 3:
                return maxLen;
        }
        System.err.println("Invalid dimension for upper bound (ConstrPressureVessel)");
        return 100.;
//    	if (dim<=1) {
//    		return maxThickness;
//    	} else if (dim==2) return maxRad;
//    	else return maxLen;
    }

    @Override
    public String getName() {
        return "Constrained-Pressure-Vessel";
    }

    public static String globalInfo() {
        return "Minimize the material cost of a pressure vessel";
    }

    @Override
    public void hideHideable() {
        super.hideHideable();
        GenericObjectEditor.setHideProperty(this.getClass(), "defaultRange", true);
    }

    public boolean isDiscreteThickness() {
        return discreteThickness;
    }

    public void setDiscreteThickness(boolean discreteThickness) {
        this.discreteThickness = discreteThickness;
    }

}
