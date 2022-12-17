---
title: csapp_02_information
top: false
comment: false
lang: zh-CN
date: 2022-07-29 16:47:39
tags:
categories:
  - CSAPP
  - theory
---

# Information

## 前言

本章将讲述计算机中信息的存在形式。

由于本人学疏才浅，文章所谈及的知识或许会有出入，还望斧正。

## 信息表示

在现代计算机中，`信息在存储和处理`时以二值信号表示，我们也可以称它们为`二进制信号`，或者`二进制`，甚至可以用0和1来表示它们。或许你可以理解成`低电平`和`高电平`，也可以再通俗一点理解为`开关关闭`和`开关打开`。正是这种对立同一的概念构成了数字革命的基础。

可是现实一点，我们在电脑屏幕上看到的信息并非都是0和1，而是色彩多样的图片和不同的文字、字母。这当然是一个好的问题，但是不要着急，这里面会涉及到许多编码译码等等问题，需要慢慢研究学习。

既然选择了二进制，我们就需要讨论，为什么是`0`和`1`？首先来说说我们所熟知的十进制，它诞生于一千多年前的印度，在十二世纪被阿拉伯数学家改进（这就是为什么它们叫阿拉伯数字），在十三世纪时经过意大利数学家`Fibonacci`带到了西方世界。我们为什么要用十进制呢？因为我们有十根手指吗？是的，没错:cat:。（如果我们只有两根手指，说不定计算机会早几年诞生，不过或许人类会因为无法抓握物品先行灭绝:laughing:）但是对于机器而言呢？如果机器使用二进制能够运行得更高效，比如只判断是是非对错，再加上二值信号能够很容易地被表示、存储和传输。想我之前说的高电平低电平，卡片上有孔无孔。利用二值信号可以设计出可靠而且简单的逻辑电路，将它们集成起来就能够构造乃至数十亿如此般的电路。

众人拾材火焰高，如果只是简单的一个0或者一个1，看起来自然没有什么作用。但是当我们把它组合起来，再加上人为设定的一些`解释(interpretation)`，即赋予其一些符合使用环境的含义，就能够使它表示为任何有限集合的元素。

在这里我们会学习计算机中的三种类型的数字表示，即：

- `无符号(unsigned)`编码：表示大于或等于零的数字

- `补码(two's-complement)`编码：表示有符号整数的最常见方式

- `浮点数(floating-point)`编码：表示实数的科学记数法的以2为基数

计算机使用上述不同的表示方法进行算术运算，由于计算机用规定数量的位数来表示这些数字，因此当结果太大时，某些运算便会产生溢出(overflow)。

在这里还需要注意的是：

- 整数的表示虽然只能编码一个相对较小的数值范围，但是是精确表示的
- 浮点数虽然可以编码一个较大的数值范围，但是是近似的

## 信息存储

大多数计算机使用8位的块，也就是`字节(byte)`，作为最小的可寻址的内存单位，而不是访问内存中单独的一个bit。对程序而言，内存被视为一个非常大的字节数组，即`虚拟内存(virtual memory)`，其中的每一个字节由一个唯一的数字来标识，即`地址(address)`。这些所有地址的集合即为`虚拟地址空间(virtual address sapce)`。

### 十六进制

上面我们提到，一个字节由八个位组成，即：1 byte = 8 bits。

在二进制表示中的值域为：$00000000_{\{2\}} $ ~ $ 11111111_{\{2\}}$，在十进制表示中的值域为：$0_{\{10\}} $ ~ $ 255_{\{10\}}$

在`十六进制(hexadecimal)`表示中，值域为：0x00 ~ 0xFF

### 字长

每台计算机都有一个`字长(word size)`，用来指明指针数据的`标称大小(normal size)`，这是因为虚拟地址是以这样的一个字来编码的，所以字长决定了虚拟地址空间的最大大小。即，对于一个字长为 $w$ 位的机器而言，虚拟地址空间的范围为：$0$ ~ $2^w-1$，程序最多访问 $2^w$ 个字节。

以上一章的`hello.c`程序代码为例，使用下面命令进行编译：

```shell
# 生成32位的可执行文件 hello32
gcc -m32 hello.c -o hello32

# 生成64位的可执行文件 hello64
gcc -m64 hello.c -o hello64
# or gcc hello.c -o hello64
```

执行 `file hello32` 的结果如下：

