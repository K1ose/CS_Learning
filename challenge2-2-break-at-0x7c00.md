---
title: 'challenge2.2:break_at_0x7c00'
top: false
comment: false
lang: zh-CN
date: 2021-11-11 16:13:21
tags:
categories:
  - OS
  - lab1
  - challenge2:qemu&gdb
---

# challenge2.2:break at 0x7c00

在初始化位置`0x7c00`设置实地址断点,测试断点正常。

同理，直接gdbinit修改断点为`0x7c00`即可；

```纯文本
$ cat tools/gdbinit

file bin/kernel
target remote :1234
break *0x7c00

```


执行make debug，看看断点是否正确，能否正常调试；

调试信息如下：

```纯文本
remote Thread 1 In:                                                                L??   PC: 0x7c00 
0x0000fff0 in ?? ()
Breakpoint 1 at 0x7c00
(gdb) c
Continuing.

Breakpoint 1, 0x00007c00 in ?? ()
(gdb) x/i $eip
=> 0x7c00:      cli
(gdb) 

```


可以看到断点和调试正常；

