package eva2.optimization.modules;


import eva2.gui.EvAModuleButtonPanelMaker;
import eva2.gui.EvATabbedFrameMaker;
import eva2.gui.JParaPanel;
import eva2.gui.editor.GenericObjectEditor;
import eva2.optimization.InterfaceOptimizationParameters;
import eva2.optimization.Processor;
import eva2.optimization.statistics.*;

import java.io.Serializable;

public class GenericModuleAdapter extends AbstractModuleAdapter implements Serializable {

    private AbstractStatistics statisticsModule;
    private OptimizationJobList jobList = null;
    public String helperFilename;
    JParaPanel jobPanel = null, paramPanel = null;

    /**
     * Constructor of the ModuleAdapter
     *
     * @param adapterName     The AdapterName
     * @param helperFName     Name of a HTML help file name
     * @param params          A parameter set describing the optimizer module
     * @param optimizerExpert Set to true if setting the optimizer is an expert option being hidden
     *                        from the gui
     * @param noGUIStatOut    If null, statistics with GUI are used, else the standalone statistics
     *                        with given output filename.
     */
    public GenericModuleAdapter(String adapterName, String helperFName, InterfaceOptimizationParameters params, boolean optimizerExpert, String noGUIStatOut) {
        remoteModuleAdapter = this;
        this.adapterName = adapterName;
        helperFilename = helperFName;

        if (noGUIStatOut == null) {
            statisticsModule = new StatisticsWithGUI();
        } else {
            statisticsModule = new StatisticsStandalone(noGUIStatOut);
        }
        processor = new Processor(statisticsModule, params);
        processor.addListener(this);

        // this prevents the optimizer property to be shown by the GOE if optimizerExpert is true
        GenericObjectEditor.setExpertProperty(params.getClass(), "optimizer", optimizerExpert);

        ((Processor) processor).start();
    }

    /**
     * Constructor of the ModuleAdapter. Convenience constructor with GUI.
     *
     * @param adapterName     The AdapterName
     * @param helperFName     name of a html help file name
     * @param params          a parameter set describing the optimizer module
     * @param optimizerExpert set to true if setting the optimizer is an expert option being hidden from the gui
     */
    public GenericModuleAdapter(String adapterName, String helperFName, InterfaceOptimizationParameters params, boolean optimizerExpert) {
        this(adapterName, helperFName, params, optimizerExpert, null);
    }

    /**
     * This method returns a newly created GUI element containing the EvA button panel
     * and two JParaPanel instances representing the module parameters (e.g. GO)
     * and the statistics parameters.
     * If the statistics are not of type StatisticsWithGUI, it is assumed that no GUI is
     * desired (and this method should not be called).
     *
     * @return the EvATabbedFrameMaker
     * @see JParaPanel
     * @see EvAModuleButtonPanelMaker
     * @see StatisticsWithGUI
     */
    @Override
    public EvATabbedFrameMaker getModuleFrame() {
        if (!(statisticsModule instanceof StatisticsWithGUI)) {
            System.err.println("Error: Unable to create Frame when started with noGUI option (GenericModuleAdapter)!");
            return null;
        }
        EvATabbedFrameMaker frmMkr = new EvATabbedFrameMaker();

        InterfaceStatisticsParameters Stat = statisticsModule.getStatisticsParameters();
        EvAModuleButtonPanelMaker buttonPanel = new EvAModuleButtonPanelMaker(remoteModuleAdapter, ((Processor) processor).isOptimizationRunning());
        buttonPanel.setHelperFilename(helperFilename);
        frmMkr.addPanelMaker(buttonPanel);
        InterfaceOptimizationParameters optimizationParameters = ((Processor) processor).getOptimizationParameters();

        frmMkr.addPanelMaker(paramPanel = new JParaPanel(optimizationParameters, optimizationParameters.getName()));

        frmMkr.addPanelMaker(new JParaPanel(Stat, Stat.getName()));

        jobList = new OptimizationJobList(new OptimizationJob[]{});
        jobList.setModule(this);
        jobList.addTextListener((AbstractStatistics) ((Processor) processor).getStatistics());


        jobPanel = new JParaPanel(jobList, jobList.getName());

        frmMkr.addPanelMaker(jobPanel);

        ((Processor) processor).getOptimizationParameters().addInformableInstance(frmMkr);
        return frmMkr;
    }

    @Override
    public void performedStart(String infoString) {
        super.performedStart(infoString);
        OptimizationJob job = scheduleJob();
        ((Processor) processor).getStatistics().addDataListener(job);
    }

    @Override
    public void performedStop() {
        super.performedStop();
        jobPanel.getEditor().getCustomEditor().repaint();
    }

    public static String getName() {
        return null;
    }

    /**
     * Return the statistics module instance of this module.
     *
     * @return
     */
    public AbstractStatistics getStatistics() {
        return statisticsModule;
    }

    @Override
    public OptimizationJob scheduleJob() {
        OptimizationJob job = jobList.addJob(((Processor) processor).getOptimizationParameters(), (AbstractStatistics) (((Processor) processor).getStatistics()));
        jobPanel.getEditor().setValue(jobList);
        return job;
    }

    @Override
    public void setOptimizationParameters(InterfaceOptimizationParameters goParams) {
        super.setOptimizationParameters(goParams);
        paramPanel.getEditor().setValue(goParams);
    }
}
