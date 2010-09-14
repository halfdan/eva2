package eva2.gui;


import javax.swing.*;

import eva2.server.go.SwingWorker;
import eva2.tools.BasicResourceLoader;

import java.beans.PropertyEditor;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Vector;
import java.util.Enumeration;


/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 14.12.2004
 * Time: 11:33:43
 * To change this template use File | Settings | File Templates.
 */

public class GenericRemoteServersEditor extends JPanel implements PropertyEditor {

    /** Handles property change notification */
    private PropertyChangeSupport   m_Support = new PropertyChangeSupport(this);
    /** The label for when we can't edit that type */
    private JLabel                  m_Label = new JLabel("Can't edit", SwingConstants.CENTER);
    /** The FilePath that is to be edited*/
    private PropertyRemoteServers   m_RemoteServers;

    /** The gaphix stuff */
    private JComponent              m_Editor;
    private JPanel                  m_ParameterPanel;
    private JTextField              m_Login;
    private JPasswordField          m_Password;
    private JPanel                  m_ServerList;
    private JButton[]                   m_Status;
    private JTextField[]                m_Names;
    private JComboBox[]                 m_CPUs;
    private JButton[]                   m_Delete;
    private int prefEditorHeight = 200;

    public GenericRemoteServersEditor() {

    }

    /** This method will init the CustomEditor Panel
     */
    private void initCustomEditor() {
    	this.m_Editor = new JPanel();
    	// This is the upper panel
    	this.m_ParameterPanel   = new JPanel();
    	GridBagConstraints gbc = new GridBagConstraints();
    	setGBC(gbc, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0, 0, 1);
    	this.m_ParameterPanel.setLayout(new GridBagLayout());
    	this.m_ParameterPanel.add(new JLabel("Login: "), gbc);
    	this.m_Login    = new JTextField(""+this.m_RemoteServers.getLogin());
    	this.m_Login.addKeyListener(loginListener);
    	setGBC(gbc, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 2, 1, 0, 100);
    	this.m_ParameterPanel.add(this.m_Login, gbc);
    	setGBC(gbc, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 3, 0, 1);
    	this.m_ParameterPanel.add(new JLabel("Password: "), gbc);
    	this.m_Password = new JPasswordField(""+this.m_RemoteServers.getPassword());
    	this.m_Password.addKeyListener(passwordListener);
    	setGBC(gbc, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 2, 4, 0, 100);
    	this.m_ParameterPanel.add(this.m_Password, gbc);

    	JButton tmpB;
    	tmpB = makeButtonWith("resources/images/Add24.gif", "add");
    	tmpB.addActionListener(addServer);
    	setGBC(gbc, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0, 1, 1);
    	this.m_ParameterPanel.add(tmpB, gbc);
    	tmpB = makeButtonWith("resources/images/Export24.gif", "Load");
    	tmpB.addActionListener(loadServers);
    	setGBC(gbc, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1, 1, 1);
    	this.m_ParameterPanel.add(tmpB, gbc);
    	tmpB = makeButtonWith("resources/images/Import24.gif", "Save");
    	tmpB.addActionListener(saveServers);
    	setGBC(gbc, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 2, 1, 1);
    	this.m_ParameterPanel.add(tmpB, gbc);
    	tmpB = makeButtonWith("resources/images/Refresh24.gif", "Update Status");
    	tmpB.addActionListener(updateServers);
    	setGBC(gbc, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 3, 1, 1);
    	this.m_ParameterPanel.add(tmpB, gbc);
    	tmpB = makeButtonWith("resources/images/Play24.gif", "Start Server");
    	tmpB.addActionListener(startServers);
    	setGBC(gbc, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 4, 1, 1);
    	this.m_ParameterPanel.add(tmpB, gbc);
    	tmpB = makeButtonWith("resources/images/Stop24.gif", "Stop Server");
    	tmpB.addActionListener(killServers);
    	setGBC(gbc, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 5, 1, 1);
    	this.m_ParameterPanel.add(tmpB, gbc);

    	this.m_ServerList       = new JPanel();
    	this.updateServerList();
    	JScrollPane scrollServer = new JScrollPane(this.m_ServerList);
    	scrollServer.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	scrollServer.setPreferredSize(new Dimension(200,prefEditorHeight));
    	
    	this.m_Editor.setLayout(new BorderLayout());
    	this.m_Editor.add(this.m_ParameterPanel, BorderLayout.NORTH);
    	this.m_Editor.add(scrollServer, BorderLayout.CENTER);
    	this.updateEditor();
    }

