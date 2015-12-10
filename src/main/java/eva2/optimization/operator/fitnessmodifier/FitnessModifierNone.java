package eva2.optimization.operator.fitnessmodifier;

import eva2.optimization.population.Population;
import eva2.util.annotation.Description;

/**
 * The fitness modifier are defunct and are to be moved to
 * the selection operators...
 */
@Description("With this method the fitness remains unaltered.")
public class FitnessModifierNone implements java.io.Serializable, InterfaceFitnessModifier {

    /**
     * This method allows you to modify the fitness of the individuals
     * of a population. Note that by altering the fitness you may require
     * your problem to store the unaltered fitness somewhere else so that
     * you may still fetch it!
     */
    @Override
    public void modifyFitness(Population population) {
        // as the name might suggest this guy is pretty lazy
    }

    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    public String getName() {
        return "No Fitness Modification";
    }
}

