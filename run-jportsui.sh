#!/bin/bash
# run JPortUI .JAR as Posix compliant Bash shell script without deploying Ant, Maven, etc.
JAR_APP="JPortsUI.jar"

if [ ! -e $JAR_APP ]
then
# show basic info for tier 1 support
echo 
echo "###################################################################################################"
echo "Execution environment"
echo `uname -a ; javac -version`

# non-committed changes
echo 
echo "==================================================================================================="
echo "Files with pending changes"
hg stat | grep ^[RAMC]

# this a temp destination for building and running as a demo, make all [-p]arent dirs (Posix/MacOsX switch)
DISTRO="TEMP"
mkdir -p $DISTRO/

# bring in copy of sources and all resources [-R]ecursively
cp -R src/* $DISTRO/

# Posix does not have -target so a change dir is required, i.e. Posix 'ln' can not "-target $DISTRO/"
cd $DISTRO

# use build date as version
date "+%Y.%m.%d" > build-date.txt

# compile (After the design is approved, this will be built and placed in distribution via 'Make')
echo 
echo "==================================================================================================="
SOURCE="`find . -type f -iname '*.java' | sort`"
echo "Compiling `wc -l $SOURCE | grep total` lines from `echo $SOURCE | wc -w` source files with `which javac`"

# invoke a single instance of the java compiler to generate vm byte code '.class' files
WARNINGS="-Xlint:all"
ALL_CLASS_PATHS="-classpath .:$CLASSPATH"
DESTINATION="-d ."
javac $WARNINGS $ALL_CLASS_PATHS $DESTINATION -g $SOURCE

# archive all rsrc & .class
echo 
echo "==================================================================================================="
CLASSE="`find . -type f -name '*.class'`"
echo "Archiving `echo $CLASSE | wc -w` classes"

# remove ../TEMP/ copied sources from being jarred, careful with that
rm -f $SOURCE

# no compression with option '0', JDK5 requires jar cMf  ../$JAR_APP *.class jport
jar cMf ../$JAR_APP *
echo "`ls -oshakl ../$JAR_APP`"

# done building
echo 
echo "==================================================================================================="
cd ..
rm -rf $DISTRO
fi

if [ -e $JAR_APP ]
then
echo "Built `ls -o $JAR_APP`"
java -jar $JAR_APP &
fi

# exit
