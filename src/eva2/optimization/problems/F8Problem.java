package eva2.optimization.problems;

import eva2.optimization.population.PopulationInterface;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.operator.distancemetric.EuclideanMetric;
import eva2.optimization.operator.postprocess.SolutionHistogram;
import eva2.optimization.population.Population;
import eva2.tools.ToolBox;
import eva2.tools.math.Mathematics;

import java.util.Arrays;


/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 01.09.2004
 * Time: 19:40:28
 * To change this template use File | Settings | File Templates.
 */
public class F8Problem extends AbstractProblemDoubleOffset
        implements InterfaceInterestingHistogram, InterfaceMultimodalProblem, //InterfaceFirstOrderDerivableProblem,
        InterfaceMultimodalProblemKnown, java.io.Serializable {

    transient protected Population m_ListOfOptima = null;
    private static transient boolean state_initializing_optima = false;
    private double a = 20;
    private double b = 0.2;
    private double c = 2 * Math.PI;
    final static double f8Range = 32.768;

    public F8Problem() {
        setDefaultRange(f8Range);
    }

    public F8Problem(F8Problem b) {
        super(b);
        this.a = b.a;
        this.b = b.b;
        this.c = b.c;
    }

    public F8Problem(int dim) {
        super(dim);
        setDefaultRange(f8Range);
    }

//    make this a multimodal problem known and add the best optima as in the niching ES papers!

    /**
     * This method returns a deep clone of the problem.
     *
     * @return the clone
     */
    @Override
    public Object clone() {
        return (Object) new F8Problem(this);
    }

    /**
     * Ths method allows you to evaluate a double[] to determine the fitness
     *
     * @param x The n-dimensional input vector
     * @return The m-dimensional output vector.
     */
    @Override
    public double[] eval(double[] x) {
        x = rotateMaybe(x);
        double[] result = new double[1];
        double sum1 = 0, sum2 = 0, exp1, exp2;

        for (int i = 0; i < x.length; i++) {
            double xi = x[i] - xOffset;
            sum1 += (xi) * (xi);
            sum2 += Math.cos(c * (xi));
        }
        exp1 = -b * Math.sqrt(sum1 / (double) this.problemDimension);
        exp2 = sum2 / (double) this.problemDimension;
        result[0] = yOffset + a + Math.E - a * Math.exp(exp1) - Math.exp(exp2);

        return result;
    }

    /**
     * This method returns a string describing the optimization problem.
     *
     * @return The description.
     */
    public String getStringRepresentationForProblem() {
        String result = "";

        result += "F8 Ackley's function.\n";
        result += "This problem is multimodal.\n";
        result += "Parameters:\n";
        result += "Dimension   : " + this.problemDimension + "\n";
        result += "Noise level : " + this.getNoise() + "\n";
        result += "Solution representation:\n";
        //result += this.template.getSolutionRepresentationFor();
        return result;
    }


    @Override
    public void initializeProblem() {
        super.initializeProblem();
        initListOfOptima();
    }

    /**********************************************************************************************************************
     * These are for GUI
     */
    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "F8-Problem";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "Ackley's function.";
    }

    @Override
    public SolutionHistogram getHistogram() {
        if (getProblemDimension() < 15) {
            return new SolutionHistogram(-0.1, 7.9, 16);
        } else if (getProblemDimension() < 25) {
            return new SolutionHistogram(-0.5, 15.5, 16);
        } else {
            return new SolutionHistogram(0, 16, 16);
        }
    }