	private JButton makeButtonWith(String iconSrc, String title) {
		JButton tmpB;
		byte[]  bytes;
		bytes = BasicResourceLoader.instance().getBytesFromResourceLocation(iconSrc, false);
	    if (bytes!=null) tmpB = new JButton(title, new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes)));
	    else tmpB = new JButton(title);
		return tmpB;
	}

    /** This method updates the server list
     *
     */
    private void updateServerList() {
        byte[]          bytes;
        ServerNode      t;
        this.m_ServerList.removeAll();
        this.m_ServerList.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        this.m_Status   = new JButton[this.m_RemoteServers.size()];
        this.m_Names    = new JTextField[this.m_RemoteServers.size()];
        this.m_CPUs     = new JComboBox[this.m_RemoteServers.size()];
        this.m_Delete   = new JButton[this.m_RemoteServers.size()];
        String[] cups   = new String[8];
        for (int i = 0; i < cups.length; i++) cups[i] = ""+(i+1);
        // The head title
        setGBC(gbc, GridBagConstraints.WEST, GridBagConstraints.BOTH, 0, 1);
        this.m_ServerList.add(new JLabel("Status"), gbc);
        
        setGBC(gbc, GridBagConstraints.WEST, GridBagConstraints.BOTH, 1, 80);
        this.m_ServerList.add(new JLabel("Server Name"), gbc);

        setGBC(gbc, GridBagConstraints.WEST, GridBagConstraints.BOTH, 2, 10);
        this.m_ServerList.add(new JLabel("CPUs"), gbc);
        
        setGBC(gbc, GridBagConstraints.WEST, GridBagConstraints.REMAINDER, 3, 10);
        this.m_ServerList.add(new JLabel("Remove"), gbc);

        for (int i = 0; i < this.m_RemoteServers.size(); i++) {
            t = this.m_RemoteServers.get(i);
            // the status indicator
            setGBC(gbc, GridBagConstraints.WEST, GridBagConstraints.BOTH, 0, 1);
            this.m_Status[i] = new JButton(" ");
            this.m_Status[i].setEnabled(false);
            if (this.m_RemoteServers.isServerOnline(t.m_ServerName)) this.m_Status[i].setBackground(Color.GREEN);
            else this.m_Status[i].setBackground(Color.RED);
            this.m_ServerList.add(this.m_Status[i], gbc);
            // the server name
            gbc             = new GridBagConstraints();
            setGBC(gbc, GridBagConstraints.WEST, GridBagConstraints.BOTH, 1, 80);
            this.m_Names[i] = new JTextField(""+t.m_ServerName);
            this.m_Names[i].addKeyListener(serverNameListener);
            this.m_ServerList.add(this.m_Names[i], gbc);
            // the number of CPUs
            setGBC(gbc, GridBagConstraints.WEST, GridBagConstraints.BOTH, 2, 10);
            this.m_CPUs[i]  = new JComboBox(cups);
            this.m_CPUs[i].setSelectedIndex(t.m_CPUs-1);
            this.m_CPUs[i].addItemListener(cpuStateListener);
            this.m_ServerList.add(this.m_CPUs[i], gbc);
            // The delete button
            setGBC(gbc, GridBagConstraints.WEST, GridBagConstraints.REMAINDER, 3, 10);
            bytes = BasicResourceLoader.instance().getBytesFromResourceLocation("resources/images/Sub24.gif", true);
            this.m_Delete[i] = new JButton("", new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes)));
            this.m_Delete[i].addActionListener(deleteServer);
            this.m_ServerList.add(this.m_Delete[i], gbc);
        }
        String[] h = this.m_RemoteServers.getCheckedServerNodes();
