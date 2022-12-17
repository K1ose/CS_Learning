---
title: 'challenge5:trace_stackframe'
top: false
comment: false
lang: zh-CN
date: 2021-11-11 16:26:23
tags:
categories:
  - OS
  - lab1
  - challenge5:trace stackframe
---

# challenge5:trace stack frame

完成kdebug.c中函数print_stackframe的实现，可以通过函数print_stackframe来跟踪函数调用堆栈中记录的返回地址，通过编译make qemu来理解输出结果；

在完成编译后：

1. 查看lab1/obj/bootblock.asm，了解bootloader源码与机器码的语句和地址等的对应关系；

2. 查看lab1/obj/kernel.asm，了解 ucore OS源码与机器码的语句和地址等的对应关系。

## 函数调用栈

在kern/debug/kdebug.c中，存在需要填补的print_stackframe()函数：

```C
/* *
 * print_stackframe - print a list of the saved eip values from the nested 'call'
 * instructions that led to the current point of execution
 *
 * The x86 stack pointer, namely esp, points to the lowest location on the stack
 * that is currently in use. Everything below that location in stack is free. Pushing
 * a value onto the stack will invole decreasing the stack pointer and then writing
 * the value to the place that stack pointer pointes to. And popping a value do the
 * opposite.
 *
 * The ebp (base pointer) register, in contrast, is associated with the stack
 * primarily by software convention. On entry to a C function, the function's
 * prologue code normally saves the previous function's base pointer by pushing
 * it onto the stack, and then copies the current esp value into ebp for the duration
 * of the function. If all the functions in a program obey this convention,
 * then at any given point during the program's execution, it is possible to trace
 * back through the stack by following the chain of saved ebp pointers and determining
 * exactly what nested sequence of function calls caused this particular point in the
 * program to be reached. This capability can be particularly useful, for example,
 * when a particular function causes an assert failure or panic because bad arguments
 * were passed to it, but you aren't sure who passed the bad arguments. A stack
 * backtrace lets you find the offending function.
 *
 * The inline function read_ebp() can tell us the value of current ebp. And the
 * non-inline function read_eip() is useful, it can read the value of current eip,
 * since while calling this function, read_eip() can read the caller's eip from
 * stack easily.
 *
 * In print_debuginfo(), the function debuginfo_eip() can get enough information about
 * calling-chain. Finally print_stackframe() will trace and print them for debugging.
 *
 * Note that, the length of ebp-chain is limited. In boot/bootasm.S, before jumping
 * to the kernel entry, the value of ebp has been set to zero, that's the boundary.
 * */
void
print_stackframe(void) {
     /* LAB1 YOUR CODE : STEP 1 */
     /* (1) call read_ebp() to get the value of ebp. the type is (uint32_t);
      * (2) call read_eip() to get the value of eip. the type is (uint32_t);
      * (3) from 0 .. STACKFRAME_DEPTH
      *    (3.1) printf value of ebp, eip
      *    (3.2) (uint32_t)calling arguments [0..4] = the contents in address (unit32_t)ebp +2 [0..4]
      *    (3.3) cprintf("\n");
      *    (3.4) call print_debuginfo(eip-1) to print the C calling function name and line number, etc.
      *    (3.5) popup a calling stackframe
      *           NOTICE: the calling funciton's return addr eip  = ss:[ebp+4]
      *                   the calling funciton's ebp = ss:[ebp]
      */
    uint32_t ebp = read_ebp();
    uint32_t eip = read_eip();
    int i, j;
    for (i = 0; i < STACKFRAME_DEPTH; i++)
    {
        cprintf("ebp: 0x%08x\n", ebp); // 16进制输出 %08x
        cprintf("eip: 0x%08x\n", eip);
        uint32_t *args = (uint32_t *)ebp + 2;   // 这里之所以是+2，是因为数据类型为uint32_t，即指向了*arg
        for (j = 0; j < 4; j++)
        {
            cprintf("arg%d: 0x%08x\n", j, args[j]);   // 输出本次调用的参数
        }
        cprintf("\n");
        print_debuginfo(eip - 1);
        eip = ((uint32_t *)ebp)[1];  // 更新eip的值为*ebp+4，即ret_addr
        ebp = ((uint32_t *)ebp)[0];  // 更新ebp的值即保存在该位置
    }
}
```


在`make qemu`后，得到的信息有：

```纯文本
ebp: 0x00007b38
eip: 0x00100a27
arg0: 0x00010094
arg1: 0x00010094
arg2: 0x00007b68
arg3: 0x0010007f

    kern/debug/kdebug.c:340: print_stackframe+21
ebp: 0x00007b48
eip: 0x00100d34
arg0: 0x00000000
arg1: 0x00000000
arg2: 0x00000000
arg3: 0x00007bb8

    kern/debug/kmonitor.c:125: mon_backtrace+10
ebp: 0x00007b68
eip: 0x0010007f
arg0: 0x00000000
arg1: 0x00007b90
arg2: 0xffff0000
arg3: 0x00007b94

    kern/init/init.c:48: grade_backtrace2+19
ebp: 0x00007b88
eip: 0x001000a1
arg0: 0x00000000
arg1: 0xffff0000
arg2: 0x00007bb4
arg3: 0x00000029

    kern/init/init.c:53: grade_backtrace1+27
ebp: 0x00007ba8
eip: 0x001000be
arg0: 0x00000000
arg1: 0x00100000
arg2: 0xffff0000
arg3: 0x00100043

    kern/init/init.c:58: grade_backtrace0+19
ebp: 0x00007bc8
eip: 0x001000df
arg0: 0x00000000
arg1: 0x00000000
arg2: 0x00000000
arg3: 0x00103280

    kern/init/init.c:63: grade_backtrace+26
ebp: 0x00007be8
eip: 0x00100050
arg0: 0x00000000
arg1: 0x00000000
arg2: 0x00000000
arg3: 0x00007c4f

    kern/init/init.c:28: kern_init+79
ebp: 0x00007bf8
eip: 0x00007d6e
arg0: 0xc031fcfa
arg1: 0xc08ed88e
arg2: 0x64e4d08e
arg3: 0xfa7502a8


```


