---
title: talk
top: false
comment: false
lang: zh-CN
date: 2022-01-11 17:49:59
tags:
categories:	
  - study
  - daily
---

# 扯嘴皮子

1. push项目到github

   1. github新建仓库，最好和本地项目一致，复制一下仓库地址，一般就是https://github.com/xxx/xxx.git。

   2. 选定项目文件夹，使用`git clone`来克隆仓库到本地；

   3. 做完工作后，输入

      ```
      git add .
      git commit -m "first commit" // 自定义
      git push -u origin master
      ```

   4. 可能会需要账号密码，2021/8/13后需要用个人的token来访问仓库。

   5. 可以用下面的命令来避免每次都要输入token

      ```
      git remote set-url origin https://<your_token>@github.com/<USERNAME>/<REPO>.git
      ```
   
   5. 其他一些git指令：
   
   ```
   $ git init #把当前目录变成git可以管理的仓库
   $ git add readme.txt #添加一个文件，也可以添加文件夹
   $ git add -A #添加全部文件
   $ git commit -m "some commit" #提交修改
   $ git status #查看是否还有未提交
   $ git log #查看最近日志
   $ git reset --hard HEAD^ #版本回退一个版本
   $ git reset --hard HEAD^^ #版本回退两个版本
   $ git reset --hard HEAD~100 #版本回退多个版本
   $ git remote add origin +地址 #远程仓库的提交（第一次链接）
   $ git push -u origin master #仓库关联
   $ git push #远程仓库的提交（第二次及之后）
   ```
   
   
