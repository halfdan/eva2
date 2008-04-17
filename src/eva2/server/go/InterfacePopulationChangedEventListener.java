package eva2.server.go;


/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.04.2003
 * Time: 18:09:47
 * To change this template use Options | File Templates.
 */
public interface InterfacePopulationChangedEventListener {

    /** This method allows an optimizer to register a change in the optimizer.
     * @param source        The source of the event.
     * @param name          Could be used to indicate the nature of the event.
     */
    public void registerPopulationStateChanged(Object source, String name);
}
