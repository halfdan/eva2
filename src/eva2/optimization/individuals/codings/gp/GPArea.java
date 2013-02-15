package eva2.optimization.individuals.codings.gp;


import eva2.tools.math.RNG;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;


/**  This class gives the area of GPNodes for a GP problem. The area gives
 * the range of possible nodes to select from for a GP.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 13.06.2003
 * Time: 18:32:26
 * To change this template use Options | File Templates.
 */
public class GPArea implements java.io.Serializable {
    /** Handles property change notification */
    private transient PropertyChangeSupport   m_Support = new PropertyChangeSupport(this);
    
    private ArrayList<AbstractGPNode>       m_CompleteList  = new ArrayList<AbstractGPNode>();
    private ArrayList<AbstractGPNode>       m_ReducedList   = new ArrayList<AbstractGPNode>();
    private ArrayList<Boolean>       m_BlackList     = new ArrayList<Boolean>();

    public GPArea() {

    }

    public GPArea(GPArea g) {
        if (g.m_BlackList != null) {
            this.m_BlackList    = (ArrayList<Boolean>)g.m_BlackList.clone();
        }
        if (g.m_ReducedList != null) {
            this.m_ReducedList  = (ArrayList<AbstractGPNode>)g.m_ReducedList.clone();
        }
        if (g.m_CompleteList != null) {
            this.m_CompleteList = (ArrayList<AbstractGPNode>)g.m_CompleteList.clone();
        }
    }

    @Override
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
    public ArrayList<Boolean> getBlackList() {
        return this.m_BlackList;
    }
    /** This method allows you to set the black list
     * @param a blacklist
     */
    public void SetBlackList(ArrayList<Boolean> a) {
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
    public ArrayList<AbstractGPNode> getCompleteList() {
        return this.m_CompleteList;
    }

    /** This method allows you to fetch the CompleteList
     * @return blacklist
     */
    public ArrayList<AbstractGPNode> getReducedList() {
        return this.m_ReducedList;
    }

    /** This method allows you to set the CompleteList
     * @param a blacklist
     */
    public void SetCompleteList(ArrayList<AbstractGPNode> a) {
        this.m_CompleteList = a;
	    m_Support.firePropertyChange("GPArea", null, this);
    }

    /** This method compiles the Complete List to the allowed list using the BlackList
     */
    public void compileReducedList() {
        this.m_ReducedList = new ArrayList<AbstractGPNode>();
        for (int i = 0; i < this.m_CompleteList.size(); i++) {
            if (((Boolean)(this.m_BlackList.get(i))).booleanValue()) {
                this.m_ReducedList.add(this.m_CompleteList.get(i));
            }
        }
	    m_Support.firePropertyChange("GPArea", null, this);
    }

    /** This method allows you to fetch a random node of a given arity
     * @param targetarity       The target arity.
     */
    public AbstractGPNode getRandomNodeWithArity(int targetarity) {
        ArrayList<AbstractGPNode>   tmpArray = new ArrayList<AbstractGPNode>();
        for (int i = 0; i < this.m_ReducedList.size(); i++) {
            if (((AbstractGPNode)this.m_ReducedList.get(i)).getArity() == targetarity) {
                tmpArray.add(this.m_ReducedList.get(i));
            }
        }
        if (tmpArray.size() == 0) {
            return null;
        }
        else {
            return (AbstractGPNode)tmpArray.get(RNG.randomInt(0, tmpArray.size()-1));
        }
    }

    /** This method will return a random node.
     */
    public AbstractGPNode getRandomNode() {
        if (this.m_ReducedList.size() == 0) {
            return null;
        }
        else {
            return (AbstractGPNode)this.m_ReducedList.get(RNG.randomInt(0, this.m_ReducedList.size()-1));
        }
    }

    /** This method will return a non terminal
     */
    public AbstractGPNode getRandomNonTerminal() {
        ArrayList<AbstractGPNode>   tmpArray = new ArrayList<AbstractGPNode>();
        for (int i = 0; i < this.m_ReducedList.size(); i++) {
            if (((AbstractGPNode)this.m_ReducedList.get(i)).getArity() > 0) {
                tmpArray.add(this.m_ReducedList.get(i));
            }
        }
        if (tmpArray.size() == 0) {
            return null;
        }
        else {
            return (AbstractGPNode)tmpArray.get(RNG.randomInt(0, tmpArray.size()-1));
        }
    }

	public boolean isEmpty() {
		return (m_CompleteList==null) || (m_CompleteList.size()==0);
	}
	
	public void clear() {
	    m_CompleteList  = new ArrayList<AbstractGPNode>();
	    m_ReducedList   = new ArrayList<AbstractGPNode>();
	    m_BlackList     = new ArrayList<Boolean>();
	    m_Support.firePropertyChange("GPArea", null, this);
	}
	
    public void addPropertyChangeListener(PropertyChangeListener l) {
    	if (m_Support==null) {
            m_Support = new PropertyChangeSupport(this);
        }
        m_Support.addPropertyChangeListener(l);
    }
    /**
     *
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
    	if (m_Support==null) {
            m_Support = new PropertyChangeSupport(this);
        }
        m_Support.removePropertyChangeListener(l);
    }
}
