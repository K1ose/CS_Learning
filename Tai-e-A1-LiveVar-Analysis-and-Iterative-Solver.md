---
title: Tai-e_A1_LiveVar_Analysis_and_Iterative_Solver
top: false
comment: false
lang: zh-CN
date: 2022-06-02 17:49:17
tags:
categories:
  - study
  - course
  - static_program_analysis
---

# A1: Live Variable Analysis and Iterative Solver

Link: https://tai-e.pascal-lab.net/pa1.html#_1-%E4%BD%9C%E4%B8%9A%E5%AF%BC%E8%A7%88

## 0x01 作业导览

- 为 Java 实现一个活跃变量分析（Live Variable Analysis）。
- 实现一个通用的迭代求解器（Iterative Solver），用于求解数据流分析问题，也就是本次作业中的活跃变量分析。

我们已经在 Tai-e 的框架代码中提供了你需要的一切基础设施，包括但不限于：程序分析接口、常用数据结构（如数据流信息的表示）、必要的程序信息（如控制流图）等内容。由此，你就可以便利地在 Tai-e 上实现各类数据流分析了。例如在本次作业中，你要在框架代码中补全一些关键部分，从而实现要求的活跃变量分析与迭代求解器。

需要特别注意的是，所有作业文档都只会简要地介绍本次作业中必要的一些 API。因此，如果要搞清楚 API 的实现机制、真正理解框架代码，你必须留出时间阅读、理解相关类的源代码和相应的注释。这是你提升快速上手复杂代码能力的必经之路。

## 0x02 复习

复习一下 live variable analysis 的相关知识。

Live Variable Analysis - 存活变量分析，可以理解为变量的寿命，存活变量分析即找出不被使用的变量，以减少寄存器的不必要使用。

<img src="https://jk404.cn/course/static-program-analysis/static-program-analysis-03-data-flow-analysis/understanding_live.jpg" width="500">

对于上图已知`OUT[B]={v}`的清空下，推算`IN[B]`。

则可能的情况有：

| ?           | $use_B$ | $def_B$ | IsBefore Flag | IN[B] | Explanation             |
| :---------- | :------ | :------ | :------------ | :---- | :---------------------- |
| k = n       | {n}     | {k}     | 0             | {v}   | not used, not redefined |
| k = v       | {v}     | {k}     | 0             | {v}   | used                    |
| v = 2       | {}      | {v}     | 0             | {}    | not used, redefined     |
| v = v - 1   | {v}     | {v}     | 1             | {v}   | used before redefined   |
| v = 2 k = v | {v}     | {v, k}  | 0             | {}    | redefined before used   |
| k = v v = 2 | {v}     | {k, v}  | 1             | {v}   | used before redefined   |

*因此，对于 $IN[B]$ 的推算，由 $OUT[B]-def_B$ 可以去掉那些重定义的 value ，但是如果在重定义前使用了这些变量，则需要加回来 $use_B$ ，由此才得到 $IN[B]$* ；

有如下的算法：

<img src="https://jk404.cn/course/static-program-analysis/static-program-analysis-03-data-flow-analysis/algorithm_1.jpg" width=400>

## 0x03 任务1

完成对 liveVariableAnalysis 类中的四个方法的重写 "TO DO";

