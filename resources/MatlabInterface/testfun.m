function z = testfun(x, y)
switch y
    case 1 % modulated parabola
        z (1)=sum(x.*x)+cos ( x ( 1 ) ) * sin ( x ( 2 ) ) ;
    case 2 % Branin
        z ( 1 ) = ( x(2)-(5/(4*pi ^2))* x(1)^2+5*x (1)/ pi -6)^2+10*(1-1/(8* pi ) )* cos ( x (1) )+10;
    case 3 % Himmelblau
        z ( 1 ) = ( ( x (1)^2 + x ( 2 ) - 11)^2 + ( x ( 1 ) + x (2)^2 - 7 )^2 ) ;
    case 4 % simple binary: changed to a char data type
        z(1)=0;
        for i=1:length(x)
            if (x(i)=='1') ; z(1)=z(1)+1; end
        end
    case 5 % simple parabola
        z (1)=sum( x .* x ) ;
end