//        System.out.println("My active nodes: ");
//        for (int i = 0; i < h.length; i++) {
//            System.out.println(""+h[i]);
//        }
        this.m_ServerList.repaint();
        this.m_ServerList.validate();
    }

    private void setGBC(GridBagConstraints gbc, int anchor, int fill, int gridx, int weightx) {
        gbc.anchor      = anchor;
        gbc.fill        = fill;
        gbc.gridx       = gridx;
        gbc.weightx     = weightx;
	}
    
    private void setGBC(GridBagConstraints gbc, int anchor, int fill, int gridwidth, int gridx, int gridy, int weightx) {
        setGBC(gbc, anchor, fill, gridx, weightx);
        gbc.gridwidth   = gridwidth;
        gbc.gridy       = gridy;
    }
    
	/** This action listener,...
     */
    ActionListener saveServers = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            String text = m_RemoteServers.writeToText();
            JFileChooser saver = new JFileChooser();
            int option = saver.showSaveDialog(m_Editor);
             if (option == JFileChooser.APPROVE_OPTION) {
                // now save the stuff to the file
                File file = saver.getSelectedFile();
                try {
                    BufferedWriter  OutputFile = new BufferedWriter(new OutputStreamWriter (new FileOutputStream (file.getAbsolutePath())));
                    OutputFile.write(text);
                    OutputFile.close();
                } catch (FileNotFoundException t) {
                    System.err.println("Could not open output file! Filename: " + file.getName());
                } catch (java.io.IOException t) {
                    System.err.println("Could not write to output file! Filename: " + file.getName());
                }
	        }
        }
    };


    /** This action listener,...
     */
    ActionListener killServers = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            m_RemoteServers.killServers();
            updateServerList();
        }
    };

    /** This action listener,...
     */
    ActionListener startServers = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            m_RemoteServers.startServers();
            updateServerList();
        }
    };

    /** This action listener,...
     */
    ActionListener updateServers = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            updateServerList();
        }
    };

    /** This action listener,...
     */
    ActionListener addServer = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            m_RemoteServers.addServerNode("noname-"+m_RemoteServers.size(), 1);
            updateServerList();
            updateEditor();
        }
    };

    /** This action listener,...
     */
    ActionListener deleteServer = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            for (int i = 0; i < m_Delete.length; i++) {
                if (event.getSource().equals(m_Delete[i])) m_RemoteServers.removeServerNode(m_RemoteServers.get(i).m_ServerName);
            }
            updateServerList();
            updateEditor();
        }
    };

    /** This action listener,...
     */
    ActionListener loadServers = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            String          text    = "";
            JFileChooser    reader  = new JFileChooser();
            int option = reader.showOpenDialog(m_Editor);
            if (option == JFileChooser.APPROVE_OPTION) {
                // now save the stuff to the file
                File file = reader.getSelectedFile();
                try {
                    BufferedReader inputFile = new BufferedReader(new FileReader(file.getAbsolutePath()));
                    String line;
                    while ((line = inputFile.readLine()) != null) {
                        text += line +"\n";
                    }
                    inputFile.close();
                    m_RemoteServers.readFromText(text);
//                    text = inputFile.readLine().read();
//                    OutputFile.close();
                } catch (FileNotFoundException t) {
                    System.out.println("Could not open output file! Filename: " + file.getName());
                } catch (java.io.IOException t) {
                    System.out.println("Could not write to output file! Filename: " + file.getName());
                }
	        }
            updateServerList();
        }
    };

    /** This action listener reads all values
     */
    KeyListener loginListener = new KeyListener() {
        public void keyPressed(KeyEvent event) {
        }
        public void keyTyped(KeyEvent event) {
        }
        public void keyReleased(KeyEvent event) {
            m_RemoteServers.setLogin(m_Login.getText());
        }
    };

    /** This action listener reads all values
     */
    KeyListener passwordListener = new KeyListener() {
        public void keyPressed(KeyEvent event) {
        }
        public void keyTyped(KeyEvent event) {
        }
        public void keyReleased(KeyEvent event) {
            m_RemoteServers.setPassword(m_Password.getPassword());
        }
    };

    /** This action listener reads all values
     */
    KeyListener serverNameListener = new KeyListener() {
        public void keyPressed(KeyEvent event) {
        }
        public void keyTyped(KeyEvent event) {
        }
        public void keyReleased(KeyEvent event) {
            for (int i = 0; i < m_Names.length; i++) {
                if (event.getSource().equals(m_Names[i])) m_RemoteServers.setNameFor(i, m_Names[i].getText());
            }
        }
    };

    /** This action listener adds an element to DoubleArray
     */
    ItemListener cpuStateListener = new ItemListener() {
        public void itemStateChanged(ItemEvent event) {
            for (int i = 0; i < m_CPUs.length; i++) {
                if (event.getSource().equals(m_CPUs[i])) m_RemoteServers.setCPUsFor(i, m_CPUs[i].getSelectedIndex()+1);
            }
        }
    };

    /** The object may have changed update the editor.
     */
    private void updateEditor() {
        if (this.m_Editor != null) {
            this.m_Editor.validate();
            this.m_Editor.repaint();
        }
    }


    /** This method will set the value of object that is to be edited.
     * @param o an object that must be an array.
     */
    public void setValue(Object o) {
        if (o instanceof PropertyRemoteServers) {
            this.m_RemoteServers = (PropertyRemoteServers) o;
            this.updateEditor();
        }
    }

    /** Returns the current object.
     * @return the current object
     */
    public Object getValue() {
        return this.m_RemoteServers;
    }

    public String getJavaInitializationString() {
        return "TEST";
    }

    /**
     *
     */
    public String getAsText() {
        return null;
    }

    /**
     *
     */
    public void setAsText(String text) throws IllegalArgumentException {
        throw new IllegalArgumentException(text);
    }

    /**
     *
     */
    public String[] getTags() {
        return null;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
  	  if (m_Support == null) m_Support = new PropertyChangeSupport(this);
  	  m_Support.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
  	  if (m_Support == null) m_Support = new PropertyChangeSupport(this);
  	  m_Support.removePropertyChangeListener(l);
    }

    /** This is used to hook an action listener to the ok button
     * @param a The action listener.
     */
    public void addOkListener(ActionListener a) {
        //m_OKButton.addActionListener(a);
    }

    /** This is used to remove an action listener from the ok button
     * @param a The action listener
     */
    public void removeOkListener(ActionListener a) {
        //m_OKButton.removeActionListener(a);
    }

    /** Returns true since the Object can be shown
     * @return true
     */
    public boolean isPaintable() {
        return true;
    }

    /** Paints a representation of the current classifier.
     *
     * @param gfx the graphics context to use
     * @param box the area we are allowed to paint into
     */
    public void paintValue(Graphics gfx, Rectangle box) {
        FontMetrics fm = gfx.getFontMetrics();
        int vpad = (box.height - fm.getAscent()) / 2;
        String rep = "Remote Servers";
        gfx.drawString(rep, 2, fm.getHeight() + vpad - 3  );
    }

    /** Returns true because we do support a custom editor.
    * @return true
    */
    public boolean supportsCustomEditor() {
        return true;
    }

    /** Returns the array editing component.
    * @return a value of type 'java.awt.Component'
    */
    public Component getCustomEditor() {
        if (this.m_Editor == null) this.initCustomEditor();
        return m_Editor;
    }
}