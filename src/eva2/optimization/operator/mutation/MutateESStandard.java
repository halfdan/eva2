package eva2.optimization.operator.mutation;

//package eva2.optimization.operators.mutation;
//
//import eva2.optimization.individuals.AbstractEAIndividual;
//import eva2.optimization.individuals.InterfaceDataTypeDouble;
//import eva2.optimization.individuals.InterfaceESIndividual;
//import eva2.optimization.populations.Population;
//import eva2.optimization.problems.InterfaceOptimizationProblem;
//import eva2.tools.math.RNG;
//
///**
// * Created by IntelliJ IDEA.
// * User: streiche
// * Date: 15.05.2003
// * Time: 17:04:24
// * To change this template use Options | File Templates.
// */
//public class MutateESStandard implements InterfaceMutation, java.io.Serializable  {
//    protected double      mutationStepSize = 0.1;
//    public MutateESStandard() {
//    }
//
//    public MutateESStandard(MutateESStandard d) {
//        this.mutationStepSize = d.mutationStepSize;
//    }
//
//    /** This method will enable you to clone a given mutation operator
//     * @return The clone
//     */
//    public Object clone() {
//        return new MutateESStandard(this);
//    }
//
//    /** This method allows you to evaluate wether two mutation operators
//     * are actually the same.
//     * @param mutator   The other mutation operator
//     */
//    public boolean equals(Object mutator) {
//        if (mutator instanceof MutateESStandard) {
//            MutateESStandard mut = (MutateESStandard)mutator;
//            if (this.mutationStepSize != mut.mutationStepSize) return false;
//            return true;
//        } else return false;
//    }
//
//    /** This method allows you to init the mutation operator
//     * @param individual      The individual that will be mutated.
//     * @param opt               The optimization problem.
//     */
//    public void init(AbstractEAIndividual individual, InterfaceOptimizationProblem opt) {
//
//    }
//
//    /** This method will mutate a given AbstractEAIndividual. If the individual
//     * doesn't implement InterfaceESIndividual nothing happens.
//     * @param individual    The individual that is to be mutated
//     */
//    public void mutate(AbstractEAIndividual individual) {
//        //System.out.println("Before Mutate: " +((GAIndividual)individual).getSolutionRepresentationFor());
//        if (individual instanceof InterfaceESIndividual) {
//            double[]    x       = ((InterfaceESIndividual)individual).getDGenotype();
//            double[][]  range   = ((InterfaceESIndividual)individual).getDoubleRange();
//            for (int i = 0; i < x.length; i++) {
//                x[i] += ((range[i][1] -range[i][0])/2)*RNG.gaussianDouble(this.mutationStepSize);
//                if (range[i][0] > x[i]) x[i] = range[i][0];
//                if (range[i][1] < x[i]) x[i] = range[i][1];
//            }
//            ((InterfaceESIndividual)individual).SetDGenotype(x);
//
//        }
//        //System.out.println("After Mutate:  " +((GAIndividual)individual).getSolutionRepresentationFor());
//    }
//
//    /** This method allows you to perform either crossover on the strategy parameters
//     * or to deal in some other way with the crossover event.
//     * @param indy1     The original mother
//     * @param partners  The original partners
//     */
//    public void crossoverOnStrategyParameters(AbstractEAIndividual indy1, Population partners) {
//        // nothing to do here
//    }
//
//    /** This method allows you to get a string representation of the mutation
//     * operator
//     * @return A descriptive string.
//     */
//    public String getStringRepresentation() {
//        return "ES standard mutation";
//    }
//
///**********************************************************************************************************************
// * These are for GUI
// */
//    /** This method allows the CommonJavaObjectEditorPanel to read the
//     * name to the current object.
//     * @return The name.
//     */
//    public String getName() {
//        return "ES standard mutation ";
//    }
//    /** This method returns a global info string
//     * @return description
//     */
//    public static String globalInfo() {
//        return "The standard mutation alters all elements of the double attributes with a fixed mutation step size.";
//    }
//
//    /** This method allows you to set the fixed mutation step size
//     * @param step    The new mutation step size
//     */
//    public void setMutationStepSize(double step) {
//        if (step < 0) step = 0.0000001;
//        this.mutationStepSize = step;
//    }
//    public double getMutationStepSize() {
//        return this.mutationStepSize;
//    }
//    public String mutationStepSizeTipText() {
//        return "Set the value for the fixed mutation step size.";
//    }
//}
