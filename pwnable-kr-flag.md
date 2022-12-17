---
title: pwnable.kr_flag
top: false
comment: false
lang: zh-CN
date: 2021-11-15 16:32:50
tags:
categories:
  - CTF
  - PWN
  - wp
  - pwnable.kr
---

# flag

## checksec

```
[*] '/home/klose/ctf/pwn/file/pwnable_kr/flag/flag'
    Arch:     amd64-64-little
    RELRO:    No RELRO
    Stack:    No canary found
    NX:       NX disabled
    PIE:      No PIE (0x400000)
    RWX:      Has RWX segments
    Packer:   Packed with UPX
```

看到有个UPX壳；

strings看一下；

```
$ strings flag | grep UPX | bat
───────┬──────────────────────────────────────────────────────────────────────────────────────
       │ STDIN
───────┼──────────────────────────────────────────────────────────────────────────────────────
   1   │ UPX!
   2   │ $Info: This file is packed with the UPX executable packer http://upx.sf.net $
   3   │ $Id: UPX 3.08 Copyright (C) 1996-2011 the UPX Team. All Rights Reserved. $
   4   │ UPX!
   5   │ UPX!
───────┴──────────────────────────────────────────────────────────────────────────────────────
```

## remove shell

直接用工具砸壳；

[upx](https://github.com/upx/upx/releases/tag/v3.94)

```
$ upx -d flag
                       Ultimate Packer for eXecutables
                          Copyright (C) 1996 - 2017
UPX 3.94        Markus Oberhumer, Laszlo Molnar & John Reiser   May 12th 2017

        File size         Ratio      Format      Name
   --------------------   ------   -----------   -----------
    883745 <-    335288   37.94%   linux/amd64   flag

Unpacked 1 file.
```

对去壳后的可执行文件进行分析；

ida分析，在main函数中：

```
.text:0000000000401164 ; __unwind {
.text:0000000000401164                 push    rbp
.text:0000000000401165                 mov     rbp, rsp
.text:0000000000401168                 sub     rsp, 10h
.text:000000000040116C                 mov     edi, offset aIWillMallocAnd ; "I will malloc() and strcpy the flag the"...
.text:0000000000401171                 call    puts
.text:0000000000401176                 mov     edi, 64h ; 'd'
.text:000000000040117B                 call    malloc
.text:0000000000401180                 mov     [rbp+dest], rax
.text:0000000000401184                 mov     rdx, cs:flag
.text:000000000040118B                 mov     rax, [rbp+dest]
.text:000000000040118F                 mov     rsi, rdx        ; src
.text:0000000000401192                 mov     rdi, rax        ; dest
.text:0000000000401195                 call    _strcpy
.text:000000000040119A                 mov     eax, 0
.text:000000000040119F                 leave
.text:00000000004011A0                 retn
```

可以看到，`cs:flag`被放到了rdx，下断点，调试一下看一下rdx寄存器的内容就可以了；

```
gef➤  x/s $rdx
0x496628:	"UPX...? sounds like a delivery service :)"
```

