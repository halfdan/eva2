package javaeva.server.go.tools;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 28.01.2004
 * Time: 13:12:40
 * To change this template use Options | File Templates.
 */
public class Test2 implements InterfaceTest, Serializable {
    private double m_Var    = 1.0;
    private boolean m_Bol   = true;

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "Test2";
    }
    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "Test2.";
    }
    public void setVar(double v) {
        this.m_Var = v;
    }
    public double getVar() {
        return this.m_Var;
    }
    public String varTipText() {
        return "Test2";
    }
    public void setBol(boolean v) {
        this.m_Bol = v;
    }
    public boolean getBol() {
        return this.m_Bol;
    }
    public String bolTipText() {
        return "Test2";
    }
}
