package eva2.optimization.go;

import eva2.optimization.problems.InterfaceAdditionalPopulationInformer;
import java.util.List;

public interface InterfaceNotifyOnInformers {
	/**
	 * Notify the object about informer instances.
	 */
	public void setInformers(List<InterfaceAdditionalPopulationInformer> informers);
}
