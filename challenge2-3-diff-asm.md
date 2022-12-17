---
title: 'challenge2.3:diff_asm'
top: false
comment: false
lang: zh-CN
date: 2021-11-11 16:13:35
tags:
categories:
  - OS
  - lab1
  - challenge2:qemu&gdb
---

# challenge2.3:diff asm

从`0x7c00`开始跟踪代码运行,将单步跟踪反汇编得到的代码与`bootasm.S`和 `bootblock.asm`进行比较。

设置gdbinit，使得调试时能够强制反汇编每一个断点的汇编代码；

```纯文本
file bin/kernel
target remote :1234
break *0x7c00
define hook-stop
x/i $pc
end

```


在make debug后，可以看到断点停留在0x7c00，使用ni观察并记录汇编代码；

```纯文本
reakpoint 1, 0x00007c00 in ?? ()
(gdb) ni
=> 0x7c01:      cld
0x00007c01 in ?? ()
=> 0x7c02:      xor    %eax,%eax
0x00007c02 in ?? ()
=> 0x7c04:      mov    %eax,%ds
0x00007c04 in ?? ()
=> 0x7c06:      mov    %eax,%es
0x00007c06 in ?? ()
=> 0x7c08:      mov    %eax,%ss
0x00007c08 in ?? ()
=> 0x7c0a:      in     $0x64,%al
0x00007c0a in ?? ()

```


现在去找到`bootasm.S`文件，进行对比；

`bootasm.S`

```纯文本
# start address should be 0:7c00, in real mode, the beginning address of the running bootloader
.globl start
start:
.code16                                             # Assemble for 16-bit mode
    cli                                             # Disable interrupts
    cld                                             # String operations increment

    # Set up the important data segment registers (DS, ES, SS).
    xorw %ax, %ax                                   # Segment number zero
    movw %ax, %ds                                   # -> Data Segment
    movw %ax, %es                                   # -> Extra Segment
    movw %ax, %ss                                   # -> Stack Segment


```


几乎没有差异；

同样，在bootblock.asm中：

```NASM
Disassembly of section .text:

00007c00 <start>:

# start address should be 0:7c00, in real mode, the beginning address of the running bootloader
.globl start
start:
.code16                                             # Assemble for 16-bit mode
    cli                                             # Disable interrupts
    7c00:  fa                     cli    
    cld                                             # String operations increment
    7c01:  fc                     cld    

    # Set up the important data segment registers (DS, ES, SS).
    xorw %ax, %ax                                   # Segment number zero
    7c02:  31 c0                  xor    %eax,%eax
    movw %ax, %ds                                   # -> Data Segment
    7c04:  8e d8                  mov    %eax,%ds
    movw %ax, %es                                   # -> Extra Segment
    7c06:  8e c0                  mov    %eax,%es
    movw %ax, %ss                                   # -> Stack Segment
    7c08:  8e d0                  mov    %eax,%ss


```


几乎没有差异；

