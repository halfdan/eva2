function [ z ] = convertUnsignedJE( int, x )
%CONVERTUNSIGNEDJE Convert signed 32-bit integer to unsigned.
%   Detailed explanation goes here

z=zeros(size(x,1),size(x,2), 'uint32');
for j=1 : size(x,1)
    for i=1 : size(x,2) 
        if (x(j,i) < 0) 
            z(j,i) = 1+bitxor(uint32(-x(j,i)), int.hexMask);
        else
            z(j,i) = x(j,i);
        end
    end
end