package eva2.gui;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 284 $
 *            $Date: 2007-11-27 14:37:05 +0100 (Tue, 27 Nov 2007) $
 *            $Author: mkron $
 */

import eva2.optimization.OptimizationStateListener;
import eva2.optimization.modules.ModuleAdapter;
import eva2.optimization.stat.OptimizationJob;
import eva2.tools.ToolBoxGui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

/**
 * Contains the GUI elements of start and stop buttons and optionally a help
 * button.
 */
public class EvAModuleButtonPanelMaker implements OptimizationStateListener, Serializable, PanelMaker {

    private static final Logger LOGGER = Logger.getLogger(EvAModuleButtonPanelMaker.class.getName());
    private String m_Name = "undefined";
    private ModuleAdapter moduleAdapter;
    private boolean runningState;
    private JButton runButton;
    private JButton postProcessButton;
    private JButton stopButton;
    private JButton scheduleButton;
    private JButton helpButton;
    private JToolBar toolBar;
    private String helpFileName;

    /**
     *
     */
    public EvAModuleButtonPanelMaker(ModuleAdapter adapter, boolean state) {
        m_Name = "GENERAL";
        runningState = state;
        moduleAdapter = adapter;
    }

    @Override
    public JToolBar makePanel() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false);

        moduleAdapter.addOptimizationStateListener((OptimizationStateListener) (this));


        //////////////////////////////////////////////////////////////
        runButton = ToolBoxGui.createIconifiedButton("images/Play24.gif", "Start", true);
        runButton.setToolTipText("Start the current optimization run.");

        runButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                //Run Opt pressed !
                onUserStart();
            }
        });

        runButton.setEnabled(!runningState); // enabled if not running

        toolBar.add(runButton);

        stopButton = ToolBoxGui.createIconifiedButton("images/Stop24.gif", "Stop", true);
        stopButton.setToolTipText("Stop the current optimization run.");
        //stopButton.setBorderPainted(false);
        stopButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    // this means user break
                    moduleAdapter.stopOpt();
                } catch (Exception ee) {
                    LOGGER.log(Level.WARNING, "Error while stopping job.", ee);
                }
            }
        });

        stopButton.setEnabled(runningState);
        toolBar.add(stopButton);

        postProcessButton = ToolBoxGui.createIconifiedButton("images/History24.gif", "Post Process", true);
        postProcessButton.setToolTipText("Start post processing according to available parameters.");
        //postProcessButton.setBorderPainted(false);
        postProcessButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    if (!moduleAdapter.startPostProcessing()) {
                        JOptionPane.showMessageDialog(null, "Post processing seems deactivated! Check the settings.", "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ee) {
                    LOGGER.log(Level.WARNING, "Error in run", ee);
                }
            }
        });
        postProcessButton.setEnabled(runningState && moduleAdapter.hasPostProcessing());
        toolBar.add(postProcessButton);

        scheduleButton = ToolBoxGui.createIconifiedButton("images/Server24.gif", "Schedule", true);
        scheduleButton.setToolTipText("Schedule the currently configured optimization as a job.");
        //scheduleButton.setBorderPainted(false);
        scheduleButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                OptimizationJob job = moduleAdapter.scheduleJob();
                if (job == null) {
                    LOGGER.log(Level.WARNING, "There was an error on scheduling your job");
                }
            }
        });
        scheduleButton.setEnabled(true);
        toolBar.add(scheduleButton);

        makeHelpButton();

        return toolBar;
    }

    public void onUserStart() {
        try {
            moduleAdapter.startOpt();
            stopButton.setEnabled(true);
            runButton.setEnabled(false);
            postProcessButton.setEnabled(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.print("Error in run: " + ex + " : " + ex.getMessage());
        }
    }

    private void makeHelpButton() {
        if (helpFileName != null && (!helpFileName.equals(""))) {
            helpButton = new JButton("Description");
            helpButton.setToolTipText("Description of the current optimization algorithm.");
            helpButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (helpFileName != null) {
                            HtmlDemo temp = new HtmlDemo(helpFileName);
                            temp.show();
                        }
                        helpButton.setEnabled(true);
                    } catch (Exception ee) {
                        ee.printStackTrace();
                        System.out.print("Error in run: " + ee + " : " + ee.getMessage());
                    }
                }
            });
            toolBar.add(helpButton);
        }
    }

    /**
     *
     */
    @Override
    public void performedStop() {
        runButton.setEnabled(true);
        postProcessButton.setEnabled(true);
        runButton.repaint();
        stopButton.setEnabled(false);
        toolBar.repaint();
    }

    @Override
    public void performedStart(String infoString) {
    }

    @Override
    public void performedRestart(String infoString) {
    }

    @Override
    public void updateProgress(final int percent, String msg) {
    }

    /**
     *
     */
    public String getName() {
        return m_Name;
    }

    /**
     *
     */
    public void setHelperFilename(String fileName) {
        if ((fileName == null) && (fileName.equals(helpFileName))) {
            return; // both are null, do nothing
        }
        if (fileName != null) {
            if (helpFileName == null) {
                // only old is null, nothing to be removed
                helpFileName = fileName;
                makeHelpButton();
            } else {
                if (!helpFileName.equals(fileName)) {
                    toolBar.remove(helpButton);
                    helpFileName = fileName;
                    makeHelpButton();
                } //else // both are equal, do nothing				
            }
        } else { // s is null, so just remove
            toolBar.remove(helpButton);
            helpFileName = fileName;
        }
    }
}
