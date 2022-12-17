---
title: Environment
top: false
comment: false
lang: zh-CN
date: 2021-11-12 08:47:06
tags:
categories:
  - CTF
  - PWN
  - kernel pwn
  - knowledge
---

# 环境搭建

## 环境

内核版本：4.4.72

busybox版本：1.32.1

gcc版本：5.4.0

虚拟机操作系统：ubuntu16.04 64位

## 依赖安装

```
sudo apt-get install git fakeroot build-essential ncurses-dev xz-utils libssl-dev bc qemu qemu-system
sudo apt-get install bison flex libncurses5-dev
```

## gcc版本切换

看情况切换gcc；

```plaintext
sudo apt-get install -y gcc-x.x			# x.x为版本
sudo apt-get install -y g++-x.x
# 重新建立软连接
cd /usr/bin    							#进入/usr/bin文件夹下
sudo rm -r gcc  						#移除之前的软连接
sudo ln -sf gcc-x.x gcc 				#建立gcc4.7的软连接
sudo rm -r g++  #同gcc
sudo ln -sf g++-x.x g++
```

## 内核源码下载与编译

清华库https://mirrors.tuna.tsinghua.edu.cn/kernel/

例如：

```
wget https://mirrors.tuna.tsinghua.edu.cn/kernel/v4.x/linux-4.4.72.tar.gz
```

解压

```
tar -xvf linux-4.4.72.tar.gz
```

编译

```
cd linux-4.4.72
make menuconfig
make
make all
make modules
```

未完待续...

# 调试

