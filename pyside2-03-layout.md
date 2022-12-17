---
title: pyside2_03_layout
top: false
comment: false
lang: zh-CN
date: 2022-10-18 18:02:58
tags:
categories:
  - program language
  - python
  - pyside2
---

# 概要

QT提供了很多界面布局，最简单的是水平和垂直。为什么要使用布局？有些人可能遇到过， 窗口绘制好后，运行时偶然拖动窗口或控件位置大小，会破坏整个布局的美观。在Qt中使用布局功能， 能很好的控制控件的大小和位置随窗孔变化而变化。

# 绘制窗口

添加MainWindow，放置一个Vertical Layout，并拖入label和pushButton。组件会在layout框架中自动排列好，给label设置属性，在filter里搜索alignment，将水平和垂直都设置为Center。
