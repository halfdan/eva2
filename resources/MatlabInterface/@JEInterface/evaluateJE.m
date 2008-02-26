function v = evaluate(int)
% Interface function for JavaEvA 2.
% Will be called by the MatlabProblem to obtain the target function value
% for a sample individual.

x = int.mp.getCurrentDoubleArray;
if (isempty(int.args))
    int.mp.setResult(feval(int.f, x));
else
    int.mp.setResult(feval(int.f, x, int.args));
end
