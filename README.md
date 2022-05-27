# Yet another todo application

## Prerequisites

Install the following tools:

- Docker
- Maven
- Java JDK11

Also you need a Google account.

## How to run

Create a jar file by executing

```
mvn install
```
in the project directory.

After that build the docker image by using

```
docker build -t todoapp .
```

this will create a docker image with the current jar using the provided Dockerfile.

To run the application just use

```
docker run -p <hostPort>:<containerPort> todoapp
```

e.g.

```
docker run -p 1234:8080 todoapp
```

and the application will be reachable at localhost:1234 in your browser. 

NOTE: Currently the login will only work on port 6001, since this port is registered in the Google Cloud. 

To update an already running container, the updateDocker.sh can be used from the project directory. 

