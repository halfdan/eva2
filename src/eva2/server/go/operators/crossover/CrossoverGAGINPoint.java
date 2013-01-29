package eva2.server.go.operators.crossover;


import eva2.gui.BeanInspector;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceGAIndividual;
import eva2.server.go.individuals.InterfaceGIIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import java.util.BitSet;

/**
 * The famous n-point crossover operator on a binary and integer genotype. Genotypes of
 * parent individuals are recombined by exchanging subsegments within randomly
 * selected points. Therefore, far-away allels (larger GA schemas) are more likely to be split
 * between individuals.  
 * 
 * @author mkron, streiche
 */
public class CrossoverGAGINPoint implements InterfaceCrossover, java.io.Serializable {
//    private InterfaceOptimizationProblem    m_OptimizationProblem;
    private int                             m_NumberOfCrossovers = 3;
    private static boolean TRACE=false;
    
    public CrossoverGAGINPoint() {

    }
    
    public CrossoverGAGINPoint(int nPoints) {
    	this();
    	setNumberOfCrossovers(nPoints);
    }
    
    public CrossoverGAGINPoint(CrossoverGAGINPoint mutator) {
//        this.m_OptimizationProblem    = mutator.m_OptimizationProblem;
        this.m_NumberOfCrossovers     = mutator.m_NumberOfCrossovers;
    }

    /** This method will enable you to clone a given crossover operator
     * @return The clone
     */
    @Override
    public Object clone() {
        return new CrossoverGAGINPoint(this);
    }
    
    protected Object getGenotype(AbstractEAIndividual individual) {
    	Object genotype=null;
		if (individual instanceof InterfaceGAIndividual) {
			genotype = ((InterfaceGAIndividual)individual).getBGenotype();
		} else {
			genotype = ((InterfaceGIIndividual)individual).getIGenotype();
		}
		return genotype;
    }

    @Override
    public AbstractEAIndividual[] mate(AbstractEAIndividual individual, Population partners) {
        AbstractEAIndividual[] result = null;
        result = new AbstractEAIndividual[partners.size()+1];
        result[0] = (AbstractEAIndividual) (individual).clone();
        for (int i = 0; i < partners.size(); i++) {
            result[i+1] = (AbstractEAIndividual) ((AbstractEAIndividual)partners.get(i)).clone();
        }

        if (partners.size() == 0) {
            return result;
        }
        
		if (individual instanceof InterfaceGAIndividual || (individual instanceof InterfaceGIIndividual)) {
			int         length          =  getGenotypeLength(individual);
//            Object[][]  tmpGenotypes       = new Object[2][partners.size()+1];
            Object[]  origGenotypes      = new Object[partners.size()+1];
            Object[]  newGenotypes       = new Object[partners.size()+1];
            
            origGenotypes[0] = getGenotype(individual);
            newGenotypes[0] = getGenotype(result[0]);
            
            for (int i = 0; i < partners.size(); i++) { // clone all individuals
            	origGenotypes[i+1] = getGenotype(partners.getEAIndividual(i));
            	newGenotypes[i+1] = getGenotype(result[i+1]);
                length = Math.max(length, getGenotypeLength(partners.getEAIndividual(i)));
            }
            if (TRACE) {
                        System.out.println("Before CO: " + BeanInspector.toString(newGenotypes));
                    }
            int         mixer           = RNG.randomInt(0, partners.size()); // partner index with which to exchange genes
            int[] crossoverPoints=getCrossoverPoints(length, m_NumberOfCrossovers);
            if (TRACE) {
                        System.out.println("CO points: " + BeanInspector.toString(crossoverPoints));
                    }
            for (int i = 0; i < length; i++) { // loop positions
                for (int j = 0; j < this.m_NumberOfCrossovers; j++) {
                    if (i == crossoverPoints[j]) {
                        mixer++;
                    } // possibly switch partner to exchange with
                }
                for (int j = 0; j < origGenotypes.length; j++) { // loop individuals
                	// exchange values at position i between indies 
                	exchangePos(origGenotypes, newGenotypes, (j+mixer) % origGenotypes.length, j, i);
                }
            }

            for (int i = 0; i < result.length; i++) {
            	writeBack(result[i], newGenotypes[i]);
            }
            if (TRACE) {
                        System.out.println("After CO: " + BeanInspector.toString(newGenotypes));
                    }
		}
		return result;
    }

    private void writeBack(AbstractEAIndividual indy,
			Object newGenotype) {
		if (indy instanceof InterfaceGAIndividual) {
			((InterfaceGAIndividual)indy).SetBGenotype((BitSet)newGenotype);
		} else {
			((InterfaceGIIndividual)indy).SetIGenotype((int[])newGenotype);
		}
	}

	protected void exchangePos(Object[] origGenotypes, Object[] newGenotypes, int a, int b, int position) {
    	setVal(newGenotypes[a], position, getVal(origGenotypes[b], position));
    	setVal(newGenotypes[b], position, getVal(origGenotypes[a], position));
    	
    }
    
    private void setVal(Object genotype, int position, Object val) {
		if (genotype instanceof BitSet) {
			((BitSet)genotype).set(position, (Boolean)val);
		} else {
			((int[])genotype)[position]=(Integer)val;
		}
	}

	private Object getVal(Object genotype, int position) {
		if (genotype instanceof BitSet) {
			return ((BitSet)genotype).get(position);
		} else {
			return ((int[])genotype)[position];
		}
	}

