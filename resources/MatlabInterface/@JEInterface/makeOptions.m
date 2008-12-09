function opts = makeOptions(int, varargin)
% Create a JEInterface options set from scratch. Possible fields are:
%'Display';
%'MaxFunEvals';'MaxIter';'TolFun';'TolFunEvals';TolX';'TolXEvals', where
% all but 'TolFunEvals', 'TolXEvals' are used similar to the optimset. 
% The latter two are interpreted as the numbers of evaluations required
% to assume convergence. Default values are TolXEvals=TolFunEvals=200,
% TolX=TolFun=1e-4, MaxFunEvals uses a default from EvA2.
% Notice that this method creates a parameter set but does not assign it
% to the interface instance. Use setOptions to do that.

allfields = {'Display'; 'MaxFunEvals';'MaxIter';'TolFun';'TolFunEvals';...
    'TolX';'TolXEvals'};

specialfields={'TolFunEvals', 'TolXEvals'};

nvararg=nargin-1;
if rem(nvararg,2)==1
    error('Pass options in name-value pairs!');
end

% create cell array
structinput = cell(2,length(allfields));
% fields go in first row
structinput(1,:) = allfields';
% []'s go in second row
structinput(2,:) = {[]};
% turn it into correctly ordered comma separated list and call struct
opts = struct(structinput{:});

stdSet=optimset();

% standard options:
opts.('MaxFunEvals') = eva2.OptimizerFactory.getDefaultFitCalls;
opts.('TolX') = 1e-4;
opts.('TolXEvals') = 200;
opts.('TolFun') = 1e-4;
opts.('TolFunEvals') = 200;

for i=1:nvararg/2
    name=varargin{2*i-1};
    value=varargin{(2*i)};
    % parse arguments
    if ~ischar(name)
        error('Expected char parameter name at index %d!', 2*i+1);
    else
        optIndex=strmatch(name,allfields, 'exact');
        if isempty(optIndex)
            error('Unknown option %s !', name);
        else
            if ~isempty(strmatch(name, specialfields,'exact'))
                % test for integer
                if (~isscalar(value) || ~isnumeric(value) || round(value)<1)
                    error('invalid value type for %s, expecting numeric scalar > 1!', name);
                end
                value=round(value);
            else
                % test using optimset
                optimset(stdSet, name, value);
            end
            % assign to struct
            opts.(allfields{optIndex,:}) = value;
        end
    end
end