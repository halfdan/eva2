package eva2.optimization.population;

import eva2.optimization.individuals.ESIndividualDoubleData;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class PopulationTest {
    Population emptyPopulation;

    @Before
    public void setUp() throws Exception {
        emptyPopulation = new Population(10);
    }

    @Test
    public void testEquals() throws Exception {

    }

    @Test
    public void testPutData() throws Exception {

    }

    @Test
    public void testGetData() throws Exception {
        // Simple data
        emptyPopulation.putData("someInt", 12);
        assertEquals(12, emptyPopulation.getData("someInt"));

        // Arrays
        double[] doubleArray = new double[]{1.0};
        emptyPopulation.putData("someDoubleArray", doubleArray);
        assertArrayEquals(doubleArray, (double[])emptyPopulation.getData("someDoubleArray"), 0.0);

        // Objects
        Object obj = new Object();
        emptyPopulation.putData("someObject", obj);
        assertEquals(obj, emptyPopulation.getData("someObject"));
    }

    @Test
    public void testHasData() throws Exception {
        assertFalse(emptyPopulation.hasData("someKey"));

        // Simple data
        emptyPopulation.putData("someInt", 12);
        assertTrue(emptyPopulation.hasData("someInt"));

        // Arrays
        emptyPopulation.putData("someDoubleArray", new double[]{1.0});
        assertTrue(emptyPopulation.hasData("someDoubleArray"));

        // Objects
        emptyPopulation.putData("someObject", new Object());
        assertTrue(emptyPopulation.hasData("someObject"));
    }

    @Test
    public void testResetProperties() throws Exception {

    }

    @Test
    public void testGetInitPos() throws Exception {

    }

    @Test
    public void testSetInitPos() throws Exception {

    }

    @Test
    public void testSetInitAround() throws Exception {

    }

    @Test
    public void testGetInitAround() throws Exception {

    }

    @Test
    public void testFill() throws Exception {
        assertEquals(0, emptyPopulation.size());

        emptyPopulation.fill(new ESIndividualDoubleData());

        // Should be filled with 10 individuals
        assertEquals(10, emptyPopulation.getTargetSize());
    }

    @Test
    public void testSetUseHistory() throws Exception {
        emptyPopulation.setUseHistory(true);
        assertTrue(emptyPopulation.isUsingHistory());

        emptyPopulation.setUseHistory(false);
        assertFalse(emptyPopulation.isUsingHistory());
    }

    @Test
    public void testIsUsingHistory() throws Exception {
        // Not using history by default
        assertFalse(emptyPopulation.isUsingHistory());
    }

    @Test
    public void testSetAutoAging() throws Exception {
        emptyPopulation.setAutoAging(true);
        assertTrue(emptyPopulation.isAutoAging());


        emptyPopulation.setAutoAging(false);
        assertFalse(emptyPopulation.isAutoAging());
    }

    @Test
    public void testIsAutoAging() throws Exception {
        // Is auto-aging by default
        assertTrue(emptyPopulation.isAutoAging());
    }

    @Test
    public void testSetMaxHistoryLength() throws Exception {

    }

    @Test
    public void testGetMaxHistLength() throws Exception {

    }

    @Test
    public void testGetHistoryLength() throws Exception {

    }

    @Test
    public void testGetHistory() throws Exception {

    }

    @Test
    public void testSetHistory() throws Exception {

    }

    @Test
    public void testIncrFunctionCalls() throws Exception {
        int currentFunctionCalls = emptyPopulation.getFunctionCalls();
        emptyPopulation.incrFunctionCalls();
        assertEquals(currentFunctionCalls + 1, emptyPopulation.getFunctionCalls());
    }

    @Test
    public void testIncrFunctionCallsBy() throws Exception {
        int currentFunctionCalls = emptyPopulation.getFunctionCalls();
        emptyPopulation.incrFunctionCallsBy(150);
        assertEquals(currentFunctionCalls + 150, emptyPopulation.getFunctionCalls());
    }

    @Test
    public void testGetFunctionCalls() throws Exception {
        // New and empty population has 0 function calls
        assertEquals(0, this.emptyPopulation.getFunctionCalls());
    }

    @Test
    public void testSetFunctionCalls() throws Exception {
        emptyPopulation.setFunctionCalls(150);
        assertEquals(150, emptyPopulation.getFunctionCalls());
    }

    @Test
    public void testSetAllFitnessValues() throws Exception {

    }

    @Test
    public void testIncrGeneration() throws Exception {
        int currentGeneration = emptyPopulation.getGeneration();
        emptyPopulation.incrGeneration();

        assertEquals(currentGeneration + 1, emptyPopulation.getGeneration());
    }

    @Test
    public void testGetGeneration() throws Exception {
        // New population has generation 0
        assertEquals(0, emptyPopulation.getGeneration());
    }

    @Test
    public void testSetGeneration() throws Exception {
        emptyPopulation.setGeneration(10);
        assertEquals(10, emptyPopulation.getGeneration());
    }

    @Test
    public void testAddPopulation() throws Exception {

    }

    @Test
    public void testAddPopulation1() throws Exception {

    }

    @Test
    public void testResetFitness() throws Exception {

    }

    @Test
    public void testGetDominatingSet() throws Exception {

    }

    @Test
    public void testGetDominatingSet1() throws Exception {

    }

    @Test
    public void testGetIndexOfBestIndividualPrefFeasible() throws Exception {

    }

    @Test
    public void testGetIndexOfWorstIndividualNoConstr() throws Exception {

    }

    @Test
    public void testGetIndexOfBestIndividualPrefFeasible1() throws Exception {

    }

    @Test
    public void testGetIndexOfWorstIndividualNoConstr1() throws Exception {

    }

    @Test
    public void testMoveNInds() throws Exception {

    }

    @Test
    public void testMoveRandIndFromTo() throws Exception {

    }

    @Test
    public void testGetSpecificData() throws Exception {

    }

    @Test
    public void testGetSpecificDataNames() throws Exception {

    }

    @Test
    public void testGetIDList() throws Exception {

    }

    @Test
    public void testGetIndyList() throws Exception {

    }

    @Test
    public void testSetTargetSize() throws Exception {

    }

    @Test
    public void testSetTargetPopSize() throws Exception {

    }

    @Test
    public void testGetBestIndividual() throws Exception {

    }

    @Test
    public void testGetWorstIndividual() throws Exception {

    }

    @Test
    public void testGetBestFitness() throws Exception {

    }

    @Test
    public void testGetWorstFitness() throws Exception {

    }

    @Test
    public void testGetMeanFitness() throws Exception {

    }

    @Test
    public void testGetPopulationMeasures() throws Exception {

    }

    @Test
    public void testGetCorrelations() throws Exception {

    }

    @Test
    public void testGetCorrelations1() throws Exception {

    }

    @Test
    public void testGetFitnessMeasures() throws Exception {

    }

    @Test
    public void testGetCenter() throws Exception {

    }

    @Test
    public void testGetCenterIndy() throws Exception {

    }

    @Test
    public void testGetCenterWeighted() throws Exception {

    }

    @Test
    public void testGetNeighborIndex() throws Exception {

    }

    @Test
    public void testGetFitSum() throws Exception {

    }

    @Test
    public void testUpdateRange() throws Exception {

    }

    @Test
    public void testTargetSizeReached() throws Exception {

    }

    @Test
    public void testTargetSizeExceeded() throws Exception {

    }

    @Test
    public void testGetFreeSlots() throws Exception {

    }

    @Test
    public void testClearHistory() throws Exception {

    }

    @Test
    public void testCheckNoNullIndy() throws Exception {

    }

    @Test
    public void testFilterByFitness() throws Exception {

    }
}