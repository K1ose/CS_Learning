---
title: 'challenge2.4:costomize_breakpoint'
top: false
comment: false
lang: zh-CN
date: 2021-11-11 16:13:51
tags:
categories:
  - OS
  - lab1
  - challenge2:qemu&gdb
---

# challenge2.4:costomize breakpoint

找一个bootloader或内核中的代码位置，设置断点并进行测试。

随便断在`0x7c08`看看；

gdbinit：

```纯文本
file bin/kernel
target remote :1234
break *0x7c08
define hook-stop
x/i $pc
end

```


调试结果

```纯文本
(gdb) ni
=> 0x7c0a:      in     $0x64,%al
0x00007c0a in ?? ()
=> 0x7c0c:      test   $0x2,%al
0x00007c0c in ?? ()
=> 0x7c0e:      jne    0x7c0a
0x00007c0e in ?? ()

```

