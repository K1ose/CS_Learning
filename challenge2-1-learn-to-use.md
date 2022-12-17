---
title: 'challenge2.1:learn_to_use'
top: false
comment: false
lang: zh-CN
date: 2021-11-11 16:13:06
tags:
categories:
  - OS
  - lab1
  - challenge2:qemu&gdb
---

# challenge2.1:learn to use

根据提示，学习一下如何使用gdb下断点调试；

### Makefile-lab1mon

到路径/labcodes_answer/lab1_result/中；

1. 执行

```纯文本
make lab1-mon
```


&ensp;&ensp;&ensp;&ensp;此时qemu启动，运行后立即暂停，gdb显示相关信息；

```纯文本
0x0000fff0 in ?? ()
warning: A handler for the OS ABI "GNU/Linux" is not built into this configuration
of GDB.  Attempting to continue with the default i8086 settings.

The target architecture is assumed to be i8086
Breakpoint 1 at 0x7c00

Breakpoint 1, 0x00007c00 in ?? ()
=> 0x7c00:  cli    
   0x7c01:  cld    
(gdb) 

```


2. 查看Makefile中lab1-mon的信息，依次执行；

```纯文本
less Makefile
/lab1-mon
```


&ensp;&ensp;&ensp;&ensp;可以看到：

```Makefile
lab1-mon: $(UCOREIMG)
        $(V)$(TERMINAL) -e "$(QEMU) -S -s -d in_asm -D $(BINDIR)/q.log -monitor stdio -hda $< -serial null"
        $(V)sleep 2
        $(V)$(TERMINAL) -e "gdb -q -x tools/lab1init"

```


&ensp;&ensp;&ensp;&ensp;在开启qemu后，qemu等待指令。短暂休眠后，gdb加载lab1init参数，进行调试；

3. 查看/tools/lab1init的内容；

```纯文本
file bin/kernel
target remote :1234
set architecture i8086
b *0x7c00
continue
x /2i $pc

```


&ensp;&ensp;&ensp;&ensp;可以看到，gdb加载了bin/kernel，连接远程调试的端口，并设置了架构为i8086，断点设置在了0x7c00，执行继续，并打印出eip所在和下一条的汇编指令；

### 单步跟踪BIOS

CPU加电后，CPU中ROM存储器将初始值赋值给寄存器，其中`CS`为`0xf000`，`IP`为`0xfff0`；因此读取的位置为`f000:fff0`；

调试可知：

让qemu等待调试；

```纯文本
qemu -S -s -hda bin/ucore.img -monitor stdio
```


gdb调试；

```纯文本
(gdb) target remote 127.0.0.1:1234
Remote debugging using 127.0.0.1:1234
0x0000fff0 in ?? ()
(gdb) 

```


根据PC = 16*CS + IP，我们可以得到PC = 0xffff0，因此BIOS第一条指令的位置为`0xffff0`；

在Makefile中，存在debug操作：

```Makefile
debug: $(UCOREIMG)
  $(V)$(QEMU) -S -s -parallel stdio -hda $< -serial null &
  $(V)sleep 2
  $(V)$(TERMINAL) -e "gdb -q -tui -x tools/gdbinit"
```


这里载入了gdbinit的命令，可以去看一下；

```纯文本
file bin/kernel
target remote :1234
break kern_init
coutinue

```


去掉`continue`，因为`break`了又`continue`，相当于没有`break`；

```纯文本
file bin/kernel
target remote :1234
break kern_init
```


再次执行`make debug`，即有：

```纯文本
0x0000fff0 in ?? ()
Breakpoint 1 at 0x100000: file kern/init/init.c, line 17.
(gdb) ni
0x0000e05b in ?? ()
(gdb) 

```


可以看到：

```纯文本
(gdb) x/i 0xffff0
   0xffff0:     ljmp   $0x3630,$0xf000e05b

```


第一条指令即是跳转到`0xf000e05b`，证明修改gdbinit后执行的即是第一条指令；

