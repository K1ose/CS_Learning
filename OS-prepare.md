---
title: OS_prepare
top: false
comment: false
lang: zh-CN
date: 2022-05-03 23:01:05
tags:
categories:
  - OS
  - Prepare
---

# Prepare

## Info

### Piazza

Piazza访问地址：https://piazza.com/tsinghua.edu.cn/spring2015/30240243x

本课程Piazza注册方法见石墨文档：https://shimo.im/docs/dxWCCphVvhg9JjCw/read

### Course

https://www.xuetangx.com/course/THU08091000267/10322317

### Gitbook

https://chyyuu.gitbooks.io/ucore_os_docs/content/

### Question & Answer

https://chyyuu.gitbooks.io/os_course_qa/content/

### Source Code

https://github.com/chyyuu/os_kernel_lab/tree/x86-32



## Source code

从github的名为os_kernel_lab的repo中的master分支取出；

```
mkdir ucore
git init
git remote add origin https://github.com/chyyuu/os_kernel_lab.git
git fetch origin master
git checkout -b ucore origin/master
git pull origin master
```

得到：

```
klose@ubuntu:~/ucore$ ls
bootloader       opensource_os_list.md  related_info
labcodes         os                     resources.md
labcodes_answer  README-chinese.md      rust-toolchain
LICENSE          README.md
```