	protected int getGenotypeLength(AbstractEAIndividual individual) {
		if (individual instanceof InterfaceGAIndividual) {
			return ((InterfaceGAIndividual)individual).getGenotypeLength();
		} else {
			return((InterfaceGIIndividual)individual).getGenotypeLength();
		}
	}

//	/** This method performs crossover on multiple individuals. If the individuals do
//     * not implement InterfaceGAIndividual, then nothing will happen.
//     * @param indy1 The first individual
//     * @param partners The second individual
//     */
//    public AbstractEAIndividual[] mateOld(AbstractEAIndividual indy1, Population partners) {
//        AbstractEAIndividual[] result = null;
//        result = new AbstractEAIndividual[partners.size()+1];
//        result[0] = (AbstractEAIndividual) (indy1).clone();
//        for (int i = 0; i < partners.size(); i++) {
//            result[i+1] = (AbstractEAIndividual) ((AbstractEAIndividual)partners.get(i)).clone();
//        }
//        if (partners.size() == 0) return result;
//        //for (int i = 0; i < result.length; i++) System.out.println("Before Crossover: " +result[i].getSolutionRepresentationFor());
//        if ((indy1 instanceof InterfaceGAIndividual) && (partners.get(0) instanceof InterfaceGAIndividual)) {
//            int         length          =  ((InterfaceGAIndividual)indy1).getGenotypeLength();
//            int         mixer           = RNG.randomInt(0, partners.size());
//            int[]       crossoverPoints = null;
//            BitSet[][]  tmpBitSet       = new BitSet[2][partners.size()+1];
//
//            tmpBitSet[0][0]     = ((InterfaceGAIndividual)indy1).getBGenotype();
//            tmpBitSet[1][0]     = ((InterfaceGAIndividual)result[0]).getBGenotype();
//            for (int i = 0; i < partners.size(); i++) {
//                tmpBitSet[0][i+1] = ((InterfaceGAIndividual)partners.get(i)).getBGenotype();
//                tmpBitSet[1][i+1] = ((InterfaceGAIndividual)result[i+1]).getBGenotype();
//                length = Math.max(length, ((InterfaceGAIndividual)partners.get(i)).getGenotypeLength());
//            }
//
//            crossoverPoints=getCrossoverPoints(length, m_NumberOfCrossovers);
//
//            for (int i = 0; i < length; i++) {
//                for (int j = 0; j < this.m_NumberOfCrossovers; j++) {
//                    if (i == crossoverPoints[j]) mixer++;
//                }
//                for (int j = 0; j < tmpBitSet[0].length; j++) {
//                    //if ((mixer % tmpBitSet[0].length) != 0) {
//                        //System.out.println(""+((j + mixer) % tmpBitSet[0].length)+ " - " + (j + mixer) +" - "+(tmpBitSet[0].length));
//                        if (tmpBitSet[0][(j + mixer) % tmpBitSet[0].length].get(i)) tmpBitSet[1][j].set(i);
//                        else tmpBitSet[1][j].clear(i);
//                    //}
//                }
//            }
//
//            for (int i = 0; i < result.length; i++) ((InterfaceGAIndividual)result[i]).SetBGenotype(tmpBitSet[1][i]);
//        }
//        //in case the crossover was successful lets give the mutation operators a chance to mate the strategy parameters
//        for (int i = 0; i < result.length; i++) result[i].getMutationOperator().crossoverOnStrategyParameters(indy1, partners);
//        //for (int i = 0; i < result.length; i++) System.out.println("After Crossover: " +result[i].getSolutionRepresentationFor());
//        return result;
//    }

    /**
     * Select the crossover points within the genotype of given length.
     * @param length
     * @param numberOfCrossovers
     * @return
     */
    protected int[] getCrossoverPoints(int length, int numberOfCrossovers) {
    	int[] crossoverPoints = new int[numberOfCrossovers];
        for (int i = 0; i < numberOfCrossovers; i++) {
            crossoverPoints[i] = RNG.randomInt(0, length-1);
        }
		return crossoverPoints;
	}

	/** This method allows you to evaluate wether two crossover operators
     * are actually the same.
     * @param crossover   The other crossover operator
     */
    @Override
    public boolean equals(Object crossover) {
        if (crossover instanceof CrossoverGAGINPoint) {
            CrossoverGAGINPoint cross = (CrossoverGAGINPoint)crossover;
            if (this.m_NumberOfCrossovers != cross.m_NumberOfCrossovers) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /** This method will allow the crossover operator to be initialized depending on the
     * individual and the optimization problem. The optimization problem is to be stored
     * since it is to be called during crossover to calculate the exogene parameters for
     * the offsprings.
     * @param individual    The individual that will be mutated.
     * @param opt           The optimization problem.
     */
    @Override
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
//        this.m_OptimizationProblem = opt;
    }

    @Override
    public String getStringRepresentation() {
        return this.getName();
    }    

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "GA-GI N-Point Crossover";
    }
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This is an n-point crossover between m individuals which may be binary or integer based.";
    }

    /** This method allows you to set the number of crossovers that occur in the
     * genotype.
     * @param crossovers   The number of crossovers.
     */
    public void setNumberOfCrossovers(int crossovers) {
        if (crossovers < 0) {
            crossovers = 0;
        }
        this.m_NumberOfCrossovers = crossovers;
    }
    public int getNumberOfCrossovers() {
        return this.m_NumberOfCrossovers;
    }
    public String numberOfCrossoversTipText() {
        return "The number of crossoverpoints.";
    }
}
