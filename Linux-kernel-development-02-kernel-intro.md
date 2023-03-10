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

内核提供了各种不同的工具，用于简化对内核的配置：

- `make config` - 一种耗费巨大时间的对选项逐一进行配置的方式。
- `make menuconfig`  - 基于 `ncurse` 库的图形界面工具。
- `make gconfig` - 基于 `gtk+` 的图形工具。
- `make defconfig`  - 默认配置

配置项存放在根目录的 `.config` 中，在修改了配置后，在编译内核之前都应当执行 `make oldconfig` 进行验证和更新。

配置选项 `CONFIG_IKCONFIG_PROC` 把完整的压缩过的内核配置文件存放在 `/proc/config.gz` 下，当编译一个新内核时可以克隆当前配置。如果已启用，则可从 `/proc` 下复制出配置文件并且使用它来编译一个新内核：

```bash
zcat /proc/config.gz > .config    # 克隆当前配置
make oldconfig
```

一旦内核配置完成，使用命令 `make` 进行编译。

### 减少编译的垃圾信息

对输出进行重定向：

```bash
make > .. /detritus
```

或者是把无用的输出信息重定向到永无返回值的黑洞 `/dev/null` :

```bash
make > /dev/null
```

### 衍生多个编译作业

默认情况下， make 只衍生一个作业，因为 Makefiles 经常会出现不正确的依赖关系，导致编译出错。可以以多个作业编译内核：

```bash
make -j<n>    # n 是要衍生出的作业数，实际上单核处理器一般衍生出一个或两个作业
```

另外，利用 `distcc` 或者 `ccache` 工具也可以动态改善内核编译的时间。

## 安装新内核

内核编译后需要安装，而安装和体系结构、启动引导工具(`bootloader`)相关。查阅 `bootloader` 的说明，按照其指示将内核映像拷贝到指定位置，按照启动要求安装它。

一定保证随时有一个或两个可以启动的内核，以免新编译的内核出问题。

【例子】 在使用 `grub` 的 `x86` 系统上，可能需要把 `arch/i386/boot/bzImage` 拷贝到 `/boot` 目录下，像 `vmlinuz-version` 这样命名它，然后编辑 `/etc/grub/grub.conf` 文件，为内核新建一个新的启动项。 使用 `LILO` 启动的系统应当编辑 `/etc/lilo.conf` ，然后运行 lilo。

模块的安装只需要用root身份执行：

```bash
make modules_install
```

将已编译的模块安装到主目录 `/lib/modules` 下。

编译时，根目录下出现 `System.map` 文件，这是一份符号对照表，用以将内核符号和它们的起始地址对应起来，方便调试。

## 内核开发的特点

- 不能访问C库和标准的C头文件；
- 必须使用GNU C；
- 缺乏用户空间的内存保护机制；
- 难以执行浮点运算；
- 每个进程只有一个很小的定长堆栈；
- 内核支持异步中断、抢占和 `SMP` ，要时刻注意同步和并发；
- 要考虑可移植性的重要性；

### 不能访问C库和C头文件

基本的头文件放置在根目录的 `include` 文件夹中。

体系结构相关的头文件在 `arch/<architecture>/include/asm` 目录下。

【例子】在内核中，没有 `printf()` 函数，取而代之的是 `printk()` 。它们之间的一个显著区别是， `printk()` 允许用一个标志来设置优先级， `syslogd` 会根据该标志来决定什么地方显示这条消息。例如： `printk(KERN_ERR “this is an error!\\n”)`

### GNU C

内核并不完全符合 `ANSI C` 标准。实际上，只要有可能，内核开发者经常需要用到 `gcc` 提供的语言扩展功能。 `gcc` 是多种 `GNU` 编译器的集合。

内核代码中的一些C语言扩展：

- 内联(inline)函数

  直译应该是：“在字里行间展开”。函数执行时会在它所调用的位置上展开，节省了函数调用和返回的开销（函数栈、寄存器存储和恢复）。这么做，还能让编译器把被调用函数(callee)的代码和调用函数(caller)本身一起进行优化。

  但是，内联函数使得代码变长，占用更多内存空间和指令缓存。

  ```c
  static inline void foo(unsigned long param)
  ```

  技巧：

  - 通常把对时间要求较高、代码长度短的函数定义为内联函数，如果代码长度长，会被反复调用，没有特别的时间限制，则不推荐用内联函数；
  - 通常在头文件中定义内联函数；
  - 在内核中，优先使用内联函数，不使用复杂的宏；

- 内联汇编

  当知道对应的体系结构时，可以用 `asm()` 指令嵌入汇编代码。

  【例子】下面的内联汇编代码用于执行 `x86` 处理器的 `rdtsc` 指令，返回时间戳  `tsc` 寄存器的值。

  ```c
  unsigned int low, high;
  asm volatile("rdtsc" : "=a" (low), "=d" (high));
  ```

  在近体系结构的底层或对执行时间有严格要求时会使用内联汇编。

- 分支声明

  使用 `likely()` 和 `unlikely()` 来分别标记通常为真和绝少为真的条件分支。

  【例子】

  ```c
  if (unlikely(error)){    /* 认为error绝少为 1 ，大多数时间为 0 */
  		/* .... */
  }
  
  if(ikely(success)){    /* 认为success通常为 1 ，通常不为 0 */
  		/* ... */
  }
  ```

### 无内存保护机制

如果是用户程序进行一次非法的内存访问，内核会发现这个错误，并发送 `SIGSEGV` 信号来结束整个进程。但是，若是内核自己非法访问内存，则会导致 `oops` ，而杀死自己。内核的内存都不分页，所以每用一个字节，物理内存就会减少一个字节。

### 不要轻易使用浮点数

用户程序使用浮点数时，内核会完成从整数操作到浮点数操作的模式转换，通常捕获陷阱并着手于整数到浮点数的转变。但内核不能完美地支持浮点数操作，因为它本身不能陷入。

### 容积小且固定的栈

内核栈大小随体系结构而变。在 x86 上，栈在编译时配置，可以是 4KB 或是 8KB。内核栈的大小是两页，32 位机上是 8KB ，而 64 位机上是 16 KB，这是固定不变的。每个处理器都有自己的栈。

### 同步和并发

内核很容易发生条件竞争，通常用自旋锁和信号量来解决这些问题。

- Linux 是抢占多任务操作系统，内核的进程调度程序即兴对进程进行调度和重新调度，内核必须和这些任务同步。
- Linux 内核支持对称多处理器系统 (SMP) ，如果没有适当保护，同时在两个或以上的处理器上执行内核代码，可能会同时访问共享的同一个资源。
- 中断是异步到来的，不会顾及正在执行的代码。因此，中断可能在代码访问资源的时候到来，这样，中断处理程序可能访问同一资源。
- Linux 内核可以抢占，所以不加以保护的话，内核中一段正在执行的代码可能会被另一段代码抢占，从而导致几段代码同时访问相同的资源。

### 可移植性的重要性

大部分C代码应当与体系结构无关，在不同体系结构的计算机上要能编译和执行，因此必须把体系结构相关的代码从内核代码树的特定目录中适当分离出来。

诸如 保持字节序、64位对齐、不假定字长和页面长度 等准则都有助于移植性。

### 在线检索源代码

https://elixir.bootlin.com/linux/v2.6.34/source