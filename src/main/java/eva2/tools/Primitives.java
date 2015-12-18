package eva2.tools;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by halfdan on 17/12/15.
 */
public class Primitives {
    public static Class<?> unwrap(Class<?> clazz) {
        return getWrapperTypes().get(clazz);
    }

    public static boolean isWrapperType(Class<?> clazz)
    {
        return getWrapperTypes().containsKey(clazz);
    }

    private static Map<Class<?>, Class<?>> getWrapperTypes()
    {
        Map<Class<?>, Class<?>> ret = new HashMap<>();
        ret.put(Boolean.class, boolean.class);
        ret.put(Character.class, char.class);
        ret.put(Byte.class, byte.class);
        ret.put(Short.class, short.class);
        ret.put(Integer.class, int.class);
        ret.put(Long.class, long.class);
        ret.put(Float.class, float.class);
        ret.put(Double.class, double.class);
        ret.put(Void.class, void.class);
        return ret;
    }
}
