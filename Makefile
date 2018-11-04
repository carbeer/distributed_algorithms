JAVA_FILES:=$(wildcard src/main/java/daProc/*.java)
JAVA_FILES+=${wildcard src/main/java/utils/*.java}
JAVA_FILES+=${wildcard src/main/java/*.java}
JVM=java
#
# the rest is independent of the directory
#
JAVA_CLASSES:=$(patsubst %.java,%.class,$(JAVA_FILES))
COMPILED_TARGET:=.
MAIN:=Da_proc

.PHONY: classes
LIST:=

classes: $(JAVA_CLASSES)
		javac -Xlint:none -d ${COMPILED_TARGET} $(LIST) ;

$(JAVA_CLASSES) : %.class : %.java
	$(eval LIST+=$$<)

run: ${COMPILED_TARGET}/$(MAIN).class
	$(JVM) -cp ${COMPILED_TARGET} $(MAIN)

clean:
	$(RM) ${COMPILED_TARGET}/*.class
	rm -rf daProc
	rm -rf utils