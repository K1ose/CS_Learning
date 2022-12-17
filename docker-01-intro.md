---
title: docker_01_intro
top: false
comment: false
lang: zh-CN
date: 2022-11-14 16:23:07
tags:
categories:
  - docker
---

# Introduction

## 简介

Docker 最初是由 dotCloud 公司创始人 Solomon Hykes 在法国期间发起的一个公司内部项目。其使用 Go 语言进行开发，基于 Linux 内核的 `cgroup` 、 `namespace` 、 `AUFS` 类的 `Union FS` 等技术，对进程进行封装隔离，属于操作系统层面的虚拟化技术。

由于隔离的进程独立于宿主和其他的隔离进程，因此也称其为容器。最初实现基于 `LXC` ，从 0.7 版本后开始去除 `LXC` ，转而使用自行研发的 `libcontainer` ，从 1.11 版本开始则使用 `runC` 和 `containerd` 。

Docker与传统的虚拟化方式不同在于：

- 传统虚拟机技术是虚拟出一套硬件后，在器上运行一个完整操作系统，再再该系统上运行所需应用进程。
- 容器内的应用进程直接运行于宿主的内核，容器内没有自己的内核，也没有虚拟的硬件。

这也突出了 Docker 的优点：

- 更高效的利用系统资源：不需要虚拟硬件以及运行完整的操作系统。
- 更快速的启动时间：直接运行与宿主内核，无需启动完整的操作系统。
- 抑制的运行环境：Docker的镜像提供了除内核外完整的运行时环境，确保了应用运行环境一致性。
- 持续交付和部署：对于开发和运维（DevOps）人员来说，一次创建或配置就可以在任意地方正常运行是很好的。Docker 可以通过定制应用镜像来实现持续集成、持续交付、部署。通过 `Dockerfile` 进行镜像构建，结合持续集成系统 `Continuous Integration` 进行集成测试，而运维人员则可以直接在生产环境中快速部署该镜像。
- 更轻松的迁移：在很多平台上都可以运行，运行结果都是一致的。
- 更轻松的维护和扩展：使用分层存储以及镜像的技术，使得应用重复部分的复用更为容易，各个开源项目团队一起维护了一大批高质量的官方镜像。

## 概念

- 镜像 image
- 容器 Container
- 仓库 Repository

### 镜像

操作系统分为内核和用户空间，内核启动后，会挂在`root`文件系统提供用户空间支持。`Docker`镜像就相当于是一个`root`文件系统，只不过比较特殊。除了提供容器运行时所需的程序、库、资源、配置等文件外，还包含了一些为运行时准备的配置参数（匿名卷、环境变量、用户等）。

镜像不包含任何动态数据，其内容在构建后不会被改变。

利用`Union FS`技术，设计为**分层存储**的架构。严格来说，镜像并非是一个ISO那样的文件，而只是一个虚拟的概念，其实际体现并非由一个文件组成，而是由一组文件系统组成（多层文件系统联合组成）。

镜像构建时，会一层层构建，前一层是后一层的基础，每一层构建完成后就不会发生改变，后一层的任何改变只发生在自身层。例如，删除前一层的文件，实际上不是真的删除了前一层的文件，而是仅仅在当前层标记为文件已删除，在最终容器运行时，虽然看不到该文件，但事实上该文件一直跟随镜像。因此，构建镜像要格外小心，遵循最小化原则。

分层存储使得镜像复用、定制更为容易，甚至可以用之前构建好的镜像作为基础层，然后进一步添加新的层，以定制自己所需的内容。

### 容器

Image 和 Container 的关系，就像是面向对象程序设计中的 类 和 实例 一样，镜像是静态的定义，容器时镜像运行时的实体。

容器可以被创建、启动、停止、删除、暂停等。

容器的本质是 进程 ，但它运行在属于自己的独立的 命名空间 ，因此它可以拥有自己的`root`文件系统、网络配置、进程空间、甚至用户ID空间。

容器也采用分层存储，每个容器运行时，以镜像为基础层，在其上创建也给当前容器的存储层——容器存储层。任何保存于容器存储层的数据都会随容器删除而消失。Docker最佳实践的要求为：容器不应该向其存储层写入任何数据，容器存储层应该保持无状态化，所有文件写入操作应该使用数据卷(Volume)或者绑定宿主目录。这些位置的读写会跳过容器存储层，直接对宿主(或者remote)发生读写。

数据卷的生存周期独立于容器，不会随容器删除而消失。

### 仓库

Docker Registry 提供集中的存储、分发镜像的服务，一个 Docker Registry 中可以包含多个仓库(Repository)，每个仓库可以包含多个标签(Tag)，每个标签对应一个镜像。

通常，一个仓库会包含同一个软件不同版本的镜像，而标签就常用于对应该软件的各个版本。使用`<Repository Name>:<Tag>`的格式来指定具体是这个软件的哪个版本的镜像，如果不给出标签将以`latest`作为默认标签。

以 Ubuntu 镜像为例，ubuntu是仓库的名字，包含不同的版本标签：`14.04`、`16.04`等，可以通过`ubuntu:14.04`来指定具体的镜像。

