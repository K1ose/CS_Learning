---
title: microcorruption-3-Hanoi
top: false
comment: false
lang: zh-CN
date: 2021-12-22 18:40:09
tags:
categories:
  - PWN
  - microcorruption
---

# Hanoi

多了一个login函数，其实功能相似，题目提示密码是8-16位，接下来继续看关键函数`test_password_valid`；

```
4454 <test_password_valid>
4454:  0412           push	r4
4456:  0441           mov	sp, r4
4458:  2453           incd	r4
445a:  2183           decd	sp
445c:  c443 fcff      mov.b	#0x0, -0x4(r4)
4460:  3e40 fcff      mov	#0xfffc, r14
4464:  0e54           add	r4, r14
4466:  0e12           push	r14
4468:  0f12           push	r15
446a:  3012 7d00      push	#0x7d
446e:  b012 7a45      call	#0x457a <INT>
4472:  5f44 fcff      mov.b	-0x4(r4), r15
4476:  8f11           sxt	r15
4478:  3152           add	#0x8, sp
447a:  3441           pop	r4
447c:  3041           ret
```
