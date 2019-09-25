Alcor is a high-performance and large-scale implementation of the next-generation cloud networking control plane.

## Setting up a Development Environment

We currently use _maven_ to build the controller project and manage its dependencies.
We recommend using an ubuntu-based host for development and functionality testing.
To compile, test, and experiment Alcor on an existing host, please follow the following steps:

1. Clone the Alcor repository
```
$ git clone https://github.com/futurewei-cloud/alcor.git ~/alcor
$ cd ~/alcor
$ git submodule update --init --recursive
```

Run the bootstrap script to install all needed packages for development, 
and create a docker container _alcor:controller_ to enable you to start running the functional tests.
```
$ ./scripts/build.sh
```
Compile and run tests. The make test step will run both unit and functional test. If this step passes, then you have everything needed to develop, test, and run Mizar on a single box!
```
$ mvn test
 ```
(Optional) You may want to run unit tests and get coverage reports by:
```
$ make run_unittests
$ make lcov
```
(Optional) To execute functional tests only, run:
```
$ make functest
```
(Optional) To execute a specific functional test
```
make run_func_test TEST=<test_class_name>
```

### Compiling and Running unit tests Only

The previous steps allow you to run the functional tests as well as compiling Mizar. If you are interested only in rapidly compiling the code and running unit tests, you can follow the next steps on any Docker supported operating system:

Set up a docker image with the dependancies
```
sudo docker build -f ~/Mizar/test/Dockerfile -t buildbox:v2 ~/Mizar/test
```
Run the buildbox container:
```
sudo docker run -it -v ~/Mizar:/Mizar buildbox:v2
```
Inside the container, /Mizar will point to the same directory ~/Mizar on your host!

You can then run __make__ and __make lcov__ normally from the container. You will not be able to run __make test__ as functional tests will not run.

