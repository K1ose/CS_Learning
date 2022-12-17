---
title: Qt5.9_cpp_programming_guide_01_meet
top: false
comment: false
lang: zh-CN
date: 2022-11-01 05:50:06
tags:
categories:
  - study
  - book
  - Qt5.9 c++ 开发指南
---

# 认识Qt

Qt是一套应用程序开发类库，和MFC不同，Qt是跨平台的。Qt支持PC和服务器的平台，包括Windows、Linux、macOS等，还支持移动和嵌入式操作系统，比如iOS、Embedded Linux、Android、WinRT等。跨平台意味着只需要一次编程，在不同平台上无需改动或只需少许改动后再编译，就可以形成在不同平台上运行的版本。

## 获取与安装

### 许可类型

- 商业许可
- 开源许可
  - LGPLV3
  - GPLV2/GPLV3

商业许可 - 允许开发者不公开项目的源代码，其Qt版本包含更多的模块（这些模块只存在于商业许可的版本），并能够获得Qt公司的计数支持。需要向Qt购买商业许可才能获得。

开源许可 - 无需支付，但是需要遵守相关规定。

### 版本

Qt版本更新很快，且版本更新时会新增一些类或停止维护一些以前版本的类，Qt 5和Qt 4就有较大区别，如果不是为了维护旧版本编写的程序，一定要选用最新版本的Qt进行开发。

### 下载和安装

从Qt官网可以下载最新版本的Qt软件，根据开发项目不同，Qt分为桌面和移动设备应用开发、嵌入式设备开发 两大类不同的安装包。

- 桌面和移动设备应用开发 - PC、服务器、手机、平板电脑上运行的程序，免费下载使用
- 嵌入式设备开发 - 物联网设备、汽车电子设备等特定的嵌入式设备

需要注册用户后才可以下载Qt安装程序，安装包分为在线和离线两种，为便于重复安装，最好下载离线安装包。

安装时需要选择想要安装的组件，其中有：

- MinGW - Minimalist GNU for Windows 的缩写，是Windows平台上使用的GNU工具集导入库的集合
- 用于UWP编译的模块 - Universal Windows Platform 的缩写，有不同编译器类型的UWP
- 用于Windows平台上的MSVC编译器模块 - 如 msvc2015 32-bit 等，要安装 MSVC 编译器的模块，需要计算机上安装相应版本的Visual Studio
- 用于Android平台的模块 - 如 Android x86 和 Android ARMv7
- Sources - Qt源程序
- Qt Charts - 二位图标模块，用于绘制各类型二位图标
- Qt Data Visualization - 三维数据图表模块
- Qt Purchasing、QtWebEngine、Qt Network Auth(TP)
- Qt Script(Deprecated) - 已经过时的脚本模块

## 初步使用

### 设置

[Tools] - [Options]

- Environment 可切换语言
- Kits中可以设置编译工具

[Windows Software Development Kit](https://developer.microsoft.com/zh-cn/windows/downloads/sdk-archive/)

## 第一个程序

### 项目创建流程

[File] - [New FIle or Project]

Qt Creator中可以创建多种项目：

- Qt Widgets Application 支持桌面平台的有图形用户界面(Graphic User Interface,GUI)
- Qt Console Application 控制台应用程序，无GUI界面
- Qt Quick Application 创建可部署的Qt Quick 2应用程序，界面采用QML语言，程序采用C++，可以设计出非常炫酷的用户界面，一般用于移动设备或嵌入设备上无边框的应用程序设计
- Qt Quick Controls 2 Application 创建基于Qt Quick Controls 2组件的可部署的Qt Quick 2应用程序
- Qt Canvas 3D Application 创建Qt Canvas 3D QML项目，支持3D画布



在这里选择项目类型为 Qt Widgets Application，选择目录，再设置项目名称，这样在该目录下会创建一个项目文件夹。



选择编译工具，在编译项目时可以选择一个作为当前使用的编译工具，编译生成不同版本的可执行程序。



选择基类(base class)，三种类型可以选择：

- QMainWindow 是主窗口类，带有主菜单栏、工具栏和状态栏；
- QWidget 是所有具有可视界面类的基类，选择QWidget创建的界面对各种界面组件都可以支持；
- QDialog 是对话框类，可建立一个基于对话框的界面；

### 项目文件组成和管理

在项目名称节点下，分组管理着项目内的各种源文件，分别为：

- xxx.pro 项目管理文件，包括一些对项目的设置项；
- Headers分组，该节点下是项目内所有头文件(.h)；

- Sources分组，该节点下是项目内的所有C++源文件(.cpp)，mainwindow.cpp是主窗口类的实现文件，main.cpp是主函数文件，也是程序的入口；
- Forms分组，该节点下是项目内的所有界面文件(.ui)，是用XML语言描述的。



在下拉栏里，有如下的几个分组：

![](./Qt5-9-cpp-programming-guide-01-meet\figure_01.jpg)

其中 Class View 可以显示项目内所有的类结构，便于快速切换和浏览；

双击 mainwindow.ui 会出现窗体设计界面，实际上是继承的 Qt Designer。

### 编译、调试和执行

[Projects] - [Build&Run] 选择哪一个编译器用于编译项目，选择[Shadow build] 会在编译后在项目同级目录下建立一个编译后的目录，如果不选择此项，会在目录下建立 [Debug] 和 [Release] 子目录。
