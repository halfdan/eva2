package eva2.server.go.operators.mutation;

import eva2.gui.GenericObjectEditor;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceGAIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.EVAERROR;
import eva2.tools.math.RNG;
import java.io.Serializable;

/**
 * Uniform mutation mutates every GA bit with a fixed probability.
 * 
 * @author mkron
 *
 */
public class MutateGAUniform implements InterfaceMutation, Serializable {
	double bitwiseProb = 0.1;
	private boolean useInvertedLength = true;
	
	public MutateGAUniform() {}
	
	public MutateGAUniform(MutateGAUniform o) {
		setBitwiseProb(o.getBitwiseProb());
	}

    @Override
	public void crossoverOnStrategyParameters(AbstractEAIndividual indy1,
			Population partners) {
		if (indy1.getMutationOperator() instanceof MutateGAUniform) {
			MutateGAUniform mute = (MutateGAUniform)indy1.getMutationOperator();
			double smallerProb, largerProb;
			smallerProb = Math.min(getBitwiseProb(), mute.getBitwiseProb());
			largerProb = Math.max(getBitwiseProb(), mute.getBitwiseProb());
			setBitwiseProb(RNG.randomDouble(smallerProb, largerProb));
		}
	}

    @Override
	public Object clone() {
		return new MutateGAUniform(this);
	}
	
	public void hideHideable() {
		setUseInvertedLength(isUseInvertedLength());
	}
	
    @Override
	public String getStringRepresentation() {
		return "Uniform GA mutation (" + getBitwiseProb() + ")";
	}

    @Override
	public void init(AbstractEAIndividual individual,
			InterfaceOptimizationProblem opt) {
		if (useInvertedLength && (individual instanceof InterfaceGAIndividual)) {
                                setBitwiseProb(1./((double)((InterfaceGAIndividual)individual).getGenotypeLength()));
                            }
	}

	/**
	 * Flip every bit with a certain probability.
	 */
    @Override
	public void mutate(AbstractEAIndividual individual) {
		if (individual instanceof InterfaceGAIndividual) {
			InterfaceGAIndividual indy = (InterfaceGAIndividual)individual;
			for (int i=0; i<indy.getGenotypeLength(); i++) {
				if (RNG.flipCoin(bitwiseProb)) {
                                indy.getBGenotype().flip(i);
                            }
			}
		} else {
                EVAERROR.errorMsgOnce("Error: Skipped mutation since " + this.getClass() + " is applicable for InterfaceGAIndividual individuals only! (pot. multiple occ.)");
            }
	}

	public double getBitwiseProb() {
		return bitwiseProb;
	}
	public void setBitwiseProb(double bitwiseProb) {
		if (bitwiseProb <0. && (bitwiseProb > 1.)) {
                System.err.println("Warning, probability should be within [0,1], given: " + bitwiseProb);
            } 
		this.bitwiseProb = bitwiseProb;
	}
	public String bitwiseProbTipText() {
		return "The probability of every bit to be flipped.";
	}

	public void setUseInvertedLength(boolean useInvertedLength) {
		this.useInvertedLength = useInvertedLength;
		GenericObjectEditor.setHideProperty(this.getClass(), "bitwiseProb", useInvertedLength);
	}
	public boolean isUseInvertedLength() {
		return useInvertedLength;
	}
	public String useInvertedLengthTipText() {
		return "Switch for using 1/l as mutation rate.";
	}
	
	public String getName() {
		return "Uniform-GA-Mutation";
	}
	
	public static String globalInfo() {
		return "Uniform mutation mutates every GA bit with a fixed probability.";
	}
}
