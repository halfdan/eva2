package eva2.tools;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PairTest {
    private Pair<String, Double> p;

    @Before
    public void setUp() {
        p = new Pair<>("Head", 12.0);
    }

    @Test
    public void testCar() throws Exception {
        assertEquals("Head", p.car());
    }

    @Test
    public void testCdr() throws Exception {
        assertEquals(12.0, p.cdr(), 0.0);
    }

    @Test
    public void testClone() throws Exception {
        //assertEquals(p, p.clone());
    }

    @Test
    public void testHashCode() throws Exception {
        assertNotNull(p.hashCode());
    }

    @Test
    public void testHead() throws Exception {
        assertEquals("Head", p.head());
    }

    @Test
    public void testGetHead() throws Exception {
        assertEquals("Head", p.getHead());
    }

    @Test
    public void testTail() throws Exception {
        assertEquals(12.0, p.tail(), 0.0);
    }

    @Test
    public void testGetTail() throws Exception {
        assertEquals(12.0, p.getTail(), 0.0);
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("(Head,12.0)", p.toString());
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals("(Head,12.0)", p.getName());
    }

    @Test
    public void testSetHead() throws Exception {
        String newHead = "newHead";
        p.setHead(newHead);
        assertEquals(newHead, p.getHead());
    }

    @Test
    public void testSetTail() throws Exception {
        Double newTail = 42.0;
        p.setTail(newTail);
        assertEquals(newTail, p.getTail());
    }
}