可以看到输出了函数调用栈的内容，输出的内容由栈顶到栈底；

## bootloader源码与机器码

在`bootblock.asm`中，存放了读取磁盘扇区与载入ELF文件的操作；

## ucore OS源码与机器码

从在kernel.asm中，可以看到：

```纯文本
00100d29 <mon_backtrace>:
/* *
 * mon_backtrace - call print_stackframe in kern/debug/kdebug.c to
 * print a backtrace of the stack.
 * */
int
mon_backtrace(int argc, char **argv, struct trapframe *tf) {
  100d29:  55                     push   %ebp
  100d2a:  89 e5                  mov    %esp,%ebp
  100d2c:  83 ec 08               sub    $0x8,%esp
    print_stackframe();
  100d2f:  e8 dd fc ff ff         call   100a11 <print_stackframe>
    return 0;
  100d34:  b8 00 00 00 00         mov    $0x0,%eax
}
  100d39:  c9                     leave  
  100d3a:  c3                     ret    
```


mon_backtrace()函数调用了print_stackframe；

```纯文本
0010006b <grade_backtrace2>:
}

void __attribute__((noinline))
grade_backtrace2(int arg0, int arg1, int arg2, int arg3) {
  10006b:  55                     push   %ebp
  10006c:  89 e5                  mov    %esp,%ebp
  10006e:  83 ec 08               sub    $0x8,%esp
    mon_backtrace(0, NULL, NULL);
  100071:  83 ec 04               sub    $0x4,%esp
  100074:  6a 00                  push   $0x0
  100076:  6a 00                  push   $0x0
  100078:  6a 00                  push   $0x0
  10007a:  e8 aa 0c 00 00         call   100d29 <mon_backtrace>
  10007f:  83 c4 10               add    $0x10,%esp
}
  100082:  90                     nop
  100083:  c9                     leave  
  100084:  c3                     ret    
```


grade_backtrace2调用了mon_backtrace；

以此类推：

```纯文本
00100085 <grade_backtrace1>:

void __attribute__((noinline))
grade_backtrace1(int arg0, int arg1) {
  100085:  55                     push   %ebp
  100086:  89 e5                  mov    %esp,%ebp
  100088:  53                     push   %ebx
  100089:  83 ec 04               sub    $0x4,%esp
    grade_backtrace2(arg0, (int)&arg0, arg1, (int)&arg1);
  10008c:  8d 4d 0c               lea    0xc(%ebp),%ecx
  10008f:  8b 55 0c               mov    0xc(%ebp),%edx
  100092:  8d 5d 08               lea    0x8(%ebp),%ebx
  100095:  8b 45 08               mov    0x8(%ebp),%eax
  100098:  51                     push   %ecx
  100099:  52                     push   %edx
  10009a:  53                     push   %ebx
  10009b:  50                     push   %eax
  10009c:  e8 ca ff ff ff         call   10006b <grade_backtrace2>
  1000a1:  83 c4 10               add    $0x10,%esp
}
  1000a4:  90                     nop
  1000a5:  8b 5d fc               mov    -0x4(%ebp),%ebx
  1000a8:  c9                     leave  
  1000a9:  c3                     ret    

001000aa <grade_backtrace0>:

void __attribute__((noinline))
grade_backtrace0(int arg0, int arg1, int arg2) {
  1000aa:  55                     push   %ebp
  1000ab:  89 e5                  mov    %esp,%ebp
  1000ad:  83 ec 08               sub    $0x8,%esp
    grade_backtrace1(arg0, arg2);
  1000b0:  83 ec 08               sub    $0x8,%esp
  1000b3:  ff 75 10               pushl  0x10(%ebp)
  1000b6:  ff 75 08               pushl  0x8(%ebp)
  1000b9:  e8 c7 ff ff ff         call   100085 <grade_backtrace1>
  1000be:  83 c4 10               add    $0x10,%esp
}
  1000c1:  90                     nop
  1000c2:  c9                     leave  
  1000c3:  c3                     ret    

001000c4 <grade_backtrace>:

void
grade_backtrace(void) {
  1000c4:  55                     push   %ebp
  1000c5:  89 e5                  mov    %esp,%ebp
  1000c7:  83 ec 08               sub    $0x8,%esp
    grade_backtrace0(0, (int)kern_init, 0xffff0000);
  1000ca:  b8 00 00 10 00         mov    $0x100000,%eax
  1000cf:  83 ec 04               sub    $0x4,%esp
  1000d2:  68 00 00 ff ff         push   $0xffff0000     # arg3
  1000d7:  50                     push   %eax            # arg2
  1000d8:  6a 00                  push   $0x0            # arg1
  1000da:  e8 cb ff ff ff         call   1000aa <grade_backtrace0>
  1000df:  83 c4 10               add    $0x10,%esp
}
  1000e2:  90                     nop
  1000e3:  c9                     leave  
  1000e4:  c3                     ret    

```


有如下的调用关系：

```纯文本
kern_init -> grade_backtrace -> grade_backtrace0 -> grade_backtrace1 -> grade_backtrace2 -> mon_backtrace -> print_stackframe
```

