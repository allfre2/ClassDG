BROWSER=x-www-browser

build:
	@javac *.java

clean:
	@rm *.class data.js

test:
	@$(BROWSER) index.html &>/dev/null &
