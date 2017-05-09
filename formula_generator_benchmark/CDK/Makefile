all:    cdk-1.5.15.jar CDKFormulaGeneratorCLI.class

cdk-1.5.15.jar:
	wget "https://github.com/cdk/cdk/releases/download/cdk-1.5.15/cdk-1.5.15.jar"

CDKFormulaGeneratorCLI.class:
	javac -cp "./*" CDKFormulaGeneratorCLI.java
clean:
	rm -f *.class *.jar

