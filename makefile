BROWSER=x-www-browser
TestDirectory= $(projdir)/songs

build:
	@javac *.java

run:
	@java ClassDG $(TestDirectory)

clean:
	@rm *.class data.js

test: build run 
	@$(BROWSER) index.html &>/dev/null &
