---
title: qctf2018_stack2
top: false
comment: false
lang: zh-CN
date: 2021-11-18 21:47:47
tags:
categories:
  - CTF
  -	PWN
  - wp
  - buuoj
---

# qctf2018_stack2

## 信息收集

### checksec

```
    Arch:     i386-32-little
    RELRO:    Partial RELRO
    Stack:    Canary found
    NX:       NX enabled
    PIE:      No PIE (0x8048000)
```

### 执行

```
***********************************************************
*                      An easy calc                       *
*Give me your numbers and I will return to you an average *
*(0 <= x < 256)                                           *
***********************************************************
How many numbers you have:
1
Give me your numbers
2
1. show numbers
2. add number
3. change number
4. get average
5. exit
```

### ida

