---
title: pwnable.kr_passcode
top: false
comment: false
lang: zh-CN
date: 2021-11-15 18:07:11
tags:
categories:
  - CTF
  - PWN
  - wp
  - pwnable.kr
---

# passcode

## 信息收集

在ssh连接靶机之后，看文件：

```
$ ls -ll
total 16
-r--r----- 1 root passcode_pwn   48 Jun 26  2014 flag
-r-xr-sr-x 1 root passcode_pwn 7485 Jun 26  2014 passcode
-rw-r--r-- 1 root root          858 Jun 26  2014 passcode.c
```

查看C文件：

```c
$ cat passcode.c 
#include <stdio.h>
#include <stdlib.h>

void login(){
	int passcode1;
	int passcode2;

	printf("enter passcode1 : ");
	scanf("%d", passcode1);			// lose '&'
	fflush(stdin);

	// ha! mommy told me that 32bit is vulnerable to bruteforcing :)
	printf("enter passcode2 : ");
    scanf("%d", passcode2);			// lose '&'

	printf("checking...\n");
	if(passcode1==338150 && passcode2==13371337){
                printf("Login OK!\n");
                system("/bin/cat flag");
        }
        else{
                printf("Login Failed!\n");
		exit(0);
        }
}

void welcome(){
	char name[100];
	printf("enter you name : ");
	scanf("%100s", name);			// lose '&'
	printf("Welcome %s!\n", name);	
}

int main(){
	printf("Toddler's Secure Login System 1.0 beta.\n");

	welcome();
	login();

	// something after login...
	printf("Now I can safely trust you that you have credential :)\n");
	return 0;	
}

```

由于没有加地址引用，所以按逻辑输入会报错；

```
passcode@pwnable:~$ ./passcode 
Toddler's Secure Login System 1.0 beta.
enter you name : klose
Welcome klose!
enter passcode1 : 123
Segmentation fault (core dumped)
passcode@pwnable:~$ 
```

通过objdump来获取汇编信息；

