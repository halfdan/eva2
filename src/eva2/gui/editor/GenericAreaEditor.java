package eva2.gui.editor;

import eva2.optimization.individuals.codings.gp.AbstractGPNode;
import eva2.optimization.individuals.codings.gp.GPArea;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 27.06.2003
 * Time: 11:41:01
 * To change this template use Options | File Templates.
 */
public class GenericAreaEditor extends AbstractListSelectionEditor {
    /**
     * The GPArea that is to be edited
     */
    private GPArea m_AreaObject;

    public GenericAreaEditor() {
        // compiled code
    }

    @Override
    protected int getElementCount() {
        return m_AreaObject.getCompleteList().size();
    }

    @Override
    protected String getElementName(int i) {
        AbstractGPNode an = (AbstractGPNode) m_AreaObject.getCompleteList().get(i);
        return an.getName();
    }

    @Override
    protected boolean isElementSelected(int i) {
        return ((Boolean) m_AreaObject.getBlackList().get(i)).booleanValue();
    }

    @Override
    protected boolean actionOnSelect() {
        /** This method checks the current BlackList and compiles it
         * to a new ReducedList.
         */
        for (int i = 0; i < this.m_BlackCheck.length; i++) {
            this.m_AreaObject.setBlackListElement(i, this.m_BlackCheck[i].isSelected());
        }
        this.m_AreaObject.compileReducedList();
        return true;
    }

    @Override
    protected boolean setObject(Object o) {
        if (o instanceof GPArea) {
            this.m_AreaObject = (GPArea) o;
            m_AreaObject.addPropertyChangeListener(this);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Object getValue() {
        return this.m_AreaObject;
    }
}