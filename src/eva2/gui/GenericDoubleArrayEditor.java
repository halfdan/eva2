package eva2.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * A simple focus listener with an object ID and callback.
 * 
 * @author mkron
 *
 */
class MyFocusListener implements FocusListener {
	private int myID = -1;
	private GenericDoubleArrayEditor arrEditor = null;
	
	public MyFocusListener(int id, GenericDoubleArrayEditor gdae) {
		myID = id;
		this.arrEditor = gdae;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
    @Override
	public void focusLost(FocusEvent e) { }
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
    @Override
	public void focusGained(FocusEvent e) { arrEditor.notifyFocusID(myID);};
};


/**
 * A generic editor for PropertyDoubleArray.
 */
public class GenericDoubleArrayEditor extends JPanel implements PropertyEditor {

	private static final long serialVersionUID = 7749892624600018812L;
	/** Handles property change notification */
    private PropertyChangeSupport   m_Support = new PropertyChangeSupport(this);
    /** The label for when we can't edit that type */
    private JLabel                  m_Label = new JLabel("Can't edit", SwingConstants.CENTER);
    /** The FilePath that is to be edited*/
    private PropertyDoubleArray             m_DoubleArray;

    /** The gaphix stuff */
    private JPanel                  m_CustomEditor, m_DataPanel, m_ButtonPanel;
    private JTextField[][]            m_InputTextFields;
    private JButton                 m_OKButton, m_AddButton, m_DeleteButton, m_NormalizeButton;
    
    /** Which columns has the focus? **/
    private int lastFocussedRow = -1;    
    public GenericDoubleArrayEditor() {
        // compiled code
    }

    /** This method will init the CustomEditor Panel
     */
    private void initCustomEditor() {
        this.m_CustomEditor     = new JPanel();
        this.m_CustomEditor.setLayout(new BorderLayout());

        this.m_CustomEditor.add(new JLabel("Current Double Array:"), BorderLayout.NORTH);

        // init data panel
        this.m_DataPanel = new JPanel();
        this.updateDataPanel();
        this.m_CustomEditor.add(this.m_DataPanel, BorderLayout.CENTER);

        // init button panel
        this.m_ButtonPanel = new JPanel();
        this.m_AddButton = new JButton("Add");
        this.m_AddButton.addActionListener(this.addAction);
        this.m_DeleteButton = new JButton("Delete");
        this.m_DeleteButton.addActionListener(this.deleteAction);
        this.m_NormalizeButton = new JButton("Normalize");
        this.m_NormalizeButton.addActionListener(this.normalizeAction);
        this.m_OKButton         = new JButton("OK");
        this.m_OKButton.setEnabled(true);
        this.m_OKButton.addActionListener(new ActionListener() {
            @Override
	        public void actionPerformed(ActionEvent e) {
	            //m_Backup = copyObject(m_Object);
	            if ((m_CustomEditor.getTopLevelAncestor() != null) && (m_CustomEditor.getTopLevelAncestor() instanceof Window)) {
	                Window w = (Window) m_CustomEditor.getTopLevelAncestor();
	                w.dispose();
	            }
	        }
        });
        this.m_ButtonPanel.add(this.m_AddButton);
        this.m_ButtonPanel.add(this.m_DeleteButton);
        this.m_ButtonPanel.add(this.m_NormalizeButton);
        this.m_ButtonPanel.add(this.m_OKButton);
        this.m_CustomEditor.add(this.m_ButtonPanel, BorderLayout.SOUTH);
        this.updateEditor();
    }

    /** This action listener adds an element to DoubleArray
     */
    ActionListener addAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
        	m_DoubleArray.addRowCopy(lastFocussedRow); // copy the last focussed row
            updateEditor();
        }
    };
    
    /** This action listener removes an element from the DoubleArray.
     */
    ActionListener deleteAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
        	if (!m_DoubleArray.isValidRow(lastFocussedRow)) {
        		m_DoubleArray.deleteRow(m_DoubleArray.getNumRows()-1);
        	} else  {
            	m_DoubleArray.deleteRow(lastFocussedRow);
            } 
            updateEditor();
        }
    };
    
    /** 
     * This action listener nomalizes each columng of the values of the DoubleArray.
     */
    ActionListener normalizeAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
        	m_DoubleArray.normalizeColumns();
            updateEditor();
        }
    };
    
    /** This action listener reads all values
     */
    KeyListener readDoubleArrayAction = new KeyListener() {
        @Override
    	public void keyPressed(KeyEvent event) {
        }
        @Override
        public void keyTyped(KeyEvent event) {
        }

        @Override
        public void keyReleased(KeyEvent event) {
            double[][]    tmpDD    = new double[m_InputTextFields.length][m_InputTextFields[0].length];

            for (int i = 0; i < tmpDD.length; i++) {
            	for (int j=0; j< tmpDD[0].length; j++) {
            		try {
            			double d = 0;
            			d = new Double(m_InputTextFields[i][j].getText()).doubleValue();
            			tmpDD[i][j] = d;
            		} catch (Exception e) {
            		}
            	}
                //tmpD[i] = new Double(m_InputTextField[i].getText()).doubleValue();
            }

            m_DoubleArray.setDoubleArray(tmpDD);
            //updateEditor();
         }
    };


	/** The object may have changed update the editor.
     */
    private void updateEditor() {
        if (this.m_CustomEditor != null) {
            this.updateDataPanel();
            this.m_CustomEditor.validate();
            this.m_CustomEditor.repaint();
        }
    }

