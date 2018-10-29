JAVA_FILES:=$(wildcard src/main/java/daProc/*.java)
#
# the rest is independent of the directory
#
JAVA_CLASSES:=$(patsubst %.java,%.class,$(JAVA_FILES))

.PHONY: classes
LIST:=

classes: $(JAVA_CLASSES)
		javac $(JAVA_CLASSES) ;

MAIN:=$(src/main/java/daProc/FIFOBroadcast)

default: classes

$(JAVA_CLASSES) : %.class : %.java
	$(eval LIST+=$$<)

run: $(MAIN).class
	$(JVM) $(MAIN)

clean:
	$(RM) *.class