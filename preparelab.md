---
title: preparelab
top: false
comment: false
lang: zh-CN
date: 2021-11-20 12:08:06
tags:
categories:
  - CSAPP
  -	CSAPP-lab
---

# preparelab

## docker

使用`docker`挂载目录，实现宿主机与镜像的数据同步；

1. 拉取一个centos系统

   ```
   docker pull centos
   ```

2. 创建目录挂载

   ```
   docker container run -it -v /home/klose/ctf/pwn/csapp_lab:/csapp_lab --name=csapp_env centos /bin/bash
   ```

3. 使用完毕后退出：

   ```
   ctrl ^ p + q
   ```

4. 列出当前镜像

   ```
   docker ps -a
   ```

5. 开启

   ```
   docker start <id>
   ```

6. 进入

   ```
   docker attach <id>
   ```

   

## environment

1. update

   ```
   yum -y update
   ```

2. sudo

   ```
   yum install sudo
   ```

3. c/c++ environ

   ```
   yum install make automake gcc gcc-c++ kernel-devel
   ```

4. gdb for debug

   ```
   yum install gdb
   ```

5. 32bit environ

   ```
   yum install glibc-devel.i686
   ```

## csapp-readme

1. make

   ```
   make btest
   ```

2. 完成xxx.c的lab任务后，可以用以下指令来计算得分

   ```
   ./btest
   ```

3. 对单个func进行检查；

   ```
   ./btest -f <func_name>
   ```

   

