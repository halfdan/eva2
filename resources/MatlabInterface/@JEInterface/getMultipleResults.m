function [sols, fits] = getMultipleResults(int)
% Returns a set of optimization solutions if the run has been finished, or
% a singel intermediate solution if the run has not finished yet or an
% empty array if there is no intermediate solution yet.

if (isFinished(int)) 
    sols = int.resultArr;
else
    sols = int.mp.getIntermediateResult();
end

fits=zeros(size(sols,1),1);

for i=1:size(sols,1) 
    %disp(sols(i,:));
    if (isempty(int.args))
        fits(i) = feval(int.f, sols(i,:));
    else
        fits(i) = feval(int.f, sols(i,:), int.args);
    end
end