package eva2.tools;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ReflectPackageTest {

    @Test
    public void testInstantiateWithParams() throws Exception {
        List<Pair<String,Object>> params = new ArrayList<>();
        params.add(new Pair<>("i", 108));

        DummyClass dummy = (DummyClass)ReflectPackage.instantiateWithParams(DummyClass.class.getCanonicalName(), params);
        assertEquals(108, dummy.getI());
        assertNull(dummy.getText());

        params.add(new Pair<>("text", "New Jersey"));
        dummy = (DummyClass)ReflectPackage.instantiateWithParams(DummyClass.class.getCanonicalName(), params);
        assertEquals(108, dummy.getI());
        assertEquals("New Jersey", dummy.getText());

        // Invalid param
        params.add(new Pair<>("invalid", 42.0));
        dummy = (DummyClass)ReflectPackage.instantiateWithParams(DummyClass.class.getCanonicalName(), params);
        assertNull(dummy);
    }

    @Test
    public void testGetInstance() throws Exception {
        Object obj = ReflectPackage.getInstance(DummyClass.class.getCanonicalName(), new Object[]{});
        assertTrue(obj instanceof DummyClass);

        // Calls default constructor
        DummyClass dummy = (DummyClass)obj;
        assertEquals(42, dummy.getI());

        dummy = (DummyClass)ReflectPackage.getInstance(DummyClass.class.getCanonicalName(), new Object[]{23});
        assertEquals(23, dummy.getI());
    }
}