仓库名常以两段式路径形式出现，如：klose/ubuntu，klose为Docker Registry多用户环境下的用户名，后者是对应的软件名。这种形式并非绝对。

最常使用的公开服务是官方的 `Docker Hub`，也是默认的Registry。还有 `CoreOS`的`Quay.io`，以及`Google`的`Google Container Registry`，`Kubernetes`的镜像使用的就是这个服务。云服务商针对Docker Hub的镜像服务称为加速器。有：阿里云加速器、DaoCloud加速器、灵雀加速器等。是用加速器会直接从国内的地址下载Docker Hub的镜像。

用户可以本地搭建私有Docker Registry。

## 安装

这里使用的是 Ubuntu Xenial 16.04(LTS)。

Docker目前支持的Ubuntu最低版本为18.04LTS，需要安装在64位的x86或ARM平台上，并且要求内核版本不低于3.10，实际上内核越新越好。

官方安装文档：https://docs.docker.com/engine/install/ubuntu/

阿里云安装文档：https://developer.aliyun.com/article/110806

### 内核

通过下列命令查看内核版本详细信息：

```shell
$ uname -a
```

如果内核版本过低可以进行升级：

Ubuntu 12.04 LTS

```
sudo apt-get install -y --install-recommends linux-generic-lts-trusty
```

Ubuntu 14.04 LTS

```
sudo apt-get install -y --install-recommends linux-generic-lts-xenial
```

### 配置GRUB引导参数

在使用Docker期间，或者在 `docker info` 中可以看到警告信息：

```
WARNING: Your kernel does not support cgroup swap limit. WARNING: Your kernel does not support swap limit capabilities. Limitation discarded.
```

或者是：

```
WARNING: No memory limit support
WARNING: No swap limit support
WARNING: No oom kill disable support
```

如果需要这些功能，要修改GRUB的配置文件`/etc/default/grub`，在`GRUB_CMDLINE_LINUX`中添加内核引导参数`cgroup_enable=memory swapaccount=1`，然后对GRUB进行更新：

```shell
$ sudo update-grub
$ sudo reboot
```

### 脚本自动安装

官方的简化安装脚本，适用于Ubuntu和Debian：

```shell
$ curl -sSL https://get.docker.com/ | sh
```

该脚本自动将一切工作做好，并且把Docker安装在系统中。

阿里云的安装脚本：

```shell
$ curl -sSL http://acs-public-mirror.oss-cn-hangzhou.aliyuncs.com/docker-engine/internet | sh -
```

DaoCloud 的安装脚本

```shell
$ curl -sSL https://get.daocloud.io/docker | sh
```

### 添加APT镜像源

使用Docker官方的软件源以保持最新状态。

国内的一些软件源镜像（比如阿里云）不是太在意系统安全上的细节，可能依旧使用不安全的 HTTP，对于这些源可以不执行这一步。

```shell
$ sudo apt-get update
$ sudo apt-get install apt-transport-https ca-certificates
```

为了确认所下载软件包的合法性，需要添加 Docker 官方软件源的 GPG 密钥。

```shell
$ sudo apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D
```

然后，我们需要向 source.list 中添加 Docker 软件源。

| 操作系统版本       | REPO                                                       |
| ------------------ | ---------------------------------------------------------- |
| Precise 12.04(LTS) | deb https://apt.dockerproject.org/repo ubuntu-precise main |
| Trusty 14.04(LTS)  | deb https://apt.dockerproject.org/repo ubuntu-trusty main  |
| Xenial 16.04(LTS)  | deb https://apt.dockerproject.org/repo ubuntu-xenial main  |

用下面的命令将APT源添加到`source.list`（将其中的<REPO>替换位上表的值）：

```shell
$ echo "<REPO>" | sudo tee /etc/apt/sources.list.d/docker.list
```

添加成功后进行更新：

```shell
$ sudo apt-get update
```

### 安装docker

```shell
$ sudo apt-get install docker-engine
```

### 启动Docker引擎

Ubuntu 12.04/14.04、Debian 7 Wheezy

```
$ sudo service docker start
```

Ubuntu 16.04、Debian 8 Jessie/Stretch

```
$ sudo systemctl enable docker
$ sudo systemctl start docker
```

### 建立docker用户组

默认情况下，`docker` 命令会使用 `Unix socket` 与 Docker 引擎通讯，而只有 `root` 用户和 `dcoker` 用户组的用户才可以访问Docker引擎的`Unix socket` 处于安全考虑，Linux系统上不会直接用root用户，因此更好的做法是将需要使用docker的用户加入docker用户组。

建立`docker`组：

```shell
$ sudo groupadd docker
```

将当前用户加入docker组：

```shell
$ sudo usermod -aG docker $USER
```

### 其他操作系统

`CentOS`：http://shouce.jb51.net/docker_practice/install/centos.html

`macOS`：http://shouce.jb51.net/docker_practice/install/mac.html

### 镜像加速器

关于加速器的使用参考：http://shouce.jb51.net/docker_practice/install/mirror.html
