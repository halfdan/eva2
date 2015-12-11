package eva2.optimization.individuals;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ESIndividualDoubleDataTest {
    ESIndividualDoubleData indy;

    @Before
    public void setUp() throws Exception {
        indy = new ESIndividualDoubleData();
    }

    @Test
    public void testEqualGenotypes() throws Exception {
        // Genotype of two different types is not equal
        assertFalse(indy.equalGenotypes(new ESIndividualBinaryData()));

        // Default init genotype should be equal
        assertTrue(indy.equalGenotypes(new ESIndividualDoubleData()));
    }

    @Test
    public void testSize() throws Exception {
        // Default individual has size 1
        assertEquals(1, indy.size());

        // Returns length of genotype
        indy.setDoubleGenotype(new double[]{1.0, 1.0, 1.0, 1.0, 1.0});
        assertEquals(5, indy.size());
    }

    @Test
    public void testSetDoubleGenotype() throws Exception {
        double[] genotype = {1.0, 1.0, 1.0, 1.0, 1.0};

        indy.setDoubleGenotype(genotype);

        // Setting the genotype clears the phenotype and matches the genotype
        assertArrayEquals(genotype, indy.getDoubleData(), 0.0);

        assertArrayEquals(genotype, indy.getDGenotype(), 0.0);
    }

    @Test
    public void testGetStringRepresentation() throws Exception {
        assertEquals(
            "ESIndividual coding double: (Fitness {0.0;}/SelProb{0.0;}) Value: [0.0; ]",
            indy.getStringRepresentation()
        );
    }

    @Test
    public void testEqual() throws Exception {
        // Is equal to itself
        assertTrue(indy.equals(indy));
    }
}