package eva2.server.stat;

import eva2.tools.StringSelection;
import java.io.Serializable;
import java.util.List;
import javax.swing.JButton;

public class EvAStatisticalEvaluationParams implements Serializable {

	private StringSelection singleStats = new StringSelection(StatsOnSingleDataSetEnum.mean, StatsOnSingleDataSetEnum.getInfoStrings());
	private StringSelection twoSampledStats = new StringSelection(StatsOnTwoSampledDataEnum.tTestUneqLenEqVar, StatsOnTwoSampledDataEnum.getInfoStrings());
	private List<JButton> additionalButtons = null;

	public void setGenericAdditionalButtons(List<JButton> buts) {
		this.additionalButtons = buts;
	}
	
	public StringSelection getTwoSampledStats() {
		return twoSampledStats;
	}
	public void setTwoSampledStats(StringSelection twoSStats) {
		this.twoSampledStats = twoSStats;
	}
	public String twoSampledStatsTipText() {
		return "Statistical tests on two-sampled data";
	}
	
	public StringSelection getOneSampledStats() {
		return singleStats;
	}
	public void setOneSampledStats(StringSelection singleStats) {
		this.singleStats = singleStats;
	}
	public String oneSampledStatsTipText() {
		return "Statistical tests on one-sampled data";
	}
	
	public String getName() {
		return "Statistical evaluation parameters";
	}
	
	public String globalInfo() {
		return "Select statistical values to be calculated and tests to be performed.";
	}
	
	public List<JButton> getAdditionalButtons() {
		return additionalButtons;
	}
	
	public boolean withGenericOkButton() {
		return false;
	}
	public boolean withGenericLoadSafeButtons() {
		return false;
	}
}