//	public double[] getFirstOrderGradients(double[] x) {
//		double sum1=0, sum2=0;
//		double[] derivs = new double[x.length];
//    	x = rotateMaybe(x);
//        double dim = (double)this.problemDimension;
//
//        for (int i = 0; i < x.length; i++) {
//        	double xi = x[i]-m_XOffSet;
//        	sum1 += (xi)*(xi);
//        	sum2 += Math.cos(c * (xi));
//        }
//        
//        for (int i=0; i<x.length; i++) {
//        	if (sum1==0) derivs[i]=0;
//        	else derivs[i]=((this.b*2*x[i]*this.a)/(Math.sqrt(sum1/dim)))*Math.exp(-this.b*Math.sqrt(sum1/dim));
//        	derivs[i]+= ((this.c/dim)*(Math.sin(this.c*x[i])))*Math.exp(sum2/dim);
//        }
//        System.out.println("at " + BeanInspector.toString(x) + " sum1 " + sum1 + " sum2 " + sum2 + " deriv " + BeanInspector.toString(derivs));
//        return derivs;
//	}

    @Override
    public boolean fullListAvailable() {
        return true;
    }

    @Override
    public double getMaximumPeakRatio(Population pop) {
        return AbstractMultiModalProblemKnown.getMaximumPeakRatioMinimization(m_ListOfOptima, pop, getDefaultAccuracy(), 0, 5);
    }

    @Override
    public int getNumberOfFoundOptima(Population pop) {
        return AbstractMultiModalProblemKnown.getNoFoundOptimaOf(this, pop);
    }

    @Override
    public Population getRealOptima() {
        return m_ListOfOptima;
    }

    @Override
    public String[] getAdditionalDataHeader() {
        String[] superHd = super.getAdditionalDataHeader();
        return ToolBox.appendArrays(new String[]{"numOptimaFound", "maxPeakRatio"}, superHd);
//		return "#Optima found \tMaximum Peak Ratio \t" + super.getAdditionalDataHeader(population);
    }

    @Override
    public Object[] getAdditionalDataValue(PopulationInterface pop) {
        Object[] myRet = new Object[2];
        myRet[0] = this.getNumberOfFoundOptima((Population) pop);
        myRet[1] = this.getMaximumPeakRatio((Population) pop);
        return ToolBox.appendArrays(myRet, super.getAdditionalDataValue(pop));
    }

    /**
     * We identify a set of 2*n+1 optima, which are the global and the first level
     * of local optima.
     * Interestingly, the local optima in the first sphere (close to (1,0,0,0,...) vanish starting
     * with n=18 (checked with Matlab). The next sphere contains O(n^2) optima (I think 3*n*(n-1)).
     * This is unfortunately not mentioned in the papers by Shir
     * and Bäck who still seemed to be able to find 2*n+1...
     */
    @Override
    public void initListOfOptima() {
        if (listOfOptimaNeedsUpdate()) {
            state_initializing_optima = true;
            m_ListOfOptima = new Population();
            // ingeniously avoid recursive calls during refinement!
            double[] pos = new double[getProblemDimension()];
            Arrays.fill(pos, getXOffset());
            addOptimum(pos); // the global optimum
            double refinedX = 0;
            if (getProblemDimension() < 18) {
                for (int i = 0; i < getProblemDimension(); i++) {
                    // TODO what about dimensions higher than 18???
                    for (int k = -1; k <= 1; k += 2) {
                        Arrays.fill(pos, getXOffset());
                        if (refinedX == 0) { // we dont know the exact x-offset for the optima, so refine once
                            pos[i] = k + getXOffset();
                            double[] dir = pos.clone();
                            dir[i] -= 0.05;
                            pos = inverseRotateMaybe(pos);
//						ab dim 18/20 oder so finde ich plötzlich keine optima bei x[i]<1 mehr???
                            dir = inverseRotateMaybe(dir);
                            pos = refineSolution(this, pos, dir, 0.0005, 1e-20, 0);
                            if (EuclideanMetric.euclideanDistance(pos, m_ListOfOptima.getEAIndividual(0).getDoublePosition()) < 0.5) {
                                System.err.println("Warning, possibly converged to a wrong optimum in F8Problem.initListOfOptima!");
                            }
                            pos = rotateMaybe(pos);
                            refinedX = Math.abs(pos[i] - getXOffset()); // store the refined position which is equal in any direction and dimension
                        } else {
                            pos[i] = (k * refinedX) + getXOffset();
                        }
                        addOptimum(pos);
                    }
                }
            }
//			System.out.println("Inited " + listOfOptima.size() + " optima, measures: " + BeanInspector.toString(listOfOptima.getPopulationMeasures(new PhenotypeMetric())));
//			System.out.println("Inited " + listOfOptima.size() + " optima, measures: " + BeanInspector.toString(listOfOptima.getPopulationMeasures(new EuclideanMetric())));
//			System.out.println(listOfOptima.getStringRepresentation());
            state_initializing_optima = false;
        }
    }

    private double[] refineSolution(AbstractProblemDouble prob, double[] pos, double[] vect, double initStep, double thresh, int fitCrit) {
//		return AbstractProblemDouble.refineSolutionNMS(prob, pos);
        // a line search along a vector
        double[] tmpP = pos.clone();
        double[] normedVect = Mathematics.normVect(vect);
        double dx = initStep;
        double tmpFit, oldFit = prob.eval(pos)[fitCrit];

        int dir = 1;
        while (dx > thresh) {
            // add a step to tmpP
            Mathematics.svvAddScaled(dx * dir, normedVect, pos, tmpP);
            // evaluate tmpP
            tmpFit = prob.eval(tmpP)[fitCrit];
            if (tmpFit < oldFit) {
                // if tmpP is better than pos continue at new pos
                double[] tmp = pos;
                pos = tmpP;
                tmpP = tmp;
                oldFit = tmpFit;
            } else {
                // otherwise invert direction, reduce step, continue
                dx *= 0.73;
                dir *= -1;
            }
        }
        return pos;
    }

    private boolean listOfOptimaNeedsUpdate() {
        if (state_initializing_optima) {
            return false;
        } // avoid recursive call during refining with GDA
        if (m_ListOfOptima == null || (m_ListOfOptima.size() != (1 + 2 * getProblemDimension()))) {
            return true;
        } else { // the number of optima is corret - now check different offset or rotation by comparing one fitness value
            AbstractEAIndividual indy = m_ListOfOptima.getEAIndividual(1);
            double[] curFit = eval(indy.getDoublePosition());
            if (Math.abs(Mathematics.dist(curFit, indy.getFitness(), 2)) > 1e-10) {
                return true;
            } else {
                return false;
            }
        }
//		else {
//			if (listOfOptima.isEmpty()) return true;
//			else {
//				// test for correctness of the second optimum - if its gradient is nonzero, reinit optima
//				AbstractEAIndividual testIndy = listOfOptima.getEAIndividual(1);
//				double grad[] = this.getFirstOrderGradients(testIndy.getDoublePosition());
//				for (int i=0; i<grad.length; i++) {
//					if (Math.abs(grad[i])>1e-20) {
//						listOfOptima.clear();
//						return true;
//					}
//				}
//				return false;
//			}
//		}
    }

    private void addOptimum(double[] pos) {
        AbstractProblemDouble.addUnrotatedOptimum(m_ListOfOptima, this, pos);
    }

//	private double[] refineSolution(double[] pos) {
//		Population population = new Population();
//		InterfaceDataTypeDouble tmpIndy;
//		tmpIndy = (InterfaceDataTypeDouble)((AbstractEAIndividual)this.template).clone();
//		tmpIndy.setDoubleGenotype(pos);
//		((AbstractEAIndividual)tmpIndy).SetFitness(eval(pos));
//		population.add(tmpIndy);
//		FitnessConvergenceTerminator convTerm = new FitnessConvergenceTerminator(1e-15, 10, false, true);
//		int calls = PostProcess.processWithGDA(population, this, convTerm, 0, 0.0000000000000001, 0.01);
//		return ((InterfaceDataTypeDouble)population.getBestEAIndividual()).getDoubleData();
//	}
}