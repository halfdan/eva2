package eva2.server.go.mocco;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import eva2.gui.PropertyBoolSelector;
import eva2.gui.PropertyPanel;
import eva2.gui.PropertyText;
import eva2.gui.PropertyValueSelector;
import eva2.server.go.MOCCOStandalone;
import eva2.server.go.tools.GeneralGOEProperty;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.10.2005
 * Time: 14:43:49
 * To change this template use File | Settings | File Templates.
 */
public abstract class MOCCOPhase implements InterfaceProcessElement {

    public MOCCOStandalone      m_Mocco;
    public volatile boolean     m_Finished = false;

    /** This method will call the init method and will go to stall
     */
    public abstract void initProcessElementParametrization();

    /** This method will wait for the parametrisation result
     * @return boolean  Result
     */
    public boolean isFinished() {
        return this.m_Finished;
    }

    /** Save the stuff to *.ser file for offline optimization
     *
     */
    ActionListener saveState2FileForOfflineOptimization = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            // @todo
        }
    };

//    /** Fetch the alternatives for a given class
//     * @param type      The class to test
//     * @return The class alternatives
//     */
//    public Vector getClassAlternatives4(String type) {
//        String          typeOptions     = EvAClient.getProperty(type);
//        Vector          classes         = new Vector();
//        StringTokenizer st              = new StringTokenizer(typeOptions, ", ");
//        while (st.hasMoreTokens()) {
//            String current = st.nextToken().trim();
//            try {
//                try {
//                    Class c = Class.forName(current);
//                    classes.addElement(current);
//                } catch (java.lang.ClassNotFoundException et) {
//                    System.out.println("Could not find class for value " + current + " for type " + type);
//                }
//            } catch (Exception ex) {
//                System.out.println("Couldn't load class with name: " + current + " for type " + type);
//                    System.out.println("ex:"+ex.getMessage());
//                ex.printStackTrace();
//            }
//        }
//        return classes;
//    }

    /** Find a proper property editor for a given GeneralGOEProperty
      * @param editor   The object to search the visualization for.
     */
    public void findViewFor(GeneralGOEProperty editor) {
        if (editor.m_Editor instanceof sun.beans.editors.BoolEditor) {
            editor.m_View = new PropertyBoolSelector(editor.m_Editor);
        } else {
            if (editor.m_Editor instanceof sun.beans.editors.DoubleEditor) {
                editor.m_View = new PropertyText(editor.m_Editor);
            } else {
                if (editor.m_Editor.isPaintable() && editor.m_Editor.supportsCustomEditor()) {
                    editor.m_View = new PropertyPanel(editor.m_Editor);
                } else {
                    if (editor.m_Editor.getTags() != null ) {
                        editor.m_View = new PropertyValueSelector(editor.m_Editor);
                    } else {
                        if (editor.m_Editor.getAsText() != null) {
                            editor.m_View = new PropertyText(editor.m_Editor);
                        } else {
                            System.out.println("Warning: Property \"" + editor.m_Name
                                 + "\" has non-displayabale editor.  Skipping.");
                        }
                    }
                }
            }
        }
    }

    /** This method makes a helptext element similar to that used in JavaEvA
     * @param help  The text to display
     * @return the helptext component
     */
    public JComponent makeHelpText(String help) {
        return this.makeInformationText("Info", help);
    }

    /** This method makes a helptext element similar to that used in JavaEvA
     * @param title  The title of the help text
     * @param help  The text to display
      * @return the helptext component
     */
    public JComponent makeInformationText(String title, String help) {
        JPanel      result  = new JPanel();
        JTextArea   jt      = new JTextArea();
	    jt.setFont(new Font("SansSerif", Font.PLAIN,12));
	    jt.setEditable(false);
	    jt.setLineWrap(true);
	    jt.setWrapStyleWord(true);
	    jt.setText(help);
        jt.setBackground(result.getBackground());
	    result.setBorder(BorderFactory.createCompoundBorder(
		    BorderFactory.createTitledBorder(title),
			BorderFactory.createEmptyBorder(0, 5, 5, 5)));
	    result.setLayout(new BorderLayout());
	    result.add(jt, BorderLayout.CENTER);
        return result;
    }
}
