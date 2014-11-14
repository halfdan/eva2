package eva2.cli;

import eva2.gui.BeanInspector;
import eva2.optimization.go.InterfaceOptimizationParameters;
import eva2.optimization.modules.OptimizationParameters;
import eva2.optimization.statistics.InterfaceStatisticsParameters;
import eva2.optimization.statistics.StatisticsParameters;
import eva2.tools.ReflectPackage;
import eva2.util.annotation.Hidden;
import eva2.util.annotation.Parameter;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;

class ArgumentTree extends LinkedHashMap<String, Object> {
    private Object value;

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return ((value != null) ? value.toString() + ", " : "") + super.toString();
    }

    /**
     * If there are no key, value pairs present and the value is unset,
     * this tree belongs to a flag.
     *
     * @return
     */
    public boolean isFlag() {
        return this.size() == 0 && this.value == null;
    }
}
/**
 *
 */
public final class OptimizationBuilder {
    private OptimizationBuilder() {}

    public static InterfaceOptimizationParameters parseOptimizerArguments(String[] args) {
        ArgumentTree argumentTree = parseArguments(args);
        return constructFromArgumentTree(OptimizationParameters.class, argumentTree);
    }

    public static InterfaceStatisticsParameters parseStatisticsArguments(String[] args) {
        ArgumentTree argumentTree = parseArguments(args);
        return constructFromArgumentTree(StatisticsParameters.class, argumentTree);
    }

    private static ArgumentTree parseArguments(String[] args) {
        HashMap<String, String> argumentMap = new HashMap<>(args.length/2);
        int i = 0;
        while (i < args.length) {
            // Is it a parameter?
            if (args[i].startsWith("--")) {
                String key = args[i].substring(2);
                // Is the next a value?
                if (i < args.length - 1 && !args[i+1].startsWith("--")) {
                    argumentMap.put(key, args[i + 1]);
                    i += 2;
                } else {
                    argumentMap.put(key, null);
                    i++;
                }
            }
        }

        ArgumentTree argumentTree = new ArgumentTree();
        for (String key : argumentMap.keySet()) {
            insertIntoArgumentTree(argumentTree, key, argumentMap.get(key));
        }

        return argumentTree;
    }

    private static void insertIntoArgumentTree(ArgumentTree tree, String key, String value) {
        // Basic type?
        if (!key.contains("-")) {
            if (!tree.containsKey(key)) {
                tree.put(key, new ArgumentTree());
            }
            ((ArgumentTree)tree.get(key)).setValue(value);
        } else {
            String baseKey = key.substring(0, key.indexOf('-'));
            String restKey = key.substring(key.indexOf('-') + 1);
            if (!tree.containsKey(baseKey)) {
                tree.put(baseKey, new ArgumentTree());
            }
            insertIntoArgumentTree((ArgumentTree)tree.get(baseKey), restKey, value);
        }
    }

    /**
     *
     * @param clazz
     * @param tree Tree containing key, value pairs
     */
    private static <T> T constructFromArgumentTree(Class<T> clazz, ArgumentTree tree) {
        T instance = null;

        // Create new instance
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            // Find subclasses of clazz that match tree.getValue()
        } else {
            Class<?>[] params = new Class[0];
            try {
                Constructor constructor = clazz.getConstructor(params);
                instance = (T)constructor.newInstance();
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }

        /* No need to continue if there are no parameters to set */
        if (tree.isEmpty()) {
            return instance;
        }

        BeanInfo info;
        try {
            if (clazz.isInterface()) {
                info = Introspector.getBeanInfo(clazz);
            } else {
                info = Introspector.getBeanInfo(clazz, Object.class);
            }
            PropertyDescriptor[] properties = info.getPropertyDescriptors();
            int foundParameters = 0;
            for (PropertyDescriptor pd : properties) {
                String name = pd.getName();
                Method getter = pd.getReadMethod();
                Method setter = pd.getWriteMethod();
                Class<?> type = pd.getPropertyType();
                // We skip non-existing setters or setters that are hidden by annotation
                if (getter == null || setter == null || setter.isAnnotationPresent(Hidden.class)) {
                    continue;
                }

                // We use the name of the descriptor or if possible
                // one that is given by the @Parameter annotation.
                if (setter.isAnnotationPresent(Parameter.class)) {
                    Parameter param = setter.getAnnotation(Parameter.class);
                    if (!param.name().isEmpty()) {
                        name = param.name();
                    }
                }

                /**
                 * If the tree contains this property we try to set it on the object.
                 */
                if (tree.containsKey(name)) {
                    foundParameters++;
                    Object obj = null;
                    if (type.isPrimitive() && ((ArgumentTree)tree.get(name)).getValue() != null) {
                        obj = BeanInspector.stringToPrimitive((String) ((ArgumentTree) tree.get(name)).getValue(), type);
                    } else if (type.isArray() && ((ArgumentTree)tree.get(name)).getValue() != null) {

                    } else if (type.isEnum() && ((ArgumentTree)tree.get(name)).getValue() != null) {
                        int enumIndex = Integer.parseInt((String)((ArgumentTree)tree.get(name)).getValue());
                        // ToDo: Properly check
                        obj = type.getEnumConstants()[enumIndex];
                    } else {
                        // The subtree has the name of the class
                        String className = (String)((ArgumentTree)tree.get(name)).getValue();

                        Class subType;
                        if (className != null) {
                            // Try to get the actual class from its name
                            subType = getClassFromName(className, type);
                        } else {
                            subType = type;
                        }

                        // Here the recursion starts
                        obj = constructFromArgumentTree(subType, (ArgumentTree) tree.get(name));
                    }

                    // We preserve the default if obj is null
                    if (obj != null) {
                        BeanInspector.callIfAvailable(instance, setter.getName(), new Object[]{obj});
                    }
                }

                // If we configured all parameters in the tree we can break the loop
                if (tree.size() == foundParameters) {
                    break;
                }
            }
        } catch (IntrospectionException ex) {
            ex.printStackTrace();
        }

        return instance;
    }

    private static Class<?> getClassFromName(String name, Class type) {
        Class<?>[] classes = ReflectPackage.getAssignableClassesInPackage("eva2", type, true, true);
        for (Class clazz : classes) {
            // We allow both the fully qualified name (eva2.optimization.strategies.GeneticAlgorithm
            // and the simple name (GeneticAlgorithm)
            if (clazz.getName().equals(name) || clazz.getSimpleName().equals(name)) {
                return clazz;
            }
        }

        return null;
    }
}
