function int=runEvalLoopJE(int, optOrPostProc, optType, outputFilePrefix, steps, sigmaClust, nBest)
% Internal method starting a EvA2 optimization loop.
% Calling this directly may interfere with optimization.

% This function handles the communciation between JE and Matlab, main
% optimization configuration is done in the
% optimize/optimizeWith/postProcess functions from which this one should be
% called.
% optOrPostProc: 1 for optimize, 2 for postProcess
% optType, outputFilePrefix are parameters for optimize, dont care when
% postprocessing.
% steps, sigmaClust and nBest are parameters for postProcess, dont care
% when optimizing. nBest may be -1 to show all.

global stopOptimization
global JEMediator

if ~isempty(int.mediator)
    int.mediator.quit
    int.mediator='';
end

% set up a mediator and inform JE
int.mediator = eva2.server.go.problems.MatlabEvalMediator;
int.mp.setMediator(int.mediator);
JEMediator=int.mediator;

% start the JE thread
if (optOrPostProc == 1)
    stopText='Stop EvA2 optimization';
    int.mp.optimize(optType, outputFilePrefix, int.optParams, int.optParamValues);
else % post processing
    stopText='Stop EvA2 post processing';
    int.mp.requestPostProcessing(steps, sigmaClust, nBest);
end

% handle the case when the optimization has been called from a users script
% and not from the toolboxes parameter estimation function (which has an
% own stop button). we decide this by checking if the global variable
% stopOptimization is empty. if it is then it is not the toolbox calling
% and we create an own button to stop it.
if isempty(stopOptimization),    
    % set switch to 0 now
    stopOptimization = 0;
    
    % create a cancel button box (case without SBtoolbox)
    boxHandle=figure('Position',[100 600 250 80], 'MenuBar', 'none', 'Name', 'EvA2 optimization running...', 'NumberTitle','off');
    uicontrol(boxHandle,'Style', 'pushbutton', 'String', 'Cancel', 'Position', [25 25 60 30], 'Callback', 'global stopOptimization; stopOptimization=1;');
    uicontrol(boxHandle,'Style', 'text', 'String', stopText, 'Position', [100 25 120 30]);
    drawnow;
    % set flag for non toolbox optimization
    nontoolboxopt = 1;
else
    % disp('seems like the toolbox is going on');
    % its an estimation using the toolbox' parameter estimation thing
    nontoolboxopt = 0;
end

stopOnce=1;

% repeat the mediator thread and eval call until finished
try
    while (~int.mediator.isFinished())
        int.mediator.run;
        if (~int.mediator.isFinished())
            x = int.mediator.getQuestion();
            if (isempty(int.range))
                %size(x)
                x=convertUnsignedJE(int, x);
                %disp('here B');
                %x
            end
%            size(x)
            try
                if (isempty(int.args))
                    res = feval(int.f, x);
                else
                    res = feval(int.f, x, int.args);
                end
                %res
            catch ME
                disp('function evaluation failed:');
                disp(ME.message);
                stopOptimization=1;
            end
            int.mediator.setAnswer(res);
            drawnow;
            if ((stopOptimization==1) && (stopOnce==1))
                disp('User interrupt requested ...');
                stopOptimize(int);
                stopOnce=0;
            end
        end
    end
    clear global JEMediator;
catch ME
    disp('Error in evaluate!');
    disp(ME.message);
    %int.mediator.quit; % just in case
    %int.mediator='';
    
    % why should this be done more than once in the end?
    %if (nontoolboxopt == 1)
    %    if (ishandle(int.boxHandle)) , close(int.boxHandle); int.boxHandle=''; end
    %    clear global stopOptimization
    %end
end

% write back results
int=setResultJE(int, int.mediator.getSolution());
int=setResultArrayJE(int, int.mediator.getSolutionSet());

int.mediator.quit; % just in case
int.mediator='';

% handle the case when the optimization has been called from a users script
% and not from the toolboxes parameter estimation function (which has an
% own stop button). we decide this by checking nontoolboxopt
if nontoolboxopt == 1,
    if (ishandle(boxHandle)) , close(boxHandle); end
    clear global stopOptimization
end
