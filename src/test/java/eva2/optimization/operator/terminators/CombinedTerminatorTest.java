package eva2.optimization.operator.terminators;

import eva2.optimization.population.PopulationInterface;
import eva2.optimization.population.SolutionSet;
import eva2.problems.InterfaceOptimizationProblem;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CombinedTerminatorTest {
    private CombinedTerminator terminator;
    private InterfaceTerminator term1;
    private InterfaceTerminator term2;
    private InterfaceOptimizationProblem prob;

    @Before
    public void setUp() throws Exception {
        terminator = new CombinedTerminator();
        term1 = mock(InterfaceTerminator.class);
        term2 = mock(InterfaceTerminator.class);
        prob = mock(InterfaceOptimizationProblem.class);

        terminator.setTerminatorOne(term1);
        terminator.setTerminatorTwo(term2);
    }

    @Test
    public void testInitialize() throws Exception {
        terminator.initialize(prob);

        verify(term1).initialize(prob);
        verify(term2).initialize(prob);
    }

    @Test
    public void testIsTerminatedPop() throws Exception {
        PopulationInterface pop = mock(PopulationInterface.class);

        terminator.isTerminated(pop);
        verify(term1).isTerminated(pop);
        verify(term2).isTerminated(pop);

        reset(term1, term2);

        terminator.setLogicalOperator(CombinedTerminator.LogicalOperator.OR);
        // Should still call both terminators
        terminator.isTerminated(pop);
        verify(term1).isTerminated(pop);
        verify(term2).isTerminated(pop);
    }

    @Test
    public void testIsTerminatedSolSet() throws Exception {
        SolutionSet solutionSet = mock(SolutionSet.class);

        terminator.isTerminated(solutionSet);
        verify(term1).isTerminated(solutionSet);
        verify(term2).isTerminated(solutionSet);

        reset(term1, term2);

        terminator.setLogicalOperator(CombinedTerminator.LogicalOperator.OR);
        // Should still call both terminators
        terminator.isTerminated(solutionSet);
        verify(term1).isTerminated(solutionSet);
        verify(term2).isTerminated(solutionSet);
    }

    @Test
    public void testLastTerminationMessage() throws Exception {

    }

    @Test
    public void testGetLogicalOperator() throws Exception {
        // Defaults to an AND
        assertEquals(CombinedTerminator.LogicalOperator.AND, terminator.getLogicalOperator());

        terminator.setLogicalOperator(CombinedTerminator.LogicalOperator.OR);
        assertEquals(CombinedTerminator.LogicalOperator.OR, terminator.getLogicalOperator());
    }

    @Test
    public void testGetTerminatorOne() throws Exception {
        assertEquals(term1, terminator.getTerminatorOne());
    }

    @Test
    public void testGetTerminatorTwo() throws Exception {
        assertEquals(term2, terminator.getTerminatorTwo());
    }
}