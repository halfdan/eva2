package eva2.cli;

import eva2.optimization.modules.OptimizationParameters;
import eva2.tools.ReflectPackage;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates a human readable document with all command line parameters available
 * for EvA2.
 */
public class ParameterGenerator {
    private Class<?> clazz;
    private boolean recursive;

    /**
     * Maps class name to a list of parameters
     */
    private Map<String, List<Parameter>> parameterList;

    public ParameterGenerator(Class<?> clazz) {
        this(clazz, true);
    }

    public ParameterGenerator(Class<?> clazz, boolean recursive) {
        this.clazz = clazz;
        this.parameterList = new HashMap<>();
        this.recursive = recursive;
    }



    public Map<String, List<Parameter>> getParameterList() {
        return parameterList;
    }

    public void generate() {
        generateForClass(this.clazz);
    }

    private void generateForClass(Class<?> clazz) {
        List<Parameter> parameters = new ArrayList<>();

        this.parameterList.put(clazz.getName(), parameters);

        BeanInfo info;
        try {
            if (clazz.isInterface()) {
                info = Introspector.getBeanInfo(clazz);
            } else {
                info = Introspector.getBeanInfo(clazz, Object.class);
            }

            PropertyDescriptor[] properties = info.getPropertyDescriptors();
            for (PropertyDescriptor pd : properties) {
                String name = pd.getName();
                Method getter = pd.getReadMethod();
                Method setter = pd.getWriteMethod();
                Class<?> type = pd.getPropertyType();

                // Skip if setter is hidden or getter is not available
                if (setter == null || setter.isAnnotationPresent(eva2.util.annotation.Hidden.class) || getter == null) {
                    continue;
                }

                Parameter parameter;
                if (setter.isAnnotationPresent(eva2.util.annotation.Parameter.class)) {
                    eva2.util.annotation.Parameter param = setter.getAnnotation(eva2.util.annotation.Parameter.class);
                    if (!param.name().isEmpty()) {
                        name = param.name();
                    }
                    parameter = new Parameter(name, param.description(), type);
                } else {
                    parameter = new Parameter(name, "No description available.", type);
                }

                parameters.add(parameter);

                if (type == Object.class || !recursive) {
                    continue;
                }

                Class<?>[] classes = ReflectPackage.getAssignableClassesInPackage("eva2", type, true, true);
                for (Class assignable : classes) {
                    // Recurse if not in List
                    if (!parameterList.containsKey(assignable.getName()) && assignable.getName().startsWith("eva2") && !assignable.getName().startsWith("eva2.gui")) {
                        System.out.println(type.getName() + "\t->\t" + assignable.getName());
                        generateForClass(assignable);
                    }
                }
            }
        } catch (IntrospectionException ex) {
            // die
        }

    }

    public static void main(String[] args) {
        ParameterGenerator generator = new ParameterGenerator(OptimizationParameters.class);
        generator.generate();
        int paramCount = 0;
        Map<String, List<Parameter>> paramList = generator.getParameterList();
        for(String key : paramList.keySet()) {
            paramCount += paramList.get(key).size();
        }
        System.out.println("Total Parameter Count: " + paramCount);
        System.out.println("Total Type Count: " + paramList.size());
    }
}

class Type {
    private List<Parameter> parameters;
    private final String name;
    private final String description;

    public Type(String name, String description) {
        this.name = name;
        this.description = description;
        this.parameters = new ArrayList<>();
    }

    public List<Parameter> getParameters() {
        return this.parameters;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }
}

class Parameter {
    private final String name;
    private final String description;
    private final Class<?> type;

    public Parameter(String name, String description, Class<?> type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Class<?> getType() {
        return this.type;
    }
}
