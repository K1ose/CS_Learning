---
title: pwn-college-program-interaction
top: false
comment: false
lang: zh-CN
date: 2021-12-20 02:20:09
tags:
categories:
  - pwn.college
  - Chapter01 Program interaction
---

# Program Interaction

## Linux Process Loading

一个示范程序：

```c
// cat.c
int main(int argc, char *argv[]){
    char buf[1024];
    int n;
    int fd = argc == 1 ? 0 : open(argv[1], 0);
    while ((n = read(fd, buf, 1024)) > 0 && write(1, buf, n) > 0);
}
```

当执行`cat /flag`时，会依次发生：

- 进程创建
- 程序装载
- 程序初始化
- 程序运行
- 程序读取参数和环境
- 程序功能执行
- 程序终止

下面将讨论前三个：进程创建，程序装载，程序初始化；

### create process

在启动一个进程前，先来简要了解进程包含的一些结构属性；

### struct of process

对于Linux而言，每个Linux进程包含：

- 状态state：描述进程当前处于何种状态
  - running
  - waiting
  - stopped
  - zombie
- 优先级priority：用于调度
- 亲属关系parent,siblings,children：
  - 父进程
  - 子进程
  - 兄弟姐妹进程
- 共享资源share resource
  - files
  - pipes
  - sockets
- 虚拟内存空间virtual memory space：资源存储
- 安全上下文security context：一些标志
  - effective uid and gid
  - saved uid and gid
  - capabilities

那么进程从何而来呢？

### Where do processes come from？

就像细胞分裂一样，进程由其父进程“分裂”而来；

关于fork和clone的内容会后面补充；

### load

程序时如何执行并装载的呢？

- 在fork出一个新的进程并尝试execve执行时，会检查是否有执行权限

