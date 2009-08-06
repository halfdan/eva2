# This makefile builds JMatLink library on RH7.3.
# Joerg K. Wegner 27/09/02
#
# for using this feature
# setenv MATLAB /afs/informatik.uni-tuebingen.de/i386_rh62/ra/Matlab-6.1
# setenv LD_LIBRARY_PATH ${LD_LIBRARY_PATH}:$MATLAB/extern/lib/glnx86/

CC = gcc
LD = ld

# MATLAB
MATLAB = /afs/informatik.uni-tuebingen.de/i386_rh62/ra/Matlab-6.1
MLIB = -L$(MATLAB)/extern/lib/glnx86
MINCLUDE = -I$(MATLAB)/extern/include

# JAVA
JAVA = /usr/java/j2sdk1.4.0
JINCLUDE = -I$(JAVA)/include -I$(JAVA)/include/linux

libJMatlink.so  : JMatLink.o
	$(LD) -G $(MLIB) -leng -lmx -o libJMatLink.so JMatLink.o
	\rm JMatLink.o
#	\mkdir ../../../../../build
#	\mv libJMatLink.so ../../../../../build

JMatLink.o      : JMatLink.c JMatLink.h
	$(CC) -c -shared $(JINCLUDE) $(MINCLUDE) JMatLink.c
