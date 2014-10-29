package eva2.problems.simple;

import eva2.util.annotation.Description;

import java.io.Serializable;
import java.util.BitSet;

@Description("A simple binary problem. Override globalInfo() to insert more information.")
public abstract class SimpleProblemBinary implements InterfaceSimpleProblem<BitSet>, Serializable { }