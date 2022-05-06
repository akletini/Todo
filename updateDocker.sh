#!/bin/bash
timestamp=$(date +%d-%m-%Y\ %H:%M:%S)
echo $timestamp Rebuilding Todo-JAR
rm target/Todo-0.0.1-SNAPSHOT.jar

mvn clean install

install_path=target/Todo-0.0.1-SNAPSHOT.jar
if test -f "$install_path"; then
  echo "Found Todo jar"

echo $timestamp Built new jar
echo $timestamp Copying new jar to container
winpty docker cp ~/Todo/target/Todo-0.0.1-SNAPSHOT.jar todoapp://

echo $timestamp Copy new jar into docker container
echo $timestamp Rename jar within container
winpty docker exec -it todoapp sh -c "rm app.jar; mv Todo-0.0.1-SNAPSHOT.jar app.jar"

echo $timestamp Restarting container

docker restart todoapp

echo $timestamp Done!
else
  RED='\033[0;31m'
  NC='\033[0m' # No Color
  echo -e "\n ############################################"
  echo -e "\n ${RED} Build failed. Check logs!!! ${NC}"
  echo -e "\n ############################################"
fi
