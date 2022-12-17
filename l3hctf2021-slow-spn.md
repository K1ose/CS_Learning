---
title: l3hctf2021_slow-spn
top: false
comment: false
lang: zh-CN
date: 2021-11-13 09:15:46
tags:
categories:
  - CTF
  - PWN
  - wp
  - l3hctf
---

# slow-spn

题目做了一个虚拟的缓存，用于加速地址获取；

## 信息收集

### checksec

```
    Arch:     amd64-64-little
    RELRO:    Partial RELRO
    Stack:    No canary found
    NX:       NX enabled
    PIE:      No PIE (0x400000)
```

### ida

#### main

```c
int __cdecl main(int argc, const char **argv, const char **envp)
{
  init();
  load_flag();
  SPN(key, plaintext1, &cipher, &message);
  return 0;
}
```

#### load_flag

```c
void __cdecl load_flag()
{
  FILE *fp; // [rsp+28h] [rbp-18h]
  char buf[9]; // [rsp+37h] [rbp-9h]

  memset(buf, 0, 9uLL);
  fp = fopen("./flag.txt", "r");
  if ( !fp )
    abort();
  fread(buf, 6uLL, 1uLL, fp);			
  key = strtol(buf, 0LL, 16);				
  memset(buf, 0, 9uLL);						
  fread(buf, 4uLL, 1uLL, fp);				
  plaintext1 = (unsigned __int16)strtol(buf, 0LL, 16);
  fclose(fp);
}
```

明文为2bytes，key为3bytes；

#### SPN

```c
void __cdecl SPN(unsigned int k, unsigned int p, unsigned int *cipher, unsigned int *message)
{
  __int64 v4; // [rsp+8h] [rbp-68h]
  __int64 v5; // [rsp+10h] [rbp-60h]
  __int64 v6; // [rsp+18h] [rbp-58h]
  __int64 v7; // [rsp+20h] [rbp-50h]
  __int64 v8; // [rsp+28h] [rbp-48h]
  __int64 v9; // [rsp+30h] [rbp-40h]
  __int64 v10; // [rsp+38h] [rbp-38h]

  cacheCB((uint64_t)&ss_box[(unsigned __int16)p]);
  v10 = ss_box[(unsigned __int16)p];
  cacheCB((uint64_t)&p_box[v10]);
  v9 = (unsigned __int16)p_box[v10] ^ (unsigned int)(unsigned __int16)(k >> 8);
  cacheCB((uint64_t)&ss_box[v9]);
  v8 = ss_box[v9];
  cacheCB((uint64_t)&p_box[v8]);
  v7 = (unsigned __int16)p_box[v8] ^ (unsigned int)(unsigned __int16)(k >> 4);
  cacheCB((uint64_t)&ss_box[v7]);
  v6 = ss_box[v7];
  cacheCB((uint64_t)&p_box[v6]);
  v5 = (unsigned __int16)p_box[v6] ^ (unsigned int)(unsigned __int16)k;
  cacheCB((uint64_t)&ss_box[v5]);
  v4 = ss_box[v5];
  cacheCB((uint64_t)&p_box[v4]);
  *cipher = (unsigned __int16)p_box[v4];
}
```

在获取s盒和p盒的数据前，将访问数据的地址传递给缓存函数；

#### __cacheCB

```c
void __cdecl __cacheCB(uint64_t addr)
{
  uint8_t speed; // [rsp+17h] [rbp-19h]
  uint64_t access_addr; // [rsp+18h] [rbp-18h]
  uint8_t skip; // [rsp+20h] [rbp-10h]
  int choice; // [rsp+24h] [rbp-Ch]

  skip = 0;
  puts("THE WORLD!\n");
  menu();
  while ( 1 )
  {
    while ( 1 )
    {
      puts("What to do?");
      choice = read_ll();
      if ( choice != 1 )
        break;
      puts("Where?");
      access_addr = read_ll();
      puts("Speed up?");
      speed = read_ll();
      __maccess(access_addr, speed);
    }
    if ( choice == 2 )
      break;
    if ( choice == 3 )
      exit(0);
    if ( choice == 4 )
    {
      skip = 1;
      break;
    }
  }
  ++tick;			// global time
  __maccess(addr, skip);
}
```

