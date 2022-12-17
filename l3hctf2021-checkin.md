---
title: l3hctf2021_checkin
top: false
comment: false
lang: zh-CN
date: 2021-11-14 13:01:49
tags:
categories:
  - CTF
  - PWN
  - wp
  - l3hctf
---

# checkin

## 修改链接

使用`patchelf`修改一下`libc`和`ld`的链接；

## 信息收集

### 编译选项

首先关注到hint，给了一堆不知道是gcc还是clang的编译选项：

```
-fsanitize=address -fsanitize-recover=address -fno-omit-frame-pointer -mno-omit-leaf-frame-pointer -fstack-protector-all -Wl,-z,relro,-z,now
```

先看一下二进制文件是用的哪一个版本的编译器编译的；

```
$ objdump -s -j .comment checkin | bat
───────┬──────────────────────────────────────────────────────────────────────────────────────
       │ STDIN
───────┼──────────────────────────────────────────────────────────────────────────────────────
   1   │ 
   2   │ checkin:     file format elf64-x86-64
   3   │ 
   4   │ Contents of section .comment:
   5   │  0000 4743433a 20285562 756e7475 20392e34  GCC: (Ubuntu 9.4
   6   │  0010 2e302d31 7562756e 7475317e 31362e30  .0-1ubuntu1~16.0
   7   │  0020 34292039 2e342e30 00636c61 6e672076  4) 9.4.0.clang v
   8   │  0030 65727369 6f6e2038 2e302e30 2d337e75  ersion 8.0.0-3~u
   9   │  0040 62756e74 7531362e 30342e31 20287461  buntu16.04.1 (ta
  10   │  0050 67732f52 454c4541 53455f38 30302f66  gs/RELEASE_800/f
  11   │  0060 696e616c 2900                        inal).          
───────┴──────────────────────────────────────────────────────────────────────────────────────
```

可以看到是9.4.0版本，查一下[GCC Manual](https://gcc.gnu.org/onlinedocs/gcc/Instrumentation-Options.html)；

这里还有个clang的8.0.0版本？

#### -fsanitize=address

```
-fsanitize=address
```

> Enable AddressSanitizer, a fast memory error detector. Memory access instructions are instrumented to detect out-of-bounds and use-after-free bugs. The option enables -fsanitize-address-use-after-scope. See https://github.com/google/sanitizers/wiki/AddressSanitizer for more details. The run-time behavior can be influenced using the `ASAN_OPTIONS` environment variable. When set to `help=1`, the available options are shown at startup of the instrumented program. See https://github.com/google/sanitizers/wiki/AddressSanitizerFlags#run-time-flags for a list of supported options. The option cannot be combined with -fsanitize=thread or -fsanitize=hwaddress. Note that the only target -fsanitize=hwaddress is currently supported on is AArch64.

这个选项开启了一个名为`AddressSanitizer`的快速内存错误检测器；检测器会对访问内存的指令进行检查，检查是否存在越界和uaf；

#### -fsanitize-recover[=opts]

```
-fsanitize-recover[=opts]
```

>  -fsanitize-recover= controls error recovery mode for sanitizers mentioned in comma-separated list of opts. Enabling this option for a sanitizer component causes it to attempt to continue running the program as if no error happened. This means multiple runtime errors can be reported in a single program run, and the exit code of the program may indicate success even when errors have been reported. The -fno-sanitize-recover= option can be used to alter this behavior: only the first detected error is reported and program then exits with a non-zero exit code.
>
> Currently this feature only works for -fsanitize=undefined (and its suboptions except for -fsanitize=unreachable and -fsanitize=return), -fsanitize=float-cast-overflow, -fsanitize=float-divide-by-zero, -fsanitize=bounds-strict, -fsanitize=kernel-address and -fsanitize=address. For these sanitizers error recovery is turned on by default, except -fsanitize=address, for which this feature is experimental. -fsanitize-recover=all and -fno-sanitize-recover=all is also accepted, the former enables recovery for all sanitizers that support it, the latter disables recovery for all sanitizers that support it.
>
> Even if a recovery mode is turned on the compiler side, it needs to be also enabled on the runtime library side, otherwise the failures are still fatal. The runtime library defaults to `halt_on_error=0` for ThreadSanitizer and UndefinedBehaviorSanitizer, while default value for AddressSanitizer is `halt_on_error=1`. This can be overridden through setting the `halt_on_error` flag in the corresponding environment variable.
>
> Syntax without an explicit opts parameter is deprecated. It is equivalent to specifying an opts list of:
>
> ```
> undefined,float-cast-overflow,float-divide-by-zero,bounds-strict
> ```

#### -fno-omit-frame-pointer

该优化选项会去掉栈中的rbp指针；

#### -mno-omit-leaf-frame-pointer

开启了`-fno-omit-frame-pointer`后，函数具有栈帧操作，空函数是一种`leaf function`，因此需要增加-mno-omit-leaf-frame-pointer；

#### fstack-protector-all

栈的相关保护全开；

> -fstack-protector-all
> 	Like `-fstack-protector`except that all functions are protected.

#### -Wl,-z,relro,-z,now

`-Wl`：传递参数给ld，后面的参数以`,`分割；

`-z,relro,-z,now`：开启了full relro；

### 检查保护

```
$ checksec checkin 
[*] '/home/klose/ctf/pwn/file/adworld/l3hctf2021/checkin/checkin'
    Arch:     amd64-64-little
    RELRO:    Full RELRO
    Stack:    Canary found
    NX:       NX enabled
    PIE:      No PIE (0x400000)
    ASAN:     Enabled
    UBSAN:    Enabled
```

开启了`AddressSanitizer`后，多了ASAN和UBSAN的内存检测保护；

### IDA

