---
title: bomblab
top: false
comment: false
lang: zh-CN
date: 2021-11-20 15:47:31
tags:
categories:
  - CSAPP
  -	CSAPP-lab
---

# bomblab

## 运行

```
[root@1e4873216d41 bomb]# ./bomb 
Welcome to my fiendish little bomb. You have 6 phases with
which to blow yourself up. Have a nice day!

```

看的出要拆炸弹，但是需要有几个条件，看一下部分源代码；

## source code

```c
initialize_bomb();

    printf("Welcome to my fiendish little bomb. You have 6 phases with\n");
    printf("which to blow yourself up. Have a nice day!\n");

    /* Hmm...  Six phases must be more secure than one phase! */
    input = read_line();             /* Get input                   */
    phase_1(input);                  /* Run the phase               */
    phase_defused();                 /* Drat!  They figured it out!
				      * Let me know how they did it. */
    printf("Phase 1 defused. How about the next one?\n");

    /* The second phase is harder.  No one will ever figure out
     * how to defuse this... */
    input = read_line();
    phase_2(input);
    phase_defused();
    printf("That's number 2.  Keep going!\n");

    /* I guess this is too easy so far.  Some more complex code will
     * confuse people. */
    input = read_line();
    phase_3(input);
    phase_defused();
    printf("Halfway there!\n");

    /* Oh yeah?  Well, how good is your math?  Try on this saucy problem! */
    input = read_line();
    phase_4(input);
    phase_defused();
    printf("So you got that one.  Try this one.\n");
    
    /* Round and 'round in memory we go, where we stop, the bomb blows! */
    input = read_line();
    phase_5(input);
    phase_defused();
    printf("Good work!  On to the next...\n");

    /* This phase will never be used, since no one will get past the
     * earlier ones.  But just in case, make this one extra hard. */
    input = read_line();
    phase_6(input);
    phase_defused();

    /* Wow, they got it!  But isn't something... missing?  Perhaps
     * something they overlooked?  Mua ha ha ha ha! */
    
    return 0;
}

```

需要满足几个phase函数的要求，才能defuse bomb；

由于没有给出具体的函数了，所以反编译一下；

## ida

### phrase1

```c
__int64 __fastcall phase_1(__int64 a1)
{
  __int64 result; // rax

  result = strings_not_equal(a1, "Border relations with Canada have never been better.");
  if ( (_DWORD)result )
    explode_bomb();
  return result;
}
```

`strings_not_equal`是一个判断输入字符串与原定字符串是否相等的函数，显然当不相等的时候，炸弹会引爆；

因此对phrase1的defuse只需要输入`Border relations with Canada have never been better.`即可；

### phrase2

```c
__int64 __fastcall phase_2(__int64 a1)
{
  __int64 result; // rax
  char *v2; // rbx
  int v3; // [rsp+0h] [rbp-38h] BYREF
  char v4; // [rsp+4h] [rbp-34h] BYREF
  char v5; // [rsp+18h] [rbp-20h] BYREF

  read_six_numbers(a1, (__int64)&v3);
  if ( v3 != 1 )
    explode_bomb();
  v2 = &v4;
  do
  {
    result = (unsigned int)(2 * *((_DWORD *)v2 - 1));
    if ( *(_DWORD *)v2 != (_DWORD)result )
      explode_bomb();
    v2 += 4;
  }
  while ( v2 != &v5 );
  return result;
}
```

`phrase2`有两个要求。

首先调用`read_six_numbers`函数；

```c
__int64 __fastcall read_six_numbers(__int64 a1, __int64 a2)
{
  __int64 result; // rax

  result = __isoc99_sscanf(a1, "%d %d %d %d %d %d", a2, a2 + 4, a2 + 8, a2 + 12, a2 + 16, a2 + 20);
  if ( (int)result <= 5 )
    explode_bomb();
  return result;
}
```

从汇编代码的角度分析；

```assembly
var_18= qword ptr -18h
var_10= qword ptr -10h

; __unwind {
sub     rsp, 18h
mov     rdx, rsi
lea     rcx, [rsi+4]
lea     rax, [rsi+14h]
mov     [rsp+18h+var_10], rax   ; rsp+10h
lea     rax, [rsi+10h]
mov     [rsp+18h+var_18], rax   ; rsp
lea     r9, [rsi+0Ch]
lea     r8, [rsi+8]
mov     esi, offset aDDDD ; "%d %d %d %d "
mov     eax, 0
call    ___isoc99_sscanf
cmp     eax, 5
jg      short loc_401499
```

在进行判断前，由于是64位，利用寄存器传参，所以各个寄存器被赋值；

```
+------+
| 
```

