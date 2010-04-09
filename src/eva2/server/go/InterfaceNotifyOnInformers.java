package eva2.server.go;

import java.util.List;

import eva2.server.go.problems.InterfaceAdditionalPopulationInformer;

public interface InterfaceNotifyOnInformers {
	/**
	 * Notify the object about informer instances.
	 */
	public void setInformers(List<InterfaceAdditionalPopulationInformer> informers);
}
