---
title: csapp_11_network_programming
top: false
comment: false
lang: zh-CN
date: 2023-02-06 14:47:37
tags:
categories:
  - CSAPP
  - theory
---

# 网络编程

主要内容：

- 理解基本的客户端-服务器编程模型
- 编写使用因特网提供的服务的客户端-服务器程序
- 开发功能齐全的Web服务器

## CS模型

CS模型，即：Client-Server模型。一个应用是由 <u>一个服务器进程</u> 和 <u>一个/多个客户端进程</u> 组成的。服务器管理资源，通过操作这种资源来为客户端提供服务，例如：

- 一个FTP服务器管理一组磁盘文件，为客户端进行存储和检索。

CS模型中的基本操作是 <u>事务(transaction)</u> ，其由以下四步组成：

<img src="./csapp-11-network-programming\figure_01.jpg" height=200 width=550>

- 当一个客户端需要服务时，它向服务器发送一个请求。
- 服务器收到请求后进行解析，并以适当的方式对资源进行操作。
- 服务器给客户端发送一个响应，并等待下一个请求。
- 客户端收到响应并斤进行处理。

## 网络

Client进程 和 Server进程 通常运行在不同的主机上。对于主机而言，网络只是又一种 <u>I/O设备</u>，是<u>数据源和数据接收方</u>。

一个插到 I/O总线扩展槽 的适配器（网卡）提供了网络的物理接口。从网络上接收到的数据 从 适配器 经过 I/O 和 内存总线 复制到内存（通常称为 <u>下载</u>），通常是通过 DMA(Direct Memory Access，直接内存访问) 传送。相似的，数据也能从内存复制到网络（通常称为 <u>上传</u>）。

物理上而言，网络是一个按照地理远近组成的层次系统。从下至上为：

- 局域网 LAN (Local Area Network) ：在一个建筑或者小型单位内，最流行的局域网技术是 以太网 (Ethernet)。
- 城域网 MAN (Metropolitan Area Network) ：在一个城市范围内简历的计算机通信网络。
- 广域网 WAN (Wide-Area Network) ：通过路由器(router)将 多个局域网 连接起来，组成一个 internet。

### 局域网

最流行的局域网技术是以太网(Ethernet) 。

一个 以太网段(Ethernet Segment) 包括 一些电缆（通常是双绞线）和一个叫做 <u>集线器(Hub)</u> ，每根电缆又相同的最大位带宽，一端连接到主机的适配器（连接主机网卡），一端连接到集线器的一个端口。

每个以太网适配器都有一个全球唯一的 48bit 的地址，即为 MAC地址，它存储在适配器的非易失性存储器上。

以太网工作在 TCP/IP 模型的最底层：数据链路层，该层的消息单位被称为 <u>帧(frame)</u> 。帧包括

- 一些固定数量的 头部(header)位 ：用来标识此帧的源和目的MAC地址、帧的长度等信息；
- 有效载荷(payload) ：消息主体；

> 由于 集线器(Hub) 工作在物理层，它没有解析比特串的功能，因此它只能将消息广播出去，即：连接到一个集线器的不同主机处在同一个冲突域。当一个主机发送消息时，其他主机无法发送消息，否则消息将因碰撞而丢失。（半双工）

基于集线器的弊端，多采用 <u>交换机(Switch)</u> 来连接同一个网段内的不同主机。交换机基于维持的 MAC地址表 来组织帧的传输。在每个端口接收到数据帧时：

1. 将主机的 源MAC地址 与 端口 记录在 MAC地址表 内；
2. 在MAC地址表内，查找 目的MAC地址 与其对应的端口；
3. 将数据帧从找到的端口转发出去；

<img src="./csapp-11-network-programming\figure_02.jpg" height=150 width=250 >

例如，一个交换机的 三个端口(G0/0/1、G0/0/2、G0/0/3) 分别连接了三台主机 (C、A、B) 。当 A 发送消息给 B 时，交换机的端口 G0/0/2 接收到数据帧，其中的 源MAC地址为 MAC_A 。于是交换机将该信息存储到 MAC地址表 中。在获知数据帧的 目的MAC地址 后，经过查询 MAC地址表 ，找到了匹配的 MAC_B ，于是将数据帧转发到对应的端口 G0/0/3 上。

### 广域网

通过路由器(router)将 多个局域网 连接起来，组成一个互联网络 internet 。 internet ≠ 广域网，简单来说，两个路由器可以连接多个局域网和一个广域网。

<img src="csapp-11-network-programming\figure_03.jpg" height=140 width=500>

### internet

互联网络的重要特性是，它能由采用 完全不同和不兼容的技术 的各种局域网和广域网组成。实现这一功能的方法是：在每台主机、路由器上运行一种协议软件，该软件实现一种 <u>协议(protocol)</u> ，用于控制主机和路由器如何协同工作来实现数据传输，以消除不同网络之间的差异。

