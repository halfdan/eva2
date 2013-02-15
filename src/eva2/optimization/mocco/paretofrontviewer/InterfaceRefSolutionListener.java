package eva2.optimization.mocco.paretofrontviewer;

import eva2.optimization.individuals.AbstractEAIndividual;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.11.2005
 * Time: 15:43:09
 * To change this template use File | Settings | File Templates.
 */
public interface InterfaceRefSolutionListener {

    /** This method will notify the listener that an
     * Individual has been selected
     * @param indy  The selected individual
     */
    public void individualSelected(AbstractEAIndividual indy);
}