```
hello32: ELF 32-bit LSB shared object, Intel 80386, version 1 (SYSV), dynamically linked, interpreter /lib/ld-linux.so.2, BuildID[sha1]=c83c99fe2858071279df9798a635c95cc2b1f480, for GNU/Linux 3.2.0, not stripped
```

而执行 `file hello64` 的结果如下：

```
hello64: ELF 64-bit LSB shared object, x86-64, version 1 (SYSV), dynamically linked, interpreter /lib64/ld-linux-x86-64.so.2, BuildID[sha1]=32decbcb5bd212c70c8c4018c32c656c76e72c4a, for GNU/Linux 3.2.0, not stripped
```

需要知道的是，32位程序在32位机和64位机上都可以正常运行，但是64位程序只能在64位机上正常运行。

如下表格是C语言各种数据类型分配的字节数，为32位和64位程序的典型值。其中，有些数据类型所分配的字节数大小受程序如何编译的影响而变化。

<table style="text-align:center">
    <tr>
        <td colspan="2">
            <b>C语言声明</b>
        </td>
        <td colspan="2">
            <b>字节数</b>
        </td>
    </tr>
	<tr>
        <td>
            <b>有符号</b>
        </td>
        <td>
            <b>无符号</b>
        </td>
        <td>
            <b>32位</b>
        </td>
        <td>
            <b>64位</b>
        </td>
    </tr>
    <tr>
        <td>
            [signed] char
        </td>
        <td>
            unsigned char
        </td>
        <td>
            1
        </td>
        <td>
            1
        </td>
    </tr>
    <tr>
        <td>
            short
        </td>
        <td>
            unsigned short
        </td>
        <td>
            2
        </td>
        <td>
            2
        </td>
    </tr>
    <tr>
        <td>
            int
        </td>
        <td>
            unsigned
        </td>
        <td>
            4
        </td>
        <td>
            4
        </td>   
    </tr>
    <tr>
        <td>
            long
        </td>
        <td>
            unsigned long
        </td>
        <td>
            4
        </td>
        <td>
            8
        </td>
    </tr>
    <tr>
        <td>
            int32_t
        </td>
        <td>
            uint32_t
        </td>
        <td>
            4
        </td>
        <td>
            4
        </td>
    </tr>
    <tr>
        <td>
            int64_t
        </td>
        <td>
            uint64_t
        </td>
        <td>
            8
        </td>
        <td>
            8
        </td>
    </tr>
    <tr>
        <td>
            char *
        </td>
        <td>
        </td>
        <td>
            4
        </td>
        <td>
            8
        </td>
    </tr>
    <tr>
        <td>
            float
        </td>
        <td></td>
        <td>
        	4
        </td>
        <td>
            4
        </td>
	</tr>
    <tr>
        <td>
            double
        </td>
        <td></td>
        <td>
            8
        </td>
        <td>
            8
        </td>
    </tr>
</table>

-  `char` 表示一个单独字节，但它也能被用来存储整数值；
-  `short`、 `int` 、 `long` 分别提供了各种数据大小；
-  `int32_t` 、 `int64_t` 是 `ISO C99` 规定的确定大小的数据类型，它们不随编译器和机器设置而变化。

大部分的数据类型都编码位有符号数值，除非有前缀关键字 `unsigned` 进行无符号声明。大多数编译器视 `char` 为有符号数，但C标准不保证，需要用有符号字符的声明来保证其为一个字节的有符号数值。

下面的声明是一个意思：

```c
unsigned long
unsigned long int
long unsigned
long unsigned int
```

下面的程序展示了各种数据大小在不同位下的占用大小：

