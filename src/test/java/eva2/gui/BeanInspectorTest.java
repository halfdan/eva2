package eva2.gui;

import eva2.util.annotation.Parameter;
import org.junit.Test;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import static org.junit.Assert.*;

public class BeanInspectorTest {
    private class DummyBean {
        private int foo;
        private int bar;

        public void setFoo(int f) {
            foo = f;
        }

        public int getFoo() {
            return foo;
        }

        public String fooTipText() {
            return "This is foo";
        }

        @Parameter(description = "This is bar")
        public void setBar(int b) {
            bar = b;
        }

        public int getBar() {
            return bar;
        }
    }

    @Test
    public void testGetToolTipText() throws Exception {
        DummyBean dummy = new DummyBean();
        BeanInfo bi = Introspector.getBeanInfo(dummy.getClass());
        PropertyDescriptor[] pds = bi.getPropertyDescriptors();

        // bar, class, foo
        assertEquals(3, pds.length);

        // bar is 0
        assertEquals("This is bar", BeanInspector.getToolTipText(dummy, pds[0]));
        // foo is 2
        assertEquals("This is foo", BeanInspector.getToolTipText(dummy, pds[2]));
    }

    @Test
    public void testCallIfAvailable() throws Exception {
        DummyBean dummy = new DummyBean();
        dummy.setBar(12);

        assertEquals(12, (int)BeanInspector.callIfAvailable(dummy, "getBar", null));

        BeanInspector.callIfAvailable(dummy, "setFoo", new Object[]{42});
        assertEquals(42, dummy.getFoo());

        assertNull(BeanInspector.callIfAvailable(dummy, "derp", null));
    }
}