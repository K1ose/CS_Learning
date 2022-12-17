---
title: microcorruption-2-Sydney
top: false
comment: false
lang: zh-CN
date: 2021-12-22 18:09:08
tags:
categories:
  - PWN
  - microcorruption
---

# Sydney

题目类似，还是看关键函数`check_password`；

```
448a <check_password>
448a:  bf90 4560 0000 cmp	#0x6045, 0x0(r15)
4490:  0d20           jnz	$+0x1c
4492:  bf90 706c 0200 cmp	#0x6c70, 0x2(r15)
4498:  0920           jnz	$+0x14
449a:  bf90 6528 0400 cmp	#0x2865, 0x4(r15)
44a0:  0520           jne	#0x44ac <check_password+0x22>
44a2:  1e43           mov	#0x1, r14
44a4:  bf90 6269 0600 cmp	#0x6962, 0x6(r15)
44aa:  0124           jeq	#0x44ae <check_password+0x24>
44ac:  0e43           clr	r14
44ae:  0f4e           mov	r14, r15
44b0:  3041           ret
```

这里可以看到比较了r15寄存器中是否存放了`0x6045,0x6c70,0x2865,0x6962`，如果不是则会跳转到0x44ac的位置。

由于小端序的原因，需要将顺序调整，即r15寄存器中的内容应当为：`4560706c65286269`，在输入密码时选择以binary的形式输入即可；

```
> r r15
   439c:   4560 706c 6528 6269  E`ple(bi
   43a4:   0000 0000 0000 0000  ........
   43ac:   0000 0000 0000 0000  ........
   43b4:   0000 0000 0000 0000  ........
```

最后跳转到0x44ae，即绕过了对r14寄存器清零再赋值给r15的操作，保证r15的值不为零，从而通过后面的验证；

```
4450:  0f93           tst	r15
4452:  0520           jnz	#0x445e <main+0x26>
4454:  3f40 d444      mov	#0x44d4 "Invalid password; try again.", r15
4458:  b012 6645      call	#0x4566 <puts>
445c:  093c           jmp	#0x4470 <main+0x38>
445e:  3f40 f144      mov	#0x44f1 "Access Granted!", r15
4462:  b012 6645      call	#0x4566 <puts>
```

