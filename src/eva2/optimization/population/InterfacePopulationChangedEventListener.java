package eva2.optimization.population;


/**
 *
 */
public interface InterfacePopulationChangedEventListener {

    /**
     * This method allows an optimizer to register a change in the optimizer.
     *
     * @param source The source of the event.
     * @param name   Could be used to indicate the nature of the event.
     */
    void registerPopulationStateChanged(Object source, String name);
}
