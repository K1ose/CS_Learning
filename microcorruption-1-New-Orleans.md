---
title: microcorruption-1-New-Orleans
top: false
comment: false
lang: zh-CN
date: 2021-12-22 02:39:56
tags:
categories:
  - PWN
  - microcorruption
---

# New Orleans

程序和之前的题目相似，关键的地方仍然在`check_password`；

第一次输入密码先随便输入，这里输入`12345678`；

```
44bc <check_password>
44bc:  0e43           clr	r14
44be:  0d4f           mov	r15, r13
44c0:  0d5e           add	r14, r13
44c2:  ee9d 0024      cmp.b	@r13, 0x2400(r14)
44c6:  0520           jne	#0x44d2 <check_password+0x16>
44c8:  1e53           inc	r14
44ca:  3e92           cmp	#0x8, r14
44cc:  f823           jne	#0x44be <check_password+0x2>
44ce:  1f43           mov	#0x1, r15
44d0:  3041           ret
44d2:  0f43           clr	r15
44d4:  3041           ret
```

在`check_password`函数中，如果r13寄存器的值和0x2400的内存中的值不相等，则会跳转到0x44d2，导致r15寄存器被清零，产生的后续影响就是输出`Invalid password; try again.`，如下：

```
4454:  0f93           tst	r15
4456:  0520           jnz	#0x4462 <main+0x2a>
4458:  3f40 0345      mov	#0x4503 "Invalid password; try again.", r15
445c:  b012 9445      call	#0x4594 <puts>
4460:  063c           jmp	#0x446e <main+0x36>
4462:  3f40 2045      mov	#0x4520 "Access Granted!", r15
4466:  b012 9445      call	#0x4594 <puts>
446a:  b012 d644      call	#0x44d6 <unlock_door>
```

因此，在运行到`cmp.b	@r13, 0x2400(r14)`时，查看一下r13寄存器的内容和0x2400的内容，可以发现r13寄存器中的内容是我们输入的密码，而0x2400的内容则是存储在内存中的指定字符串`iQZt9Z'`；

```
> r r13
   439c:   3132 3334 3536 3738  12345678
   43a4:   0000 0000 0000 0000  ........
   43ac:   0000 0000 0000 0000  ........
   43b4:   0000 0000 0000 0000  ........

> r 2400
   2400:   6951 5a74 395a 2700  iQZt9Z'.
   2408:   0000 0000 0000 0000  ........
   2410:   0000 0000 0000 0000  ........
   2418:   0000 0000 0000 0000  ........
```

因此只需要输入这个字符串就可以unlock；
