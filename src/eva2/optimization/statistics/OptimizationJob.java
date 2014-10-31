package eva2.optimization.statistics;

import eva2.gui.BeanInspector;
import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.tools.StringSelection;

import java.io.Serializable;
import java.util.List;

/**
 * An OptimizationJob is a set of optimization parameters and potential results from the statistics class.
 * Each job has a unique ID and may have been completely finished or not. Once finished, the
 * framework should guarantee that the job is removed as a statistics listener.
 * <p/>
 * A job contains data fields of a multi-run experiment and header strings describing the data.
 */
public class OptimizationJob implements Serializable, InterfaceStatisticsListener {

    private InterfaceOptimizationParameters params = null;
    private String[] fieldHeaders = null;
    private List<Object[]> multiRunFinalObjectData = null;
    private int jobID = 0;
    private static int jobIDCounter = 0;
    private int numRuns = 0;
    private boolean lastRunIncomplete = false;
    private StateEnum state = StateEnum.idle;

    private enum StateEnum {running, idle, complete, incomplete}

    public OptimizationJob() {
        jobID = jobIDCounter;
        jobIDCounter++;
    }

    public OptimizationJob(InterfaceOptimizationParameters params, InterfaceStatistics sts) {
        this();
        this.params = params;
        if (sts instanceof AbstractStatistics) {
            fieldHeaders = ((AbstractStatistics) sts).getCurrentFieldHeaders();
        }
        sts.addDataListener(this);
    }

    /**
     * Clear the job by resetting its state and clearing all data (which will be lost!).
     * The parameters of course remain.
     */
    public void resetJob() {
        numRuns = 0;
        state = StateEnum.idle;
        lastRunIncomplete = false;
        fieldHeaders = null;
        multiRunFinalObjectData = null;
    }

    public InterfaceOptimizationParameters getParams() {
        return params;
    }

    /**
     * Set the GO parameters for this instance.
     *
     * @param params
     */
    public void setParams(InterfaceOptimizationParameters params) {
        // how should this be treated? In case the run is already finished, changing
        // the parameters will be evil, so avoid that case.
        if (state == StateEnum.complete) {
            System.err.println("Warning, ignoring changed parameters for finished job!");
        } else {
            this.params = params;
        }
    }

    public String getName() {
        if (params == null) {
            return "Invalid Job (" + jobID + ")";
        } else {
            String name = getStateTag();
            name = name + " Job (" + jobID + "), "; // +params.getName();
            name = name + params.getOptimizer().getName() + "/" + params.getProblem().getName();
            name += (", " + numRuns + " runs");
            if (fieldHeaders != null) {
                name += (", " + fieldHeaders.length + " fields");
            }
            return name;
        }
    }

    private String getStateTag() {
        String tag = null;
        switch (state) {
            case complete:
                tag = "*";
                break;
            case incomplete:
                tag = "?";
                break;
            case idle:
                tag = ".";
                break;
            case running:
                tag = "!";
                break;
        }

        tag = tag + numRuns + " ";
        return tag;
    }

    public boolean isFinishedAndComplete() {
        return (state == StateEnum.complete) && !lastRunIncomplete;
    }

    public String[] getFieldHeaders() {
        return fieldHeaders;
    }

    public List<Object[]> getJobData() {
        return multiRunFinalObjectData;
    }

    public InterfaceOptimizationParameters getOptimizationParameters() {
        return params;
    }

    @Override
    public void finalMultiRunResults(String[] header, List<Object[]> multiRunFinalObjDat) {
        fieldHeaders = header;
        multiRunFinalObjectData = multiRunFinalObjDat;
    }

    @Override
    public void notifyGenerationPerformed(String[] header,
                                          Object[] statObjects, Double[] statDoubles) {
        fieldHeaders = header;
        if (state != StateEnum.running) {
            throw new RuntimeException("Sent data to job with invalid state!");
        }
    }

    @Override
    public void notifyRunStarted(int runNumber, int plannedMultiRuns,
                                 String[] header, String[] metaInfo) {
        state = StateEnum.running;
    }

    @Override
    public void notifyRunStopped(int runsPerformed, boolean completedLastRun) {
        numRuns = runsPerformed;
        lastRunIncomplete = !completedLastRun;
    }

    @Override
    public boolean notifyMultiRunFinished(String[] header, List<Object[]> multiRunFinalObjDat) {
        fieldHeaders = header;
        multiRunFinalObjectData = multiRunFinalObjDat;
        if (lastRunIncomplete) {
            state = StateEnum.incomplete;
        } else {
            state = StateEnum.complete;
        }
        return true;
    }

    /**
     * Retrieve the index of a data field within the data lines.
     * Returns -1 if the field has not been found.
     *
     * @param field
     * @return
     */
    public int getFieldIndex(String field) {
        if (fieldHeaders != null) {
            for (int i = 0; i < fieldHeaders.length; i++) {
                if (fieldHeaders[i].equals(field)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int getNumRuns() {
        if (multiRunFinalObjectData != null) {
            return multiRunFinalObjectData.size();
        } else {
            return -1;
        }
    }

    /**
     * Retrieve a single column of double data indicated by a field name. If the field is unknown
     * or there is no double data, null is returned.
     *
     * @param field Field name
     * @return An array of values from the field provided
     */
    public double[] getDoubleDataColumn(String field) {
        int index = getFieldIndex(field);
        int numRuns = getNumRuns();
        if (index >= 0) {
            double[] data = new double[numRuns];
            for (int i = 0; i < numRuns; i++) {
                Object o = multiRunFinalObjectData.get(i)[index];
                try {
                    if ((o instanceof Double)) {
                        data[i] = (Double) o;
                    } else {
                        data[i] = Double.parseDouble(o + "");
                    }
                } catch (Exception e) {
                    return null;
                }
            }
            return data;
        } else {
            return null;
        }
    }

    /**
     * Retrieve a single column of data indicated by a field name. If the field is unknown
     * or there is no data, null is returned.
     *
     * @param field
     * @return
     */
    public Object[] getDataColumn(String field) {
        int index = getFieldIndex(field);
        if (index >= 0) {
            Object[] data = new Object[getNumRuns()];
            for (int i = 0; i < getNumRuns(); i++) {
                data[i] = multiRunFinalObjectData.get(i)[index];
            }
            return data;
        } else {
            return null;
        }
    }

    public StringSelection getFieldSelection(StringSelection curSelection) {
        StringSelection newSel = (StringSelection) curSelection.clone();

        curSelection.setAllSelectionStates(false);
        if (fieldHeaders != null) {
            for (String field : fieldHeaders) {
                curSelection.setSelected(field, true);
            }
        } else {
            System.err.println("Warning, empty field selection in job " + this);
        }
        return newSel;
    }
}