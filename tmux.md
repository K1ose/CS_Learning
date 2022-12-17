---
title: tmux
top: false
comment: false
lang: zh-CN
date: 2021-11-14 05:55:20
tags:
categories:
  - tools
---

# tmux

tmux是一个terminal multiplexer（终端复用器），可以启动一系列终端会话， 它解绑了会话和终端窗口。关闭终端窗口再打开，会话并不终止，而是继续运行在执行。将会话与终端窗后彻底分离。  tmux使用C语言实现，可运行在OpenBSD，FreeBSD，NetBSD，Linux，OS，X，Solaris上。

## 链接、安装、启动、退出

### tmux在github上的链接

```
https://github.com/tmux/tmux
```

### 安装

```
git clone https://github.com/tmux/tmux.git
cd tmux
sh autogen.sh
./configure && make
```

或者

```
# ubuntu
sudo apt-get install tmux
```

### 启动与退出

```
# 启动
$ tmux

# 退出
$ exit		# Ctrl^D
```

## 功能用法

### 新窗口创建于命名

```
$ tmux new -s <name>
```

### 重命名会话

重命名tmux会话

```
$ tmux rename-session -t <id> <new-name>
```

### 会话后台化

会话后台化可以退出当前的tmux窗口，但是tmux窗口仍然在后台运行

```
# 分离后台化
$ tmux detach
```

### 会话重连

会话在后台时，使用以下命令重连tmux会话

```
$ tmux attach -t <id>/<name>
```

### 会话展开

显示当前所有的tmux窗口

```
$ tmux ls

# 所有会话信息
$ tmux info
```

### 会话切换

切换tmux会话窗口

```
$ tmux switch -t <id>/<name>
```

### 会话杀死

杀死tmux会话

```
$ tmux kill-session -t <id>/<name>
```

## 高级功能

### 快捷键、命令展示

展示tmux下的快捷键、命令

```
# 快捷键
$ tmux list-keys

#命令
$ tmux list-commands
```

### 快捷键前缀+快捷键功能

注意，使用快捷键前必须先使用前缀`ctrl^b`

#### 帮助

- ？ 获取帮助信息

#### 会话（Session）管理

- s 列出所有会话
- $ 重命名当前的会话

- d 断开当前的会话

#### 窗口（Window）管理

- c 创建一个新窗口
- , 重命名当前窗口

- w 列出所有窗口
- % 水平分割窗口

- " 竖直分割窗口
- n 选择下一个窗口

- p 选择上一个窗口
- 0~9 选择0~9对应的窗口

#### 窗格（Pane）管理

- % 创建一个水平窗格
- " 创建一个竖直窗格

- q 显示窗格的编号
- o 在窗格间切换

- } 与下一个窗格交换位置
- { 与上一个窗格交换位置

- ! 在新窗口中显示当前窗格
- x 关闭当前窗格

#### 其他

- t 在当前窗格显示时间
