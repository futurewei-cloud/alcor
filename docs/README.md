# Getting Started

Alcor is a high-performance and large-scale implementation of the next-generation cloud networking control plane.
Please follow the following instruction to set up your development environment and to deploy Alcor.

## Setting up a Development Environment

The Alcor controller project currently uses _Apache Maven_ to manage the dependencies and its build.
We recommend using an Ubuntu, Mac OS X or Windows OS for development and functionality testing.
To set up your local development environment, we recommend to use fork-and-branch git workflow.

1. Fork Alcor Github repository by clicking the Fork button on the upper right-hand side of Alcor home page.
2. Make a local clone:
    ```
    $ git clone https://github.com/<your_github_username>/alcor.git ~/alcor
    $ cd ~/alcor
    $ git submodule update --init --recursive
    ```
3. Add a remote pointing back to the Alcor Official repository
    ```
    $ git remote add upstream https://github.com/futurewei-cloud/alcor.git
    ```
4. Always keep your forked repo (both local and remote) in sync with upstream. Try to run the following commands daily:
    ```
    $ git checkout master
    $ git pull upstream master
    $ git push origin master
    ```
5. Work in a feature branch
    ```
    $ git checkout -b <new_branch_name>
    $ ... (make changes in your branch)
    $ git add .
    & git commit -m "commit message"
    ```
6. Push changes to your remote fork
    ```
    $ git push origin <new_branch_name>
    ```
7. Open a Pull Request on Alcor home page, notify community on [Alcor Slack](https://alcor-networking.slack.com/) channels.
You will need approval from at least one maintainer, who will merge your codes to Alcor master.
8. Clean up after a merged Pull Request
    ```
    $ git checkout master
    $ git pull upstream master
    $ git push origin master
    $ git branch -d <branch_name>
    $ git push --delete origin <branch_name>
    ```

## Building Alcor

To compile, run, and test Alcor, please follow the following steps:

Run the build script to install all needed packages, and clean install the project for development.
```
Windows:
PS > .\scripts\build.ps1

Ubuntu or Mac OS X:
$ ./scripts/build.sh
```

Compile and run tests.
The make test step will run both unit and functional test.
If this step passes, then you have everything needed to develop, test, and run Alcor.
```
$ mvn test
```

## Deploying Alcor Controller

NOTE: This section is outdated. Please follow up with the community on [Alcor Slack](https://alcor-networking.slack.com/) channels
for latest instruction before the instructions get updated.

The previous steps allow you to compile and install Alcor.
If you are interested in deploy alcor controller and its associated components (e.g. DB and cache) as docker containers,
you can follow the next steps on any Docker supported operating system:

Set up docker images with the dependencies and run docker containers.
```
Windows:
PS > .\scripts\deploy.ps1

Ubuntu or Mac OS X:
$ ./scripts/deploy.sh
```

## Sanity Test

Test if your local controller is up.
```
curl localhost:8080/actuator/health
{"status":"UP"}
```
Now you are ready to use Alcor Controller.


## Creating First VPC

To create your first VPC, you can deploy a sample VPC with one subnet and one port with the following script.
```
Ubuntu:
$ ./scripts/sampeVpcTest.sh localhost 8080 false
```

Next Step:
- [Install Control Agents](https://github.com/futurewei-cloud/alcor-control-agent/blob/master/src/README.md)
- [API Document](./apis/index.adoc)
- [Alcor Design Documents](./design/table_of_content.adoc)
- [Kubernetes cluster setup guide](https://github.com/futurewei-cloud/alcor-int/wiki/K8s-Cluster-Setup-Guide-with-Mizar-MP)
