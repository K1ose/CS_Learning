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
- 保证了系统的稳定和安全
- 
