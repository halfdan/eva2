package eva2.optimization.statistics;

import eva2.gui.JParaPanel;
import eva2.gui.PropertySelectableList;
import eva2.gui.editor.ArrayEditor;
import eva2.optimization.AbstractOptimizationParameters;
import eva2.optimization.InterfaceOptimizationParameters;
import eva2.optimization.enums.StatisticsOnSingleDataSet;
import eva2.optimization.enums.StatisticsOnTwoSampledData;
import eva2.optimization.modules.AbstractModuleAdapter;
import eva2.optimization.modules.GenericModuleAdapter;
import eva2.optimization.modules.ModuleAdapter;
import eva2.optimization.tools.FileTools;
import eva2.tools.Serializer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A selectable list of OptimizationJobs. Each job contains a OptimizationParameters instance and potentially
 * statistical data.
 */
@eva2.util.annotation.Description(value = "Display a set of jobs consisting of a multi-run experiment.")
public class OptimizationJobList extends PropertySelectableList<OptimizationJob> implements Serializable, InterfaceTextListener {

    List<InterfaceTextListener> listeners = null;
    private ModuleAdapter module = null;

    public OptimizationJobList(OptimizationJob[] initial) {
        super(initial);
    }

    public String getName() {
        return "Job Set";
    }

    /**
     * This adds a new job to the list.
     *
     * @param params Optimization parameters
     * @param stats Statistics instance
     */
    public OptimizationJob addJob(InterfaceOptimizationParameters params, AbstractStatistics stats) {
        OptimizationJob job = new OptimizationJob((InterfaceOptimizationParameters) Serializer.deepClone(params), stats);
        stats.addDataListener(job);
        addJob(job, true);
        return job;
    }

    private void addJob(OptimizationJob j, boolean selected) {
        OptimizationJob[] curArr = getObjects();
        OptimizationJob[] newArr;
        boolean[] newSelection;
        if (curArr != null && curArr.length > 0) {
            newArr = new OptimizationJob[curArr.length + 1];
            newSelection = new boolean[newArr.length];
            System.arraycopy(curArr, 0, newArr, 0, curArr.length);
            System.arraycopy(selections, 0, newSelection, 0, curArr.length);
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
     * @return The last job added
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
     * @return A List of selected jobs
     */
    public ArrayList<OptimizationJob> getSelectedJobs() {
        OptimizationJob[] selected = getSelectedObjects();
        ArrayList<OptimizationJob> l = new ArrayList<>();
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
            if (job.getOptimizationParameters() == params) {
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
        final ArrayEditor arrayEditor = new ArrayEditor();
        arrayEditor.setWithAddButton(false);
        arrayEditor.setWithSetButton(false);
        ActionListener al = e -> StatisticalEvaluation.evaluate(jobList, jobList.getObjects(), arrayEditor.getSelectedIndices(),
                (StatisticsOnSingleDataSet[]) StatisticalEvaluation.statsParams.getOneSampledStats().getSelectedEnum(StatisticsOnSingleDataSet.values()),
                (StatisticsOnTwoSampledData[]) StatisticalEvaluation.statsParams.getTwoSampledStats().getSelectedEnum(StatisticsOnTwoSampledData.values()));
        ActionListener sl = e -> arrayEditor.selectDeselectAll();

        ActionListener sal = e -> jobList.saveSelectedJobs(arrayEditor);

        arrayEditor.addUpperActionButton("(De-)Sel. all", sl);
        arrayEditor.addUpperActionButton("Test Stats", al);

        arrayEditor.addLowerActionButton("Save selected", sal);

        arrayEditor.addPopupItem("Reuse as current settings", getReuseActionListener(parent, jobList));
        arrayEditor.setAdditionalCenterPane(createStatsPanel(jobList, arrayEditor));
        arrayEditor.setValue(jobList);

        return arrayEditor;
    }

    private static JComponent createStatsPanel(final OptimizationJobList jobList, final ArrayEditor edi) {
        JParaPanel pan = new JParaPanel(StatisticalEvaluation.statsParams, "Statistics");
        return pan.makePanel();
    }

    private static ActionListener getReuseActionListener(final Component parent, final OptimizationJobList jobList) {
        return e -> {
            List<OptimizationJob> jobs = jobList.getSelectedJobs();
            if (jobs.size() == 1) {
                OptimizationJob job = jobs.get(0);
                AbstractOptimizationParameters curParams = (AbstractOptimizationParameters) ((AbstractModuleAdapter) jobList.module).getOptimizationParameters();
                curParams.setSameParams((AbstractOptimizationParameters) job.getOptimizationParameters());
                ((GenericModuleAdapter) jobList.module).setOptimizationParameters(curParams);
                ((GenericModuleAdapter) jobList.module).getStatistics().getStatisticsParameters().setMultiRuns(job.getNumRuns());
                ((GenericModuleAdapter) jobList.module).getStatistics().getStatisticsParameters().setFieldSelection(job.getFieldSelection(((GenericModuleAdapter) jobList.module).getStatistics().getStatisticsParameters().getFieldSelection()));
            } else {
                JOptionPane.showMessageDialog(parent, "Select exactly one job to reuse!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        };
    }

    private static ActionListener getClearSelectedActionListener(final Component parent, final OptimizationJobList jobList) {
        return e -> {
            List<OptimizationJob> jobs = jobList.getSelectedJobs();
            for (OptimizationJob j : jobs) {
                j.resetJob();
            }
        };
    }

    /**
     * Link a processor to the job list for re-scheduling jobs.
     *
     * @param mod
     */
    public void setModule(ModuleAdapter mod) {
        module = mod;
    }

    public void addTextListener(InterfaceTextListener tListener) {
        if (listeners == null) {
            listeners = new LinkedList<>();
        }
        if (!listeners.contains(tListener)) {
            listeners.add(tListener);
        }
    }

    public boolean removeTextListener(InterfaceTextListener tListener) {
        return listeners != null && listeners.remove(tListener);
    }

    @Override
    public void print(String str) {
        if (listeners != null) {
            for (InterfaceTextListener lst : listeners) {
                lst.print(str);
            }
        }
    }

    @Override
    public void println(String str) {
        if (listeners != null) {
            for (InterfaceTextListener lst : listeners) {
                lst.println(str);
            }
        }
    }
}
