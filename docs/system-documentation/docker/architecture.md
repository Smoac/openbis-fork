# Architecture

We highly recommend to run **openBIS** as a light-weigt Docker container, fostering portability across environments and platforms.


## Requirements

Please refer to the official documentation pages on Docker Engine (aka Docker CE) to learn more about requirements and installation instructions of the packages needed for running docker containers: [https://docs.docker.com/engine/install/](https://docs.docker.com/engine/install/)

We recommend to run the openBIS docker container on top of Ubuntu server for running the application in production: [read more](../standalone/system-requirements.md)

Read more on Docker architecture if you are still unfamiliar with its core concepts: [https://docs.docker.com/get-started/overview/#docker-architecture](https://docs.docker.com/get-started/overview/#docker-architecture)


## Application Layout

OpenBIS can be split into distinct sub-units, which are virtualized either all-in-one or within multiple Docker containers. Independent of the scenario, we recommend clients to communicate with the application via a reverse proxy:

![openBIS Docker User](../../_static/docker_openbis_user.svg)