package eva2.server.go.operators.constraint;

/**
 * A constraint that is already calculated by the fitness function as an
 * independent criterion. This class can be used to transform it into 
 * a fitness penalty.
 * 
 * @author mkron
 *
 */
public class ImplicitConstraint extends AbstractConstraint {
	int index=0;
	
	public ImplicitConstraint() {}
	
	public ImplicitConstraint(int cIndex) {
		index=cIndex;
	}
	
	@Override
	public Object clone() {
		return new ImplicitConstraint(index);
	}

	@Override
	protected double getRawViolationValue(double[] indyX) {
		if (index<0 || index>=indyX.length) {
			System.err.println("Error in ImplicitConstraint!");
			return 0.;
		}
		return indyX[index];
	}

	public String getName() {
		return "ImplicitCnstr-"+index;
	}

	public static String globalInfo() {
		return "Similar to a multi-objective translation into fitness, this class allows to interpret fitness criteria as constraints.";
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		if (index>0) {
                this.index = index;
            }
		else {
                System.err.println("Error, invalid index (<=0) in ImplicitConstraint.");
            }
	}
	
	public String indexTipText() {
		return "Set the index of the fitness criterion to be translated into a constraint, must be > 0";
	}
}
