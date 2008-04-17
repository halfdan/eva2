package eva2.server.go.problems;

import java.util.List;

import eva2.server.go.PopulationInterface;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.operators.distancemetric.InterfaceDistanceMetric;
import eva2.server.go.operators.distancemetric.PhenotypeMetric;
import eva2.server.go.operators.postprocess.PostProcess;
import eva2.server.go.populations.Population;

import eva2.server.go.problems.Interface2DBorderProblem;

public abstract class AbstractMultiModalProblemKnown extends AbstractProblemDouble implements Interface2DBorderProblem, InterfaceMultimodalProblemKnown {
	protected static InterfaceDistanceMetric   m_Metric = new PhenotypeMetric();
	private double                    m_GlobalOpt = 0;
	private Population                  m_Optima;
	protected double                    m_Epsilon = 0.05;
//	protected double[][]                m_Range;
//	protected double[]                  m_Extrema;
	protected int						m_ProblemDimension = 2;
	// if the global optimum is zero and we want to see logarithmic plots, the offset must be a little lower. see addOptimum()
	protected boolean 					makeGlobalOptUnreachable = false;

	public AbstractMultiModalProblemKnown() {
		this.m_ProblemDimension = 2;
		this.m_Template         = new ESIndividualDoubleData();
//		this.m_Extrema          = new double[2];
//		this.m_Range            = makeRange();
//		this.m_Extrema[0]       = -2;
//		this.m_Extrema[1]       = 6;
	}

	protected void cloneObjects(AbstractMultiModalProblemKnown b) {
		super.cloneObjects(b);
		if (b.m_Optima != null)
			this.m_Optima           = (Population)((Population)b.m_Optima).clone();
//		if (b.m_Range != null) {
//			this.m_Range          = new double[b.m_Range.length][b.m_Range[0].length];
//			for (int i = 0; i < this.m_Range.length; i++) {
//				for (int j = 0; j < this.m_Range[i].length; j++) {
//					this.m_Range[i][j] = b.m_Range[i][j];
//				}
//			}
//		}		
		this.m_ProblemDimension = b.m_ProblemDimension;
		this.m_GlobalOpt        = b.m_GlobalOpt;
		this.m_Epsilon         = b.m_Epsilon;
//		if (b.m_Extrema != null) {
//		this.m_Extrema          = new double[b.m_Extrema.length];
//		for (int i = 0; i < this.m_Extrema.length; i++) {
//			this.m_Extrema[i] = b.m_Extrema[i];
//		}
//	}
	}
	
	public AbstractMultiModalProblemKnown(AbstractMultiModalProblemKnown b) {
		cloneObjects(b);
	}

//	/** This method returns a deep clone of the problem.
//	 * @return  the clone
//	 */
//	public Object clone() {
//		return (Object) new AbstractMultiModalProblem(this);
//	}

	/** This method inits a given population
	 * @param population    The populations that is to be inited
	 */
	public void initPopulation(Population population) {
		AbstractEAIndividual tmpIndy;

		population.clear();

//		this.m_ProblemDimension = 2;
		((InterfaceDataTypeDouble)this.m_Template).setDoubleDataLength(this.m_ProblemDimension);
		((InterfaceDataTypeDouble)this.m_Template).SetDoubleRange(makeRange());
		for (int i = 0; i < population.getPopulationSize(); i++) {
			tmpIndy = (AbstractEAIndividual)((AbstractEAIndividual)this.m_Template).clone();
			tmpIndy.init(this);
			population.add(tmpIndy);
		}
		// population init must be last
		// it set's fitcalls and generation to zero
		population.init();
		this.m_GlobalOpt = Double.NEGATIVE_INFINITY;
		m_Optima = new Population();
		this.initListOfOptima();
	}
	
	public void initProblem() {
		super.initProblem();
		this.m_GlobalOpt = Double.NEGATIVE_INFINITY;
		m_Optima = new Population();
		this.initListOfOptima();
	}