```c
// 64 bit
#include <stdio.h>

int main()
{
    signed char ch = 0x41;     // -128~127
    unsigned char u_ch = 0x42; // 0~255

    short sh = 0x8000;            // 2 bytes = 8 btis -> 0x????
    unsigned short u_sh = 0xffff; // %u

    int integer = 0x80000000;        // 4 bytes = 32 bits
    unsigned u_integer = 0xffffffff; // unsigned int u_integer

    long l = 0x8000000000000000; // 8 bytes = 64 bits
    unsigned long u_l = 0xffffffffffffffff;

    __int32_t int32 = 0x80000000;
    __uint32_t u_int32 = __UINT32_MAX__;

    __int64_t int64 = 0x8000000000000000;
    __uint64_t u_int64 = __UINT64_MAX__;

    char *p = "abcdefghijklmnopqrstuvwxyz";

    float f = 8.25;

    double d = 0.000002;

    printf("signed char ch: %c \t size: %ld\n", ch, sizeof(ch));
    printf("unsigned char u_ch: %u \t size: %ld\n", u_ch, sizeof(u_ch));

    printf("short sh: %hd \t size: %ld\n", sh, sizeof(sh));
    printf("unsigned short u_sh: %u \t size: %ld\n", u_sh, sizeof(u_sh));

    printf("int integer: %d \t size: %ld\n", integer, sizeof(integer));
    printf("unsigned int u_integer: %u \t size: %ld\n", u_integer, sizeof(u_integer));

    printf("long l: %ld \t size: %ld\n", l, sizeof(l));
    printf("unsigned long u_l: %lu \t size: %ld\n", u_l, sizeof(u_l));

    printf("__int32_t int32: %d \t size: %ld\n", int32, sizeof(int32));
    printf("__uint32_t u_int32: %u \t size: %ld\n", u_int32, sizeof(u_int32));

    printf("__int64_t int64: %ld \t size: %ld\n", int64, sizeof(int64));
    printf("__uint64_t u_int64: %lu \t size: %ld\n", u_int64, sizeof(u_int64));

    printf("char *p: %s \t size: %ld\n", p, sizeof(p));

    printf("float f: %f \t size: %ld\n", f, sizeof(f));

    printf("double d: %lf \t size: %ld\n", d, sizeof(d));
}
```

```c
// 32 bit
#include <stdio.h>

int main()
{
    signed char ch = 0x41;     // -128~127
    unsigned char u_ch = 0x42; // 0~255

    short sh = 0x8000;            // 2 bytes = 8 btis -> 0x????
    unsigned short u_sh = 0xffff; // %u

    int integer = 0x80000000;        // 4 bytes = 32 bits
    unsigned u_integer = 0xffffffff; // unsigned int u_integer

        long l = 0x80000000; // in 32 bit machine: 4 bytes = 32 bits
    unsigned long u_l = 0xffffffff;

    __int32_t int32 = 0x80000000;
    __uint32_t u_int32 = __UINT32_MAX__;

    __int64_t int64 = 0x8000000000000000;
    __uint64_t u_int64 = 0xffffffffffffffff;

    char *p = "abcdefghijklmnopqrstuvwxyz";

    float f = 8.25;

    double d = 0.000002;

    printf("signed char ch: %c \t size: %d\n", ch, sizeof(ch));
    printf("unsigned char u_ch: %u \t size: %d\n", u_ch, sizeof(u_ch));

    printf("short sh: %hd \t size: %d\n", sh, sizeof(sh));
    printf("unsigned short u_sh: %u \t size: %d\n", u_sh, sizeof(u_sh));

    printf("int integer: %d \t size: %d\n", integer, sizeof(integer));
    printf("unsigned int u_integer: %u \t size: %d\n", u_integer, sizeof(u_integer));

    printf("long l: %ld \t size: %d\n", l, sizeof(l));
    printf("unsigned long u_l: %lu \t size: %d\n", u_l, sizeof(u_l));

    printf("__int32_t int32: %d \t size: %d\n", int32, sizeof(int32));
    printf("__uint32_t u_int32: %u \t size: %d\n", u_int32, sizeof(u_int32));

    printf("__int64_t int64: %lld \t size: %d\n", int64, sizeof(int64));
    printf("__uint64_t u_int64: %llu \t size: %d\n", u_int64, sizeof(u_int64));

    printf("char *p: %s \t size: %d\n", p, sizeof(p));

    printf("float f: %f \t size: %d\n", f, sizeof(f));

    printf("double d: %lf \t size: %d\n", d, sizeof(d));
```

这里需要注意的是，如果变量声明有什么问题，可以引入 `stdint.h` ，并且将 `__int32_t` 和 `__int64_t` 换成 `int32_t` 和 `int64_t` 。

可以得到执行的结果：

- 64位

  ```
  signed char ch: A        size: 1
  unsigned char u_ch: 66   size: 1
  short sh: -32768         size: 2
  unsigned short u_sh: 65535       size: 2
  int integer: -2147483648         size: 4
  unsigned int u_integer: 4294967295       size: 4
  long l: -9223372036854775808     size: 8
  unsigned long u_l: 18446744073709551615          size: 8
  __int32_t int32: -2147483648     size: 4
  __uint32_t u_int32: 4294967295   size: 4
  __int64_t int64: -9223372036854775808    size: 8
  __uint64_t u_int64: 18446744073709551615         size: 8
  char *p: abcdefghijklmnopqrstuvwxyz      size: 8
  float f: 8.250000        size: 4
  double d: 0.000002       size: 8
  ```

