package eva2.optimization.problems;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.strategies.InterfaceOptimizer;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/** 
 * A dynamically "jumping" problem. The severity gives the length of one jump in problem space, occurring
 * with the given frequency. 
 * 
 * @author Geraldine Hopf
 * @date 25.06.2007
 */

public class DynJumpProblem extends AbstractDynTransProblem {

	private static final long serialVersionUID = 2693154860448970283L;
	
	/* correlation value: 0.0 moving is at random, 1.0 moving is totally correlated to the previous moving */
	private double lambda 			=   0.0;
	
	protected double[] translation;
	private double[] previousMovement;
	protected double[] randomNumber;

	private double evaluations = 0.0;
	
	/* for the output file */
	private int changeCounter;
	private Writer fw = null; 
	private String s = "";
		
	public DynJumpProblem() {
		super();
		translation = new double[getProblemDimension()];
		randomNumber = new double[getProblemDimension()];
		initialize(0.0, 0.1, 0.1);
		changeCounter = 0;
	}
	public DynJumpProblem(DynJumpProblem other) {
		other.clone();
	}

    @Override
	protected double getTranslation(int dim, double time) {
		return translation[dim];
	}
	
	
    @Override
	protected void changeProblemAt(double problemTime) {
		super.changeProblemAt(problemTime);
		makeTranslation();
		/* prooving results */
		if (TRACE) {
                writeFile();
            }
		++changeCounter;
	}
	
    @Override
	protected void countEvaluation() {
		super.countEvaluation();
		evaluations += 1.;
	}
	
	protected void makeTranslation() {
		/* will be shifted with shift, creation is the same like previousMovement*/
		double shift[] = new double[getProblemDimension()];	
		double norm = 0.0;
		for (int i = 0; i < getProblemDimension(); ++i) {
			shift[i] = rand.nextGaussian();
			norm += Math.pow(shift[i], 2);
		}
		/* normalize */
		if (norm > 0.0){
			norm = getSeverity() / Math.sqrt(norm);
		} else {
			norm = 0.0; // rounding errors
		}
		double norm2 = 0.0;
		for (int i = 0; i < getProblemDimension(); ++i) {
			/* crux: if lambda = 1, only previousMovement is calculated
			 *       if lambda = 0, only random is calculated
			 *       other          rand and previousMovement are weighted 
			 */
			shift[i] = (1 - getLambda()) * (norm * shift[i])+ getLambda() * previousMovement[i];
			norm2 += Math.pow(shift[i], 2);
		}	
		if (norm2 > 0.0) {
			norm2 = getSeverity() / Math.sqrt(norm2);
		} else {
			norm2 = 0.0;
		}
		
		for (int i = 0; i < getProblemDimension(); ++i) {
			shift[i] = norm2 * shift[i];	
			/* test if still between boundaries, if not bounce it off like a pool ball */
			if (translation[i] + shift[i] < range[0][0]) {
				translation[i] = 2.0 * range[0][0] - translation[i] - shift[i];
				shift[i] *= -1.0;
			} else if (translation[i] + shift[i] > range[0][1]) {
				translation[i] = 2.0 * range[0][1] - translation[i] - shift[i];
				shift[i] *= -1.0;
			} else {
				translation[i] += shift[i];
			}			
			/* Update previousMovement */
			previousMovement[i] = shift[i];
		}
		if (TRACE) {
			System.out.print("Jumped to ");
			for (int i = 0; i < getProblemDimension(); i++) {
                System.out.print(" " + translation[i]);
            }
			System.out.println();
		}
	}

    @Override
	public void initializeProblem() {
		super.initializeProblem();
		translation = new double[getProblemDimension()];
		evalsSinceChange = 0.0;
		evaluations = 0.0;
		changeCounter = 0;
		
		/* if lambda > 0.0 initialize the first random direction */
		previousMovement = new double[getProblemDimension()];
		double norm = 0.0;
		for (int i = 0; i < getProblemDimension(); ++i) {
			/* previousMovement has values between -0.5 and 0.5 -> direction of movement */
			previousMovement[i] = rand.nextGaussian();
			/* needed to standardize to moveLength */
			norm += Math.pow(previousMovement[i], 2);
		}
		/* (a[]/ |a[]|) = length(1)
		 * (a[]/ |a[]|) * moveLength = length(moveLength)
		 * |a[]| = sqare_root( (sum (a_i)^2) )
		 * norm = moveLength / |a[]|
		 * moveLenght = getSeverity()
		 */
		if (norm > 0.0) {
			norm = getSeverity() / Math.sqrt(norm);
		} else {
			norm = 0.0; // rounding errors
		}
		/* now previousMovement is standardized */
		for (int i = 0; i < getProblemDimension(); ++i) {
			previousMovement[i] = norm * previousMovement[i];
		}
	}

		
			

    @Override
	public Object clone() {
		return new DynJumpProblem(this);
	}

    @Override
	public AbstractEAIndividual getCurrentOptimum() {
		return null;
	}
	
/**************************************************************************
 * These are for the GUI
 * 
 */

    @Override
	public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
		return "DynJumpProblem";
	}
    @Override
	public String getName() {
		return "DynJumpProblem";
	}
    public static String globalInfo() {
    	return "A real valued problem jumping dynamically."; 
    }
    
	public String severityTipText() {
		return "length of the jump";
	}
	public double getLambda() {
		return lambda;
	}
	public void setLambda(double lambda) {
		this.lambda = lambda;
	}
	public String lambdaTipText() {
		return "direction of movement: [0,1] 0 = random, 1 = dependent";
	}
	/**************************************************************************
	 * These are for debugging and determining the output file
	 * 
	 */
	
	public void myPrint(double[] toPrint) {
		for (int i = 0; i < toPrint.length; ++i){
			System.out.print(toPrint[i] + " ");
		}
		System.out.println("");
	}
	
	public void myPrint(double[][] toPrint) {
		for (int i = 0; i < toPrint.length; i ++){
			for (int j = 0; j < toPrint[i].length; ++j){
				System.out.print(toPrint[i][j] + " ");
			}
			System.out.println("");
		}
		System.out.println("");
	}

	public void writeFile() {
		if (fw == null) {
			try {
				fw = new FileWriter( "DynJumpProblem.txt" ); 
			}
			catch ( IOException e ) { 
				System.err.println( "Konnte Datei nicht erstellen" ); 
			}
		} else {
			try 
			{ 
				fw.write("Problem wurde " + changeCounter + " mal geaendert!\n");
				fw.write(evaluations + " Evaluierungen wurden gemacht\n");
				fw.write(myPrints(translation));
				
			} 
			catch ( IOException e ) { } 
			finally { 
				if ( fw != null ) {
                                try { fw.flush(); } catch ( IOException e ) { }
                            } 
			}
		}
	}

	public String myPrints(double[][] toPrint) {
		for (int i = 0; i < toPrint.length; i ++){
			for (int j = 0; j < toPrint[i].length; ++j){
				if (j != getProblemDimension()) {
                                s += toPrint[i][j] + "\t";
                            }
			}
			s += "\n";
		}
		s += "\n";
		return s;
	}
	
	public String myPrints(double[] toPrint) {		
		for (int i = 0; i < toPrint.length; i ++){
			s += toPrint[i] + "\t";
			s += "\n";
		}
		s += "\n";
		return s;
	}
}