	/** Ths method allows you to evaluate a simple bit string to determine the fitness
	 * @param x     The n-dimensional input vector
	 * @return  The m-dimensional output vector.
	 */
	public double[] eval(double[] x) {
		double[] result = new double[1];
		result[0]   = this.m_GlobalOpt - evalUnnormalized(x)[0];
		return result;
	}

	/** 
	 * This method returns the unnormalized function value for an maximization problem
	 * @param x     The n-dimensional input vector
	 * @return  The m-dimensional output vector.
	 */
	public abstract double[] evalUnnormalized(double[] x);
	
	/** 
	 * This method returns the header for the additional data that is to be written into a file
	 * @param pop   The population that is to be refined.
	 * @return String
	 */
	public String getAdditionalFileStringHeader(PopulationInterface pop) {
		return "Solution \t Number of Optima found \t Maximum Peak Ratio";
	}

	/** 
	 * This method returns the additional data that is to be written into a file
	 * @param pop   The population that is to be refined.
	 * @return String
	 */
	public String getAdditionalFileStringValue(PopulationInterface pop) {
		String result = "";
		result += AbstractEAIndividual.getDefaultDataString(pop.getBestIndividual()) +"\t";
		result += this.getNumberOfFoundOptima((Population)pop)+"\t";
		result += this.getMaximumPeakRatio((Population)pop);
		return result;
	}
//
//	/** This method returns a string describing the optimization problem.
//	 * @return The description.
//	 */
//	public String getStringRepresentation() {
//		String result = "";
//
//		result += "M0 function:\n";
//		result += "This problem has one global and one local optimum.\n";
//		result += "Parameters:\n";
//		result += "Dimension   : " + this.m_ProblemDimension +"\n";
//		result += "Noise level : " + this.getNoise() + "\n";
//		result += "Solution representation:\n";
//		//result += this.m_Template.getSolutionRepresentationFor();
//		return result;
//	}

	/**********************************************************************************************************************
	 * Implementation of InterfaceMultimodalProblemKnown
	 */

	/** This method allows you to add a 2d optima to the list of optima
	 * @param x
	 * @param y
	 */
	protected void add2DOptimum(double x, double y) {
		double[] point = new double[2];
		point[0] = x;
		point[1] = y;
		addOptimum(point);
	}

	/** This method allows you to add a 2d optima to the list of optima
	 * @param x
	 * @param y
	 */
	protected void addOptimum(double[] point) {
		InterfaceDataTypeDouble tmpIndy;
		tmpIndy = (InterfaceDataTypeDouble)((AbstractEAIndividual)this.m_Template).clone();
		tmpIndy.SetDoubleDataLamarkian(point);
		((AbstractEAIndividual)tmpIndy).SetFitness(evalUnnormalized(point));
		if (((AbstractEAIndividual)tmpIndy).getFitness(0)>=m_GlobalOpt) {
			m_GlobalOpt = ((AbstractEAIndividual)tmpIndy).getFitness(0);
			if (makeGlobalOptUnreachable) {
				double tmp=m_GlobalOpt;
				double dx = 1e-30;
				while (tmp==m_GlobalOpt) {
					// this increases the optimum until there is a real difference.
					// tries to avoid zero y-values which break the logarithmic plot
					tmp+=dx;
					dx *= 10;
				}
				m_GlobalOpt = tmp;
			}
		} 
		this.m_Optima.add(tmpIndy);
	}

	/** 
	 * This method will prepare the problem to return a list of all optima
	 * if possible and to return quality measures like NumberOfOptimaFound and
	 * the MaximumPeakRatio. When implementing, use the addOptimum(double[])
	 * method for every optimum, as it keeps track the global optimum.
	 * This method will be called on initialization.
	 */
	public abstract void initListOfOptima();

