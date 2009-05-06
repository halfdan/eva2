package eva2.gui;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 23.03.2004
 * Time: 15:04:05
 * To change this template use File | Settings | File Templates.
 */
public class PropertySelectableList implements java.io.Serializable {

    private Object[]     m_Objects;
    private boolean[]    m_Selection;

    public PropertySelectableList() {
    }
    
    public PropertySelectableList(PropertySelectableList b) {
        if (b.m_Objects != null) {
            this.m_Objects = new Object[b.m_Objects.length];
            System.arraycopy(b.m_Objects, 0, this.m_Objects, 0, this.m_Objects.length);
        }
        if (b.m_Selection != null) {
            this.m_Selection = new boolean[b.m_Selection.length];
            System.arraycopy(b.m_Selection, 0, this.m_Selection, 0, this.m_Selection.length);
        }
    }
    
    public Object clone() {
        return (Object) new PropertySelectableList(this);
    }
    
    public void setObjects(Object[] o) {
        this.m_Objects = o;
        this.m_Selection = new boolean[o.length];
    }
    public Object[] getStrings() {
        return this.m_Objects;
    }

    public void setSelection(boolean[] selection) {
        this.m_Selection = selection;
    }
    
    public boolean[] getSelection() {
        return this.m_Selection;
    }

    public void setSelectionForElement(int index, boolean b) {
        this.m_Selection[index] = b;
    }
    
    public int size() {
    	if (m_Objects == null) return 0;
    	else return m_Objects.length;
    }
    
    public Object get(int i) {
    	return m_Objects[i];
    }
    
    public boolean isSelected(int i) {
    	return m_Selection[i];
    }

	public void clear() {
		m_Objects=null;
		m_Selection=null;
	}
}
