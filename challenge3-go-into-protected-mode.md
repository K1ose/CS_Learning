---
title: 'challenge3:go_into_protected_mode'
top: false
comment: false
lang: zh-CN
date: 2021-11-11 16:18:57
tags:
categories:
  - OS
  - lab1
  - challenge3:go into protected mode
---

# challenge3:go into protected mode

BIOS将通过读取硬盘主引导扇区到内存，并转跳到对应内存中的位置执行bootloader。分析bootloader是如何完成从实模式进入保护模式的。

提示：需要阅读**小节“保护模式和分段机制”**和lab1/boot/bootasm.S源码，了解如何从实模式切换到保护模式，需要了解：

- 为何开启A20，以及如何开启A20

- 如何初始化GDT表

- 如何使能和进入保护模式

### bootLoader开始运作

#### 初始化

首先进入`bootasm.S`中，从`0x7c00`（BIOS加载`bootLoader`到内存`0x7c00`处）开始执行命令；

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


可以看到，先执行`cli`关闭中断，再执行`cld`，将标志寄存器flag的方向标志位df清零，接着将寄存器初始化置零；

> df，全称是direction Flags，是Intel8086/8088CPU程序状态标志寄存器（16位寄存器）九个标志位（CF,PF,AF,ZF,SF,TF,IF,OF,DF）之一。当该位置1时（DF=1），存储器地址自动减少，串操作指令为自动减量指令，即从高位到低位处理字符串；当该位置0时（DF=0），存储器地址自动增加，串操作指令为自动增量指令。


#### 开启A20

##### About A20

###### 历史问题

Intel早期的8086 CPU提供了20根地址线,可寻址空间范围即0~2^20(00000H~FFFFFH)的 1MB内存空间。但8086的数据处理位宽位16位，无法直接寻址1MB内存空间，所以8086提供了段地址加偏移地址的地址转换机制。PC机的寻址结构是segment:offset，segment和offset都是16位的寄存器，最大值是0ffffh，换算成物理地址的计算方法是把segment左移4位，再加上offset，所以segment:offset所能表达的寻址空间最大应为0ffff0h + 0ffffh = 10ffefh（前面的0ffffh是segment=0ffffh并向左移动4位的结果，后面的0ffffh是可能的最大offset），这个计算出的10ffefh是多大呢？大约是1088KB，就是说，segment:offset的地址表示能力，超过了20位地址线的物理寻址能力。所以当寻址到超过1MB的内存时，会发生“回卷”（不会发生异常）。但下一代的基于Intel 80286 CPU的PC AT计算机系统提供了24根地址线，这样CPU的寻址范围变为 2^24=16M,同时也提供了保护模式，可以访问到1MB以上的内存了，此时如果遇到“寻址超过1MB”的情况，系统不会再“回卷”了，这就造成了向下不兼容。为了保持完全的向下兼容性，IBM决定在PC AT计算机系统上加个硬件逻辑，来模仿以上的回绕特征，于是出现了A20 Gate。他们的方法就是把A20地址线控制和键盘控制器的一个输出进行AND操作，这样来控制A20地址线的打开（使能）和关闭（屏蔽\禁止）。一开始时A20地址线控制是被屏蔽的（总为0），直到系统软件通过一定的IO操作去打开它（参看bootasm.S）。很显然，在实模式下要访问高端内存区，这个开关必须打开，在保护模式下，由于使用32位地址线，如果A20恒等于0，那么系统只能访问奇数兆的内存，即只能访问0--1M、2-3M、4-5M......，这样无法有效访问所有可用内存。所以在保护模式下，这个开关也必须打开。

###### 8042键盘控制器

在保护模式下，为了使能所有地址位的寻址能力，需要打开A20地址线控制，即需要通过向键盘控制器8042发送一个命令来完成。键盘控制器8042将会将它的的某个输出引脚的输出置高电平，作为 A20 地址线控制的输入。一旦设置成功之后，内存将不会再被绕回(memory wrapping)，这样我们就可以寻址整个 286 的 16M 内存，或者是寻址 80386级别机器的所有 4G 内存了。

