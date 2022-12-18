---
title: Linux_kernel_development_02_kernel_intro
top: false
comment: false
lang: zh-CN
date: 2022-12-18 12:21:37
tags:
categories:
  - study
  - book
  - Linux Kernel Development
---

# 从内核出发

第二章介绍内核源码获取、编译、安装以及内核程序和用户态程序的差别。

## 获取内核源码

http://www.kernel.org

https://mirrors.edge.kernel.org/pub/linux/kernel/

- 使用Git：git clone git://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git

- 安装内核源代码：`tar xvjf linux-x.y.z.tar.bz2` or `tar xvzf linux-x.y.z.tar.gz`

  内核源码一般安装在`/usr/src/linux`下，，不要把这个源码树用于开发，因为编译用的内核版本就链接到这棵树。不要用root身份对内核进行修改，应当建立自己的主目录，仅以root身份安装内核，且安装新内核时，`/usr/src/linux`目录都应当原封不动。

- 使用补丁：`patch -p1 < ../patch-x.y.z`

## 内核源码树

- `arch` - 特定体系结构的源码
- `block` - 块设备I/O层
- `crypto` - 加密API
- `Documentation` - 内核源码文档
- `drivers` - 设备驱动程序
- `firmware` - 使用某些驱动程序而需要的设备固件
- `fs` - VFS和各种文件系统
- `include` - 内核头文件
- `init` - 内核引导和初始化
- `ipc` - 进程间通信代码
- `kernel` - 核心子系统
- `lib` - 通用内核函数
- `mm` - 内存管理子系统和VM
- `net` - 网络子系统
- `samples` - 示例代码
- `scripts` - 编译内核所用的脚本
- `security` - 安全模块
- `sound` - 语音子系统
- `usr` - 早期用户空间代码，所谓的initramfs
- `tools` - 开发工具
- `virt` - 虚拟化基础结构

## 编译内核

### 内核配置

可以配置的各种选项，以`CONFIG_FEATURE`形式表示，其前缀为`CONFIG`。

选项的选择一般有两种：

- `yes` or `no`：yes表示把代码编译进主内核印象中，而不是作为模块
- `yes` or `no` or `module`：驱动程序一般都用三选一的配置项，module意味着配置项被选定了，以独立代码段的模块形式编译安装。