- 32位

  ```
  signed char ch: A        size: 1
  unsigned char u_ch: 66   size: 1
  short sh: -32768         size: 2
  unsigned short u_sh: 65535       size: 2
  int integer: -2147483648         size: 4
  unsigned int u_integer: 4294967295       size: 4
  long l: -2147483648      size: 4
  unsigned long u_l: 4294967295    size: 4
  __int32_t int32: -2147483648     size: 4
  __uint32_t u_int32: 4294967295   size: 4
  __int64_t int64: -9223372036854775808    size: 8
  __uint64_t u_int64: 18446744073709551615         size: 8
  char *p: abcdefghijklmnopqrstuvwxyz      size: 4
  float f: 8.250000        size: 4
  double d: 0.000002       size: 8
  ```

可以注意到，32bit和64bit的机器中，指针的大小和`long`类型的数据所分配的字节大小不相同。

<table style="text-align:center">
    <tr>
        <td>
            <b>类型</b>
        </td>
        <td>
            <b>位数</b>
        </td>
        <td>
            <b>字节大小</b>
        </td>
    </tr>
    <tr>
        <td rowspan="2">
            pointer(*)
        </td>
        <td>
        	64 位
        </td>
        <td>
          	8 bytes
        </td>
    </tr>
    <tr>
        <td>
        	32 位
        </td>
        <td>
          	4 bytes
        </td>
    </tr>
<tr>
        <td rowspan="2">
            long
        </td>
        <td>
        	64 位
        </td>
        <td>
          	8 bytes
        </td>
    </tr>
    <tr>
        <td>
        	32 位
        </td>
        <td>
          	4 bytes
        </td>
    </tr>
</table>

对于任何数据类型 $T$ ，声明：`T *p;` 表明 `p` 是一个指针变量，指向一个类型为 `T` 的对象。例如， `char *p;` 将一个指针声明为指向一个 `char` 类型的对象。

### 寻址和字节顺序

对于跨越多个字节的程序对象，需要关注两个内容：

- 该对象在内存中存放的地址
- 该对象的多个字节在内存中的排列顺序

在几乎所有的机器上，多字节对象都被存储为连续的字节序列，对象的地址为所使用字节中最小的地址。就拿上面程序中 `char *p` 作为例子。实际上，我们知道它包含了26个英文字母，而这些数据占用的内存空间大小为 `26 bytes` 。现在来看一下这些字母所对应的内存地址。

```c
#include <stdio.h>
#include <stdint.h>

int main()
{
    char *p = "zyxwvutsrqponmlkjihgfedcba";
    printf("address of p: %p\n", p);   // 数据存放地址
    printf("address of *p: %p\n", &p); // 指针存放地址
    printf("p: %s\n", p);              // 数据

    // 这里注意区分*(p+2)和*p+2的区别
    printf("%c\n", *p + 2);
    printf("%p: %c\n", p + 2, *(p + 2));
    printf("%p: %c\n", p + 3, *(p + 3));

    // 指针存放地址相邻的地址（堆）
    printf("%p\n", &p + 1);
    printf("%p\n", &p + 2);
}
```

执行后可以得到运行结果：

```
address of p: 00007ff620599000
address of *p: 0000006b729ff768
p: zyxwvutsrqponmlkjihgfedcba
|
00007ff620599002: x
00007ff620599003: w
0000006b729ff770
0000006b729ff778
```

再来看看调试时内存地址的展示：

```
-exec x/10gx 0x7ff620599000
0x7ff620599000:	0x737475767778797a	0x6b6c6d6e6f707172
0x7ff620599010:	0x636465666768696a	0x6572646461006162
0x7ff620599020:	0x3a7020666f207373	0x646461000a702520
0x7ff620599030:	0x20666f2073736572	0x000a7025203a702a
0x7ff620599040:	0x25000a7325203a70	0x25203a7025000a63

-exec x/4gx 0x6b729ff768
0x6b729ff768:	0x00007ff620599000	0x0000000000000001
0x6b729ff778:	0x00007ff6205913b1	0x0000000000000000

-exec x/1xb 0x7ff620599002
0x7ff620599002:	0x78
-exec x/1xb 0x7ff620599003
0x7ff620599003:	0x77
```

