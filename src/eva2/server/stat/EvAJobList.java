package eva2.server.stat;

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
import javax.swing.JPanel;

import eva2.gui.GenericArrayEditor;
import eva2.gui.JParaPanel;
import eva2.gui.PropertySelectableList;
import eva2.server.go.InterfaceGOParameters;
import eva2.server.go.tools.FileTools;
import eva2.server.modules.AbstractGOParameters;
import eva2.server.modules.AbstractModuleAdapter;
import eva2.server.modules.GenericModuleAdapter;
import eva2.server.modules.ModuleAdapter;
import eva2.tools.Serializer;

/**
 * A selectable list of EvAJobs. Each job contains a GOParameters instance and potentially statistical data.
 * 
 * @author mkron
 *
 */
public class EvAJobList extends PropertySelectableList<EvAJob> implements Serializable, InterfaceTextListener {
	List<InterfaceTextListener> listeners = null;
	
	private ModuleAdapter module = null;

	public EvAJobList(EvAJob[] initial) {
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
	public EvAJob addJob(InterfaceGOParameters params, AbstractStatistics stats) {
		EvAJob job = new EvAJob((InterfaceGOParameters)Serializer.deepClone(params), stats);
		stats.addDataListener(job);
		addJob(job, true);
		return job;
	}
	
	private void addJob(EvAJob j, boolean selected) {
		EvAJob[] curArr = getObjects();
		EvAJob[] newArr = null;
		boolean[] newSelection = null;
		if (curArr!=null && curArr.length>0) {
			newArr = new EvAJob[curArr.length + 1];
			newSelection = new boolean[newArr.length];
			System.arraycopy(curArr, 0, newArr, 0, curArr.length);
			System.arraycopy(m_Selection, 0, newSelection, 0, curArr.length);
		} else {
			newArr = new EvAJob[1];
			newSelection = new boolean[1];
		}
		newSelection[newArr.length-1]=selected;
		newArr[newArr.length-1] = j;
		setObjects(newArr, newSelection);
	}

	/**
	 * Return the last job in the list, which is also the last one added.
	 *  
	 * @return
	 */
	public EvAJob lastJob() {
		EvAJob[] curArr = getObjects();
		if (curArr!=null && curArr.length>0) return curArr[curArr.length-1];
		else return null;
	}

	/**
	 * Return a list of the currently selected jobs.
	 * 
	 * @return
	 */
	public ArrayList<EvAJob> getSelectedJobs() {
		EvAJob[] selected = getSelectedObjects();
		ArrayList<EvAJob> l = new ArrayList<EvAJob>();
		for (EvAJob j : selected) {
			if (j!=null) l.add(j);
		}
		return l;
	}

	public boolean saveSelectedJobs(Component parentComponent) {
		EvAJob[] selected = getSelectedObjects();
		if (selected!=null && (selected.length>0)) {
			JFileChooser fc = new JFileChooser();
			fc.setName("Select a directory to save jobs to...");
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fc.showSaveDialog(parentComponent);
			if (returnVal==JFileChooser.APPROVE_OPTION) {
				File sFile = fc.getSelectedFile();
				if (sFile.exists()) {
					for (EvAJob job : selected) {
						if (job!=null) {
							if (!FileTools.saveObjectToFolder(job, sFile, false, parentComponent)) {
								System.err.println("Error on saving jobs...");
								return false;
							}
						}
					}
				} else return false; // invalid folder chosen
			} else return false; // user break
		}
		return true;
	}

	/**
	 * Search for a job in the list which has the given parameter structure assigned.
	 * This is tested by reference, so the exact same instance of InterfaceGOParameters
	 * must be known. If no matching job is found, null is returned.
	 * @param params
	 * @return
	 */
	public EvAJob getJobOf(InterfaceGOParameters params) {
		for (EvAJob job : getObjects()) {
			if (job.getGOParams()==params) return job;
		}
		return null;
	}

	/**
	 * Create a customized editor for the job list based on an array editor.
	 * 
	 * @param jobList
	 * @return
	 */
	public static PropertyEditor makeEditor(final Component parent, final EvAJobList jobList) {
		final GenericArrayEditor genericArrayEditor = new GenericArrayEditor();
    	genericArrayEditor.setWithAddButton(false);
    	genericArrayEditor.setWithSetButton(false);
    	ActionListener al=new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				System.out.println("PING!");
//				System.out.println(BeanInspector.toString(edi.getSelectedIndices()));
				EvAStatisticalEvaluation.evaluate((InterfaceTextListener)jobList, jobList.getObjects(), genericArrayEditor.getSelectedIndices(),
						(StatsOnSingleDataSetEnum[])EvAStatisticalEvaluation.statsParams.getOneSampledStats().getSelectedEnum(StatsOnSingleDataSetEnum.values()),
						(StatsOnTwoSampledDataEnum[])EvAStatisticalEvaluation.statsParams.getTwoSampledStats().getSelectedEnum(StatsOnTwoSampledDataEnum.values()));		
//				System.out.println(BeanInspector.toString(EvAStatisticalEvaluation.statsParams.getPairedStats().getSelected()));
			}
//    		public void actionPerformed(ActionEvent e) {
//    			if (statsFrame ==null) {
//    				statsFrame = new JEFrame("EvA2 Statistics Evaluation", true);
//    				JPanel tmpPan = createStatsPanel(jobList, edi);
//    				statsFrame.getContentPane().add(tmpPan);
//    			}
//    			if (!statsFrame.isVisible()) {
//    				statsFrame.pack();
//    				statsFrame.validate();
//    				statsFrame.setVisible(true);
//    			} else statsFrame.requestFocus();
//    		}
    	};
    	ActionListener sl=new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			genericArrayEditor.selectDeselectAll();			
    		}
    	};
    	ActionListener sal=new ActionListener() {
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

	private static JPanel createStatsPanel(final EvAJobList jobList, final GenericArrayEditor edi) {
		JParaPanel pan = new JParaPanel(EvAStatisticalEvaluation.statsParams, "Statistics");
		JComponent paraPan = pan.makePanel();
		JPanel tmpPan = new JPanel();
		tmpPan.add(paraPan);
		return tmpPan;
	}
		
	private static ActionListener getReuseActionListener(final Component parent, final EvAJobList jobList) {
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<EvAJob> jobs = jobList.getSelectedJobs();
				if (jobs.size()==1) {
					EvAJob job = jobs.get(0);
					AbstractGOParameters curParams = (AbstractGOParameters)((AbstractModuleAdapter)jobList.module).getGOParameters();
					curParams.setSameParams((AbstractGOParameters) job.getGOParams());
					((GenericModuleAdapter)jobList.module).setGOParameters(curParams);
					((GenericModuleAdapter)jobList.module).getStatistics().getStatisticsParameter().setMultiRuns(job.getNumRuns());
					((GenericModuleAdapter)jobList.module).getStatistics().getStatisticsParameter().setFieldSelection(job.getFieldSelection(((GenericModuleAdapter)jobList.module).getStatistics().getStatisticsParameter().getFieldSelection()));
				} else JOptionPane.showMessageDialog(parent, "Select exactly one job to reuse!", "Error", JOptionPane.ERROR_MESSAGE);
			} 
		};
		return al;
	}

	private static ActionListener getClearSelectedActionListener(final Component parent, final EvAJobList jobList) {
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<EvAJob> jobs = jobList.getSelectedJobs();
				for (EvAJob j : jobs) j.resetJob();
			}
		};
		return al;
	}

	/**
	 * Link a processor to the job list for re-scheduling jobs.
	 * @param processor
	 */
	public void setModule(ModuleAdapter mod) {
		module = mod;
	}

	public void addTextListener(InterfaceTextListener tListener) {
		if (listeners==null) listeners = new LinkedList<InterfaceTextListener>();
		if (!listeners.contains(tListener)) listeners.add(tListener);
	}
	
	public boolean removeTextListener(InterfaceTextListener tListener) {
		if (listeners!=null) {
			return listeners.remove(tListener);
		} else return false;
	}

	/*
	 * (non-Javadoc)
	 * @see eva2.server.stat.InterfaceTextListener#print(java.lang.String)
	 */
	public void print(String str) {
		if (listeners!=null) for (InterfaceTextListener lst : listeners) {
			lst.print(str);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see eva2.server.stat.InterfaceTextListener#println(java.lang.String)
	 */
	public void println(String str) {
		if (listeners!=null) for (InterfaceTextListener lst : listeners) {
			lst.println(str);
		}
	}
}
