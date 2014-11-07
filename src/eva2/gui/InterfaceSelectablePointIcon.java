package eva2.gui;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.mocco.paretofrontviewer.InterfaceRefSolutionListener;

/**
 *
 */
public interface InterfaceSelectablePointIcon {

    /**
     * This method allows to add a selection listner to the PointIcon
     * it should need more than one listener to this abstruse event
     *
     * @param a The selection listener
     */
    public void addSelectionListener(InterfaceRefSolutionListener a);

    /**
     * This method allows to remove the selection listner to the PointIcon
     */
    public void removeSelectionListeners();

    /**
     * This method returns the selection listner to the PointIcon
     *
     * @return InterfacePointIconSelectionListener
     */
    public InterfaceRefSolutionListener getSelectionListener();

    /**
     * Of course the PointIcon needs a reference to the individual
     * otherwise it can't tell the listener what has been selected.
     *
     * @param indy
     */
    public void setEAIndividual(AbstractEAIndividual indy);

    /**
     * This method allows you to get the EAIndividual the icon stands for
     *
     * @return AbstractEAIndividual
     */
    public AbstractEAIndividual getEAIndividual();
}