//    /** This method updates the data panel
//     */
//    private void updateDataPanelOrig() {
//        double[] tmpD = this.m_DoubleArray.getDoubleArray();
//
//        this.m_DataPanel.removeAll();
//        this.m_DataPanel.setLayout(new GridLayout(tmpD.length, 2));
//        this.m_InputTextField = new JTextField[tmpD.length];
//        for (int i = 0; i < tmpD.length; i++) {
//            JLabel label = new JLabel("Value X"+i+": ");
//            this.m_DataPanel.add(label);
//            this.m_InputTextField[i]   = new JTextField();
//            this.m_InputTextField[i].setText(""+tmpD[i]);
//            this.m_InputTextField[i].addKeyListener(this.readDoubleArrayAction);
//            this.m_DataPanel.add(this.m_InputTextField[i]);
//        }
//    }

    /** This method updates the data panel
     */
    private void updateDataPanel() {
    	int numRows = m_DoubleArray.getNumRows();
    	int numCols = m_DoubleArray.getNumCols();
        this.m_DataPanel.removeAll();
        this.m_DataPanel.setLayout(new GridLayout(numRows, numCols+1));
        this.m_InputTextFields = new JTextField[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            JLabel label = new JLabel("Value X"+i+": ");
            this.m_DataPanel.add(label);
            for (int j=0; j<numCols; j++) {
            	this.m_InputTextFields[i][j]   = new JTextField();
            	this.m_InputTextFields[i][j].setText(""+m_DoubleArray.getValue(i,j));
            	this.m_InputTextFields[i][j].addKeyListener(this.readDoubleArrayAction);
            	this.m_InputTextFields[i][j].addFocusListener(new MyFocusListener(i, this));
            	this.m_DataPanel.add(this.m_InputTextFields[i][j]);
            }
        }
    }
    
    public void notifyFocusID(int id) {
    	// notification of which column has the focus
    	lastFocussedRow =id;
//    	System.out.println("Focus now on " + id);
    }

    /** This method will set the value of object that is to be edited.
     * @param o an object that must be an array.
     */
    @Override
    public void setValue(Object o) {
        if (o instanceof PropertyDoubleArray) {
            this.m_DoubleArray = (PropertyDoubleArray) o;
            this.updateEditor();
        }
    }

    /** Returns the current object.
     * @return the current object
     */
    @Override
    public Object getValue() {
        return this.m_DoubleArray;
    }

    @Override
    public String getJavaInitializationString() {
        return "TEST";
    }

    /**
     *
     */
    @Override
    public String getAsText() {
        return null;
    }

    /**
     *
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        throw new IllegalArgumentException(text);
    }

    /**
     *
     */
    @Override
    public String[] getTags() {
        return null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
  	  if (m_Support == null) {
            m_Support = new PropertyChangeSupport(this);
        }
  	  m_Support.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
  	  if (m_Support == null) {
            m_Support = new PropertyChangeSupport(this);
        }
  	  m_Support.removePropertyChangeListener(l);
    }

    /** This is used to hook an action listener to the ok button
     * @param a The action listener.
     */
    public void addOkListener(ActionListener a) {
        m_OKButton.addActionListener(a);
    }

    /** This is used to remove an action listener from the ok button
     * @param a The action listener
     */
    public void removeOkListener(ActionListener a) {
        m_OKButton.removeActionListener(a);
    }

    /** Returns true since the Object can be shown
     * @return true
     */
    @Override
    public boolean isPaintable() {
        return true;
    }

    /** Paints a representation of the current classifier.
     *
     * @param gfx the graphics context to use
     * @param box the area we are allowed to paint into
     */
    @Override
    public void paintValue(Graphics gfx, Rectangle box) {
        FontMetrics fm = gfx.getFontMetrics();
        int vpad = (box.height - fm.getAscent()) / 2;
        String rep = "Edit double array...";
        gfx.drawString(rep, 2, fm.getHeight() + vpad - 3  );
    }

    /** Returns true because we do support a custom editor.
    * @return true
    */
    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    /** Returns the array editing component.
    * @return a value of type 'java.awt.Component'
    */
    @Override
    public Component getCustomEditor() {
        if (this.m_CustomEditor == null) {
            this.initCustomEditor();
        }
        return m_CustomEditor;
    }
}