排列表示一个对象的字节右两个通用的规则，考虑一个 $w$ 位的整数，其位表示位 [$x_{w-1}$, $x_{w-2}$, ..., $x_1$, $x_0$] ，其中 $x_{w-1}$ 最高有效位，而 $x_0$ 是最低有效位。假设 $w$ 是8的倍数，这些位就能被分组成为字节，其中最高位字节包含位 [$x_{w-1}$, $x_{w-2}$, ..., $x_{w-7}$, $x_{w-8}$] ，而最低有效位包含位 [$x_{7}$, $x_{6}$, ..., $x_{2}$, $x_{1}$] ，其他字节则包含中间的位。

- `小端法(little endian)`：最低有效字节在最前面
- `大端法(big endian)`：最高有效字节在最前面

可以从内存中看到，字符指针指向的数据 `zyxwvutsrqponmlkjihgfedcba` 在内存中存放形式为：

```
0x7ff620599000:	0x737475767778797a	0x6b6c6d6e6f707172
0x7ff620599010:	0x636465666768696a	0x6572646461006162
```

直接转成字符后是：

> 可以直接使用 `man ascii` 来获取一张ASCII字符表

```
0x7ff620599000:	stuvwxyz klmnopqr
0x7ff620599010:	cdefghij ......ab
```

可以看到 `0x7ff620599000` 存放的为 `z` ，而 `0x7ff620599008` 存放的是 `r `。

```
-exec x/8xb 0x7ff620599000
0x7ff620599000:	0x7a	0x79	0x78	0x77	0x76	0x75	0x74	0x73
```

可以很清楚地看到，Windows是采取小端序的操作系统，即：高字节放在高位，低字节放在低位。

许多微处理器是`双端法(bi-endian)`，它们可以被配置成作为大端或者小端的机器运行。但是实际情况是，一旦选择了特定操作系统，那么字节顺序也就被固定下来。比如，ARM处理器的硬件可以按小端或者大端两种模式操作，但是这些芯片上最常见的两种操作系统是`Android(Google)`和`iOS(Apple)`，它们只能运行于小端模式。

字节顺序的三个重要问题：

- 两种端序的操作系统进行网络传输数据，字节可能出现逆序的情况
- 两种端序的操作系统对指令中地址解析的顺序不同
- 编写规避正常的类型系统的程序

对于第三个问题这里详细展开，下面的程序代码使用强制类型转换来访问和打印不同程序对象的字节表示。

> `typedef` 提供了一种给数据类型命名的方式。
>
> ```c
> typedef int *int_pointer
> int_pointer ip;
> ```
>
> 等价于
>
> ```c
> int *ip;
> ```

```c
#include <stdio.h>

/* typedef 将数据类型byte_pointer定义位一个指向类型为 unsigned char 的对象的指针 */
typedef unsigned char *byte_pointer;

/*
    byte_pointer start: 一个字节序列的地址
    size_t len: 字节序列的长度
    %.2x: 表示必须用至少两个数字的十六进制格式输出
*/
void show_bytes(byte_pointer start, size_t len)
{
    size_t i;
    for (i = 0; i < len; i++)
        printf("0x%.2x ", start[i]);
    printf("\n");
}

/* 使用 show_int, show_flaot, show_pointer 展示如何使用函数show_bytes分别输出类型为int、float和void *的对象的字节表示，&x 为指向参数x的指针，这个指针被强制类型转换为 unsigned char *，这样编译器就知道应该把这个指针看作是指向一个字节序列，而非先前所定义的类型(int, float, *) */

void show_int(int x)
{
    /*  */
    show_bytes((byte_pointer)&x, sizeof(int));
}

void show_float(float x)
{
    show_bytes((byte_pointer)&x, sizeof(float));
}

void show_pointer(void *x)
{
    show_bytes((byte_pointer)&x, sizeof(void *));
}

/* 测试用例 */
void test_show_bytes(int val)
{
    // byte_pointer 字节序列
    byte_pointer bp = "abcdefghijklmnopqrstuvwxyz";

    int ival = val;           // integer
    float fval = (float)ival; // float
    int *pval = &ival;        // pointer -> the address of ival

    // output
    show_bytes(bp, 26);
    show_int(ival);
    show_float(fval);
    show_pointer(pval);
}

int main()
{
    test_show_bytes(12345);
}
```

可以得到结果：

```
0x61 0x62 0x63 0x64 0x65 0x66 0x67 0x68 0x69 0x6a 0x6b 0x6c 0x6d 0x6e 0x6f 0x70 0x71 0x72 0x73 0x74 0x75 0x76 0x77 0x78 0x79 0x7a
0x39 0x30 0x00 0x00
0x00 0xe4 0x40 0x46 
0xc4 0xf7 0x9f 0xc7 0xc5 0x00 0x00 0x00
```

