---
title: router_vuln_environ
top: false
comment: false
lang: zh-CN
date: 2021-12-12 00:16:30
tags:
categories:
  - study
  - book
  - 揭秘家用路由器0day漏洞挖掘技术
---

# 必备软件和环境

## 软件

### VMware

不多介绍...

### Python

不多介绍...

### IDA pro(Linux)

#### wine

这里没有用wine，直接用了linux版本的IDA Pro；

在Linux中运行Windows环境的IDA pro，需要wine这个模拟器软件，还挺大的，要`800M+`；

- 安装;

  ```
  sudo apt-get install wine
  ```

- 拷贝整个Windows下的IDA Pro目录文件;

- 编写一个启动脚本，用来快速启动IDA pro;

  ```bash
  #!/bin/bash
  wine pathto/ida.exe
  ```

- 麻烦的是动态链接库的问题，需要把缺失的动态链接库文件放到当前目录下；

#### plugin

这里从github上下载插件包进行安装；

```
git clone git://github.com/devttys0/ida.git
```

下载完毕后，使用如下命令安装；

```
python ./install.py --install -d /path/to/your/ida/install/directory
```

## 环境

### binwalk

安装不多介绍...

#### 使用

- 获取帮助

  ```
  binwalk -h/--help
  ```

- 固件扫描

  ```
  binwalk xxx.bin
  ```

- 提取文件

  按照预定义的配置文件中的提取方法提取探测到的文件系统

  ```
  binwalk -e xxx.bin
  ```

  `-M`根据`magic`签名扫描结果进行递归提取

  ```
  binwalk -Me xxx.bin
  ```

  `-d/--depth=<int>`用于限制递归深度，默认为`8`，仅当`-M`存在时有效

  ```
  binwalk -Me -d 5 xxx.bin
  ```

  `-D/--dd=<type:ext[:cmd]`

  ```
  binwalk --dd 'zip archive:zip:unzip %e' xxx.bin
  ```

- 过滤选项

  `-y/--include=<filter>`只包含与`magic`签名相匹配的指定过滤器

  ```
  binwalk -y filesystem xxx.bin
  ```

  `-x/--exclude=<filter>`与`-y`作用相反，匹配的`magic`签名不加载

  ```
  binwalk -x 'mach-o' -x '^hp' xxx.bin
  ```

- 显示完整扫描结果

  ```
  binwalk -I xxx.bin
  ```

- 文件比较

  ```
  binwalk -W xxx1.bin xxx2.bin xxx3.bin
  ```

- 日志记录

  ```
  binwalk -f xxx.log -q xxx.bin
  binwalk -f xxx.log --csv xxx.bin
  ```

- 指令系统分析

  确定一个可执行文件的CPU架构

  ```
  binwalk -A xxx.bin
  ```

- 熵分析

  对输入文件执行熵分析，打印原始数据并生成熵图

  ```
  binwalk -E xxx.bin
  ```

- 启发式

  对输入文件进行启发式分析，判断得到的熵值分类数据块是压缩的还是加密的

  ```
  binwalk -H xxx.bin
  ```

### Qemu

这个之前在做kernel pwn的时候装了，不知道到时候调试环境会有什么问题，先放着...

用qemu跑的时候需要注意的问题：

- qemu-mips/qemu-mipsel要先拷贝到文件系统根目录

  ```
  cp $(which qemu-mips) ./
  ```

- 用chroot切换根目录到当前文件系统根目录

  ```
  sudo chroot . ./qemu-mips ./bin/[program]
  ```

- qemu装载文件系统

### MIPS交叉编译环境

为了能够编译MIPS架构的应用程序，需要建立交叉编译环境；

https://www.uclibc.org/downloads/binaries/0.9.30.1/

这里下载的工具也不错，可以直接用；

#### Buildroot

- 下载

  ```
  wget http://buildroot.uclibc.org/downloads/snapshots/buildroot-snapshot.tar.bz2
  tar -jxvf buildroot-snapshot.tar.bz2
  ```

- 配置

  ```
  sudo apt-get install libncurses5-dev patch
  cd buildroot
  make clean
  make menuconfig
  ```

  3处修改:

  - `Target Architecture` -> `MIPS(little endian)`，这里主要还是看需要大端小端；
  - `Traget Architecture Variant` -> `mips32`；
  - `Toolchain`中将`Kernel Headers`改成机器环境的`Kernel`版本，我的是`4.15`的内核

  修改完毕后保存；

- 编译

  ```
  sudo make
  ```

  要挺久的...结束之后回生成`output`文件夹，其中有编译好的文件，同时可以在`buildroot/output/host/usr/bin`中找到交叉编译工具，编译器则为该目录下的`mipsel-linux-gcc`文件；

  ```
  ~/buildroot/output$ ls
  build  host  images  staging  target
  
  ~/buildroot/output/host/usr/bin$ ls | grep linux-gcc
  mipsel-linux-gcc
  ```

  可以通过下面命令查看编译器版本；

  ```
  ~/buildroot/output/host/usr/bin$ ./mipsel-linux-gcc --version
  mipsel-linux-gcc.br_real (Buildroot 2022.02-git) 10.3.0
  Copyright (C) 2020 Free Software Foundation, Inc.
  This is free software; see the source for copying conditions.  There is NO
  warranty; not even for MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  ```

- 测试

  测试代码`vuln.c`如下：

  ```c
  #include <stdio.h>
  void vuln(char *src){
  	char output[20] = {0};
      strcpy(output, src);		// 缓冲区溢出
      printf("%s\n", output);
  }
  
  int main(int argc, char *argv[]){
      if(argc < 2){
          printf("need more input arguments\n");
          return 1;
      }
      vuln(argv[1]);			// 参数作为源字符串输入
      return 0;
  }
  ```

  为了不让编译生成的程序依赖动态库，所以使用静态编译；

  ```
  mipsel-linux-gcc vuln.c -static -o vuln 
  ```

  > ~/iot_pwn/sample_vuln$ file vuln
  > vuln: ELF 32-bit LSB executable, MIPS, MIPS32 version 1 (SYSV), statically linked, not stripped
