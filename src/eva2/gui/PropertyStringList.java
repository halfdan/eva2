//package eva2.gui;
//
///**
// * Created by IntelliJ IDEA.
// * User: streiche
// * Date: 23.03.2004
// * Time: 15:04:05
// * To change this template use File | Settings | File Templates.
// */
//public class PropertyStringList implements java.io.Serializable {
//
//    public String[]     m_Strings;
//    public boolean[]    m_Selection;
//
//    public PropertyStringList() {
//
//    }
//    public PropertyStringList(PropertyStringList b) {
//        if (b.m_Strings != null) {
//            this.m_Strings = new String[b.m_Strings.length];
//            System.arraycopy(b.m_Strings, 0, this.m_Strings, 0, this.m_Strings.length);
//        }
//        if (b.m_Selection != null) {
//            this.m_Selection = new boolean[b.m_Selection.length];
//            System.arraycopy(b.m_Selection, 0, this.m_Selection, 0, this.m_Selection.length);
//        }
//    }
//    public Object clone() {
//        return (Object) new PropertyStringList(this);
//    }
//    public void setStrings(String[] strings) {
//        this.m_Strings = strings;
//        this.m_Selection = new boolean[this.m_Strings.length];
//    }
//    public String[] getStrings() {
//        return this.m_Strings;
//    }
//
//    public void setSelection(boolean[] selection) {
//        this.m_Selection = selection;
//    }
//    public boolean[] getSelection() {
//        return this.m_Selection;
//    }
//
//    public void setSelectionForElement(int index, boolean b) {
//        this.m_Selection[index] = b;
//    }
//}
