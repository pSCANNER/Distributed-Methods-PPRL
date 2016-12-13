# Compiler and options
CC      = javac
FLAGS   = -Xlint:unchecked 

# Directory Variables
SRCDIR	= src/
BINDIR	= bin/
LIBDIR	= lib/

# Source Variable
SOURCES = $(wildcard $(SRCDIR)*.java)

all:
	javac -cp "$(LIBDIR)*" -d $(BINDIR) $(FLAGS) $(SOURCES)

