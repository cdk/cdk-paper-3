
.PHONY: cdk

all:	lib/cdk-1.4.19.jar cdk

lib/cdk-1.4.19.jar:
	curl https://netcologne.dl.sourceforge.net/project/cdk/cdk/1.4.19/cdk-1.4.19.jar > cdk-1.4.19.jar

cdk:
	mvn clean install -DskipTests
