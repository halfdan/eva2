package eva2.yaml;

import eva2.util.annotation.Hidden;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.representer.Representer;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 */
public class BeanSerializer {

    public static String serializeObject(Object obj) {
        DumperOptions options = new DumperOptions();
        options.setAllowReadOnlyProperties(false);
        options.setIndent(4);
        Yaml yaml = new Yaml(new OptimizationRepresenter(), options);

        return yaml.dump(obj);
    }


}

class OptimizationRepresenter extends Representer {
    @Override
    protected Set<Property> getProperties(Class<?> type)
            throws IntrospectionException {

        Set<Property> set = super.getProperties(type);
        Set<Property> filtered = new TreeSet<>();
        BeanInfo info = Introspector.getBeanInfo(type, Object.class);
        PropertyDescriptor[] properties = info.getPropertyDescriptors();
        ArrayList<String> hiddenProperties = new ArrayList<>();

        // We don't want to save Hidden properties
        for (PropertyDescriptor p : properties) {
            Method setter = p.getWriteMethod();
            if (setter != null && setter.isAnnotationPresent(Hidden.class) || p.isHidden()) {
                hiddenProperties.add(p.getName());
            }
        }

        for (Property prop : set) {
            String name = prop.getName();
            if (!hiddenProperties.contains(name)) {
                filtered.add(prop);
            }
        }
        return filtered;
    }
}