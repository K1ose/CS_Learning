---
title: wustctf2020_babyfmt
top: false
comment: false
lang: zh-CN
date: 2021-11-30 14:47:08
tags:
categories:
  - CTF
  - PWN
  - uncategorized
---

# wustctf2020_babyfmt

## 信息收集

### checksec

    Arch:     amd64-64-little
    RELRO:    Full RELRO
    Stack:    Canary found
    NX:       NX enabled
    PIE:      PIE enabled

常规的保护基本都开了；

### 执行程序

```
$ ./wustctf2020_babyfmt 
dididada.....
tell me the time:13
23
11
ok! time is 13:23:11
1. leak
2. fmt_attack
3. get_flag
4. exit
>>
```

### IDA

放到ida64中分析；

#### main()

```c
int __cdecl __noreturn main(int argc, const char **argv, const char **envp)
{
  int v3; // [rsp+Ch] [rbp-14h] BYREF
  int v4; // [rsp+10h] [rbp-10h] BYREF
  unsigned int choice; // [rsp+14h] [rbp-Ch]
  unsigned __int64 v6; // [rsp+18h] [rbp-8h]

  v6 = __readfsqword(0x28u);
  initial();
  ask_time(argc, argv);
  v3 = 0;
  v4 = 0;
  while ( 1 )
  {
    while ( 1 )
    {
      while ( 1 )
      {
        menu();
        choice = get_int();
        if ( choice != 2 )
          break;
        fmt_attack(&v3);
      }
      if ( choice > 2 )
        break;
      if ( choice == 1 )
        leak(&v4);
    }
    if ( choice == 3 )
      get_flag();
    if ( choice == 4 )
    {
      puts("Bye!");
      exit(0);
    }
  }
}
```

可以看到初始化函数`initial()`、获取并输出时间的函数`ask_time()`；

#### initial()

```c
unsigned __int64 initial()
{
  int fd; // [rsp+4h] [rbp-Ch]
  unsigned __int64 v2; // [rsp+8h] [rbp-8h]

  v2 = __readfsqword(0x28u);
  setvbuf(stdin, 0LL, 2, 0LL);
  setvbuf(stdout, 0LL, 2, 0LL);
  setvbuf(stderr, 0LL, 2, 0LL);
  fd = open("/dev/urandom", 0);
  if ( read(fd, secret, 0x40uLL) < 0 )
  {
    puts("read error!");
    exit(1);
  }
  close(fd);
  return __readfsqword(0x28u) ^ v2;
}
```

向`.bss`段上的`secret`写入随机数；

#### ask_time()

```c
unsigned __int64 ask_time()
{
  __int64 v1; // [rsp+0h] [rbp-20h] BYREF
  __int64 v2; // [rsp+8h] [rbp-18h] BYREF
  __int64 v3; // [rsp+10h] [rbp-10h] BYREF
  unsigned __int64 v4; // [rsp+18h] [rbp-8h]

  v4 = __readfsqword(0x28u);
  puts("dididada.....");
  printf("tell me the time:");
  _isoc99_scanf("%ld", &v1);
  _isoc99_scanf("%ld", &v2);
  _isoc99_scanf("%ld", &v3);
  printf("ok! time is %ld:%ld:%ld\n", v1, v2, v3);
  return __readfsqword(0x28u) ^ v4;
}
```

获取了三个用户输入的三个参数，并且输出；

接下来看关键的格式化漏洞函数`fmt_attack()`；

#### fmt_attack()

```c
unsigned __int64 __fastcall fmt_attack(int *a1)
{
  char format[56]; // [rsp+10h] [rbp-40h] BYREF
  unsigned __int64 v3; // [rsp+48h] [rbp-8h]

  v3 = __readfsqword(0x28u);
  memset(format, 0, 0x30uLL);
  if ( *a1 > 0 )
  {
    puts("No way!");
    exit(1);
  }
  *a1 = 1;
  read_n(format, 40LL, format);
  printf(format);
  return __readfsqword(0x28u) ^ v3;
}
```

