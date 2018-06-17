BROWSER=x-www-browser

#For tests
BASEDIR=/home/allfre2/backupnew/proyectos/practica/java/musicTools/src/main/java/com/allfre2/musicruler
ARGS=$(BASEDIR) $(BASEDIR)/songs $(BASEDIR)/tools
TESTDIR=$(ARGS)

build:
	@javac *.java

run: build
	@java ClassDG $(TESTDIR)

clean:
	@rm *.class data.js

test: build run
	@$(BROWSER) index.html &>/dev/null &
