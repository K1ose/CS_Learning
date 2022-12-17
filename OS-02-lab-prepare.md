---
title: OS_02_lab_prepare
top: false
comment: false
lang: zh-CN
date: 2022-05-06 13:30:08
tags:
categories:	
  - OS
  - lab
---

# 02-lab-prepare

## 实验目标

- 原理知识的补充和完善
- 操作系统设计的全局观
- 适合不同层次学生的需求

包括：

- 外设：I/O管理、中断管理
- 内存：虚拟内存管理、页表、缺页处理、页替换算法
- CPU：进程管理、调度器算法
- 并发：信号量实现、同步互斥应用
- 存储：基于链表/FAT的文件系统

## 实验概述

- OS启动、中断与设备管理
- 物理内存管理
- 虚拟内存管理
- 内核线程管理
- 用户进程管理
- 处理器调度
- 同步互斥
- 文件系统



## 环境搭建

实验环境：vitualbox + ubuntu 1404 x86

源码阅读工具：understand(Windows)

源码文档自动生成工具：Doxygen

代码比较工具：diff、meld

编译环境：gcc, make, Binutils

真实/虚拟运行环境：X86或Qemu

调试工具：Qemu+或GDB

IDE工具：Vscode

### ubuntu

https://pan.baidu.com/s/11zjRK

### understand

百度云盘

### doxygen

```
sudo apt-get install doxygen
sudo apt-get install doxygen-gui
sudo apt-get install graphviz
```

### build-essential

```
sudo apt-get install build-essential
```

### Qemu

```
sudo apt-get install qemu-system
```



## 硬件介绍

Intel 80386

### 四种运行模式

- 实模式 16位，物理内存空间不超过1M，没有4GB的内存管理能力
- 保护模式 32位，分页分段机制->0~4个ring，更大的寻址空间
- SMM模式
- 虚拟8086模式

### 内存构架

- 地址是访问内存空间的索引
- 32bit处理器->2^32=4G字节的寻址空间
- 物理内存地址空间是处理器提交到总线上用于访问计算机系统中内存和外设的最终地址，一个计算机系统中只有一个物理地址空间；
- 线性地址空间是虚拟管理下每个运行的程序能访问的地址空间，每个程序都都认为自己独享这个空间，程序之间相互隔离；
- 逻辑地址空间是应用程序直接使用的地址空间

段机制启动、页机制未启动：逻辑地址->段机制处理->线性地址=物理地址

段机制和页机制都启动：逻辑地址->段机制处理->线性地址->页机制处理->物理地址

可以看到段机制和页机制是映射关系；

### 寄存器

- 通用寄存器

  EAX、EBX、ECX、EDX、ESI、EDI、ESP、EBP

- 段寄存器

  CS、DS、ES、SS、FS、GS

- 指令指针寄存器

  EIP

- 标志寄存器

  EFLAGS(IF,...)

- 控制寄存器

- 系统地址寄存器，调试寄存器，测试寄存器

## 编程技巧

### 面向对象编程

```c
// kern/mm/pmm.h
struct pmm_manager {
	const char *name;
	void (*init)(void);
	void (*init_memmap)(struct Page *base, size_t n);
	struct Page *(*alloc_pages)(size_t n);
	void (*free_pages)(struct Page *base, size_t n);
	size_t (*nr_free_pages)(void);
	void (*check)(void);
};
```

在lab2中，存在一个结构体`pmm_manager`内存管理器，它定义了很多函数指针，提供了函数接口。当有不同的管理方法，可以满足不同的实现方法；



### 数据结构

```c
// 通用双向链表结构
struct list_entry {
	struct list_entry *prev, *next;
};

typedef struct{
	list_entry_t free_list;
	unsigned int nr_free;
}free_erea_t;

struct Page{
	atomic_t ref;
	...
	list_entry_t page_link;
};
```

双向链表数据结构套用Page；

当然还会有元素访问、删除、插入的方法；

## 实验的一些tips

### Meld

Meld工具来比较文件的不同；

```
meld /lab1 /lab2
```

### GDB

```
make debug
n,c,s,where,p,...
```

