function int = JEInterface(fhandle, range, varargin)
% EvA2 Interface for Matlab
%       JEInterface(fhandle, range)
%       JEInterface(fhandle, range, defaultargs)
%       JEInterface(fhandle, range, defaultargs, options...)
%
% Arguments: 
%   fhandle: a function handle defining the optimization target.
%   range: a 2 x dim array defining the solution subspace with lower and
%      upper bounds; or a scalar defining the bitwidth for binary
%      problems.
%   defaultArgs: (optional) additional constant argument to the target
%       function, empty by default.
%   options: (optional) options as name-value pairs defining optimization parameters,
%       especially tolerance and maximum function calls.
%       Check makeOoptions for default settings.
%
% Further options may be specified using setOptions with a JE options struct.
% Main options are:
%   TolX: convergence criterion in the solution space
%   TolFun: convergence criterion in the target space
%   MaxFunEvals: maximum number of function evaluations
%   Display: 'off'/'final'/'notify'/'iter', where 'notify' corresponds to
%       displaying every k-th iteration, with k=10 as default.
% The termination criterion of a run is a combination of the TolX, TolFun and
% MaxFunEvals criteria. The run terminates if MaxFunEvals has been reached
% or the best solution changes both in domain and codomain less than TolX 
% and TolFun for a certain number of evaluations, which may be set using 
% TolXEvals and TolFunEvals, respectively.
% To ignore a criterion, set it to 0. E.g. to perform 10^5 evaluations in
% any case, set TolX=TolFun=0 and MaxFunEvals=10^5.
%
% Define a 2 x dim range with a double valued function, or for
% binary problems set a scalar as range defining the number of bits to be
% used. The values passed to the function handle will then be arrays of 
% uint32, each of them representing 32 bits.

int.args = [];
int.opts = [];
int.finished = 1;
int.result = [];
int.resultArr = [];
int.f = '';
int.dim = 0;
int.range = [];
int.mp = [];
int.msg = '';
int.funCalls = 0;
int.mediator = '';
int.optParams = [];
int.optParamValues = [];
int.hexMask=hex2dec('ffffffff');

if (isa(fhandle, 'function_handle'))
    int.f = fhandle;
else
    error('Wrong second argument type, expected function_handle');
end

disp('Setting up JEInterface...');
if (isa(range, 'double') && (size(range,1) == 2))
    int.dim=size(range,2);
    int.range=transpose(range);
    s = sprintf('Double valued search space, dimension: %d', int.dim);
    disp(s);
else
    if (length(range)==1)
        int.range=[];
        int.dim=range;
        s = sprintf('Binary valued search space, dimension: %d', int.dim);
        disp(s);
    else 
        error('Wrong third argument type, expected double array of 2 x dim (double ranges) or scalar (binary bit width).');
    end
end

int = class(int,'JEInterface');
int.opts = makeOptions(int);

if (nargin>2)
    int.args = varargin{1};
    disp('Fitness function argument: '); disp(int.args);
    if (nargin > 3)
        if (rem(nargin,2)==0)
            error('Invalid number of arguments!');
        end
        disp('Reading options:');
        for i=2:2:nargin-2
            int=setOpt(int, varargin{i}, varargin{i+1});
        end
    end
end  
display(getOptions(int));
% finally create the java object
int.mp = eva2.server.go.problems.MatlabProblem(int.dim, int.range);
disp('Java object created');

testEvalFunc(int);

        


