package eva2.problems.simple;

import eva2.util.annotation.Description;

import java.io.Serializable;

@Description("A simple double valued problem. Override globalInfo() to insert more information.")
public abstract class SimpleProblemDouble implements InterfaceSimpleProblem<double[]>, Serializable {}
