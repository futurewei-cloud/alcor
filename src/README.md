# Getting Started

Alcor is a high-performance and large-scale implementation of the next-generation cloud networking control plane.
Please follow the following instruction to set up your development environment and to deploy Alcor.

## Setting up a Development Environment

The Alcor controller project currently uses _Apache Maven_ to manage the dependencies and its build.
We recommend using an Ubuntu or Windows OS for development and functionality testing.
To compile, run, and test Alcor, please follow the following steps:

Clone the Alcor repository.
```
$ git clone https://github.com/futurewei-cloud/alcor.git ~/alcor
$ cd ~/alcor
$ git submodule update --init --recursive
```

Run the build script to install all needed packages, and clean install the project for development.
```
Windows:
PS > .\scripts\build.ps1

Ubuntu:
$ ./scripts/build.sh
```

Compile and run tests.
The make test step will run both unit and functional test.
If this step passes, then you have everything needed to develop, test, and run Alcor.
```
$ mvn test
```

### Deploying Alcor Controller

The previous steps allow you to compile and install Alcor.
If you are interested in deploy alcor controller and its associated components (e.g. DB and cache) as docker containers,
you can follow the next steps on any Docker supported operating system:

Set up docker images with the dependencies and run docker containers.
```
Windows:
PS > .\scripts\deploy.ps1

Ubuntu:
$ ./scripts/deploy.sh
```

Test if your local controller is up.
```
curl localhost:8080/actuator/health
{"status":"UP"}
```

Now you are ready to use Alcor Controller.

Next Step: [API Document](../docs/apis/index.adoc)