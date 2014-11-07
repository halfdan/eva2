package eva2.optimization.mocco.paretofrontviewer;

import eva2.optimization.individuals.AbstractEAIndividual;

/**
 *
 */
public interface InterfaceRefSolutionListener {

    /**
     * This method will notify the listener that an
     * Individual has been selected
     *
     * @param indy The selected individual
     */
    public void individualSelected(AbstractEAIndividual indy);
}
