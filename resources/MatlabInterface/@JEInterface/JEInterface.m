function int = JEInterface(interfaceName, fhandle, range, varargin)
% JavaEva Interface for Matlab
%       JEInterface(interfaceName, fhandle, range [, optset, defaultargs])
% arguments: 
%   interfaceName: a JEInterface instance needs to know its own
%       name as a String to allow callbacks from Java.
%   fhandle: a function handle defining the optimization target.
%   range: a 2 x dim array defining the solution subspace with lower and
%      upper bounds.
%   optset: (optional) an optimset struct defining optimization parameters,
%       especially tolerance and maximum function calls. Defaults to the
%       JavaEvA default values.
%   defaultArgs: (optional) additional constant argument to the target
%       function, empty by default.

int.args = [];
int.opts = optimset('MaxFunEvals', javaeva.OptimizerFactory.getDefaultFitCalls, 'TolX', 1e-4, 'TolFun', 1e-4);
int.finished = 1;
int.result = [];
int.resultArr = [];
int.callback='';
int.f = '';
int.dim = 0;
int.range = [];
int.mp = [];
int.msg = '';
int.funCalls = 0;
int.mediator = '';
int.optParams = [];
int.optParamValues = [];

if (isa(interfaceName, 'char'));
    int.callback = interfaceName;
else 
    error('Wrong first argument type, expected char');
end
if (isa(fhandle, 'function_handle'))
    int.f = fhandle;
else
    error('Wrong second argument type, expected function_handle');
end

if (isa(range, 'double') && (size(range,1) == 2))
    int.dim=length(range);
    int.range=transpose(range);
else
    error('Wrong third argument type, expected double array of 2 x dim');
end

int = class(int,'JEInterface');

switch nargin
    case {3}
    case {4,5}
        if (isa(varargin{1}, 'struct'))
            int.opts = varargin{1};
            % DONT set default values if user leaves them blank
%            if (isempty(int.opts.TolX)) ; int.opts.TolX = 1e-4; end
%            if (isempty(int.opts.TolFun)) ; int.opts.TolFun = 1e-4; end
        else
            error('Wrong fifth argument type, expected optimset struct');
        end
        if (nargin > 4)        
            int.args = varargin{2};
        end
    otherwise
        error('Wrong number of arguments!')
end

% finally create the java object
int.mp = javaeva.server.go.problems.MatlabProblem(int.callback, int.dim, int.range);

        


