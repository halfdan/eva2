package eva2.optimization.operator.mutation;


import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceGAIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import java.util.BitSet;

/**
 * Shift a certain substring within the individual. The length and shift distance
 * can be predefined or will be selected at random. The shift is performed cyclic.
 * 
 * @author mkron
 *
 */
public class MutateGAShiftSubstring implements InterfaceMutation, java.io.Serializable {

	private int         m_subStringLength = 0;
    private int         m_shiftDistance   = 0;

	public MutateGAShiftSubstring() {

	}
	public MutateGAShiftSubstring(MutateGAShiftSubstring mutator) {
        this.m_subStringLength        = mutator.m_subStringLength;
        this.m_shiftDistance        = mutator.m_shiftDistance;
	}

	/** This method will enable you to clone a given mutation operator
	 * @return The clone
	 */
    @Override
	public Object clone() {
		return new MutateGAShiftSubstring(this);
	}
    
    /** This method allows you to evaluate wether two mutation operators
     * are actually the same.
     * @param mutator   The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateGAShiftSubstring) {
            MutateGAShiftSubstring mut = (MutateGAShiftSubstring)mutator;
            if (this.m_subStringLength != mut.m_subStringLength) {
                return false;
            }
            if (this.m_shiftDistance != mut.m_shiftDistance) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

	/** This method allows you to init the mutation operator
	 * @param individual      The individual that will be mutated.
     * @param opt               The optimization problem.
     */
    @Override
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {

	}

	/** 
	 * This method will mutate a given AbstractEAIndividual. If the individual
	 * doesn't implement InterfaceGAIndividual nothing happens.
	 * @param individual    The individual that is to be mutated
	 */
    @Override
	public void mutate(AbstractEAIndividual individual) {
//		System.out.println("Before Mutate: " +(individual.getStringRepresentation()));
		if (individual instanceof InterfaceGAIndividual) {
			BitSet      tmpBitSet   = (BitSet)((InterfaceGAIndividual)individual).getBGenotype().clone();
			int len = ((InterfaceGAIndividual)individual).getGenotypeLength();
//			System.out.println(tmpBitSet.cardinality());
			int[] substr = selectSubstring(tmpBitSet, len);
			int a=substr[0];
			int b=substr[1];
			int d = selectShiftDist(len);
//			System.out.println("a, b, d: " + a + " " + b + " " + d);
			for (int i = 0; i <b-a+1; i++) {
//                System.out.println("Indices: " + (len+a-d+i)%len + "<-"  + (len+a+i)%len );
                tmpBitSet.set((len+a-d+i)%len, tmpBitSet.get((len+a+i)%len));
            }
			BitSet      origBitSet   = (BitSet)((InterfaceGAIndividual)individual).getBGenotype();
			for (int i = 0; i <d; i++) {
//                System.out.println("Indices: " + (len+b-d+i+1)%len + "<-" + ((len+a-d+i)%len));
                tmpBitSet.set((len+b-d+i+1)%len, origBitSet.get((len+a-d+i)%len));
            }
//			System.out.println(tmpBitSet.cardinality());
			((InterfaceGAIndividual)individual).SetBGenotype(tmpBitSet);
		}
//		System.out.println("After Mutate: " +(individual.getStringRepresentation()));
	}

    private int selectShiftDist(int len) {
    	if (m_shiftDistance<=0) {
            return RNG.randomInt(len);
        }
    	else {
            return m_shiftDistance;
        }
	}
    
	private int[] selectSubstring(BitSet tmpBitSet, int len) {
		int[] str = new int[2];
		if (m_subStringLength<=0) {
			str[0]=RNG.randomInt(len); // TODO check for collision? Not a problem in higher dims...
			str[1]=RNG.randomInt(len);
		} else {
			str[0]=RNG.randomInt(len);
			str[1]=m_subStringLength+str[0]-1; // this may be larger than len, but its modulo-ed away in mutate
		}
		return str;
	}
	/** This method allows you to perform either crossover on the strategy parameters
     * or to deal in some other way with the crossover event.
     * @param indy1     The original mother
     * @param partners  The original partners
     */
    @Override
    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1, Population partners) {
        // nothing to do here
    }

	/** This method allows you to get a string representation of the mutation
	 * operator
	 * @return A descriptive string.
	 */
    @Override
	public String getStringRepresentation() {
		return "GA inversion mutation";
	}

/**********************************************************************************************************************
 * These are for GUI
 */
	/** This method allows the CommonJavaObjectEditorPanel to read the
	 * name to the current object.
	 * @return The name.
	 */
	public String getName() {
		return "GA shift bitstring mutation";
	}
	/** This method returns a global info string
	 * @return description
	 */
	public static String globalInfo() {
		return "This mutation operator shifts n successive along the genotype (cyclic).";
	}
	public int getSubStringLength() {
		return m_subStringLength;
	}
	public void setSubStringLength(int stringLength) {
		m_subStringLength = stringLength;
	}
	public String subStringLengthTipText() {
		return "Length of substring to shift or zero to select it at random";
	}
	public int getShiftDistance() {
		return m_shiftDistance;
	}
	public void setShiftDistance(int distance) {
		m_shiftDistance = distance;
	}
	public String shiftDistanceTipText() {
		return "Distance by which to shift (to the left) or zero to select it at random";
	}
}