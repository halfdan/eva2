package eva2.server.go;

import eva2.server.go.problems.InterfaceAdditionalPopulationInformer;
import java.util.List;

public interface InterfaceNotifyOnInformers {
	/**
	 * Notify the object about informer instances.
	 */
	public void setInformers(List<InterfaceAdditionalPopulationInformer> informers);
}
