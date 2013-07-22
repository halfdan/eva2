package eva2.optimization.stat;

import eva2.gui.editor.GenericArrayEditor;
import eva2.gui.JParaPanel;
import eva2.gui.PropertySelectableList;
import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.modules.AbstractOptimizationParameters;
import eva2.optimization.tools.FileTools;
import eva2.optimization.modules.AbstractModuleAdapter;
import eva2.optimization.modules.GenericModuleAdapter;
import eva2.optimization.modules.ModuleAdapter;
import eva2.tools.Serializer;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * A selectable list of EvAJobs. Each job contains a OptimizationParameters instance and potentially
 * statistical data.
 *
 * @author mkron
 *
 */
public class OptimizationJobList extends PropertySelectableList<OptimizationJob> implements Serializable, InterfaceTextListener {

    List<InterfaceTextListener> listeners = null;
    private ModuleAdapter module = null;

    public OptimizationJobList(OptimizationJob[] initial) {
        super(initial);
    }

    public String getName() {
        return "Job Set";
    }

    public String globalInfo() {
        return "Display a set of jobs consisting of a multi-run experiment.";
    }

    /**
     * This adds a new job to the list.
     *
     * @param params
     * @param stats
     */
    public OptimizationJob addJob(InterfaceOptimizationParameters params, AbstractStatistics stats) {
        OptimizationJob job = new OptimizationJob((InterfaceOptimizationParameters) Serializer.deepClone(params), stats);
        stats.addDataListener(job);
        addJob(job, true);
        return job;
    }

    private void addJob(OptimizationJob j, boolean selected) {
        OptimizationJob[] curArr = getObjects();
        OptimizationJob[] newArr = null;
        boolean[] newSelection = null;
        if (curArr != null && curArr.length > 0) {
            newArr = new OptimizationJob[curArr.length + 1];
            newSelection = new boolean[newArr.length];
            System.arraycopy(curArr, 0, newArr, 0, curArr.length);
            System.arraycopy(m_Selection, 0, newSelection, 0, curArr.length);
        } else {
            newArr = new OptimizationJob[1];
            newSelection = new boolean[1];
        }
        newSelection[newArr.length - 1] = selected;
        newArr[newArr.length - 1] = j;
        setObjects(newArr, newSelection);
    }

    /**
     * Return the last job in the list, which is also the last one added.
     *
     * @return
     */
    public OptimizationJob lastJob() {
        OptimizationJob[] curArr = getObjects();
        if (curArr != null && curArr.length > 0) {
            return curArr[curArr.length - 1];
        } else {
            return null;
        }
    }

    /**
     * Return a list of the currently selected jobs.
     *
     * @return
     */
    public ArrayList<OptimizationJob> getSelectedJobs() {
        OptimizationJob[] selected = getSelectedObjects();
        ArrayList<OptimizationJob> l = new ArrayList<OptimizationJob>();
        for (OptimizationJob j : selected) {
            if (j != null) {
                l.add(j);
            }
        }
        return l;
    }

    public boolean saveSelectedJobs(Component parentComponent) {
        OptimizationJob[] selected = getSelectedObjects();
        if (selected != null && (selected.length > 0)) {
            JFileChooser fc = new JFileChooser();
            fc.setName("Select a directory to save jobs to...");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = fc.showSaveDialog(parentComponent);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File sFile = fc.getSelectedFile();
                if (sFile.exists()) {
                    for (OptimizationJob job : selected) {
                        if (job != null) {
                            if (!FileTools.saveObjectToFolder(job, sFile, false, parentComponent)) {
                                System.err.println("Error on saving jobs...");
                                return false;
                            }
                        }
                    }
                } else {
                    return false; // invalid folder chosen
                }
            } else {
                return false; // user break
            }
        }
        return true;
    }

    /**
     * Search for a job in the list which has the given parameter structure assigned. This is tested
     * by reference, so the exact same instance of InterfaceOptimizationParameters must be known. If no
     * matching job is found, null is returned.
     *
     * @param params
     * @return
     */
    public OptimizationJob getJobOf(InterfaceOptimizationParameters params) {
        for (OptimizationJob job : getObjects()) {
            if (job.getGOParams() == params) {
                return job;
            }
        }
        return null;
    }

    /**
     * Create a customized editor for the job list based on an array editor.
     *
     * @param jobList
     * @return
     */
    public static PropertyEditor makeEditor(final Component parent, final OptimizationJobList jobList) {
        final GenericArrayEditor genericArrayEditor = new GenericArrayEditor();
        genericArrayEditor.setWithAddButton(false);
        genericArrayEditor.setWithSetButton(false);
        ActionListener al = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                EvAStatisticalEvaluation.evaluate((InterfaceTextListener) jobList, jobList.getObjects(), genericArrayEditor.getSelectedIndices(),
                        (StatsOnSingleDataSetEnum[]) EvAStatisticalEvaluation.statsParams.getOneSampledStats().getSelectedEnum(StatsOnSingleDataSetEnum.values()),
                        (StatsOnTwoSampledDataEnum[]) EvAStatisticalEvaluation.statsParams.getTwoSampledStats().getSelectedEnum(StatsOnTwoSampledDataEnum.values()));
            }
        };
        ActionListener sl = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                genericArrayEditor.selectDeselectAll();
            }
        };
        ActionListener sal = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                jobList.saveSelectedJobs(genericArrayEditor);
            }
        };
        genericArrayEditor.addUpperActionButton("(De-)Sel. all", sl);
        genericArrayEditor.addUpperActionButton("Test Stats", al);
        genericArrayEditor.addLowerActionButton("Save selected", sal);

