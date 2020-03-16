# Alcor
A Hyperscale Cloud Native SDN Platform

* For information about how to use Alcor, visit [Getting Started](src/README.md)
* To ask questions, raise feature requests and get assistance from our community, join [Alcor Slack](https://alcor-networking.slack.com/) channels ([invitation](https://join.slack.com/t/alcor-networking/shared_invite/zt-cudckviu-hcsMI4LWB4cRWy4hn3N3oQ) for first-time participant) or [Google Group](https://groups.google.com/forum/#!forum/alcor-dev)
* To report an issue, visit [Issues page](https://github.com/futurewei-cloud/Alcor/issues)
* To find many useful documents, visit our [Wiki](https://github.com/futurewei-cloud/Alcor/wiki).
For example, [Kubernetes cluster setup guide with Alcor](https://github.com/futurewei-cloud/mizar-mp/wiki/K8s-Cluster-Setup-Guide-with-Mizar-MP)
shows how to use Alcor for Kubernetes container network provisioning.

In this README:

- [Introduction](#introduction)
- [Key Features](#key-features)
- [Repositories](#repositories)

## Introduction
Cloud computing means scale and on-demand resource provisioning.
As more enterprise customers migrate their on premise workloads to the cloud,
the user base of a cloud provider could grow at a rate of 10X in just a few years.
This will require a cloud virtual networking system with a more scalable and extensible design.
As a part of the community effort,
Alcor is an open-source cloud native platform that provides high availability, high performance, and large scale
virtual networking control plane and management plane at a high resource provisioning rate.

Alcor leverages the latest SDN and container technologies as well as an advanced distributed system design to
support deployment, configuration and scale-out of millions of VM and containers.
It is built based on a distributed micro-services architecture with a uniform way to secure, connect, and monitor
control plane micro-services,
and fine-grained control of service-to-service communication including load balancing, retries, failovers, and rate limits.
Alcor also offers a way to unify VM and container networking management,
and ensures ultra-low latency and high throughput due to its
application aware fast path when provisioning containers and serverless applications.

The following diagram illustrates the high-level architecture of Alcor control plane.

![Alcor architecture](docs/visionary_design/images/alcor_architecture.PNG)

Detailed design docs:

- [Alcor high level design](/docs/visionary_design/table_of_content.adoc)
- [Alcor regional controllers](/docs/visionary_design/controller.adoc)
- [Alcor control agent](https://github.com/futurewei-cloud/AlcorControlAgent/blob/master/docs/design.adoc)

## Key Features

### Cloud-Native Architecture
Alcor leverages Kubernetes and Istio to build its distributed _micro-services_ architecture.
Depending on the control plane load, Alcor Controller scales out with multiple instances and each instance is a Kubernetes application.
One step further, each application contains various infrastructure microservices to manage different types of network resources.

### Throughput-Optimal Design
Alcor focuses on top-down throughput optimization on every system layer including API, Controller, messaging mechanism,
and host agent.
For example,
a batch API is provided to support deploying a group of ports with a single POST call, and
a message batching mechanism is proposed on a per-host basis, which is capable of driving groups (potentially thousands)
of resources to the same host in one shot.

### Fast Resource Provisioning
To support time-critical applications, Alcor enables a direct communication channel from Controller to Host Agent.
This channel bypasses a message queueing system like Kafka, and utilizes gRPC to offer 10x latency improvement compared to Kafka.

<!-- ### Large-Scale Network Resource Management-->
<!-- ### Unified Management for VM and Containers-->

### Planned Features

The planned features are listed in our current roadmap:
1. Major VPC features (e.g., security group, ACL, QoS)
2. Intelligent network function placement and auto-scaling
3. Compatibility with OVS
4. Controller Grey release
5. New resource tagging framework, and many more...

## Repositories
The Alcor project is divided across a few GitHub repositories.

- [alcor/alcor](https://github.com/futurewei-cloud/Alcor):
This is the main repository of Alcor Regional Controller that you are currently looking at.
It hosts controllers' source codes, build and deployment instructions, and various documents that detail the design of Alcor.

- [alcor/alcorcontrolagent](https://github.com/futurewei-cloud/AlcorControlAgent):
This repository contains source codes for a host-level stateless agent that connects regional controllers to the host data-plane component.
It is responsible for programming on-host data plane with various network configuration for CURD of _VPC, subnet, port, Security group etc._,
 and monitoring network health of containers and VMs on the host.

- [alcor/integration](https://github.com/futurewei-cloud/mizar-mp)
The integration repository contains codes and scripts for end-to-end integration of Alcor control plane with popular orchestration platforms and data plane implementations.
We currently support integration with Kubernetes (via CNI plugin) and Mizar Data Plane.
We will continue to integrate with other orchestration systems and data plane implementations.

As a reference, Alcor supports a high performance cloud data plane [Mizar](https://github.com/futurewei-cloud/Mizar),
which is a complementary project of Alcor.