由于编译为64位程序，所以指针地址是 8 bytes 的。

可以注意到 `12345` 的整型数据为：`0x00003039`， 而浮点型数据为：`0x4640e400`。这是因为整型数据和浮点型数据采用了不同的编码方式。我们把这两个数据转换为二进制进行观察：

```
00000000000000000011000000111001
	      01000110010000001110010000000000
```

可以发现，有13位数据是重合的。

这是因为对于 `float` 型数据，采用了三个部分进行编码存储：

| Sign 符号 | Exponent 指数 | Mantissa 尾数 |
| :-------: | :-----------: | :-----------: |
|   1 bit   |    8 bits     |    23 bits    |

因此，对于 `12345` 而言，其二进制为 `11000000111001`。

- 由于 `12345` 为正数，所以 `Sign` 位为：`0` ；
- 由于 `8 bit` 的指数部分可正可负，所以使用移位存储来移动可变区间，通过将 `-127~127` 区间 `+127` 变为 `0~255`。 `11000000111001` 化为科学计数法可以表示为：`1.1000000111001 * 2^13` ，因此 `Exponent` 位为： `13` 。所以 `127 + 13 = 140` ，二进制表示为 `10001100` ；
- 由于尾数分配了 `23 bit` ，因此将13位位数填入后，还需要补充10位 `0` ；

最终可以得到：

| Sign | Exponent |         Mantissa          |
| :--: | :------: | :-----------------------: |
|  0   | 10001100 | 11000000111001 0000000000 |

即为： `010001100110000001110010000000000` ，这样就得到了 `float` 类型在内存中的存储形式。

同理， `double` 的编码规则为：

| Sign 符号 | Exponent 指数 | Mantissa 尾数 |
| :-------: | :-----------: | :-----------: |
|   1 bit   |    11 bits    |    52 bits    |

下面来思考一下对 `show_bytes` 的三次调用：

```c
// practice 2_5
#include <stdio.h>

typedef unsigned char *byte_pointer;

void show_bytes(byte_pointer start, size_t len)
{
    size_t i;
    for (i = 0; i < len; i++)
        printf("0x%.2x ", start[i]);
    printf("\n");
}

int main()
{
    int val = 0x87654321;
    byte_pointer valp = (byte_pointer)&val;

    show_bytes(valp, 1);
    show_bytes(valp, 2);
    show_bytes(valp, 3);
}
```

在小端序的机器中，执行结果为：

```
0x21 
0x21 0x43
0x21 0x43 0x65
```

如果是在大端序的机器中，执行结果为：

```
0x87 
0x87 0x65
0x87 0x65 0x43
```

使用 `show_int` 和 `show_float`，我们确定整数 `3510593` 的十六进制数表示为 `0x00359141` ，而浮点数 `3510593.0` 的十六进制表示为 `0x4A564504`。

- 写出这两个十六进制的二进制表示形式：

  ```
  0000 0000 0011 0101 1001 0001 0100 0001
  0100 1010 0101 0110 0100 0101 0000 0100
  ```

- 移动这两个二进制串的相对位置，最多能匹配多少位？

  ```
  000 00000001 101011001000101000001
    0 10010100 101011001000101000001 00
  ```

  `32 - sign(1) - exponent(8) - 00(2) = 21 bits`

  可以知道，一共有21位匹配。 

### 表示字符串

参见ASCII码表；

`a~z` 为 `0x61~0x7A`

下面代码执行会出现什么结果呢？

```c
const char *s = "abcdef";
show_bytes((byte_pointer) s, strlen(s))
```

结果是：

```
0x61 0x62 0x63 0x64 0x65 0x66
```

### 表示代码

不同机器类型使用不同的且不兼容的指令和编码方式，即使是完全一样的程序，其指令编码也不一样，所以二进制代码很少能在不同机器和操作系统组合之间移植。

### 布尔代数

Boolean algebra，通过将真假值编码为0和1，设计出一种代数，以研究逻辑推理的基本原则。

四个基础的布尔运算为：与(&)、或(|)、非(~)、异或(^)

将四个布尔运算扩展到**位向量**的运算，例如：

```
0110 & 1100 = 0100
0010 | 1100 = 1110
0110 ^ 1100 = 1010
	 ~ 1100 = 0011
```

## 整数表示