`__cacheCB`允许：

1. 输入`access_addr`和`speed`，执行`__maccess`；
2. 执行`++tick`和`__maccess(addr, 0)`；
3. 直接退出`exit(0)`；
4. 执行`++tick`和`__maccess(addr, 1)`；

#### __maccess

```c
void __cdecl __maccess(uint64_t addr, uint8_t isFast)
{
  std::map<unsigned char,cacheLine *>::mapped_type *v2; // rax
  cacheLine *v3; // [rsp+10h] [rbp-A0h]
  uint32_t v4; // [rsp+38h] [rbp-78h]
  std::pair<const unsigned char,cacheLine *> p; // [rsp+50h] [rbp-60h]
  std::_Rb_tree_iterator<std::pair<const unsigned char,cacheLine *> > __end2; // [rsp+60h] [rbp-50h] BYREF
  std::_Rb_tree_iterator<std::pair<const unsigned char,cacheLine *> > __begin2; // [rsp+68h] [rbp-48h] BYREF
  std::map<unsigned char,cacheLine *> *__range2; // [rsp+70h] [rbp-40h]
  uint8_t toEvictLine; // [rsp+7Fh] [rbp-31h] BYREF
  cacheLine *toEvict; // [rsp+80h] [rbp-30h]
  uint32_t min_time; // [rsp+8Ch] [rbp-24h]
  std::_Rb_tree_iterator<std::pair<const unsigned char,cacheLine *> >::_Self __y; // [rsp+90h] [rbp-20h] BYREF
  std::_Rb_tree_iterator<std::pair<const unsigned char,cacheLine *> >::_Self __x; // [rsp+98h] [rbp-18h] BYREF
  uint8_t line; // [rsp+A6h] [rbp-Ah] BYREF
  uint8_t isFasta; // [rsp+A7h] [rbp-9h]
  uint64_t addra; // [rsp+A8h] [rbp-8h]

  addra = addr;
  isFasta = isFast;
  line = (addr >> 5) & 0x1F;
  __x._M_node = std::map<unsigned char,cacheLine *>::find(&caches, &line)._M_node;
  __y._M_node = std::map<unsigned char,cacheLine *>::end(&caches)._M_node;
  if ( std::operator!=(&__x, &__y)
    && (v2 = std::map<unsigned char,cacheLine *>::operator[](&caches, &line), (*v2)->tag == addra >> 10) )
  {
    v4 = tick;
    (*std::map<unsigned char,cacheLine *>::operator[](&caches, &line))->last_used = v4;
  }
  else
  {
    min_time = -1;
    toEvictLine = 0;
    if ( std::map<unsigned char,cacheLine *>::size(&caches) >= 0x20 )
    {
      __range2 = &caches;
      __begin2._M_node = std::map<unsigned char,cacheLine *>::begin(&caches)._M_node;
      __end2._M_node = std::map<unsigned char,cacheLine *>::end(&caches)._M_node;
      while ( std::operator!=(&__begin2, &__end2) )
      {
        p = *std::_Rb_tree_iterator<std::pair<unsigned char const,cacheLine *>>::operator*(&__begin2);
        if ( p.second->last_used < min_time )
        {
          min_time = p.second->last_used;
          toEvict = p.second;
          toEvictLine = p.first;
        }
        std::_Rb_tree_iterator<std::pair<unsigned char const,cacheLine *>>::operator++(&__begin2);
      }
      std::map<unsigned char,cacheLine *>::erase(&caches, &toEvictLine);
      if ( toEvict )
        operator delete(toEvict);
    }
    v3 = (cacheLine *)operator new(8uLL);
    cacheLine::cacheLine(v3, addra);
    *std::map<unsigned char,cacheLine *>::operator[](&caches, &line) = v3;
    if ( !isFasta )
      sleep(1u);
  }
}
```

该函数实现了缓存机制；

##### map<unsigned char,cacheLine *>

`v2`代表了一个缓存块；

```c
v2 = std::map<unsigned char,cacheLine *>::operator[](&caches, &line)
```

###### cacheLine

`cacheLine`结构体包含两个参数`tag`和`last_used`；

