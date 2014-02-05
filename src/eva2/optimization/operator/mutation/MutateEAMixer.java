package eva2.optimization.operator.mutation;

import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;

import java.util.ArrayList;


/**
 *
 */
public class MutateEAMixer implements InterfaceMutation, java.io.Serializable {

    private PropertyMutationMixer mutationMixer;
    private boolean useSelfAdaption = false;
    protected double tau1 = 0.15;
    protected double lowerLimitChance = 0.05;

    public MutateEAMixer() {
        InterfaceMutation[] tmpList;
        ArrayList<String> mutators = GenericObjectEditor.getClassesFromProperties(InterfaceMutation.class.getCanonicalName(), null);
        tmpList = new InterfaceMutation[mutators.size()];
        for (int i = 0; i < mutators.size(); i++) {
            if (((String) mutators.get(i)).equals(this.getClass().getName())) {
                continue;
            }
            try {
                tmpList[i] = (InterfaceMutation) Class.forName((String) mutators.get(i)).newInstance();
            } catch (java.lang.ClassNotFoundException e) {
                System.out.println("Could not find class for " + (String) mutators.get(i));
            } catch (java.lang.InstantiationException k) {
                System.out.println("Instantiation exception for " + (String) mutators.get(i));
            } catch (java.lang.IllegalAccessException a) {
                System.out.println("Illegal access exception for " + (String) mutators.get(i));
            }
        }
        this.mutationMixer = new PropertyMutationMixer(tmpList, false);
        tmpList = new InterfaceMutation[2];
        tmpList[0] = new MutateGINominal();
        tmpList[1] = new MutateGIOrdinal();
        this.mutationMixer.setSelectedMutators(tmpList);
        this.mutationMixer.normalizeWeights();
        this.mutationMixer.setDescriptiveString("Combining alternative mutation operators, please norm the weights!");
        this.mutationMixer.setWeightsLabel("Weigths");

    }

    /**
     * Create a mutation mixer with equal weights of the given mutation operators.
     *
     * @param mutators
     */
    public MutateEAMixer(InterfaceMutation... mutators) {
        this.mutationMixer = new PropertyMutationMixer(mutators, true);
    }

    public MutateEAMixer(InterfaceMutation m1, InterfaceMutation m2) {
        this(new InterfaceMutation[]{m1, m2});
    }

    public MutateEAMixer(InterfaceMutation m1, InterfaceMutation m2, InterfaceMutation m3) {
        this(new InterfaceMutation[]{m1, m2, m3});
    }

    public MutateEAMixer(MutateEAMixer mutator) {
        this.mutationMixer = (PropertyMutationMixer) mutator.mutationMixer.clone();
        this.useSelfAdaption = mutator.useSelfAdaption;
        this.tau1 = mutator.tau1;
        this.lowerLimitChance = mutator.lowerLimitChance;
    }

    /**
     * This method will enable you to clone a given mutation operator
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        return new MutateEAMixer(this);
    }

    /**
     * This method allows you to evaluate wether two mutation operators
     * are actually the same.
     *
     * @param mutator The other mutation operator
     */
    @Override
    public boolean equals(Object mutator) {
        if (mutator instanceof MutateEAMixer) {
            MutateEAMixer mut = (MutateEAMixer) mutator;

            return true;
        } else {
            return false;
        }
    }

    /**
     * This method allows you to init the mutation operator
     *
     * @param individual The individual that will be mutated.
     * @param opt        The optimization problem.
     */
    @Override
    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
        InterfaceMutation[] mutators = this.mutationMixer.getSelectedMutators();
        for (int i = 0; i < mutators.length; i++) {
            mutators[i].init(individual, opt);
        }
    }

    /**
     * This method will mutate a given AbstractEAIndividual. If the individual
     * doesn't implement InterfaceGAIndividual nothing happens.
     *
     * @param individual The individual that is to be mutated
     */
    @Override
    public void mutate(AbstractEAIndividual individual) {
        this.mutationMixer.normalizeWeights();
        double[] probs = this.mutationMixer.getWeights();
        if (this.useSelfAdaption) {
            for (int i = 0; i < probs.length; i++) {
                probs[i] *= Math.exp(this.tau1 * RNG.gaussianDouble(1));
                if (probs[i] <= this.lowerLimitChance) {
                    probs[i] = this.lowerLimitChance;
                }
                if (probs[i] >= 1) {
                    probs[i] = 1;
                }
            }
            this.mutationMixer.normalizeWeights();
        }

        InterfaceMutation[] mutators = this.mutationMixer.getSelectedMutators();
        double pointer = RNG.randomFloat(0, 1);
        double dum = probs[0];
        int index = 0;
        while ((pointer > dum) && (index < probs.length - 1)) {
            index++;
            dum += probs[index];
        }
        if (index == probs.length) {
            index = RNG.randomInt(0, probs.length - 1);
        }
        //System.out.println("Using : " + mutators[index].getStringRepresentation());
//        for (int i = 0; i < probs.length; i++) {
//            System.out.println(""+mutators[i].getStringRepresentation()+" : "+ probs[i]);
//        }
//        System.out.println("");
        mutators[index].mutate(individual);
    }

    /**
     * This method allows you to perform either crossover on the strategy parameters
     * or to deal in some other way with the crossover event.
     *
     * @param indy1    The original mother
     * @param partners The original partners
     */
    @Override
    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1, Population partners) {
        for (int i = 0; i < this.mutationMixer.getSelectedMutators().length; i++) {
            this.mutationMixer.getSelectedMutators()[i].crossoverOnStrategyParameters(indy1, partners);
        }
    }

    /**
     * This method allows you to get a string representation of the mutation
     * operator
     *
     * @return A descriptive string.
     */
    @Override
    public String getStringRepresentation() {
        return "EA mutation mixer";
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
    public String getName() {
        return "EA mutation mixer";
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "This meta-mutation operator allows you to combine multiple alternative mutation operators.";
    }

    /**
     * Choose the set of mutators.
     *
     * @param d The mutation operators.
     */
    public void setMutators(PropertyMutationMixer d) {
        this.mutationMixer = d;
    }

    public PropertyMutationMixer getMutators() {
        return this.mutationMixer;
    }

    public String mutatorsTipText() {
        return "Choose the set of mutators.";
    }

    /**
     * Set the lower limit for the mutation step size with this method.
     *
     * @param d The mutation operator.
     */
    public void setUseSelfAdaption(boolean d) {
        this.useSelfAdaption = d;
    }

    public boolean getUseSelfAdaption() {
        return this.useSelfAdaption;
    }

    public String useSelfAdaptionTipText() {
        return "Use my implementation of self-adaption for the mutation mixer.";
    }

    /**
     * Set the lower limit for the mutation step size with this method.
     *
     * @param d The mutation operator.
     */
    public void setLowerLimitChance(double d) {
        if (d < 0) {
            d = 0;
        }
        this.lowerLimitChance = d;
    }

    public double getLowerLimitChance() {
        return this.lowerLimitChance;
    }

    public String lowerLimitChanceTipText() {
        return "Set the lower limit for the mutation chance.";
    }

    /**
     * Set the value for tau1 with this method.
     *
     * @param d The mutation operator.
     */
    public void setTau1(double d) {
        if (d < 0) {
            d = 0;
        }
        this.tau1 = d;
    }

    public double getTau1() {
        return this.tau1;
    }

    public String tau1TipText() {
        return "Set the value for tau1.";
    }
}
