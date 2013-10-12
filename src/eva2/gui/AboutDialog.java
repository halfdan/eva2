/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eva2.gui;

import eva2.tools.BasicResourceLoader;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author becker
 */
public class AboutDialog extends JDialog {
    private JLabel imageLabel;
    private JEditorPane infoEditorPane;
    private JTextArea aboutTextArea;

    public AboutDialog(Frame parent) {
        super(parent);
        initComponents();
    }

    private void initComponents() {
        setTitle("About");
        setLayout(new GridBagLayout());
        GridBagConstraints gbConstraints = new GridBagConstraints();

        setSize(new Dimension(470, 600));
        setResizable(false);
        
        /* Load EvA2 Image */
        BasicResourceLoader loader = BasicResourceLoader.instance();
        byte[] bytes = loader.getBytesFromResourceLocation(eva2.EvAInfo.splashLocation, true);
        ImageIcon imageIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes));
        
        /* Create a new JLabel with the image */
        imageLabel = new JLabel(imageIcon);

        gbConstraints.gridx = 0;
        gbConstraints.gridy = 0;
        gbConstraints.ipady = 10;
        gbConstraints.insets = new Insets(10, 10, 0, 10);
        gbConstraints.anchor = GridBagConstraints.PAGE_START;
        add(imageLabel, gbConstraints);

        String infoMessage = "<html><head></head><body>"
                + "<p>EvA2 (an Evolutionary Algorithms framework, revised version 2) is a comprehensive heuristic optimization framework with emphasis on Evolutionary Algorithms implemented in Java™.</p>"
                + "<p>For more information, please visit the <a href=\"http://www.cogsys.cs.uni-tuebingen.de/software/JavaEvA/\">EvA2 Homepage</a>.</p>"
                + "</body></html>";

        infoEditorPane = new JEditorPane("text/html", infoMessage);
        infoEditorPane.setEditable(false);
        infoEditorPane.setOpaque(false);
        infoEditorPane.addHyperlinkListener(new HyperlinkListener() {

            @Override
            public void hyperlinkUpdate(HyperlinkEvent hle) {
                if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
                    java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

                    if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                        //java.net.URI uri = new java.net.URI(hle.getURL().toString());
                        //desktop.browse(uri);
                    }
                }
            }
        });
        gbConstraints.gridy++;
        gbConstraints.anchor = GridBagConstraints.CENTER;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.weightx = 1.0;
        add(infoEditorPane, gbConstraints);

        aboutTextArea = new JTextArea();
        aboutTextArea.setEditable(false);
        aboutTextArea.setRows(8);

        gbConstraints.gridy++;
        gbConstraints.weighty = 1.0;
        gbConstraints.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(aboutTextArea), gbConstraints);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                AboutDialog.this.dispose();
            }

        });
        gbConstraints.gridy++;
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.weighty = 0.0;
        gbConstraints.insets = new Insets(10, 10, 10, 10);
        add(closeButton, gbConstraints);
    }
}