键盘控制器8042的逻辑结构图如下所示。从软件的角度来看，如何控制8042呢？早期的PC机，控制键盘有一个单独的单片机8042，现如今这个芯片已经给集成到了其它大片子中，但其功能和使用方法还是一样，当PC机刚刚出现A20 Gate的时候，估计为节省硬件设计成本，工程师使用这个8042键盘控制器来控制A20 Gate，但A20 Gate与键盘管理没有一点关系。下面先从软件的角度简单介绍一下8042这个芯片。

![](https://chyyuu.gitbooks.io/ucore_os_docs/content/lab1_figs/image012.png)

图13 键盘控制器8042的逻辑结构图

8042键盘控制器的IO端口是0x60～0x6f，实际上IBM PC/AT使用的只有0x60和0x64两个端口（0x61、0x62和0x63用于与XT兼容目的）。8042通过这些端口给键盘控制器或键盘发送命令或读取状态。输出端口P2用于特定目的。位0（P20引脚）用于实现CPU复位操作，位1（P21引脚）用户控制A20信号线的开启与否。系统向输入缓冲（端口0x64）写入一个字节，即发送一个键盘控制器命令。可以带一个参数。参数是通过0x60端口发送的。 命令的返回值也从端口 0x60去读。8042有4个寄存器：

- 1个8-bit长的Input buffer；Write-Only；

- 1个8-bit长的Output buffer； Read-Only；

- 1个8-bit长的Status Register；Read-Only；

- 1个8-bit长的Control Register；Read/Write。

有两个端口地址：60h和64h，有关对它们的读写操作描述如下：

- 读60h端口，读output buffer

- 写60h端口，写input buffer

- 读64h端口，读Status Register

- 操作Control Register，首先要向64h端口写一个命令（20h为读命令，60h为写命令），然后根据命令从60h端口读出Control Register的数据或者向60h端口写入Control Register的数据（64h端口还可以接受许多其它的命令）。

Status Register的定义（要用bit 0和bit 1）：

|      |                                                         |
| ---- | ------------------------------------------------------- |
| bit  | meaning                                                 |
| 0    | output register (60h) 中有数据                          |
| 1    | input register (60h/64h) 有数据                         |
| 2    | 系统标志（上电复位后被置为0）                           |
| 3    | data in input register is command (1) or data (0)       |
| 4    | 1=keyboard enabled, 0=keyboard disabled (via switch)    |
| 5    | 1=transmit timeout (data transmit not complete)         |
| 6    | 1=receive timeout (data transmit not complete)          |
| 7    | 1=even parity rec'd, 0=odd parity rec'd (should be odd) |



除了这些资源外，8042还有3个内部端口：Input Port、Outport Port和Test Port，这三个端口的操作都是通过向64h发送命令，然后在60h进行读写的方式完成，其中本文要操作的A20 Gate被定义在Output Port的bit 1上，所以有必要对Outport Port的操作及端口定义做一个说明。

- 读Output Port：向64h发送0d0h命令，然后从60h读取Output Port的内容

- 写Output Port：向64h发送0d1h命令，然后向60h写入Output Port的数据

- 禁止键盘操作命令：向64h发送0adh

- 打开键盘操作命令：向64h发送0aeh

###### 开启

有了这些命令和知识，就可以实现操作A20 Gate来从实模式切换到保护模式了。 理论上讲，我们只要操作8042芯片的输出端口（64h）的bit 1，就可以控制A20 Gate，但实际上，当你准备向8042的输入缓冲区里写数据时，可能里面还有其它数据没有处理，所以，我们要首先禁止键盘操作，同时等待数据缓冲区中没有数据以后，才能真正地去操作8042打开或者关闭A20 Gate。打开A20 Gate的具体步骤大致如下（参考bootasm.S）：

1. 等待8042 Input buffer为空；

2. 发送Write 8042 Output Port （P2）命令到8042 Input buffer；

3. 等待8042 Input buffer为空；

4. 将8042 Output Port（P2）得到字节的第2位置1，然后写入8042 Input buffer；



接下来开启A20，使得CPU进入保护模式之后能够充分使用32位的寻址能力；

```纯文本
    # Enable A20:
    #  For backwards compatibility with the earliest PCs, physical
    #  address line 20 is tied low, so that addresses higher than
    #  1MB wrap around to zero by default. This code undoes this.
seta20.1:
    inb $0x64, %al                                  # Wait for not busy(8042 input buffer empty).
    testb $0x2, %al                                 # check 2nd bit is 0? if 0 then jump, means not empty.
    jnz seta20.1

    movb $0xd1, %al                                 # send 0xd1(1101 0001) to port 0x64
    outb %al, $0x64                                 # 0xd1 means: write data to 8042's P2 port

seta20.2:
    inb $0x64, %al                                  # Wait for not busy(8042 input buffer empty).
    testb $0x2, %al                                 # check 2nd bit is 0? if 0 then jump, means not empty.
    jnz seta20.2

    movb $0xdf, %al                                 # send 0xdf(1101 1111) to port 0x60
    outb %al, $0x60                                 # 0xdf = 1101 1111, means set P2's A20 bit(the 1 bit, 2nd bit) to 1

```


首先会等待8042键盘控制器输入缓冲区为空，接着发送Write 8042 Output Port （P2）命令到8042 Input buffer，等待8042键盘控制器输入缓冲区为空，将8042 Output Port（P2）得到字节的第2位置为1，这是因为输出端口的第二个位为A20选通标志，然后写入8042 Input buffer；

至此A20开启，进入保护模式之后可以使用4G的寻址能力；

#### GDT表初始化

在`bootasm.S`中存在：

几个参数

```纯文本
# Start the CPU: switch to 32-bit protected mode, jump into C.
# The BIOS loads this code from the first sector of the hard disk into
# memory at physical address 0x7c00 and starts executing in real mode
# with %cs=0 %ip=7c00.

.set PROT_MODE_CSEG,        0x8                     # kernel code segment selector
.set PROT_MODE_DSEG,        0x10                    # kernel data segment selector
.set CR0_PE_ON,             0x1                     # protected mode enable flag
```


GDT初始化

```纯文本
# Bootstrap GDT
.p2align 2                                          # force 4 byte alignment
gdt:
    SEG_NULLASM                                     # null seg
    SEG_ASM(STA_X|STA_R, 0x0, 0xffffffff)           # code seg for bootloader and kernel
    SEG_ASM(STA_W, 0x0, 0xffffffff)                 # data seg for bootloader and kernel

gdtdesc:
    .word 0x17                                      # sizeof(gdt) - 1
    .long gdt                                       # address gdt
```


这里设置了代码段和数据段的`base`和`limit`分别为`0x0`和`0xffffffff`，此时逻辑地址等同于线性地址；

#### 模式切换

而后，使用`lgdt gdtdesc`来载入全局描述符表；

```纯文本
    # Switch from real to protected mode, using a bootstrap GDT
    # and segment translation that makes virtual addresses
    # identical to physical addresses, so that the
    # effective memory map does not change during the switch.
    lgdt gdtdesc                         # load gdt
    movl %cr0, %eax                      # cr0的PE为开启状态，置为1
    orl $CR0_PE_ON, %eax
    movl %eax, %cr0
```


将cr0的PE位置为1，这里用异或置1，把返回值送入cr0；

接着是一个长跳转指令；

```纯文本
    # Jump to next instruction, but in 32-bit code segment.
    # Switches processor into 32-bit mode.
    ljmp $PROT_MODE_CSEG, $protcseg

```


这里将cs修改为32位段寄存器，跳转到了protcseg代码入口处，CPU进入32位模式；

进行寄存器和栈的初始化，并调用bootmain，即进入操作系统内核加载；

```纯文本
.code32                                             # Assemble for 32-bit mode
protcseg:
    # Set up the protected-mode data segment registers
    movw $PROT_MODE_DSEG, %ax                       # Our data segment selector(value = 0x10)
    movw %ax, %ds                                   # -> DS: Data Segment
    movw %ax, %es                                   # -> ES: Extra Segment
    movw %ax, %fs                                   # -> FS
    movw %ax, %gs                                   # -> GS
    movw %ax, %ss                                   # -> SS: Stack Segment

    # Set up the stack pointer and call into C. The stack region is from 0--start(0x7c00)
    movl $0x0, %ebp
    movl $start, %esp
    call bootmain

    # If bootmain returns (it shouldn't), loop.
spin:
    jmp spin
```


至此，bootLoader完成了从实模式(直接物理地址)到保护模式（逻辑地址）的任务；

