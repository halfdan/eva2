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
import eva2.server.go.InterfaceGOParameters;
import eva2.server.modules.GOModuleAdapter;
import eva2.server.modules.ModuleAdapter;
import eva2.tools.EVAERROR;
import eva2.tools.ReflectPackage;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collect available ModuleAdapter implementations and load them on request.
 */
public class ModuleServer {

    private static final Logger LOGGER = Logger.getLogger(ModuleServer.class.getName());
    private static int instanceCounter = 0;
    private List<Class<?>> moduleClassList;
    private ModuleAdapter moduleAdapter;
    private int moduleAdapterCounter = 0;

    /**
     *
     */
    public ModuleServer(Properties EvAProps) {
        if (instanceCounter > 0) {
            EVAERROR.EXIT("ModuleServer created twice");
        }
        moduleClassList = new ArrayList<Class<?>>();

        String modulePckg = null;
        Class<?> filterBy = null;
        try {
            /* Fetch the name of the package containing the modules */
            modulePckg = EvAProps.getProperty("ModulePackage");
            /* Fetch the the super class for all modules */
            filterBy = Class.forName(EvAProps.getProperty("ModuleFilterClass"));
        } catch (Exception ex) {
            System.err.println("Creating ModuleServer failed: couldnt load modules:" + ex.getMessage());
            System.err.println("module path was " + modulePckg + ", is it valid?");
            System.err.println("filter class path was " + ((filterBy == null) ? "null" : filterBy.getName()));
        }

        // this gets a list of all valid modules from the package
        Class<?>[] classes = ReflectPackage.getAssignableClassesInPackage(modulePckg, filterBy, true, true);
        for (Object cls : classes) {
            moduleClassList.add((Class<?>) cls);
        }

        instanceCounter++;
    }

    /**
     * Iterates over the list of available modules and fetches the name of the
     * module by calling the static getName() method.
     *
     * @return Array of available modules
     */
    public String[] getModuleNameList() {
        List<String> moduleNameList = new ArrayList<String>();
        for (Class<?> module : moduleClassList) {
            try {
                Method[] methods = module.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.getName().equals("getName")) {
                        String name = (String) method.invoke((Object[]) null, (Object[]) null);
                        if (name != null) {
                            moduleNameList.add(name);
                        } else {
                            LOGGER.log(Level.FINE, "Module {0} does not specify a diplayable name.", module.getCanonicalName());
                        }
                        break;
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error while fetching name from module.", ex);
            }

        }
        
        String[] x = new String[moduleNameList.size()];
        moduleNameList.toArray(x);
        return x;
    }

    /**
     * Load the module indicated by the selectedModuleName from all available
     * module classes; if necessary through a remote proxy. Try to load a given
     * parameter file in case its a GOModuleAdapter.
     *
     * @return the loaded module adapter instance
     */
    public ModuleAdapter createModuleAdapter(String selectedModuleName, InterfaceGOParameters goParams, String noGuiLogFile) {
        moduleAdapterCounter++;
        String adapterName = "ERROR MODULADAPTER !!";
        String moduleName = null;
        Method[] methods;
        for (Class<?> module : moduleClassList) {
            try {
                methods = module.getDeclaredMethods();

                for (Method method : methods) {
                    if (method.getName().equals("getName")) {
                        moduleName = (String) method.invoke((Object[]) null, (Object[]) null);
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }
            if ((moduleName != null) && (selectedModuleName.equals(moduleName))) {
                try {
                    adapterName = moduleAdapterCounter + "_Running_" + selectedModuleName;

                    Constructor<?>[] constructorArr = module.getConstructors();
                    /* create a module instance */
                    int constrIndex = 0;

                    if ((goParams == null && noGuiLogFile == null) || !module.equals(GOModuleAdapter.class)) {
                        if (goParams != null) {
                            System.err.println("Cant set params - no matching constructor found for " + adapterName + " (ModuleServer)");
                        }
                        if (noGuiLogFile != null) {
                            System.err.println("Cant deactivate GUI - no matching constructor found for " + adapterName + " (ModuleServer)");
                        }
                        Object[] Para = new Object[1];
                        while ((constructorArr[constrIndex].getParameterTypes().length != 1) && (constrIndex < constructorArr.length)) {
                            constrIndex++;
                        }
                        Class<?> paramTypes[] = (constructorArr[constrIndex]).getParameterTypes();
                        Para[0] = paramTypes[0].cast(adapterName);
                        moduleAdapter = (ModuleAdapter) constructorArr[constrIndex].newInstance(Para);
                    } else {
                        Object[] param = new Object[4];
                        param[0] = (String) adapterName;
                        param[1] = (InterfaceGOParameters) goParams;
                        param[2] = (String) noGuiLogFile;
                        while ((constructorArr[constrIndex].getParameterTypes().length != 4) && (constrIndex < constructorArr.length)) {
                            constrIndex++;
                        }
                        moduleAdapter = (ModuleAdapter) constructorArr[constrIndex].newInstance(param);
                    }                    
                    //  m_RunnungModules.add(m_ModuleAdapter);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error in RMI-Moduladapter initialization", ex);
                    EVAERROR.EXIT("Error in RMI-Moduladapter initialization: " + ex.getMessage());
                    return null;
                }
                return (ModuleAdapter) moduleAdapter;
            }
        }

        LOGGER.log(Level.SEVERE, "No valid module defined: {0}", selectedModuleName);
        return null;
    }
}