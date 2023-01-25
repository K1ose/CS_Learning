---
title: Linux_kernel_development_05_system_calls
top: false
comment: false
lang: zh-CN
date: 2023-01-03 09:43:04
tags:
categories:
  - study
  - book
  - Linux Kernel Development
---

# 系统调用

内核提供了用户进程与内核进行交互的接口，这些接口让应用程序受限地访问硬件设备，提供了创建新进程并与已有进程进行通信的机制。

## 与内核通信

系统调用在用户空间进程和硬件设备之间添加了一个中间层，主要作用为：

- 为用户空间提供了一种硬件的抽象接口，例如：对文件系统中文件的读写，等等
- 保证了系统的稳定和安全，内核可以基于权限、用户类型和其他规则对需要进行的访问进行裁决
- 提供了多任务和虚拟内存的系统稳定性

## API、POSIX和C库

一般情况下，应用程序通过在用户空间实现的应用编程接口（API）而不是直接通过系统调用。

POSIX是可移植操作系统接口（Portable Operating System Interface of UNIX，缩写为 POSIX ），它是Unix的标准。例如，glibc就是 Linux 下使用的开源标准C库，是由 GNU 发布的 libc 库，它提供了丰富的API接口，这些API都是遵循POSIX标准的。

应用程序通过调用库函数来实现特定功能，这些库函数中的一些函数则会使用系统调用的服务。

## 系统调用

// TODO
