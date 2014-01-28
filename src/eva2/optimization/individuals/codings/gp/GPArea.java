package eva2.optimization.individuals.codings.gp;


import eva2.tools.math.RNG;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;


/**
 * This class gives the area of GPNodes for a GP problem. The area gives
 * the range of possible nodes to select from for a GP.
 */
public class GPArea implements java.io.Serializable {
    /**
     * Handles property change notification
     */
    private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private ArrayList<AbstractGPNode> completeList = new ArrayList<AbstractGPNode>();
    private ArrayList<AbstractGPNode> reducedList = new ArrayList<AbstractGPNode>();
    private ArrayList<Boolean> blackList = new ArrayList<Boolean>();

    public GPArea() {

    }

    public GPArea(GPArea g) {
        if (g.blackList != null) {
            this.blackList = (ArrayList<Boolean>) g.blackList.clone();
        }
        if (g.reducedList != null) {
            this.reducedList = (ArrayList<AbstractGPNode>) g.reducedList.clone();
        }
        if (g.completeList != null) {
            this.completeList = (ArrayList<AbstractGPNode>) g.completeList.clone();
        }
    }

    @Override
    public Object clone() {
        return new GPArea(this);
    }

    /**
     * Add a Node to the list of possible operators
     *
     * @param n The Node that is to be added
     */
    public void add2CompleteList(AbstractGPNode n) {
        this.completeList.add(n);
        this.blackList.add(new Boolean(true));
    }

    /**
     * Add a Node to the list of possible operators
     *
     * @param n The Node that is to be added
     * @param b The initial BlacklLst value
     */
    public void add2CompleteList(AbstractGPNode n, boolean b) {
        this.completeList.add(n);
        this.blackList.add(new Boolean(b));
    }

    /**
     * This method allows you to fetch the black list
     *
     * @return blacklist
     */
    public ArrayList<Boolean> getBlackList() {
        return this.blackList;
    }

    /**
     * This method allows you to set the black list
     *
     * @param a blacklist
     */
    public void SetBlackList(ArrayList<Boolean> a) {
        this.blackList = a;
    }

    /**
     * This method allows you to set a BlackList element
     *
     * @param i the index
     * @param b the boolean value
     */
    public void setBlackListElement(int i, boolean b) {
        this.blackList.set(i, new Boolean(b));
    }

    /**
     * This method allows you to fetch the CompleteList
     *
     * @return blacklist
     */
    public ArrayList<AbstractGPNode> getCompleteList() {
        return this.completeList;
    }

    /**
     * This method allows you to fetch the CompleteList
     *
     * @return blacklist
     */
    public ArrayList<AbstractGPNode> getReducedList() {
        return this.reducedList;
    }

    /**
     * This method allows you to set the CompleteList
     *
     * @param a blacklist
     */
    public void SetCompleteList(ArrayList<AbstractGPNode> a) {
        this.completeList = a;
        propertyChangeSupport.firePropertyChange("GPArea", null, this);
    }

    /**
     * This method compiles the Complete List to the allowed list using the BlackList
     */
    public void compileReducedList() {
        this.reducedList = new ArrayList<AbstractGPNode>();
        for (int i = 0; i < this.completeList.size(); i++) {
            if (((Boolean) (this.blackList.get(i))).booleanValue()) {
                this.reducedList.add(this.completeList.get(i));
            }
        }
        propertyChangeSupport.firePropertyChange("GPArea", null, this);
    }

    /**
     * This method allows you to fetch a random node of a given arity
     *
     * @param targetarity The target arity.
     */
    public AbstractGPNode getRandomNodeWithArity(int targetarity) {
        ArrayList<AbstractGPNode> tmpArray = new ArrayList<AbstractGPNode>();
        for (int i = 0; i < this.reducedList.size(); i++) {
            if (((AbstractGPNode) this.reducedList.get(i)).getArity() == targetarity) {
                tmpArray.add(this.reducedList.get(i));
            }
        }
        if (tmpArray.size() == 0) {
            return null;
        } else {
            return (AbstractGPNode) tmpArray.get(RNG.randomInt(0, tmpArray.size() - 1));
        }
    }

    /**
     * This method will return a random node.
     */
    public AbstractGPNode getRandomNode() {
        if (this.reducedList.size() == 0) {
            return null;
        } else {
            return (AbstractGPNode) this.reducedList.get(RNG.randomInt(0, this.reducedList.size() - 1));
        }
    }

    /**
     * This method will return a non terminal
     */
    public AbstractGPNode getRandomNonTerminal() {
        ArrayList<AbstractGPNode> tmpArray = new ArrayList<AbstractGPNode>();
        for (int i = 0; i < this.reducedList.size(); i++) {
            if (((AbstractGPNode) this.reducedList.get(i)).getArity() > 0) {
                tmpArray.add(this.reducedList.get(i));
            }
        }
        if (tmpArray.size() == 0) {
            return null;
        } else {
            return (AbstractGPNode) tmpArray.get(RNG.randomInt(0, tmpArray.size() - 1));
        }
    }

    public boolean isEmpty() {
        return (completeList == null) || (completeList.size() == 0);
    }

    public void clear() {
        completeList = new ArrayList<AbstractGPNode>();
        reducedList = new ArrayList<AbstractGPNode>();
        blackList = new ArrayList<Boolean>();
        propertyChangeSupport.firePropertyChange("GPArea", null, this);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     *
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        propertyChangeSupport.removePropertyChangeListener(l);
    }
}
