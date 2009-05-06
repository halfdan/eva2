package eva2.gui;


import javax.swing.*;

import eva2.server.go.individuals.codings.gp.AbstractGPNode;
import eva2.server.go.individuals.codings.gp.GPArea;

import java.beans.PropertyEditor;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * TODO this should be redundant with the new GenericObjectListEditor.
 * 
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 23.03.2004
 * Time: 15:03:29
 * To change this template use File | Settings | File Templates.
 */
public class GenericStringListSelectionEditor extends AbstractListSelectionEditor {
    private PropertyStringList              m_List;

	@Override
	protected int getElementCount() {
		return m_List.getStrings().length;
	}

	@Override
	protected String getElementName(int i) {
		return m_List.getStrings()[i];
	}

	@Override
	protected boolean isElementAllowed(int i) {
		return this.m_List.getSelection()[i];
	}

	@Override
	protected void performOnAction() {
		for (int i = 0; i < this.m_BlackCheck.length; i++) {
			this.m_List.setSelectionForElement(i, this.m_BlackCheck[i].isSelected());
		}
	}

	@Override
	protected boolean setObject(Object o) {
		if (o instanceof PropertyStringList) {
            this.m_List = (PropertyStringList) o;
            return true;
        } else return false;
	}

    /** Retruns the current object.
     * @return the current object
     */
    public Object getValue() {
        return this.m_List;
    }
}