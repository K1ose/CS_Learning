---
title: kernel_debug
top: false
comment: false
lang: zh-CN
date: 2021-11-13 20:31:17
tags:
categories:
  - CTF
  - PWN
  - kernel pwn
  - knowledge
---

# kernel debug

qemu和gdb调试，qemu中有内置的gdb接口，通过help查看：

```bash
$ qemu-system-x86_64 --help | grep gdb | bat
───────┬──────────────────────────────────────────────────────────────────────────────────────
       │ STDIN
───────┼──────────────────────────────────────────────────────────────────────────────────────
   1   │ -gdb dev        wait for gdb connection on 'dev'
   2   │ -s              shorthand for -gdb tcp::1234
───────┴──────────────────────────────────────────────────────────────────────────────────────
```

可以通过`-gdb tcp:port`或者`-s`来开启调试端口；

通过 `gdb ./vmlinux` 启动时，虽然加载了 kernel 的符号表，但没有加载驱动 `core.ko` 的符号表，可以通过 `add-symbol-file core.ko textaddr` 加载；

```
pwndbg> help add-symbol-file
Load symbols from FILE, assuming FILE has been dynamically loaded.
Usage: add-symbol-file FILE ADDR [-s <SECT> <SECT_ADDR> -s <SECT> <SECT_ADDR> ...]
ADDR is the starting address of the file's text.
The optional arguments are section-name section-address pairs and
should be specified if the data and bss segments are not contiguous
with the text.  SECT is a section name to be loaded at SECT_ADDR.
```

例如，在运行的kernel中输入命令：

```
/ # cat /sys/module/core/sections/.text
```

将得到的text地址，作为符号表，通过gdb加载，即：在终端中输入：

```
gdb ./vmlinux -q
```

通过下面的指令加载符号表；

```
pwndbg> add-symbol-file core/core.ko 0xffffffffc008c000
```

有此之后就可以下断点，通过qemu和gdb进行调试；

```
pwndbg> b core_read
Breakpoint 1 at 0xffffffffc008c063
pwndbg> b *(0xffffffffc008c000+0xcc)
Breakpoint 2 at 0xffffffffc008c0cc
pwndbg> target remote localhost:1234
...
```

