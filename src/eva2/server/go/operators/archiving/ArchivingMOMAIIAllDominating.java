package eva2.server.go.operators.archiving;

import eva2.server.go.populations.Population;

/** This class is under construction and should be able to archive
 * individuals, which actually give a set of solutions.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.02.2005
 * Time: 17:16:19
 * To change this template use File | Settings | File Templates.
 */
public class ArchivingMOMAIIAllDominating implements InterfaceArchiving, java.io.Serializable {

    protected boolean                       m_Debug = false;
    transient protected eva2.gui.Plot    m_Plot = null;
    protected int                           p = 0;

    public ArchivingMOMAIIAllDominating() {
    }

    public ArchivingMOMAIIAllDominating(ArchivingMOMAIIAllDominating a) {
    }

    /** This method allows you to make a deep clone of
     * the object
     * @return the deep clone
     */
    public Object clone() {
        return (Object) new ArchivingMOMAIIAllDominating(this);
    }

    /** This method allows you to merge to populations into an archive.
     *  This method will add elements from pop to the archive but will also
     *  remove elements from the archive if the archive target size is exceeded.
     * @param pop       The population that may add Individuals to the archive.
     */
    public void addElementsToArchive(Population pop) {

        if (pop.getArchive() == null) pop.SetArchive(new Population());

        // i guess it is much simpler get a list of all dominating elements
        // from the archive, check all pop elements to this Pareto-front
        // add the non-dominated and then remove the now dominated elements
        // first get the dominant elements
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This is a straightforward strategy, which selects all dominating Pareto-front for MOMA-II (defunc).";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "MOMA-II AllDominating";
    }

    /** This method allows you to toggle the debug mode.
     * @param b     True in case of the debug mode.
     */
    public void setDebugFront(boolean b) {
        this.m_Debug = b;
    }
    public boolean getDebugFront() {
        return this.m_Debug;
    }
    public String debugFrontTipText() {
        return "Toggles the debug mode.";
    }
}