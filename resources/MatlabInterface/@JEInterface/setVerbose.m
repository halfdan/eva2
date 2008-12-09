function int=setVerbose(int, bOn, varargin)
% Activate debug output for the MatlabProblem.
%      setVerbose(JI, bOn [, dbgfile])
% It is written to a file with given name or to matlabproblem-debug.log 
% if none is given.
%   JI: the interface instance
%   bOn: 1 activates debug output, 0 deactivates it
%   dbgfile: optional filename
%

if (nargin > 2)
    if ischar(varargin{1})
        fname=varargin{1};
        disp('Writing debug output to ');
        disp(fname);
    else
        disp('Invalid third argument, expected char. Using default output file name');
    end
else
    fname=null;
end
        
int.mp.setDebugOut( bOn==1, fname);

