CURPKG=glei_0.2.tar.gz
CURJAVAPKG=glei-0.1-jar-with-dependencies.jar

${CURPKG}: ../target/${CURJAVAPKG} glei/R/*.R glei/man/*.Rd glei/DESCRIPTION glei/NAMESPACE
	rm -Rf glei/inst/java
	mkdir -p glei/inst/java
	cp $< glei/inst/java
	R CMD build glei
	R CMD check glei

all: ${CURPKG}

../target/${CURJAVAPKG}:
	mvn -f ../pom.xml package

clean:
	-rm ${CURPKG}
	-rm -Rf glei/inst

install: all
	R CMD INSTALL ${CURPKG}