```
00000000 cacheLine       struc ; (sizeof=0x8, align=0x4, copyof_47)
00000000 tag             dd ?     # address
00000004 last_used       dd ?     # tick
00000008 cacheLine       ends
```

`tag`与当前缓存块的内存地址的关系为：

```c
tag == addra >> 10
```

`last_used`标记访问上一次访问`cacheLine`的`tick`；

###### line

```c
 line = (addr >> 5) & 0x1F;
```

`line`通过映射的方式标记当前缓存块的编号，通过`& 0x1f`获得后5位数值，可以映射`2^5 = 32`个内存地址；

#### 流程分析

##### 参数获取

```c
  addra = addr;							// address
  isFasta = isFast;						// speed
  line = (addr >> 5) & 0x1F;			// line
  __x._M_node = std::map<unsigned char,cacheLine *>::find(&caches, &line)._M_node;      // cacheLine[line]参数 start
  __y._M_node = std::map<unsigned char,cacheLine *>::end(&caches)._M_node;				// cacheLine end
```

##### 非空

```c
  if ( std::operator!=(&__x, &__y)		// 如果cacheLine[line]非空
      // 若元素非空且line和tag一致，更新缓存块的last_used为tick，退出函数
    && (v2 = std::map<unsigned char,cacheLine *>::operator[](&caches, &line), (*v2)->tag == addra >> 10) )
  {
    v4 = tick;
    (*std::map<unsigned char,cacheLine *>::operator[](&caches, &line))->last_used = v4;
  }
```

##### 为空

```c
  else				// 判断caches的大小是否超过 0x20，若是，则删除caches中last_used最小的缓存块
  {
    min_time = -1;
    toEvictLine = 0;
    if ( std::map<unsigned char,cacheLine *>::size(&caches) >= 0x20 )
    {
      __range2 = &caches;
      __begin2._M_node = std::map<unsigned char,cacheLine *>::begin(&caches)._M_node;
      __end2._M_node = std::map<unsigned char,cacheLine *>::end(&caches)._M_node;
      while ( std::operator!=(&__begin2, &__end2) )
      {
        p = *std::_Rb_tree_iterator<std::pair<unsigned char const,cacheLine *>>::operator*(&__begin2);
        if ( p.second->last_used < min_time )
        {
          min_time = p.second->last_used;
          toEvict = p.second;
          toEvictLine = p.first;
        }
        std::_Rb_tree_iterator<std::pair<unsigned char const,cacheLine *>>::operator++(&__begin2);
      }
      std::map<unsigned char,cacheLine *>::erase(&caches, &toEvictLine);
      if ( toEvict )
        operator delete(toEvict);
    }
```

##### 存储

```c
// 最后按照line和tag创建新的缓存块，储存在caches[line]上
    v3 = (cacheLine *)operator new(8uLL);
    cacheLine::cacheLine(v3, addra);
    *std::map<unsigned char,cacheLine *>::operator[](&caches, &line) = v3;
```

##### 休眠

```c
// 若speed为0时，创建新的缓存块时会sleep(1)
    if ( !isFasta )
      sleep(1u);
```

## 漏洞利用

当`speed`为`0`时，可以通过时间来判断是否创建了新的缓存块（会执行`sleep(1)`）。若能够缓存某个未知的内存地址，通过该方法我们可以推算出对应缓存块的`line`和`tag`，从而计算出这个未知内存地址的大致范围；

因此，我们可以推算出 SPN 加密时访问`ss_box`和`ss_box`元素的内存地址，然后减去基址得到内部的状态值，最后利用它们恢复明文和密钥。

### guess line & tag

```python
from pwn import *

s_box = 0x645110
p_box  = 0x605110

p = None

def new_conn():
    global p
    if p:
        p.close()
        p = None
    p = remote("124.71.173.176", 9999)

def maccess(addr, speed):            
    p.sendlineafter("What to do?", '1')
    p.sendlineafter("Where?", str(addr))
    p.sendlineafter("Speed up?", str(speed))    

def maccess_addr(skip):
    if skip:            
        p.sendlineafter("What to do?", '4')
    else:
        p.sendlineafter("What to do?", '2')
```