- 满足可执行权限后，内核会读取该文件的文件头，以区别是何种文件

  - 如果以`#!`开头，则会认为文件为脚本文件

    - 几个例子：

      - sample_bash

        ```shell
        # sample_bash
        #!/bin/bash
        echo hello, world
        ```

        执行时内核读取到`#!`，因此认为该文件为脚本文件，尝试将`/bin/bash`作为解释程序interpreter执行。因此理解起来就是，执行`/bin/bash`启动了一个新的shell，在shell中执行了该文件的内容`echo hello, world`，因此执行该文件会打印出`hello, world`；

        > $ ./sample_bash 
        > hello, world

        当然这相当于执行了

      - sample_echo1

        ```shell
        # sample_echo1
        #!/bin/echo
        ```

        现在将`bash`替换成`echo`，再次运行时会发生什么呢？根据先前的理解，这里将`/bin/echo`作为解释程序interpreter执行，而输入的参数是`./sample_echo`，因此会得到下面的结果：

        > $ ./sample_echo 
        > ./sample_echo

        这相当于执行了`/bin/echo ./sample_ehco`；

      - sample_echo2

        ```shell
        # sample_echo2
        #!./sample_echo1
        ```

        尝试运行时，会得到：

        > $ ./sample_echo2 
        > ./sample_echo1 ./sample_echo2

        这是因为解释程序可以实现递归调用，执行`./sample_echo2`也相当于执行了`./sample_ehco1 ./sample_echo2`，相当于执行了`/bin/echo ./sample_echo1 ./sample_echo2`；

  - 如果匹配到`/proc/sys/fs/binfmt_misc`中的一种格式，则会按照interpreter指定格式来执行

    - 在这个目录下查看其中的内容

      ```
      /proc/sys/fs/binfmt_misc$ ls
      jar           qemu-alpha  qemu-microblaze  qemu-ppc         qemu-sh4          register
      python2.7     qemu-arm    qemu-mips        qemu-ppc64       qemu-sh4eb        status
      python3.5     qemu-armeb  qemu-mips64      qemu-ppc64abi32  qemu-sparc        wine
      python3.8     qemu-cris   qemu-mips64el    qemu-ppc64le     qemu-sparc32plus
      qemu-aarch64  qemu-m68k   qemu-mipsel      qemu-s390x       qemu-sparc64
      
      /proc/sys/fs/binfmt_misc$ cat jar
      enabled
      interpreter /usr/bin/jexec
      flags: 
      offset 0
      magic 504b0304
      ```

      这里有幻数magic的概念，查看其内容：

      ```
      /proc/sys/fs/binfmt_misc$ echo $'\x50\x4b\x03\x04'
      PK
      ```

      可以看见其文件头以`PK`开头来识别；

      所以一个文件是什么类型并非以后缀名来决定，而是以其Interpreter来决定；

  - 如果文件是动态连接的ELF文件，那么内核将会执行对应的interpreter/loader，让interpreter来接管；

    - 其主要步骤为：

      1. 程序和它的interpreter被内核加载；
      2. interpreter定位到库
         1. `LD_PRELOAD`环境变量，在`/etc/ld.so.preload`中设置
         2. `LD_LIBRARY_PATH`环境变量，可以在shell文件中设置
         3. `DT_RUNPATH` or `DT_RPATH`指定了库文件，可以用`patchelf`工具修改
         4. `system-wide`设置，可以在`/etc/ld.so.conf`中设置
         5. `/lib`和`/usr/lib`中找到对应的共享库文件
      3. 最后interpreter加载库文件
         1. 这些库文件可能会关联其他的库文件，会导致更多的库文件被装载
         2. 更新重定位
  
    - 举个例子
  
      现在回到`cat.c`，将它编译后运行；
  
      使用下面命令查看解释器；
  
      ```
      readelf -a /bin/cat | grep interpreter
      ```
  
      可以得到的结果是：
  
      > ​      [Requesting program interpreter: /lib64/ld-linux-x86-64.so.2]
  
      用`patchelf`可以修改解释器；
  
      ```
      patchelf --set-interpreter /some/interpreter ./cat
      ```
  
      执行后`cat`将不再能执行，这是因为内核执行该程序时，会发现其解释器是`/some/interpreter`，认为其是一个动态链接的ELF文件，需要将控制权交接给这个interpreter，由于不存在这个interpreter，所以就会显示如下的结果；
  
      > $ ./cat
      > bash: ./cat: No such file or directory
  
      使用`ldd`来查看interpreter等信息；
  
      这里碰到的问题是，我先前搭建了交叉编译环境，ldd被指定使用了mips架构下的/bin/ldd了，所以把先前在`.bashrc`中加的相应的`$PATH`去掉就可以了；
  
      ```
      $ ldd cat
      	linux-vdso.so.1 =>  (0x00007ffc364de000)
      	libc.so.6 => /lib/x86_64-linux-gnu/libc.so.6 (0x00007efccd0f5000)
      	/some/interpreter => /lib64/ld-linux-x86-64.so.2 (0x00007efccd4bf000)
      ```
  
      可以看到相应的interpreter已经被设置为指定的interpreter；
  
      现在重新编译cat.c，然后使用下面命令看一下具体的过程；
  
      ```
      $ strace ./cat cat.c
      execve("./cat", ["./cat", "cat.c"], [/* 77 vars */]) = 0
      brk(NULL)                               = 0x1e13000
      access("/etc/ld.so.nohwcap", F_OK)      = -1 ENOENT (No such file or directory)
      access("/etc/ld.so.preload", R_OK)      = -1 ENOENT (No such file or directory)
      open("/etc/ld.so.cache", O_RDONLY|O_CLOEXEC) = 3
      fstat(3, {st_mode=S_IFREG|0644, st_size=128025, ...}) = 0
      mmap(NULL, 128025, PROT_READ, MAP_PRIVATE, 3, 0) = 0x7fd1cded9000
      close(3)                                = 0
      access("/etc/ld.so.nohwcap", F_OK)      = -1 ENOENT (No such file or directory)
      open("/lib/x86_64-linux-gnu/libc.so.6", O_RDONLY|O_CLOEXEC) = 3
      read(3, "\177ELF\2\1\1\3\0\0\0\0\0\0\0\0\3\0>\0\1\0\0\0`\t\2\0\0\0\0\0"..., 832) = 832
      fstat(3, {st_mode=S_IFREG|0755, st_size=1868984, ...}) = 0
      mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7fd1cded8000
      mmap(NULL, 3971488, PROT_READ|PROT_EXEC, MAP_PRIVATE|MAP_DENYWRITE, 3, 0) = 0x7fd1cd90a000
      mprotect(0x7fd1cdaca000, 2097152, PROT_NONE) = 0
      mmap(0x7fd1cdcca000, 24576, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x1c0000) = 0x7fd1cdcca000
      mmap(0x7fd1cdcd0000, 14752, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_ANONYMOUS, -1, 0) = 0x7fd1cdcd0000
      close(3)                                = 0
      mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7fd1cded7000
      mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7fd1cded6000
      arch_prctl(ARCH_SET_FS, 0x7fd1cded7700) = 0
      mprotect(0x7fd1cdcca000, 16384, PROT_READ) = 0
      mprotect(0x600000, 4096, PROT_READ)     = 0
      mprotect(0x7fd1cdef9000, 4096, PROT_READ) = 0
      munmap(0x7fd1cded9000, 128025)          = 0
      open("cat.c", O_RDONLY)                 = 3
      read(3, "// cat.c\nint main(int argc, char"..., 1024) = 189
      write(1, "// cat.c\nint main(int argc, char"..., 189// cat.c
      int main(int argc, char *argv[]){
          char buf[1024];
          int n;
          int fd = argc == 1 ? 0 : open(argv[1], 0);
          while ((n = read(fd, buf, 1024)) > 0 && write(1, buf, n) > 0);
      }) = 189
      read(3, "", 1024)                       = 0
      exit_group(0)                           = ?
      +++ exited with 0 +++
      ```
  
      接着编写一个`preload.c`来体验一下`LD_PRELOAD`的作用；
  
      ```c
      // preload.c
      int read(int fd, char *buf, int n){
          buf[0] = 'p';
          buf[1] = 'w';
          buf[2] = 'n';
          buf[3] = '!';
          buf[4] = '\n';
          return 5;
      }
      ```
  
      编译成共享库文件：
  
      ```
      gcc -shared -o preload.so preload.c
      ```
  
      使用下面的命令看一下结果：
  
      ```
      LD_PRELOAD=./preload.so ./cat cat.c
      ```
  
      将会看到系统程序不停打印了'pwn!'，这是因为加载了这个库文件后，`read`相当于被覆写了，所以程序执行调用`read`函数时，不停打印；
  
      如果用strace看看；
  
      ```
      strace -E LD_PRELOAD=./preload.so ./cat cat.c 2>&1 | head -n 100
      ```
  
      可以看到：
  
      ```
      $ strace -E LD_PRELOAD=./preload.so ./cat cat.c 2>&1 | head -n 100
      execve("./cat", ["./cat", "cat.c"], [/* 78 vars */]) = 0
      brk(NULL)                               = 0x15e0000
      access("/etc/ld.so.nohwcap", F_OK)      = -1 ENOENT (No such file or directory)
      open("./preload.so", O_RDONLY|O_CLOEXEC) = 3
      read(3, "\177ELF\2\1\1\0\0\0\0\0\0\0\0\0\3\0>\0\1\0\0\0P\5\0\0\0\0\0\0"..., 832) = 832
      fstat(3, {st_mode=S_IFREG|0775, st_size=7872, ...}) = 0
      mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7f9b4a68a000
      getcwd("/home/klose/ctf/pwn/pwncollege/program_interaction/sample_dynamic_link", 128) = 71
      mmap(NULL, 2101288, PROT_READ|PROT_EXEC, MAP_PRIVATE|MAP_DENYWRITE, 3, 0) = 0x7f9b4a264000
      mprotect(0x7f9b4a265000, 2093056, PROT_NONE) = 0
      mmap(0x7f9b4a464000, 8192, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0) = 0x7f9b4a464000
      close(3)                                = 0
      access("/etc/ld.so.preload", R_OK)      = -1 ENOENT (No such file or directory)
      open("/etc/ld.so.cache", O_RDONLY|O_CLOEXEC) = 3
      fstat(3, {st_mode=S_IFREG|0644, st_size=128025, ...}) = 0
      mmap(NULL, 128025, PROT_READ, MAP_PRIVATE, 3, 0) = 0x7f9b4a66a000
      close(3)                                = 0
      access("/etc/ld.so.nohwcap", F_OK)      = -1 ENOENT (No such file or directory)
      open("/lib/x86_64-linux-gnu/libc.so.6", O_RDONLY|O_CLOEXEC) = 3
      read(3, "\177ELF\2\1\1\3\0\0\0\0\0\0\0\0\3\0>\0\1\0\0\0`\t\2\0\0\0\0\0"..., 832) = 832
      ```
  
      其中加载了`preload.so`库文件，并且环境变量也被改变了（多了一个）；
  
      现在看一下`LD_LIBRARY_PATH`；
  
      ```
      strace -E LD_LIBRARY_PATH=/some/library/path ./cat cat.c 2>&1 | head -n 100
      ```
  
      可以看到：
  
      ```
      $ strace -E LD_LIBRARY_PATH=/some/library/path ./cat cat.c 2>&1 | head -n 100
      execve("./cat", ["./cat", "cat.c"], [/* 78 vars */]) = 0
      brk(NULL)                               = 0x25ce000
      access("/etc/ld.so.nohwcap", F_OK)      = -1 ENOENT (No such file or directory)
      access("/etc/ld.so.preload", R_OK)      = -1 ENOENT (No such file or directory)
      open("/some/library/path/tls/x86_64/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/some/library/path/tls/x86_64", 0x7fffd0e719c0) = -1 ENOENT (No such file or directory)
      open("/some/library/path/tls/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/some/library/path/tls", 0x7fffd0e719c0) = -1 ENOENT (No such file or directory)
      open("/some/library/path/x86_64/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/some/library/path/x86_64", 0x7fffd0e719c0) = -1 ENOENT (No such file or directory)
      open("/some/library/path/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/some/library/path", 0x7fffd0e719c0) = -1 ENOENT (No such file or directory)
      ```
  
      程序首先会加载指定的这个库文件，因为找不到这个库文件，则重新从默认的库文件去寻找；
  
      ```
      stat("/some/library/path", 0x7fffd0e719c0) = -1 ENOENT (No such file or directory)
      open("/etc/ld.so.cache", O_RDONLY|O_CLOEXEC) = 3											<- # here
      fstat(3, {st_mode=S_IFREG|0644, st_size=128025, ...}) = 0
      mmap(NULL, 128025, PROT_READ, MAP_PRIVATE, 3, 0) = 0x7f4775fe4000
      close(3)                                = 0
      access("/etc/ld.so.nohwcap", F_OK)      = -1 ENOENT (No such file or directory)
      open("/lib/x86_64-linux-gnu/libc.so.6", O_RDONLY|O_CLOEXEC) = 3
      read(3, "\177ELF\2\1\1\3\0\0\0\0\0\0\0\0\3\0>\0\1\0\0\0`\t\2\0\0\0\0\0"..., 832) = 832
      fstat(3, {st_mode=S_IFREG|0755, st_size=1868984, ...}) = 0
      mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7f4775fe3000
      mmap(NULL, 3971488, PROT_READ|PROT_EXEC, MAP_PRIVATE|MAP_DENYWRITE, 3, 0) = 0x7f4775a15000
      mprotect(0x7f4775bd5000, 2097152, PROT_NONE) = 0
      mmap(0x7f4775dd5000, 24576, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x1c0000) = 0x7f4775dd5000
      mmap(0x7f4775ddb000, 14752, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_ANONYMOUS, -1, 0) = 0x7f4775ddb000
      close(3)                                = 0
      mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7f4775fe2000
      mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7f4775fe1000
      arch_prctl(ARCH_SET_FS, 0x7f4775fe2700) = 0
      mprotect(0x7f4775dd5000, 16384, PROT_READ) = 0
      mprotect(0x600000, 4096, PROT_READ)     = 0
      mprotect(0x7f4776004000, 4096, PROT_READ) = 0
      munmap(0x7f4775fe4000, 128025)          = 0
      open("cat.c", O_RDONLY)                 = 3
      read(3, "// cat.c\nint main(int argc, char"..., 1024) = 189
      write(1, "// cat.c\nint main(int argc, char"..., 189// cat.c
      int main(int argc, char *argv[]){
          char buf[1024];
          int n;
          int fd = argc == 1 ? 0 : open(argv[1], 0);
          while ((n = read(fd, buf, 1024)) > 0 && write(1, buf, n) > 0);
      }) = 189
      read(3, "", 1024)                       = 0
      exit_group(0)                           = ?
      +++ exited with 0 +++
      ```
  
      使用pathcelf可以指定很多环境变量，例如指定运行路径寻找库文件；
  
      ```
      patchelf --set-rpath /some/runpath ./cat
      ```
  
      然后再运行之前的命令；
  
      ```
      strace -E LD_LIBRARY_PATH=/some/library/path ./cat cat.c 2>&1 | head -n 100
      ```
  
      结果：
  
      ```
      execve("./cat", ["./cat", "cat.c"], [/* 78 vars */]) = 0
      brk(NULL)                               = 0x1fcc000
      access("/etc/ld.so.nohwcap", F_OK)      = -1 ENOENT (No such file or directory)
      access("/etc/ld.so.preload", R_OK)      = -1 ENOENT (No such file or directory)
      open("/some/library/path/tls/x86_64/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/some/library/path/tls/x86_64", 0x7fffc507a120) = -1 ENOENT (No such file or directory)
      open("/some/library/path/tls/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/some/library/path/tls", 0x7fffc507a120) = -1 ENOENT (No such file or directory)
      open("/some/library/path/x86_64/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/some/library/path/x86_64", 0x7fffc507a120) = -1 ENOENT (No such file or directory)
      open("/some/library/path/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/some/library/path", 0x7fffc507a120) = -1 ENOENT (No such file or directory)
      open("/some/runpath/tls/x86_64/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/some/runpath/tls/x86_64", 0x7fffc507a120) = -1 ENOENT (No such file or directory)
      open("/some/runpath/tls/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/some/runpath/tls", 0x7fffc507a120) = -1 ENOENT (No such file or directory)
      open("/some/runpath/x86_64/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/some/runpath/x86_64", 0x7fffc507a120) = -1 ENOENT (No such file or directory)
      open("/some/runpath/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/some/runpath", 0x7fffc507a120)   = -1 ENOENT (No such file or directory)
      open("/etc/ld.so.cache", O_RDONLY|O_CLOEXEC) = 3
      fstat(3, {st_mode=S_IFREG|0644, st_size=128025, ...}) = 0
      mmap(NULL, 128025, PROT_READ, MAP_PRIVATE, 3, 0) = 0x7f0a4a169000
      close(3)                                = 0
      access("/etc/ld.so.nohwcap", F_OK)      = -1 ENOENT (No such file or directory)
      open("/lib/x86_64-linux-gnu/libc.so.6", O_RDONLY|O_CLOEXEC) = 3
      read(3, "\177ELF\2\1\1\3\0\0\0\0\0\0\0\0\3\0>\0\1\0\0\0`\t\2\0\0\0\0\0"..., 832) = 832
      fstat(3, {st_mode=S_IFREG|0755, st_size=1868984, ...}) = 0
      mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7f0a4a168000
      mmap(NULL, 3971488, PROT_READ|PROT_EXEC, MAP_PRIVATE|MAP_DENYWRITE, 3, 0) = 0x7f0a49b9a000
      mprotect(0x7f0a49d5a000, 2097152, PROT_NONE) = 0
      mmap(0x7f0a49f5a000, 24576, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x1c0000) = 0x7f0a49f5a000
      mmap(0x7f0a49f60000, 14752, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_ANONYMOUS, -1, 0) = 0x7f0a49f60000
      close(3)                                = 0
      mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7f0a4a167000
      mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7f0a4a166000
      arch_prctl(ARCH_SET_FS, 0x7f0a4a167700) = 0
      mprotect(0x7f0a49f5a000, 16384, PROT_READ) = 0
      mprotect(0x600000, 4096, PROT_READ)     = 0
      mprotect(0x7f0a4a189000, 4096, PROT_READ) = 0
      munmap(0x7f0a4a169000, 128025)          = 0
      open("cat.c", O_RDONLY)                 = 3
      read(3, "// cat.c\nint main(int argc, char"..., 1024) = 189
      write(1, "// cat.c\nint main(int argc, char"..., 189// cat.c
      int main(int argc, char *argv[]){
          char buf[1024];
          int n;
          int fd = argc == 1 ? 0 : open(argv[1], 0);
          while ((n = read(fd, buf, 1024)) > 0 && write(1, buf, n) > 0);
      }) = 189
      read(3, "", 1024)                       = 0
      exit_group(0)                           = ?
      +++ exited with 0 +++
      ```
  
      可以看到运行程序后，程序会在指定的运行路径寻找相应的库文件；
  
      组合起来看：
  
      ```
      strace -E LD_LIBRARY_PATH=/some/library/path -E LD_PRELOAD=klose.so ./cat cat.c 2>&1 | head -n 100
      ```
  
      结果：
  
      ```
      $ strace -E LD_LIBRARY_PATH=/some/library/path LD_PRELOAD=klose.so ./cat cat.c 2>&1 | head -n 100
      strace: Can't stat 'LD_PRELOAD=klose.so': No such file or directory
      klose@ubuntu:~/ctf/pwn/pwncollege/program_interaction/sample_dynamic_link$ strace -E LD_LIBRARY_PATH=/some/library/path -E LD_PRELOAD=klose.so ./cat cat.c 2>&1 | head -n 100
      execve("./cat", ["./cat", "cat.c"], [/* 79 vars */]) = 0
      brk(NULL)                               = 0x14ea000
      access("/etc/ld.so.nohwcap", F_OK)      = -1 ENOENT (No such file or directory)
      open("/some/library/path/tls/x86_64/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/some/library/path/tls/x86_64", 0x7ffda88779c0) = -1 ENOENT (No such file or directory)
      open("/some/library/path/tls/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/some/library/path/tls", 0x7ffda88779c0) = -1 ENOENT (No such file or directory)
      open("/some/library/path/x86_64/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/some/library/path/x86_64", 0x7ffda88779c0) = -1 ENOENT (No such file or directory)
      open("/some/library/path/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/some/library/path", 0x7ffda88779c0) = -1 ENOENT (No such file or directory)
      open("/some/runpath/tls/x86_64/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/some/runpath/tls/x86_64", 0x7ffda88779c0) = -1 ENOENT (No such file or directory)
      open("/some/runpath/tls/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/some/runpath/tls", 0x7ffda88779c0) = -1 ENOENT (No such file or directory)
      open("/some/runpath/x86_64/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/some/runpath/x86_64", 0x7ffda88779c0) = -1 ENOENT (No such file or directory)
      open("/some/runpath/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/some/runpath", 0x7ffda88779c0)   = -1 ENOENT (No such file or directory)
      open("/etc/ld.so.cache", O_RDONLY|O_CLOEXEC) = 3
      fstat(3, {st_mode=S_IFREG|0644, st_size=128025, ...}) = 0
      mmap(NULL, 128025, PROT_READ, MAP_PRIVATE, 3, 0) = 0x7f472a862000
      close(3)                                = 0
      access("/etc/ld.so.nohwcap", F_OK)      = -1 ENOENT (No such file or directory)
      open("/lib/x86_64-linux-gnu/tls/x86_64/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/lib/x86_64-linux-gnu/tls/x86_64", 0x7ffda88779c0) = -1 ENOENT (No such file or directory)
      open("/lib/x86_64-linux-gnu/tls/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/lib/x86_64-linux-gnu/tls", 0x7ffda88779c0) = -1 ENOENT (No such file or directory)
      open("/lib/x86_64-linux-gnu/x86_64/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/lib/x86_64-linux-gnu/x86_64", 0x7ffda88779c0) = -1 ENOENT (No such file or directory)
      open("/lib/x86_64-linux-gnu/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/lib/x86_64-linux-gnu", {st_mode=S_IFDIR|0755, st_size=20480, ...}) = 0
      open("/usr/lib/x86_64-linux-gnu/tls/x86_64/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/usr/lib/x86_64-linux-gnu/tls/x86_64", 0x7ffda88779c0) = -1 ENOENT (No such file or directory)
      open("/usr/lib/x86_64-linux-gnu/tls/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/usr/lib/x86_64-linux-gnu/tls", 0x7ffda88779c0) = -1 ENOENT (No such file or directory)
      open("/usr/lib/x86_64-linux-gnu/x86_64/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/usr/lib/x86_64-linux-gnu/x86_64", 0x7ffda88779c0) = -1 ENOENT (No such file or directory)
      open("/usr/lib/x86_64-linux-gnu/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/usr/lib/x86_64-linux-gnu", {st_mode=S_IFDIR|0755, st_size=81920, ...}) = 0
      open("/lib/tls/x86_64/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/lib/tls/x86_64", 0x7ffda88779c0) = -1 ENOENT (No such file or directory)
      open("/lib/tls/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/lib/tls", 0x7ffda88779c0)        = -1 ENOENT (No such file or directory)
      open("/lib/x86_64/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/lib/x86_64", 0x7ffda88779c0)     = -1 ENOENT (No such file or directory)
      open("/lib/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/lib", {st_mode=S_IFDIR|0755, st_size=4096, ...}) = 0
      open("/usr/lib/tls/x86_64/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/usr/lib/tls/x86_64", 0x7ffda88779c0) = -1 ENOENT (No such file or directory)
      open("/usr/lib/tls/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/usr/lib/tls", 0x7ffda88779c0)    = -1 ENOENT (No such file or directory)
      open("/usr/lib/x86_64/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/usr/lib/x86_64", 0x7ffda88779c0) = -1 ENOENT (No such file or directory)
      open("/usr/lib/klose.so", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
      stat("/usr/lib", {st_mode=S_IFDIR|0755, st_size=4096, ...}) = 0
      writev(2, [{"ERROR: ld.so: object '", 22}, {"klose.so", 8}, {"' from ", 7}, {"LD_PRELOAD", 10}, {" cannot be preloaded (", 22}, {"cannot open shared object file", 30}, {"): ignored.\n", 12}], 7ERROR: ld.so: object 'klose.so' from LD_PRELOAD cannot be preloaded (cannot open shared object file): ignored.
      ) = 111
      access("/etc/ld.so.preload", R_OK)      = -1 ENOENT (No such file or directory)
      access("/etc/ld.so.nohwcap", F_OK)      = -1 ENOENT (No such file or directory)
      open("/lib/x86_64-linux-gnu/libc.so.6", O_RDONLY|O_CLOEXEC) = 3
      read(3, "\177ELF\2\1\1\3\0\0\0\0\0\0\0\0\3\0>\0\1\0\0\0`\t\2\0\0\0\0\0"..., 832) = 832
      fstat(3, {st_mode=S_IFREG|0755, st_size=1868984, ...}) = 0
      mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7f472a861000
      mmap(NULL, 3971488, PROT_READ|PROT_EXEC, MAP_PRIVATE|MAP_DENYWRITE, 3, 0) = 0x7f472a293000
      mprotect(0x7f472a453000, 2097152, PROT_NONE) = 0
      mmap(0x7f472a653000, 24576, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x1c0000) = 0x7f472a653000
      mmap(0x7f472a659000, 14752, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_ANONYMOUS, -1, 0) = 0x7f472a659000
      close(3)                                = 0
      mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7f472a860000
      mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7f472a85f000
      arch_prctl(ARCH_SET_FS, 0x7f472a860700) = 0
      mprotect(0x7f472a653000, 16384, PROT_READ) = 0
      mprotect(0x600000, 4096, PROT_READ)     = 0
      mprotect(0x7f472a882000, 4096, PROT_READ) = 0
      munmap(0x7f472a862000, 128025)          = 0
      open("cat.c", O_RDONLY)                 = 3
      read(3, "// cat.c\nint main(int argc, char"..., 1024) = 189
      write(1, "// cat.c\nint main(int argc, char"..., 189// cat.c
      int main(int argc, char *argv[]){
          char buf[1024];
          int n;
          int fd = argc == 1 ? 0 : open(argv[1], 0);
          while ((n = read(fd, buf, 1024)) > 0 && write(1, buf, n) > 0);
      }) = 189
      read(3, "", 1024)                       = 0
      exit_group(0)                           = ?
      +++ exited with 0 +++
      ```
  
      可以看到，首先会尝试加载LD_PRELOAD，然后再从LD_LIBRARY_PATH找，再从runpath找；
  
  - 如果文件是静态链接的ELF文件，内核会直接load
  
    - 举个例子
  
      先编译一个静态的cat；
  
      ```
      gcc -static -o cat-static cat.c
      ```
  
      首先对比两个不同链接类型的文件的大小；
  
      ```
      $ du -sb cat-static cat
      912704	cat-static
      12856	cat
      ```
  
      可以看到，静态链接编译的文件非常大；
  
      strace看一下具体执行的步骤；
  
      ```
      $ strace ./cat-static cat.c
      execve("./cat-static", ["./cat-static", "cat.c"], [/* 77 vars */]) = 0
      uname({sysname="Linux", nodename="ubuntu", ...}) = 0
      brk(NULL)                               = 0x1862000
      brk(0x18631c0)                          = 0x18631c0
      arch_prctl(ARCH_SET_FS, 0x1862880)      = 0
      readlink("/proc/self/exe", "/home/klose/ctf/pwn/pwncollege/p"..., 4096) = 81
      brk(0x18841c0)                          = 0x18841c0
      brk(0x1885000)                          = 0x1885000
      access("/etc/ld.so.nohwcap", F_OK)      = -1 ENOENT (No such file or directory)
      open("cat.c", O_RDONLY)                 = 3
      read(3, "// cat.c\nint main(int argc, char"..., 1024) = 189
      write(1, "// cat.c\nint main(int argc, char"..., 189// cat.c
      int main(int argc, char *argv[]){
          char buf[1024];
          int n;
          int fd = argc == 1 ? 0 : open(argv[1], 0);
          while ((n = read(fd, buf, 1024)) > 0 && write(1, buf, n) > 0);
      }) = 189
      read(3, "", 1024)                       = 0
      exit_group(0)                           = ?
      +++ exited with 0 +++
      ```
  
      显然少了很多的步骤；
  
      再看一下虚拟内存映射的地址；
  
      ```
      $ ./cat-static /proc/self/maps 
      00400000-004ca000 r-xp 00000000 08:01 1102410                            /home/klose/ctf/pwn/pwncollege/program_interaction/sample_dynamic_link/cat-static
      006c9000-006cc000 rw-p 000c9000 08:01 1102410                            /home/klose/ctf/pwn/pwncollege/program_interaction/sample_dynamic_link/cat-static
      006cc000-006ce000 rw-p 00000000 00:00 0 
      00b87000-00baa000 rw-p 00000000 00:00 0                                  [heap]
      7ffc6c574000-7ffc6c595000 rw-p 00000000 00:00 0                          [stack]
      7ffc6c5cd000-7ffc6c5d0000 r--p 00000000 00:00 0                          [vvar]
      7ffc6c5d0000-7ffc6c5d2000 r-xp 00000000 00:00 0                          [vdso]
      ffffffffff600000-ffffffffff601000 r-xp 00000000 00:00 0                  [vsyscall]
      ```
  
      关于构造函数来初始化；
  
      ```c
      __attribute__((constructor)) void haha(){
      	puts("Hello, world!\n");
      }
      ```
  
      现在新建一个`new_preload.c`，其内容为：
  
      ```c
      // new_preload.c
      __attribute__((constructor)) void haha(){
      	puts("Hello, world!\n");
      }
      int read(int fd, char *buf, int n){
          buf[0] = 'p';
          buf[1] = 'w';
          buf[2] = 'n';
          buf[3] = '!';
          buf[4] = '\n';
          return 5;
      }
      ```
  
      编译的时候会发生错误：
  
      ```
      $ gcc -shared -o new_preload.so new_preload.c
      new_preload.c: In function ‘haha’:
      new_preload.c:3:2: warning: implicit declaration of function ‘puts’ [-Wimplicit-function-declaration]
        puts("Hello, world!\n");
        ^
      /usr/bin/ld: /tmp/ccyvL35g.o: relocation R_X86_64_32 against `.rodata' can not be used when making a shared object; recompile with -fPIC
      /tmp/ccyvL35g.o: error adding symbols: Bad value
      collect2: error: ld returned 1 exit status
      
      ```
  
      更换一下编译的步骤：
  
      ```
      gcc -fPIC -c new_preload.c -o new_preload.o
      gcc -shared -o new_preload.so new_preload.o
      ```
  
      这次顺利编译了，但是运行的时候却发现没有打印`haha!\n`；
  
      ```
      $ LD_PRELOAD=./new_preload.so ./cat cat.c 2>&1 | head -n 10
      pwn!
      pwn!
      pwn!
      pwn!
      pwn!
      pwn!
      pwn!
      pwn!
      pwn!
      pwn!
      ```
  
      可能因为`puts`的缓存问题，这里更换为`write`函数；
  
      ```c
      // new_preload.c
      __attribute__((constructor)) void haha(){
      	write(1, "Hello, world!\n", 14);
      }
      int read(int fd, char *buf, int n){
          buf[0] = 'p';
          buf[1] = 'w';
          buf[2] = 'n';
          buf[3] = '!';
          buf[4] = '\n';
          return 5;
      }
      ```
  
      然后重新走一遍编译后，运行，即可发现打印了`Hello, world!\n`；
  
      ```
      $ LD_PRELOAD=./new_preload.so ./cat cat.c 2>&1 | head -n 10
      Hello, world!
      pwn!
      pwn!
      pwn!
      pwn!
      pwn!
      pwn!
      pwn!
      pwn!
      pwn!
      
      ```
  
      
  
  - 其他的文件格式会依次进行查询检查

- 加载的内容被放到了哪里？

  - 每个进程有自己的虚拟内存空间，资源包含：

    - 二进制代码
    - 库文件
    - 堆
    - 栈
    - 程序内存指定映射
    - 内核空间

  - 通过`/proc/self/maps`查看；

    ```
    /bin/cat /proc/self/maps
    ```

    可以得到结果：

    ```
    $ /bin/cat /proc/self/maps 
    00400000-0040c000 r-xp 00000000 08:01 3407896                            /bin/cat
    0060b000-0060c000 r--p 0000b000 08:01 3407896                            /bin/cat
    0060c000-0060d000 rw-p 0000c000 08:01 3407896                            /bin/cat
    006e7000-00708000 rw-p 00000000 00:00 0                                  [heap]
    7f18451ed000-7f1845678000 r--p 00000000 08:01 1968757                    /usr/lib/locale/locale-archive
    7f1845678000-7f1845838000 r-xp 00000000 08:01 2622365                    /lib/x86_64-linux-gnu/libc-2.23.so
    7f1845838000-7f1845a38000 ---p 001c0000 08:01 2622365                    /lib/x86_64-linux-gnu/libc-2.23.so
    7f1845a38000-7f1845a3c000 r--p 001c0000 08:01 2622365                    /lib/x86_64-linux-gnu/libc-2.23.so
    7f1845a3c000-7f1845a3e000 rw-p 001c4000 08:01 2622365                    /lib/x86_64-linux-gnu/libc-2.23.so
    7f1845a3e000-7f1845a42000 rw-p 00000000 00:00 0 
    7f1845a42000-7f1845a68000 r-xp 00000000 08:01 2622349                    /lib/x86_64-linux-gnu/ld-2.23.so
    7f1845c22000-7f1845c47000 rw-p 00000000 00:00 0 
    7f1845c67000-7f1845c68000 r--p 00025000 08:01 2622349                    /lib/x86_64-linux-gnu/ld-2.23.so
    7f1845c68000-7f1845c69000 rw-p 00026000 08:01 2622349                    /lib/x86_64-linux-gnu/ld-2.23.so
    7f1845c69000-7f1845c6a000 rw-p 00000000 00:00 0 
    7ffeb3273000-7ffeb3294000 rw-p 00000000 00:00 0                          [stack]
    7ffeb32ca000-7ffeb32cd000 r--p 00000000 00:00 0                          [vvar]
    7ffeb32cd000-7ffeb32cf000 r-xp 00000000 00:00 0                          [vdso]
    ffffffffff600000-ffffffffff601000 r-xp 00000000 00:00 0                  [vsyscall]
    ```

### lauch

ELF程序会调用libc中的`__libc_start_main`，这个函数会调用`main`函数；

可以覆写一个`__libc_start_main`函数，如下：

```c
int __libc_start_main(
	int *(main) (int, char * *, char * *),
	int argc,					// 参数
	char * * ubp_av,			// ubp_av
	void (*init) (void),		// 初始化指针
	vooid (*fini) (void),
	void (*rtld_fini),
	(void), void (* stack_end)
)
{
	puts("Hello, my name is __libc_start_main !\n");
	exit(main(argc, ubp_av, 0));
}
```

编译成共享库文件方便后面在LD_PRELOAD中添加；

```
gcc -fPIC -c start_main.c -o start_main.o
gcc -shared -o start_main.so start_main.o
```

尝试运行一下，可以发现成功加载了该共享库；

```
$ LD_PRELOAD=./start_main.so ./cat cat.c
Hello, my name is __libc_start_main !

// cat.c
int main(int argc, char *argv[]){
    char buf[1024];
    int n;
    int fd = argc == 1 ? 0 : open(argv[1], 0);
    while ((n = read(fd, buf, 1024)) > 0 && write(1, buf, n) > 0);
```

现在用命令看一下二进制文件cat；

- 查看其ELF Header信息；

  ```
  $ readelf -h cat
  ELF Header:
    Magic:   7f 45 4c 46 02 01 01 00 00 00 00 00 00 00 00 00 
    Class:                             ELF64
    Data:                              2's complement, little endian
    Version:                           1 (current)
    OS/ABI:                            UNIX - System V
    ABI Version:                       0
    Type:                              EXEC (Executable file)
    Machine:                           Advanced Micro Devices X86-64
    Version:                           0x1
    Entry point address:               0x400530
    Start of program headers:          64 (bytes into file)
    Start of section headers:          10872 (bytes into file)
    Flags:                             0x0
    Size of this header:               64 (bytes)
    Size of program headers:           56 (bytes)
    Number of program headers:         11
    Size of section headers:           64 (bytes)
    Number of section headers:         31
    Section header string table index: 30
  ```

  可以清楚看到其`Entry pint address`为`0x400530`；

- 使用objdump查看汇编；

  ```
  objdump -d cat
  ```

  在`<_start>`中可以看到调用了`__libc_start_main`；

  ```
  Disassembly of section .text:
  
  0000000000400530 <_start>:
    400530:	31 ed                	xor    %ebp,%ebp
    400532:	49 89 d1             	mov    %rdx,%r9
    400535:	5e                   	pop    %rsi
    400536:	48 89 e2             	mov    %rsp,%rdx
    400539:	48 83 e4 f0          	and    $0xfffffffffffffff0,%rsp
    40053d:	50                   	push   %rax
    40053e:	54                   	push   %rsp
    40053f:	49 c7 c0 70 07 40 00 	mov    $0x400770,%r8
    400546:	48 c7 c1 00 07 40 00 	mov    $0x400700,%rcx
    40054d:	48 c7 c7 26 06 40 00 	mov    $0x400626,%rdi
    400554:	e8 a7 ff ff ff       	callq  400500 <__libc_start_main@plt>		<- call here
    400559:	f4                   	hlt    
    40055a:	66 0f 1f 44 00 00    	nopw   0x0(%rax,%rax,1)
  ```

### read arg & environ

一个打印当前环境变量的程序；

```c
int main(int argc, char **argv, char **envp)
{
	for(int i = 0; envp[i] != 0; i++)
		puts(envp[i]);
}
```

编译执行可以看到系统的环境变量被打印；

### function to syscall

#### symbol

查看程序的符号；

```
$ nm -D cat
                 w __gmon_start__
                 U __libc_start_main
                 U open
                 U read
                 U __stack_chk_fail
                 U write
```

针对`./cat cat.c`这条命令，用strace查看其具体功能；

```
open("cat.c", O_RDONLY)                 = 3
read(3, "// cat.c\nint main(int argc, char"..., 1024) = 189
write(1, "// cat.c\nint main(int argc, char"..., 189// cat.c
int main(int argc, char *argv[]){
    char buf[1024];
    int n;
    int fd = argc == 1 ? 0 : open(argv[1], 0);
    while ((n = read(fd, buf, 1024)) > 0 && write(1, buf, n) > 0);
}) = 189
read(3, "", 1024)                       = 0
exit_group(0)                           = ?
+++ exited with 0 +++
```

可以看到就是在只读下打开了cat.c文件，读取189bytes，然后打印出来；

#### syscall

实际上cat.c中的write和read是通过调用libc中的write和read，之后实现系统调用去执行，所以基于此也可以直接使用系统调用来实现读写；

```c
// cat.c
int main(int argc, char *argv[]){
    char buf[1024];
    int n;
    int fd = argc == 1 ? 0 : open(argv[1], 0);
    while ((n = syscall(0, fd, buf, 1024)) > 0 && syscall(1, 1, buf, n) > 0);
}
```

可以通过`man 2 syscall`来查看系统调用函数syscall的使用；

编译运行上面的`cat.c`后发现效果一致；

#### signal

9 -> sigkill

19 -> sigstop

`man 7 signal` -> read handbook

一个例子；

```c
int handler(int signal){
	printf("Got signal number: %d !\n", signal);
}

int main(){
	for(int i = 1; i <= 64; i++)
		signal(i, handler);
	while(1);
}
```

编译运行一下；

```
gcc signal.c -o signal
```

可以看到在执行后会循环监听signal handler；

例如使用`ctrl + z`会产生20号信号，使用`ctrl + c`会产生2号信号；

```
$ ./signal 
^ZGot signal number: 20 !
^ZGot signal number: 20 !
^ZGot signal number: 20 !
^CGot signal number: 2 !
^CGot signal number: 2 !
^CGot signal number: 2 !
^CGot signal number: 2 !
```

如果想要退出在其他窗口使用

```
kill -19 $(pgrep signal)
```

以产生19号信号；

使用`kill -l`查看各种信号处理号和其对应的名称；

14号为sysalrm，一个提示处理的信号；

下面有一个演示例子，可以使用`man alarm`查看相关的信息；

```c
// alarm.c
int handler(int signal){
    printf("HELLO!\n");
}
int main(){
	alarm(3);
    signal(14, handler);
    sleep(10000);
}
```

编译运行可以在三秒打印一次`HELLO!\n`；

#### shared memory

use a shared memory-mapped file in `/dev/shm`；

### process termination

程序会在两种情况下终止：

- 收到了无法处理的信号；
- exit()系统调用

## Challenge

