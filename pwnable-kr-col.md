---
title: pwnable.kr_col
top: false
comment: false
lang: zh-CN
date: 2021-11-15 15:57:24
tags:
categories:
  - CTF
  - PWN
  - wp
  - pwnable.kr
---

# col

hint

```
Daddy told me about cool MD5 hash collision today.
I wanna do something like that too!

ssh col@pwnable.kr -p2222 (pw:guest)
```

列出文件并打印`col.c`文件；

```
col@pwnable:~$ ls -la
total 36
drwxr-x---   5 root    col     4096 Oct 23  2016 .
drwxr-xr-x 116 root    root    4096 Nov 11 14:52 ..
d---------   2 root    root    4096 Jun 12  2014 .bash_history
-r-sr-x---   1 col_pwn col     7341 Jun 11  2014 col
-rw-r--r--   1 root    root     555 Jun 12  2014 col.c
-r--r-----   1 col_pwn col_pwn   52 Jun 11  2014 flag
dr-xr-xr-x   2 root    root    4096 Aug 20  2014 .irssi
drwxr-xr-x   2 root    root    4096 Oct 23  2016 .pwntools-cache
col@pwnable:~$ cat col.c 
#include <stdio.h>
#include <string.h>
unsigned long hashcode = 0x21DD09EC;
unsigned long check_password(const char* p){
	int* ip = (int*)p;
	int i;
	int res=0;
	for(i=0; i<5; i++){
		res += ip[i];
	}
	return res;
}

int main(int argc, char* argv[]){
	if(argc<2){
		printf("usage : %s [passcode]\n", argv[0]);
		return 0;
	}
	if(strlen(argv[1]) != 20){
		printf("passcode length should be 20 bytes\n");
		return 0;
	}

	if(hashcode == check_password( argv[1] )){
		system("/bin/cat flag");
		return 0;
	}
	else
		printf("wrong passcode.\n");
	return 0;
}
```

可以看到当满足`hashcode == check_password( argvp[1] )时，即可拿shell；

而`hashcode`已经写死为`0x21DD09EC`；

要求我们输入的参数，加了5次后等于`0x21DD09EC`；

只需要使用`0x01010101` * 4 + `0x1DD905E8`即可；

使用Python来输入参数，即可拿到flag；

```
$ ./col `python -c "print('\x01\x01\x01\x01'*4+'\xe8\x05\xd9\x1d')"`
daddy! I just managed to create a hash collision :)
```

