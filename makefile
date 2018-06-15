BROWSER=x-www-browser
BASEDIR=/home/allfre2/backupnew/proyectos/practica/java/musicTools/src/main/java/com/allfre2/musicruler

ARGS=$(BASEDIR) $(BASEDIR)/songs $(BASEDIR)/tools

build:
	@javac *.java

run: build
	@java ClassDG $(ARGS)

clean:
	@rm *.class data.js

test: build run
	@$(BROWSER) index.html &>/dev/null &
