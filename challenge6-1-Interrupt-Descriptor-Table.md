---
title: 'challenge6.1:Interrupt_Descriptor_Table'
top: false
comment: false
lang: zh-CN
date: 2021-11-11 16:29:13
tags:
categories:
  - OS
  - lab1
  - challenge6:interrupt handle
---

# challenge6.1:Interrupt Descriptor Table

中断描述符表（也可简称为保护模式下的中断向量表）中一个表项占多少字节？其中哪几位代表中断处理代码的入口？

中断描述符表（Interrupt Descriptor Table） 中断描述符表把每个中断或异常编号和一个指向中断服务例程的描述符联系起来。同GDT一样，IDT是一个8字节的描述符数组，但IDT的第一项可以包含一个描述符。CPU把**中断（异常）号乘以8做为IDT的索引** 。IDT可以位于**内存的任意位置** ，CPU通过**IDT寄存器（IDTR）的内容来寻址IDT的起始地址** 。

因此中断描述符表的每一个表项占用8bytes；

操作系统在IDT中设置好各种中断向量对应的中断描述符，把每个中断或异常编号和一个指向中断服务例程的描述符。当产生中断时，根据指引，找到中断服务例程的描述符，最开始2个字节和最末尾2个字节定义了offset，第16-31位定义了处理代码入口地址的段选择子，由此找到GDT的base_addr，加上offset，即可得到中断处理代码的入口；