//    	edi.addPopupItem("Reset selected", getClearSelectedActionListener(parent, jobList)); // this option does not make much sense - instead of deleting data, taking over the settings for a new run is more plausible
        genericArrayEditor.addPopupItem("Reuse as current settings", getReuseActionListener(parent, jobList));
        genericArrayEditor.setAdditionalCenterPane(createStatsPanel(jobList, genericArrayEditor));
        genericArrayEditor.setValue(jobList);

        return genericArrayEditor;
    }

    private static JComponent createStatsPanel(final OptimizationJobList jobList, final GenericArrayEditor edi) {
        JParaPanel pan = new JParaPanel(EvAStatisticalEvaluation.statsParams, "Statistics");
        JComponent paraPan = pan.makePanel();
        return paraPan;
    }

    private static ActionListener getReuseActionListener(final Component parent, final OptimizationJobList jobList) {
        ActionListener al = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                List<OptimizationJob> jobs = jobList.getSelectedJobs();
                if (jobs.size() == 1) {
                    OptimizationJob job = jobs.get(0);
                    AbstractOptimizationParameters curParams = (AbstractOptimizationParameters) ((AbstractModuleAdapter) jobList.module).getGOParameters();
                    curParams.setSameParams((AbstractOptimizationParameters) job.getGOParams());
                    ((GenericModuleAdapter) jobList.module).setGOParameters(curParams);
                    ((GenericModuleAdapter) jobList.module).getStatistics().getStatisticsParameter().setMultiRuns(job.getNumRuns());
                    ((GenericModuleAdapter) jobList.module).getStatistics().getStatisticsParameter().setFieldSelection(job.getFieldSelection(((GenericModuleAdapter) jobList.module).getStatistics().getStatisticsParameter().getFieldSelection()));
                } else {
                    JOptionPane.showMessageDialog(parent, "Select exactly one job to reuse!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        return al;
    }

    private static ActionListener getClearSelectedActionListener(final Component parent, final OptimizationJobList jobList) {
        ActionListener al = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                List<OptimizationJob> jobs = jobList.getSelectedJobs();
                for (OptimizationJob j : jobs) {
                    j.resetJob();
                }
            }
        };
        return al;
    }

    /**
     * Link a processor to the job list for re-scheduling jobs.
     *
     * @param processor
     */
    public void setModule(ModuleAdapter mod) {
        module = mod;
    }

    public void addTextListener(InterfaceTextListener tListener) {
        if (listeners == null) {
            listeners = new LinkedList<InterfaceTextListener>();
        }
        if (!listeners.contains(tListener)) {
            listeners.add(tListener);
        }
    }

    public boolean removeTextListener(InterfaceTextListener tListener) {
        if (listeners != null) {
            return listeners.remove(tListener);
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc) @see eva2.server.stat.InterfaceTextListener#print(java.lang.String)
     */
    @Override
    public void print(String str) {
        if (listeners != null) {
            for (InterfaceTextListener lst : listeners) {
                lst.print(str);
            }
        }
    }

    /*
     * (non-Javadoc) @see eva2.server.stat.InterfaceTextListener#println(java.lang.String)
     */
    @Override
    public void println(String str) {
        if (listeners != null) {
            for (InterfaceTextListener lst : listeners) {
                lst.println(str);
            }
        }
    }
}