```nasm
	c7 04 24 24 9f 04 08 	movl   $0x8049f24,(%esp)
 804855f:	ff d0                	call   *%eax
 8048561:	c9                   	leave  
 8048562:	c3                   	ret    
 8048563:	90                   	nop

08048564 <login>:
 8048564:	55                   	push   %ebp
 8048565:	89 e5                	mov    %esp,%ebp
 8048567:	83 ec 28             	sub    $0x28,%esp
 804856a:	b8 70 87 04 08       	mov    $0x8048770,%eax
 804856f:	89 04 24             	mov    %eax,(%esp)
 8048572:	e8 a9 fe ff ff       	call   8048420 <printf@plt>
 8048577:	b8 83 87 04 08       	mov    $0x8048783,%eax
 804857c:	8b 55 f0             	mov    -0x10(%ebp),%edx
 804857f:	89 54 24 04          	mov    %edx,0x4(%esp)
 8048583:	89 04 24             	mov    %eax,(%esp)
 8048586:	e8 15 ff ff ff       	call   80484a0 <__isoc99_scanf@plt>
 804858b:	a1 2c a0 04 08       	mov    0x804a02c,%eax
 8048590:	89 04 24             	mov    %eax,(%esp)
 8048593:	e8 98 fe ff ff       	call   8048430 <fflush@plt>
 8048598:	b8 86 87 04 08       	mov    $0x8048786,%eax
 804859d:	89 04 24             	mov    %eax,(%esp)
 80485a0:	e8 7b fe ff ff       	call   8048420 <printf@plt>
 80485a5:	b8 83 87 04 08       	mov    $0x8048783,%eax
 80485aa:	8b 55 f4             	mov    -0xc(%ebp),%edx
 80485ad:	89 54 24 04          	mov    %edx,0x4(%esp)
 80485b1:	89 04 24             	mov    %eax,(%esp)
 80485b4:	e8 e7 fe ff ff       	call   80484a0 <__isoc99_scanf@plt>
 80485b9:	c7 04 24 99 87 04 08 	movl   $0x8048799,(%esp)
 80485c0:	e8 8b fe ff ff       	call   8048450 <puts@plt>
 80485c5:	81 7d f0 e6 28 05 00 	cmpl   $0x528e6,-0x10(%ebp)
 80485cc:	75 23                	jne    80485f1 <login+0x8d>
 80485ce:	81 7d f4 c9 07 cc 00 	cmpl   $0xcc07c9,-0xc(%ebp)
 80485d5:	75 1a                	jne    80485f1 <login+0x8d>
 80485d7:	c7 04 24 a5 87 04 08 	movl   $0x80487a5,(%esp)
 80485de:	e8 6d fe ff ff       	call   8048450 <puts@plt>
 80485e3:	c7 04 24 af 87 04 08 	movl   $0x80487af,(%esp)
 80485ea:	e8 71 fe ff ff       	call   8048460 <system@plt>
 80485ef:	c9                   	leave  
 80485f0:	c3                   	ret    
 80485f1:	c7 04 24 bd 87 04 08 	movl   $0x80487bd,(%esp)
 80485f8:	e8 53 fe ff ff       	call   8048450 <puts@plt>
 80485fd:	c7 04 24 00 00 00 00 	movl   $0x0,(%esp)
 8048604:	e8 77 fe ff ff       	call   8048480 <exit@plt>

08048609 <welcome>:
 8048609:	55                   	push   %ebp
 804860a:	89 e5                	mov    %esp,%ebp
 804860c:	81 ec 88 00 00 00    	sub    $0x88,%esp
 8048612:	65 a1 14 00 00 00    	mov    %gs:0x14,%eax
 8048618:	89 45 f4             	mov    %eax,-0xc(%ebp)
 804861b:	31 c0                	xor    %eax,%eax
 804861d:	b8 cb 87 04 08       	mov    $0x80487cb,%eax
 8048622:	89 04 24             	mov    %eax,(%esp)
 8048625:	e8 f6 fd ff ff       	call   8048420 <printf@plt>
 804862a:	b8 dd 87 04 08       	mov    $0x80487dd,%eax
 804862f:	8d 55 90             	lea    -0x70(%ebp),%edx
 8048632:	89 54 24 04          	mov    %edx,0x4(%esp)
 8048636:	89 04 24             	mov    %eax,(%esp)
 8048639:	e8 62 fe ff ff       	call   80484a0 <__isoc99_scanf@plt>
 804863e:	b8 e3 87 04 08       	mov    $0x80487e3,%eax
 8048643:	8d 55 90             	lea    -0x70(%ebp),%edx
 8048646:	89 54 24 04          	mov    %edx,0x4(%esp)
 804864a:	89 04 24             	mov    %eax,(%esp)
 804864d:	e8 ce fd ff ff       	call   8048420 <printf@plt>
 8048652:	8b 45 f4             	mov    -0xc(%ebp),%eax
 8048655:	65 33 05 14 00 00 00 	xor    %gs:0x14,%eax
 804865c:	74 05                	je     8048663 <welcome+0x5a>
 804865e:	e8 dd fd ff ff       	call   8048440 <__stack_chk_fail@plt>
 8048663:	c9                   	leave  
 8048664:	c3                   	ret    

08048665 <main>:
 8048665:	55                   	push   %ebp
 8048666:	89 e5                	mov    %esp,%ebp
 8048668:	83 e4 f0             	and    $0xfffffff0,%esp
 804866b:	83 ec 10             	sub    $0x10,%esp
 804866e:	c7 04 24 f0 87 04 08 	movl   $0x80487f0,(%esp)
 8048675:	e8 d6 fd ff ff       	call   8048450 <puts@plt>
 804867a:	e8 8a ff ff ff       	call   8048609 <welcome>
 804867f:	e8 e0 fe ff ff       	call   8048564 <login>
 8048684:	c7 04 24 18 88 04 08 	movl   $0x8048818,(%esp)
 804868b:	e8 c0 fd ff ff       	call   8048450 <puts@plt>
 8048690:	b8 00 00 00 00       	mov    $0x0,%eax
 8048695:	c9                   	leave  
 8048696:	c3                   	ret    
```

可以看到：

```
 804862a:	b8 dd 87 04 08       	mov    $0x80487dd,%eax
 804862f:	8d 55 90             	lea    -0x70(%ebp),%edx
 8048632:	89 54 24 04          	mov    %edx,0x4(%esp)
 8048636:	89 04 24             	mov    %eax,(%esp)
 8048639:	e8 62 fe ff ff       	call   80484a0 <__isoc99_scanf@plt>
```

为name开辟了很大的空间，偏移在`ebp-0x70`；而`passcode1`在偏移`ebp-0x10`，passcode2在偏移`ebp-0xc`；

因此尝试利用溢出进行覆盖；

## pwn

`338150`转换成十六进制为：`0x528E6`；

`13371337`转换成十六进制为：`0xCC07C9`；

可以试着把C文件编译为可执行文件，进行调试；

```
gcc passcode.c -g -m32 -o passcode
```
