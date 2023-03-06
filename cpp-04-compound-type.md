---
title: cpp_04_compound_type
top: false
comment: false
lang: zh-CN
date: 2022-06-18 12:58:33
tags:
categories:
  - program language
  - cpp
---

# Ch4 复合类型

## 数组

通用命名格式：`typeName arrayName(arraySize)` ；

`typeName` ：存储在每个元素中的值的类型；

`arrayName` ：数组名称；

`arraySize` ：元素数目，必须是整形或 `const` 值，也可以是常量表达式 `8*sizeof(int)` ，总而言之，`arraySize` 不能是变量，即不能是程序运行时设置的；

数组的初始化规则：

- 只有在定义数组的时候才能初始化（整体赋值）；
- 不能将一个数组赋值给另一个数组； 
- 如果只对数组的一部分进行初始化，其余元素被设置为 `0` ；
- 初始化数组时方括号内为空，编译器会计算元素个数；

## 字符串

C++的字符串有两类：

- C-风格的字符串：以空字符（null character） `\0` 为结尾，其ASCII码为 0 。例如：

  ```cpp
  char cat[8] = {'f', 'a', 't', 'e', 's', 's', 'a', '\0'}		// a string
  ```

-  字符串常量（string constant）。例如：

  ```
  char dog[] = "doggy";
  ```

  用双引号括起的字符串隐式地包括了结尾的空字符。 

要注意 `''` 和 `""` 的区别，
