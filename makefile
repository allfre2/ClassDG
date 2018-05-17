BROWSER=x-www-browser
TestDirectory= testCycles/

build:
	@javac *.java

run:
	@java ClassDG $(TestDirectory)

clean:
	@rm *.class data.js

test: build run 
	@$(BROWSER) index.html &>/dev/null &
