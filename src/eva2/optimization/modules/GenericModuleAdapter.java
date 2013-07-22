package eva2.optimization.modules;


import eva2.gui.EvAModuleButtonPanelMaker;
import eva2.gui.EvATabbedFrameMaker;
import eva2.gui.editor.GenericObjectEditor;
import eva2.gui.JParaPanel;
import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.stat.*;
import eva2.optimization.stat.OptimizationJob;

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
        processor = new Processor(statisticsModule, this, params);

        // the statistics want to be informed if the strategy or the optimizer (which provide statistical data as InterfaceAdditionalInformer) change.
        // THIS is now done directly in the constructor of a Processor
//        if (m_StatisticsModul.getStatisticsParameter() instanceof InterfaceNotifyOnInformers) 
//        	params.addInformableInstance((InterfaceNotifyOnInformers)m_StatisticsModul.getStatisticsParameter());
        // this prevents the optimizer property to be shown by the GOE if optimizerExpert is true
        GenericObjectEditor.setExpertProperty(params.getClass(), "optimizer", optimizerExpert);

        ((Processor) processor).start();
    }

    /**
     * Constructor of the ModuleAdapter. Convenience constructor with GUI.
     *
     * @param adapterName     The AdapterName
     * @param helperFName     name of a html help file name
     * @param Client          The client to serve
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
            System.err.println("Error: Unable to create Frame when startet with noGUI option (GenericModuleAdapter)!");
            return null;
        }
        EvATabbedFrameMaker frmMkr = new EvATabbedFrameMaker();

        InterfaceStatisticsParameter Stat = ((StatisticsWithGUI) statisticsModule).getStatisticsParameter();
        EvAModuleButtonPanelMaker ButtonPanel = new EvAModuleButtonPanelMaker(remoteModuleAdapter, ((Processor) processor).isOptRunning());
        ButtonPanel.setHelperFilename(helperFilename);
        frmMkr.addPanelMaker(ButtonPanel);
        InterfaceOptimizationParameters goParams = ((Processor) processor).getGOParams();

        frmMkr.addPanelMaker(paramPanel = new JParaPanel(goParams, goParams.getName()));

        frmMkr.addPanelMaker(new JParaPanel(Stat, Stat.getName()));

        jobList = new OptimizationJobList(new OptimizationJob[]{});
        jobList.setModule(this);
        jobList.addTextListener((AbstractStatistics) ((Processor) processor).getStatistics());


        jobPanel = new JParaPanel(jobList, jobList.getName());

        frmMkr.addPanelMaker(jobPanel);

        ((Processor) processor).getGOParams().addInformableInstance(frmMkr);
        return frmMkr;
    }

    @Override
    public void performedStart(String infoString) {
        super.performedStart(infoString);
        OptimizationJob job = scheduleJob();
        ((AbstractStatistics) (((Processor) processor).getStatistics())).addDataListener(job);
    }

    @Override
    public void performedStop() {
        super.performedStop();
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
        OptimizationJob job = jobList.addJob(((Processor) processor).getGOParams(), (AbstractStatistics) (((Processor) processor).getStatistics()));
        jobPanel.getEditor().setValue(jobList);
        return job;
    }

    @Override
    public void setGOParameters(InterfaceOptimizationParameters goParams) {
        super.setGOParameters(goParams);
        paramPanel.getEditor().setValue(goParams);
    }
}
