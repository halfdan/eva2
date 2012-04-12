package eva2.server;

/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 320 $
 *            $Date: 2007-12-06 16:05:11 +0100 (Thu, 06 Dec 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Properties;

import eva2.server.go.InterfaceGOParameters;
import eva2.server.modules.GOModuleAdapter;
import eva2.server.modules.ModuleAdapter;
import eva2.tools.EVAERROR;
import eva2.tools.ReflectPackage;
import eva2.tools.jproxy.MainAdapterClient;
import eva2.tools.jproxy.RMIProxyLocal;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 * Collect available ModuleAdapter implementations and load them on request.
 */
public class ModuleServer {
	public static boolean TRACE = false;
	private int m_InstanceCounter = 0;
	private ArrayList<Class<?>> m_ModuleClassList;
//	private ArrayList m_RunnungModules;
	private ModuleAdapter m_ModuleAdapter;
	private int m_ModuleAdapterCounter = 0;

	/**
	 *
	 */
	public ModuleServer(Properties EvAProps) {
		if (TRACE)
			System.out.println("Constructor ModuleServer():");
		if (m_InstanceCounter > 0) {
			EVAERROR.EXIT("ModuleServer twice created");
		}
//		m_RunnungModules = new ArrayList();
		m_ModuleClassList = new ArrayList<Class<?>>();

		String modulePckg = null;
		Class<?> filterBy = null;
		try {
			modulePckg = EvAProps.getProperty("ModulePackage");
			filterBy = Class.forName(EvAProps.getProperty("ModuleFilterClass"));
		} catch(Exception e) {
			System.err.println("Creating ModuleServer failed: couldnt load modules:" + e.getMessage());
			System.err.println("module path was " + modulePckg + ", is it valid?");
			System.err.println("filter class path was " + ((filterBy==null) ? "null" : filterBy.getName()));
//			e.printStackTrace();
		}

		// this gets a list of all valid modules from the package
		Class<?>[] classes = ReflectPackage.getAssignableClassesInPackage(modulePckg, filterBy, true, true);
		for (Object cls : classes) {
			if (TRACE) System.out.println("- " + ((Class<?>)cls).getName());
			m_ModuleClassList.add((Class<?>)cls);
		}

		m_InstanceCounter++;
	}

	/**
	 *
	 */
	public String[] getModuleNameList() {
		ArrayList<String> ModuleNameList = new ArrayList<String>();
		for (int i = 0; i < m_ModuleClassList.size(); i++) {
			try {
				Class<?> Modul = (Class<?>) m_ModuleClassList.get(i);
				Method[] methods = Modul.getDeclaredMethods();
				for (int ii = 0; ii < methods.length; ii++) {
					if (methods[ii].getName().equals("getName") == true) {
						//System.out.println("name is =="+methods[ii].invoke(null,null));
						String name = (String)methods[ii].invoke((Object[])null, (Object[])null);
						if (name != null) ModuleNameList.add(name);
						break;
					}
				}
				//ModuleNameList.add ( Modul.getName());
			}
			catch (Exception e) {
				System.err.println("ModuleServer.getModuleNameList() " + e.getMessage());
			}

		}
		// and the running modules
		// @todo running modules sind abgeschaltet

//		for (int i = 0; i < m_RunnungModules.size(); i++) {
//			String AdapterName = null;
//			try {
//				AdapterName = ( (ModuleAdapter) m_RunnungModules.get(i)).getAdapterName();
//			}
//			catch (Exception ee) {
//				System.err.println("Error: GetAdapterName" + ee.getMessage());
//			}
//			ModuleNameList.add(AdapterName);
//		}

		String[] x = new String[ModuleNameList.size()];
		ModuleNameList.toArray(x);
		return x;
	}

	/**
	 * Load the module indicated by the selectedModuleName from all available
	 * module classes; if necessary through a remote proxy. Try to load a given
	 * parameter file in case its a GOModuleAdapter.
	 * 
	 * @return the loaded module adapter instance
	 */
	public ModuleAdapter createModuleAdapter(String selectedModuleName,
			MainAdapterClient Client, boolean runWithoutRMI,
			String hostAddress, InterfaceGOParameters goParams, String noGuiLogFile) {
		m_ModuleAdapterCounter++;
		String adapterName = new String("ERROR MODULADAPTER !!");
		if (TRACE) {
			System.out.println("ModuleServer.CreateModuleAdapter()");
			System.out.println(" ModuleServer.CreateModuleAdapter for:" +
					selectedModuleName);
			System.out.println(" m_ModulAdapterCounter =" + m_ModuleAdapterCounter);
			System.out.println(" Start of EvA RMI-ModulAdapter for Module: " +
					selectedModuleName);
		}
		String moduleName;
		Class<?> module;
		Method[] methods;
		for (int i = 0; i < m_ModuleClassList.size(); i++) {
			moduleName = null;
			module = m_ModuleClassList.get(i);

			try {
				methods = module.getDeclaredMethods();

				for (int ii = 0; ii < methods.length; ii++) {
					if (methods[ii].getName().equals("getName") == true)
						moduleName = (String) methods[ii].invoke((Object[])null, (Object[])null);
				}
			}
			catch (Exception e) {
				System.err.println("ModuleServer.createModuleAdapter() " + e.getMessage());
				e.printStackTrace();
			}
			if ((moduleName != null) && (selectedModuleName.equals(moduleName))) {
				if (TRACE) System.out.println("ModuleName: " + moduleName);
				try {
					adapterName = new String(m_ModuleAdapterCounter + "_Running_" +
							selectedModuleName);

					Constructor<?>[] constructorArr = module.getConstructors();
					// create a module instance                                        
                                        int constrIndex=0;
					if ((goParams==null && noGuiLogFile==null) || !module.equals(GOModuleAdapter.class)) {
						if (goParams!=null) System.err.println("Cant set params - no matching constructor found for " + adapterName + " (ModuleServer)");
						if (noGuiLogFile!=null) System.err.println("Cant deactivate GUI - no matching constructor found for " + adapterName + " (ModuleServer)");
						Object[] Para = new Object[2];
                                                while ((constructorArr[constrIndex].getParameterTypes().length!=2) && (constrIndex < constructorArr.length)) { 
							constrIndex++;
						}
						Class<?> paramTypes[] = (constructorArr[constrIndex]).getParameterTypes();
						Para[0] = paramTypes[0].cast(adapterName);
						Para[1] = paramTypes[1].cast(Client);
						m_ModuleAdapter = (ModuleAdapter) constructorArr[constrIndex].newInstance(Para);
					} else {
						Object[] Para = new Object[4];
						Para[0] = (String)adapterName;
						Para[1] = (InterfaceGOParameters)goParams;
						Para[2] = (String)noGuiLogFile;
						Para[3] = (MainAdapterClient)Client;
						while ((constructorArr[constrIndex].getParameterTypes().length!=4) && (constrIndex < constructorArr.length)) { 
							constrIndex++;
						}
						m_ModuleAdapter = (ModuleAdapter) constructorArr[constrIndex].newInstance(Para);
					}
					if (!runWithoutRMI) { // if we're using RMI, send the object to a remote server
//						for this to work the class of m_ModuleAdapter itself must implement the ModuleAdapter interface
//						for a strange reason, it is _not_ enough if a superclass implements the same interface! 
						m_ModuleAdapter = (ModuleAdapter)RMIProxyLocal.newInstance(m_ModuleAdapter, adapterName);
						(m_ModuleAdapter).setRemoteThis(m_ModuleAdapter);
					}
					//  m_RunnungModules.add(m_ModuleAdapter);
				}
				catch (Exception e) {
					System.err.println("CLASSPATH " +
							System.getProperty("java.class.path"));
					e.printStackTrace();
					EVAERROR.EXIT("Error in RMI-Moduladapter initialization: " + e.getMessage());
					return null;
				}
				if (TRACE) System.out.println("End of RMI-Moduladapter initialization  !");
				return (ModuleAdapter) m_ModuleAdapter;
			}
		}
//		//  @todo running modules sind gerade noch abgeschaltet
//		for (int i = 0; i < m_RunnungModules.size(); i++) {
//			try {
//				adapterName = ( (ModuleAdapter) m_RunnungModules.get(i)).getAdapterName();
//			}
//			catch (Exception e) {
//				System.out.println("Error : GetAdapterName" + e);
//			}
//			if (adapterName.equals(selectedModuleName)) {
//				if (TRACE)
//					System.out.println(" Choose a running Module!!! " + adapterName);
//				m_ModuleAdapter = ( (ModuleAdapter) m_RunnungModules.get(i));
//				return (ModuleAdapter) m_ModuleAdapter;
//			}
//		}

		System.err.println("NO VALID MODULE DEFINED: " + selectedModuleName);
		return null;
	}
}