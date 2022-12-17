---
title: Linux_kernel_development_01_intro
top: false
comment: false
lang: zh-CN
date: 2022-11-03 01:32:37
tags:
categories:
  - study
  - book
  - Linux Kernel Development
---

# Linux内核简介

第一章从Unix的历史认识Linux内核和Linux操作系统的前世今生。

## Unix历史

Unix诞生于贝尔实验室的一个失败的多用户操作系统Multics。

1969年，贝尔实验室的程序员设计了一个文件系统原型，最终该原型发展成了Unix。Thompson首先在一台无人问津的 PDP-7 型机上实现了这个操作系统。

1971年，Unix被移植到了 PDP-11 型机中。

1973年，Unix操作系统用C语言进行了重写。

1977年，推出了Unix System III。

1977年，加州大学伯克利分校推出了1BSD系统，实现基于贝尔实验室的Unix版本。

1978年，推出了2BSD系统，里面博阿寒csh、vi等软件。

1979年，推出了独立开发的Unix系统，3BSD，支持虚拟内存。

1994年，重写了虚拟内存子系统VM，推出了最终官方版4.4BSD。



Unix强大的原因：

- Unix简洁，仅仅提供几百个系统调用并且有一个非常明确的设计目的。
- Unix中，万物皆为文件，抽象使得对数据和对设备的操作通过一套相同的系统调用接口来进行。
- 内核和相关系统工具软件是用C语言编写的。
- 进程创建非常迅速，有一个非常独特的`fork()`系统调用。



今天，Unix已经发展成为一个支持抢占式都多任务、多线程、虚拟内存、换页、动态链接和TCP/IP网络的现代化系统。

## Linux简介

Linux是类Unix系统，但不是Unix。它借鉴了Unix许多设计，并且实现了Unix的API，但没有直接使用Unix的源代码。Linux是一个非商业化的产品，任何人都可以开发内核，内核也是自由公开的软件，使用GNU GPL 2.0作为限制条款。

## 操作系统和内核简介

