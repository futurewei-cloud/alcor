# Alcor - Cloud Networking Control Plane

Cloud means scale and on-demand resource provisioning.
As more and more enterprise customers migrate their on premise workloads to the cloud,
its user base could grow at a rate of 10X in just a few years,
which would have required scalable and extensible design for cloud virtual networking platform.
As a part of the community effort,
Alcor is an open-source platform to provide high availability, high performance, and large scale
virtual network control plane at a high resource provisioning rate.

Alcor leverages latest SDN and container technologies as well as an advanced distributed system design to
support deployment, configuration and scale-out of millions of VM and containers.
It is built based on a distributed micro-services architecture with a uniform way to secure, connect, and monitor
control plane micro-services,
and fine-grained control of service-to-service communication including load balancing, retries, failovers, and rate limits.
Alcor as a platform also offers a unified VM and container networking management
with application aware fast path to provision containers and serverless applications
in ultra-low latency and high throughput manner.

Detailed design docs:

- [Alcor regional controllers](/docs)
- [Alcor control agent](https://github.com/futurewei-cloud/AlcorControlAgent)
- [Alcor & Mizar integration] (Coming soon)

As a reference, Alcor supports a high performance cloud data plane [Mizar](https://github.com/futurewei-cloud/Mizar).