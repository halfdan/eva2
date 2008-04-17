package eva2.server.go.individuals.codings.gp;


import java.util.ArrayList;

import eva2.server.go.tools.RandomNumberGenerator;

/**  This class gives the area of GPNodes for a GP problem. The area gives
 * the range of possible nodes to select from for a GP.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 13.06.2003
 * Time: 18:32:26
 * To change this template use Options | File Templates.
 */
public class GPArea implements java.io.Serializable {

    private ArrayList       m_CompleteList  = new ArrayList();
    private ArrayList       m_ReducedList   = new ArrayList();
    private ArrayList       m_BlackList     = new ArrayList();

    public GPArea() {

    }

    public GPArea(GPArea g) {
        if (g.m_BlackList != null)
            this.m_BlackList    = (ArrayList)g.m_BlackList.clone();
        if (g.m_ReducedList != null)
            this.m_ReducedList  = (ArrayList)g.m_ReducedList.clone();
        if (g.m_CompleteList != null)
            this.m_CompleteList = (ArrayList)g.m_CompleteList.clone();
    }

    public Object clone() {
        return new GPArea(this);
    }

    /** Add a Node to the list of possible operators
     * @param n The Node that is to be added
     */
    public void add2CompleteList(AbstractGPNode n) {
        this.m_CompleteList.add(n);
        this.m_BlackList.add(new Boolean(true));
    }

    /** Add a Node to the list of possible operators
     * @param n The Node that is to be added
     * @param b The initial BlacklLst value
     */
    public void add2CompleteList(AbstractGPNode n, boolean b) {
        this.m_CompleteList.add(n);
        this.m_BlackList.add(new Boolean(b));
    }

    /** This method allows you to fetch the black list
     * @return blacklist
     */
    public ArrayList getBlackList() {
        return this.m_BlackList;
    }
    /** This method allows you to set the black list
     * @param a blacklist
     */
    public void SetBlackList(ArrayList a) {
        this.m_BlackList = a;
    }
    /** This method allows you to set a BlackList element
     * @param i     the index
     * @param b     the boolean value
     */
    public void setBlackListElement(int i, boolean b) {
        this.m_BlackList.set(i, new Boolean(b));
    }

    /** This method allows you to fetch the CompleteList
     * @return blacklist
     */
    public ArrayList getCompleteList() {
        return this.m_CompleteList;
    }

    /** This method allows you to fetch the CompleteList
     * @return blacklist
     */
    public ArrayList getReducedList() {
        return this.m_ReducedList;
    }

    /** This method allows you to set the CompleteList
     * @param a blacklist
     */
    public void SetCompleteList(ArrayList a) {
        this.m_CompleteList = a;
    }

    /** This method compiles the Complete List to the allowed list using the BlackList
     */
    public void compileReducedList() {
        this.m_ReducedList = new ArrayList();
        for (int i = 0; i < this.m_CompleteList.size(); i++) {
            if (((Boolean)(this.m_BlackList.get(i))).booleanValue()) {
                this.m_ReducedList.add(this.m_CompleteList.get(i));
            }
        }
    }

    /** This method allows you to fetch a random node of a given arity
     * @param targetarity       The target arity.
     */
    public AbstractGPNode getRandomNodeWithArity(int targetarity) {
        ArrayList   tmpArray = new ArrayList();
        for (int i = 0; i < this.m_ReducedList.size(); i++) {
            if (((AbstractGPNode)this.m_ReducedList.get(i)).getArity() == targetarity) tmpArray.add(this.m_ReducedList.get(i));
        }
        if (tmpArray.size() == 0) return null;
        else return (AbstractGPNode)tmpArray.get(RandomNumberGenerator.randomInt(0, tmpArray.size()-1));
    }

    /** This method will return a random node.
     */
    public AbstractGPNode getRandomNode() {
        if (this.m_ReducedList.size() == 0) return null;
        else return (AbstractGPNode)this.m_ReducedList.get(RandomNumberGenerator.randomInt(0, this.m_ReducedList.size()-1));
    }

    /** This method will return a non terminal
     */
    public AbstractGPNode getRandomNonTerminal() {
        ArrayList   tmpArray = new ArrayList();
        for (int i = 0; i < this.m_ReducedList.size(); i++) {
            if (((AbstractGPNode)this.m_ReducedList.get(i)).getArity() > 0) tmpArray.add(this.m_ReducedList.get(i));
        }
        if (tmpArray.size() == 0) return null;
        else return (AbstractGPNode)tmpArray.get(RandomNumberGenerator.randomInt(0, tmpArray.size()-1));
    }
}
