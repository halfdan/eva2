function retInt = optimize(int, optType, varargin)
% Start a JavaEvA optimization run.
%       optimize(interface, optType, [, outputFilePrefix ] )
% where
%       interface: instance of JEInterface
%       optType: integer indicating the type of the optimization strategy
%       to use.
%       resultFilePrefix: (optional) char prefix for an optional verbose
%           output file

if (int.finished == 0) 
    error('please wait for the current run to finish');
end
if ((nargin == 2) || (nargin == 3))
    if (nargin == 3) 
        outputFilePrefix = varargin{1};
    else
        outputFilePrefix = 'none';
    end

    if (~isa(int, 'JEInterface') || ~isscalar(optType) || ~isa(outputFilePrefix, 'char'))
        error('Invalid argument!')
    end
    int.finished = 0;
    int.msg = 'running...';
    % adapt options possibly changed by the user, concerning
    xTol = int.opts.TolX;
    maxEvals = int.opts.MaxFunEvals;
    fTol = int.opts.TolFun;
    % construct Terminators
    import javaeva.server.go.operators.terminators.PhenotypeConvergenceTerminator;
    import javaeva.server.go.operators.terminators.FitnessConvergenceTerminator;    
    import javaeva.server.go.operators.terminators.CombinedTerminator;
    import javaeva.server.go.operators.terminators.EvaluationTerminator;

    % set some default values if theyre not given
    if (isempty(int.opts.TolX)) ; xTol = 1e-4; end
    if (isempty(int.opts.TolFun)) ; fTol = 1e-4; end
    % fminsearch, for example, always uses TolX and TolFun with default
    % values of 1e-4 in . Thats what we do as well
    convTerm = CombinedTerminator(FitnessConvergenceTerminator(fTol, int32(5), 0, 1), PhenotypeConvergenceTerminator(xTol, 5, 0, 1), 1);

    % if MaxFunEvals is defined additionally, combine an
    % EvaluationTerminator in disjunction, as Matlab does.
    if (~isempty(maxEvals))
        javaeva.OptimizerFactory.setTerminator(convTerm);
        javaeva.OptimizerFactory.addTerminator(EvaluationTerminator(maxEvals), 0);
    end
    
    int.mp.optimize(optType, outputFilePrefix);
else
    error('Wrong number of arguments!')
end
retInt=int;
