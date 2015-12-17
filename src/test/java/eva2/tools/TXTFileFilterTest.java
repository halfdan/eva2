package eva2.tools;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class TXTFileFilterTest {

    @Test
    public void testAccept() throws Exception {
        TXTFileFilter filter = new TXTFileFilter();
        assertTrue(filter.accept(new File("foobar.txt")));

        assertTrue(filter.accept(new File("foobar.TXT")));

        assertFalse(filter.accept(new File("foobar.csv")));
    }

    @Test
    public void testGetDescription() throws Exception {
        TXTFileFilter filter = new TXTFileFilter();
        assertNotNull(filter.getDescription());
    }
}