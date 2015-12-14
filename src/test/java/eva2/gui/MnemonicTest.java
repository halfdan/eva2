package eva2.gui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MnemonicTest {

    @Test
    public void testMnemonic() throws Exception {
        Mnemonic m = new Mnemonic("No mnemonic");

        // String without ampersand does not have a mnemonic
        assertEquals(0, m.getMnemonic());

        // Detects ampersand at beginning
        m = new Mnemonic("&Fancy");
        assertEquals('F', m.getMnemonic());

        // Detects ampersand in the middle
        m = new Mnemonic("Super&fancy");
        assertEquals('f', m.getMnemonic());

        // Handles ampersand at the end
        m = new Mnemonic("Hellyea&");
        assertEquals(0, m.getMnemonic());
    }

    @Test
    public void testGetMnemonic() throws Exception {
        Mnemonic m = new Mnemonic("Super &great");
        assertEquals('g', m.getMnemonic());
    }

    @Test
    public void testGetText() throws Exception {
        Mnemonic m1 = new Mnemonic("The text");
        assertEquals("The text", m1.getText());

        Mnemonic m2 = new Mnemonic("&Mnemonic");
        assertEquals("Mnemonic", m2.getText());

    }
}