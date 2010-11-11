function testEvalFunc(int) 
% Test the fitness function output format.
wordwidth=32;

if (isempty(int.range))
    % binary problem
    s=sprintf('Binary problem of bitwidth %d', int.dim);
    disp(s);
    numInts=ceil(int.dim/wordwidth);
    % generate trial vector
    x=ceil(rand(1,numInts).*(2^wordwidth));
    overheadBits=numInts*wordwidth-int.dim;
    x(numInts)=bitshift(x(numInts),-overheadBits); % shift right by overhead
else
    % double problem
    x=rand(1, int.dim);
    s=sprintf('Real valued problem in %d dimensions and range %s ', int.dim, mat2str(int.range));
    disp(s);
    for i=1:int.dim
        x(i)=int.range(i,1)+x(i)*(int.range(i,2)-int.range(i,1));
    end
end

if (isempty(int.range))
    msg=sprintf('\nTesting value: %d, bin.: %s', x, dec2bin(x, int.dim));
else
    msg=sprintf('\nTesting value: %d', x);
end
disp(msg);

try
    if (isempty(int.args))
        res = feval(int.f, x);
    else
        res = feval(int.f, x, int.args);
    end
catch ME
    disp('Function evaluation failed:');
    disp(ME.message);
    error(['Test failed! ' ME.message]);
end
            
disp('Function returned: ');
disp(res);

if (min(size(res)) > 1)
    disp('Warning: unable to optimize matrix representations - use 1 times m output only');
else
    if (size(res,1)>1)
        disp('Warning: use dimensions 1 times m instead of m times 1');
    else
        if ~(sum(isnumeric(res))==size(res,1)*size(res,2))
            disp('Warning: result seems to contain non-numeric elements!');
        else
            disp('Ok.');
        end
    end;
end

    