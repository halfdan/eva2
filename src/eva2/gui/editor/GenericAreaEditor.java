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
    private GPArea areaObject;

    public GenericAreaEditor() {
        // compiled code
    }

    @Override
    protected int getElementCount() {
        return areaObject.getCompleteList().size();
    }

    @Override
    protected String getElementName(int i) {
        AbstractGPNode an = areaObject.getCompleteList().get(i);
        return an.getName();
    }

    @Override
    protected boolean isElementSelected(int i) {
        return areaObject.getBlackList().get(i).booleanValue();
    }

    @Override
    protected boolean actionOnSelect() {
        /** This method checks the current BlackList and compiles it
         * to a new ReducedList.
         */
        for (int i = 0; i < this.blackCheck.length; i++) {
            this.areaObject.setBlackListElement(i, this.blackCheck[i].isSelected());
        }
        this.areaObject.compileReducedList();
        return true;
    }

    @Override
    protected boolean setObject(Object o) {
        if (o instanceof GPArea) {
            this.areaObject = (GPArea) o;
            areaObject.addPropertyChangeListener(this);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Object getValue() {
        return this.areaObject;
    }
}