function v = getResult(int)
% Returns the optimization solution if the run has been finished, or an
% intermediate solution if the run has not finished yet or an empty array
% if there is no intermediate solution yet.

if (isFinished(int)) 
    v = int.result;
else
    v = int.mp.getIntermediateResult();
end