	/** This method returns a list of all optima as population
	 * @return population
	 */
	public Population getRealOptima() {
		return this.m_Optima;
	}

	/** This method returns the Number of Identified optima
	 * @param pop       A population of possible solutions.
	 * @return int
	 */
	public int getNumberOfFoundOptima(Population pop) {
		List<AbstractEAIndividual> sols = PostProcess.getFoundOptima(pop, m_Optima, m_Epsilon, true);
		return sols.size();
	}

	/** 
	 * This method returns the maximum peak ratio, which is the ratio of found fitness values corresponding to
	 * known optima with the internal epsilon criterion and the sum of all fitness values seen as maximization.
	 * Thus, if all optima are perfectly found, 1 is returned. If no optimum is found, zero is returned.
	 * A return value of 0.5 may mean, e.g., that half of n similar optima have been found perfectly, or that 1 major 
	 * optimum of equal weight than all the others has been found perfectly, or that all optima have been found
	 * with about 50% accuracy, etc.
	 * 
	 * @param pop       A population of possible solutions.
	 * @return double
	 */
	public double getMaximumPeakRatio(Population pop) {
		return getMaximumPeakRatio(pop, m_Epsilon);
	}
	
	public double getMaximumPeakRatio(Population pop, double epsilon) {
		double                  optimaInvertedSum = 0, foundInvertedSum = 0;
		AbstractEAIndividual[] optsFound = PostProcess.getFoundOptimaArray(pop, m_Optima, epsilon, true);

		for (int i=0; i<m_Optima.size(); i++) {
			// sum up known optimal fitness values
			optimaInvertedSum += m_Optima.getEAIndividual(i).getFitness(0);
			// sum up best found hits, with inverted fitness
			if (optsFound[i] != null) foundInvertedSum += m_GlobalOpt - optsFound[i].getFitness(0);
		}

		return foundInvertedSum/optimaInvertedSum;
	}
	
//	public double getMaximumPeakRatio(Population pop) {
//		double                  result = 0, sum = 0;
//		AbstractEAIndividual   posOpt, opt;
//		boolean[]               found = new boolean[this.m_Optima.size()];
//		for (int i = 0; i < found.length; i++) {
//			found[i] = false;
//			sum += ((AbstractEAIndividual)this.m_Optima.get(i)).getFitness(0) ;
//			//System.out.println("Optimum " + i + ".: " + (((AbstractEAIndividual)this.m_Optima.get(i)).getFitness(0)));
//		}
//
//		for (int i = 0; i < pop.size(); i++) {
//			posOpt = (AbstractEAIndividual) pop.get(i);
//			for (int j = 0; j < this.m_Optima.size(); j++) {
//				if (!found[j]) {
//					opt = (AbstractEAIndividual) this.m_Optima.get(j);
//					if (this.m_Metric.distance(posOpt, opt) < this.m_Epsilon) {
//						found[j] = true;
//						result += this.m_GlobalOpt - posOpt.getFitness(0);
//						//System.out.println("Found Optimum " + j + ".: " + (this.m_GlobalOpt - posOpt.getFitness(0)));
//					}
//				}
//			}
//		}
//		return result/sum;
//	}
	/**********************************************************************************************************************
	 * These are for GUI
	 */

//	/** This method returns this min and may fitness occuring
//	* @return double[]
//	*/
//	public double[] getExtrema() {
//	double[] range = new double[2];
//	range[0] = -5;
//	range[1] = 5;
//	return range;
//	}

	/**
	 * @return the m_Epsilon
	 */
	public double getEpsilon() {
		return m_Epsilon;
	}

	/**
	 * @param epsilon the m_Epsilon to set
	 */
	public void setEpsilon(double epsilon) {
		m_Epsilon = epsilon;
	}

	public String epsilonTipText() {
		return "Epsilon criterion indicating whether an optimum was found";
	}

	@Override
	public int getProblemDimension() {
		return m_ProblemDimension;
	}
}