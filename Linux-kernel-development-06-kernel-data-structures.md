---
title: Linux_kernel_development_06_kernel_data_structures
top: false
comment: false
lang: zh-CN
date: 2023-01-06 14:46:09
tags:
categories:
  - study
  - book
  - Linux Kernel Development
---

# 内核数据结构

这一章其实可以放前面一点，因为许多调度算法都会涉及到这些数据结构。

在 Linux 中最常用的几个数据结构：

- 链表
- 队列
- 映射
- 二叉树

## 链表

关于链表的简介可以去参考 [另一篇文章](https://jk404.cn/study/course/data-structure/data-structure-02-linear-list/) ，这里不再过多赘述。

单链表的一般结构：

```cpp
struct list_element{
  	void *data;						/* 数据域 */
    sturct list_element *next;		/* 指向下一个链表元素的指针 */
};
```

双链表的一般结构：

```cpp
struct list_element{
	void *data;						/* 数据域 */
	struct list_element *prev;		/* 指向上一个链表元素的指针 */
	struct list_element *next;		/* 指向下一个链表元素的指针 */
};
```

### 链表遍历

这是一种线性移动，通过先访问某个元素域，再通过该域的指针域进行索引。

### Linux 内核中的实现

内核实现链表的方式比较独特，它不是将数据结构塞入链表，而是将链表节点塞入数据结构。

如何理解上面的话呢？

假设我们现在由一个 cat 数据结构来描述一只猫咪，代码如下：

```cpp
struct cat{
	unsigned long tial_length;	/* 尾巴长度 */
	unsigned long weight;		/* 体重 */
	bool sex;					/* 性别 */
};
```

如果需要用链表来处理（这里我们用双向链表），那么只需要加两个指针就可以了；

```cpp
struct cat{
	unsigned long tial_length;	/* 尾巴长度 */
	unsigned long weight;		/* 体重 */
	bool sex;					/* 性别 */
    
    struct cat *next_cat;		/* 指向下一只猫咪的指针 */
    struct cat *prev_cat;		/* 指向上一只猫咪的指针 */
};
```

在内核 2.1 版本中，首次引入了官方内核链表实现，之后内核所有链表现在都是用官方的链表来实现，而不需要程序员自己再造轮子。它们在头文件 `include\linux\list.h` 中实现。

```cpp
struct list_head {
	struct list_head *next, *prev;
};
```

那么如果我们的猫咪结构也需要套用链表结构，只需要在猫咪结构中调用该结构即可；

```cpp
struct cat{
	unsigned long tial_length;	/* 尾巴长度 */
	unsigned long weight;		/* 体重 */
	bool sex;					/* 性别 */
	
	struct list_head cat_list;	/* cat_list.next 表示下一只猫， cat_list.prev  表示上一只猫 */
};
```

官方的链表代码中还封装了许多链表操作函数，不过它们只接受 `list_head` 结构作为参数，使用 `container_of()` 宏可以很方便地从链表指针中找到父结构包含的任何变量，这是因为给定结构中的变量偏移在编译时就被 ABI (Application Binary Interface，应用程序二进制接口) 确定了。

```cpp
/* include\linux\kernel.h */
#define container_of(ptr, type, member) ({			\
	const typeof( ((type *)0)->member ) *__mptr = (ptr);	\
	(type *)( (char *)__mptr - offsetof(type,member) );})
```

在官方的链表代码中，使用 `list_entry` 方法来管理链表的各种例程，并且不需要知道嵌入对象的数据结构。

```cpp
/**
 * list_entry - get the struct for this entry
 * @ptr:	the &struct list_head pointer.
 * @type:	the type of the struct this is embedded in.
 * @member:	the name of the list_struct within the struct.
 */
#define list_entry(ptr, type, member) \
	container_of(ptr, type, member)
```

#### 定义链表

现在，我们定义我们的猫咪链表结构：

```cpp
struct cat{
	unsigned long tial_length;	/* 尾巴长度 */
	unsigned long weight;		/* 体重 */
	bool sex;					/* 性别 */
	
	struct list_head cat_list;	/* cat_list.next 表示下一只猫， cat_list.prev  表示上一只猫 */
};
```

在运行时初始化链表。

```cpp
struct cat *little_cat;
little_cat = kmalloc(sizeof(*little_cat), GFP_KERNEL);

little_cat->tail_length = 40;
little_cat->weight = 6;
little_cat->sex = 1;

INIT_LIST_HEAD(&little_cat->list);

```

// TODO
