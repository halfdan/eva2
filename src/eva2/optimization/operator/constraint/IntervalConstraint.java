package eva2.optimization.operator.constraint;

import eva2.gui.GenericObjectEditor;
import eva2.tools.EVAERROR;
import eva2.tools.math.Mathematics;
import java.io.Serializable;

/**
 * A constraint for a parameter or a generic function to lie within certain bounds.
 * @author mkron
 *
 */
public class IntervalConstraint extends AbstractConstraint implements InterfaceDoubleConstraint, Serializable{
	double lower=0;
	double upper=1;
	int index = 0;
	GenericConstraint genericConstr = null;
	
	public IntervalConstraint() {
		this(0,0.,1.);
		genericConstr = null;
	}
	
    @Override
	public void hideHideable() {
		GenericObjectEditor.setHideProperty(this.getClass(), "relation", true);
	}
	
	public IntervalConstraint(int index, double lowerBnd, double upperBnd) {
		this.index=index;
		this.lower=lowerBnd;
		this.upper=upperBnd;
		setRelation(ConstraintRelationEnum.lessEqZero);
		genericConstr = null;
	}
	
	public IntervalConstraint(String genericFunctionString, double lowerBnd, double upperBnd) {
		this(0, lowerBnd, upperBnd);
		setGenericFunction(genericFunctionString);
	}
	
	public IntervalConstraint(IntervalConstraint o) {
		this(o.index, o.lower, o.upper);
		genericConstr = o.genericConstr;
	}

	@Override
	public Object clone() {
		return new IntervalConstraint(this);
	}

	@Override
	protected double getRawViolationValue(double[] indyX) {
		if (genericConstr!=null) {
			double constrFuncValue = genericConstr.getRawViolationValue(indyX);
			return distanceToInterval(constrFuncValue);
		} else {
			if (index<0) {
				double violSum=0;
				for (int i=0;i<indyX.length; i++) {
					violSum += violInDim(i, indyX);
				}
				return violSum;
			} else {
				if (index>=indyX.length) {
					EVAERROR.errorMsgOnce("Error, invalid index for " + this.getClass().getSimpleName());
					return 0.;
				} else {
					return violInDim(index, indyX);
				}
			}
		}
	}
	
	public String getName() {
		String clsName=this.getClass().getSimpleName();
		if (genericConstr!=null) {
                return clsName+"/"+genericConstr.getConstraintString()+ " in ["+lower+","+upper+"]";
            }
		else {
			if (index<0) {
                        return clsName+"/x_i in ["+lower+","+upper+"]";
                    }
			else {
                        return clsName+"/x_" + index + " in ["+lower+","+upper+"]";
                    }
		}
	}

	/**
	 * Return zero if the position respects the range, else the positive distance.
	 * 
	 * @param i
	 * @param pos
	 * @return
	 */
	private double violInDim(int i, double[] pos) {
		return distanceToInterval(pos[i]);
	}
	
	/**
	 * Return zero if the position respects the range, else the positive distance.
	 * 
	 * @param i
	 * @param pos
	 * @return
	 */
	private double distanceToInterval(double v) {
		double tmp=Mathematics.projectValue(v, lower, upper);
		return Math.abs(tmp-v);
	}

	public void setGenericFunction(String str) {
		if (str!=null && (str.length()>0)) {
			genericConstr = new GenericConstraint(str);
			if (!genericConstr.checkValid()) {
                        genericConstr=null;
                    }
		} else {
                genericConstr=null;
            }
	}

	public String getGenericFunction() {
		if (genericConstr==null) {
                return "";
            }
		else {
                return genericConstr.getConstraintString();
            }
	}
	
	public String genericConstrTipText() {
		return "A generic function can be used as in GenericConstraint - it has priority.";
	}
}
