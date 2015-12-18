package eva2.gui;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TypeSelectorItemTest {
    TypeSelectorItem item;

    @Before
    public void setUp() throws Exception {
        item = new TypeSelectorItem(
                "eva2.problem.AbstractProblemDouble",
                "AbstractProblemDouble",
                "An abstract problem"
        );
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals("eva2.problem.AbstractProblemDouble", item.getId());
    }

    @Test
    public void testGetDescription() throws Exception {
        assertEquals("An abstract problem", item.getDescription());
    }

    @Test
    public void testGetDisplayName() throws Exception {
        assertEquals("AbstractProblemDouble", item.getDisplayName());
    }

    @Test
    public void testToString() throws Exception {
        // Should match the id
        assertEquals("eva2.problem.AbstractProblemDouble", item.toString());
    }
}