该协议具备两种基本能力：

- 命名机制：不同局域网技术有不同的主机分配地址，例如以太网使用MAC地址。为了消除这一差异，协议需要通过定义一种一致的主机地址格式，每台主机会被分配至少一个这种互联网络地址(internet address)，其将唯一标识一台主机。
- 传送机制：不同局域网技术有不同的组装帧的方式。为了消除这一差异，协议需要通过定义一种一致的消息主体单位，通过包头(header)和有效载荷(payload)来组成格式一致的包，以方便兼容解析。

在 Page 645 ，讲述了 <u>主机和路由器</u> 使用 <u>互联网络协议</u> 在 <u>不兼容的局域网</u> 间传输数据的例子。

## 因特网

因特网（Internet）是 互联网络(internet) 中最著名最成功的实现，下图是一个Internet中的CS模型应用程序的基本硬件、软件组织。

<img src="./csapp-11-network-programming\figure_04.jpg" height=350 width=500>

### TCP/IP

每台 Internet 主机都运行实现 TCP/IP 协议 (Transmission Control Protocol / Ineternet Protocol，传输控制协议/互联网络协议) 的软件。其中的客户端和服务器端混合使用 <u>套接字接口函数</u> 和 <u>Unix I/O 函数</u> 来进行通信，通常<u>将套接字函数实现为系统调用</u>，它们陷入内核后调用各种<u>内核模式的 TCP/IP 函数</u>。

TCP/IP 实际上是一个协议族，它们每一个都提供不同的功能。

- <u>IP 协议</u> 提供基本的 <u>命名方法</u> 和 <u>递送机制</u> ，这个递送机制赋予了一台主机向其他主机发送 <u>数据报(datagram)</u> 的能力，但是这个递送机制 **不保证** 数据包在网络中的<u>可靠传输</u>。UDP (Unreliable Datagram Protocol，不可靠数据报协议) 稍微扩展了IP协议，使得包可以<u>在进程间而不是主机间传送</u>。
- <u>TCP协议</u> 是构建在 IP协议 之上的复杂协议，提供了<u>进程间可靠的全双工连接</u>。

为了简化讨论，将 TCP/IP 看作是一个单独的整体协议，而不讨论 UDP。

### IP地址

将因特网看作一个世界范围的主机集合，它将满足以下特性：

- 主机集合被映射为一组 <u>32位</u> 的IP地址；
- 这组IP地址被映射位一组称为 <u>因特网域名(Internet domain name)</u> 的标识符；

- 因特网主机上的<u>进程</u>能够通过 <u>连接(connection)</u> 和任何其他因特网主机上的<u>进程</u>通信；

在IPv4中，一个IP地址就是一个32位的无符号整数，网络程序将IP地址存放在如下结构中。

```c
// code/netp/netpfragments.c

/* IP address structure */
struct in_addr {
	uint32_t  s_addr; /* Address in network byte order (big-endian) */
};
```

把一个<u>标量地址</u>存放在结构中是<u>套接字接口</u>早期实现的**不幸**产物，如果把IP地址定义为一个<u>标量类型</u>应该更有意义。

由于不同主机可以有不同的<u>主机字节顺序(host byte order)</u>，TCP/IP 为任意整数数据定义了统一的<u>网络字节顺序 (network byte order)</u> （大端字节顺序），例如IP地址。即使主机字节顺序是小端法，IP地址结构中存放的地址总是以<u>大端法</u>顺序存放。Unix 中提供了下面这样的函数在<u>网络和主机字节顺序间</u>实现转换。

```c
#include <arpa/inet.h>
uint32_t htonl(uint32_t hostlong);		// host to network long
uint16_t htons(uint16_t hostshort);		// host to network short

uint32_t ntohl(uint32_t hostlong);		// network to host long
uint16_t ntohs(uint16_t hostshort);		// network to host short
```

需要注意的是Unix中没有对应的处理64位值的函数。

IP地址通常以点分十进制表示法来表示，如 `128.2.194.242` 就是地址 `0x8002c2f2` 的点分十进制表示。在 Linux 中用 `hostname` 命令在查看点分十进制地址。

```bash
$ hostname -i
127.0.1.1
```

应用程序使用 `inet_pton` 和 `inet_ntop` 函数来实现 IP地址 和 点分十进制串 的转换。

```c
#include <arpa/inet.h>
int inet_pton(AF_INET, const char *src, void *dst);	// 成功返回 1， 出错返回 -1 ，src位非法点分十进制则返回 0
const char *inet_ntop(AF_INET, const void *src, char *dst, socklen_t size);	// 成功返回指向点分十进制字符串的指针，出错返回NULL
```

这里的 `AF_INET` 指的是 IPv4 地址，如果是 `AF_INET6` 指的是 IPv6 地址。

