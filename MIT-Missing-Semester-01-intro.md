---
title: MIT_Missing_Semester_01_intro
top: false
comment: false
lang: zh-CN
date: 2022-09-16 10:16:10
tags:
categories:
  - study
  - course
  - Missing Semester
---

# Course overview + the shell

中文课程网站链接：

https://missing-semester-cn.github.io/2020/course-shell/

官方链接：

https://missing.csail.mit.edu/2020/course-shell/



至于shell，呃，如果是稍微用过linux应该都会有所了解。

这里就完成一下课后练习。

1. 我使用的是VMware，用的是Ubuntu 14.06，在输入`echo $SHELL`后得到如下结果：

   ```shell
   echo $SHELL
   /bin/bash
   ```

2. 在 `/tmp` 下新建一个名为 `missing` 的文件夹。

   ```shell
   cd /tmp && mkdir missing
   ```

3. 用 `man` 查看程序 `touch` 的使用手册。

   ```shell
   man touch
   ```

   得到结果：

   ```
   NAME
          touch - change file timestamps
   
   SYNOPSIS
          touch [OPTION]... FILE...
   
   DESCRIPTION
          Update the access and modification times of each FILE to the current time.
   
          A FILE argument that does not exist is created empty, unless -c or -h is supplied.
   
          A FILE argument string of - is handled specially and causes touch to change the times of the file associated with standard output.
   
          Mandatory arguments to long options are mandatory for short options too.
   ```

4. 用 `touch` 在 `missing` 文件夹中新建一个叫 `semester` 的文件。

   ```shell
   cd missing && touch semester
   ```

5. 将以下内容一行一行地写入 `semester` 文件：

   ```shell
    #!/bin/sh
    curl --head --silent https://missing.csail.mit.edu
   ```

   其实很简单，只需要执行下面命令即可：

   ```shell
   echo "#!/bin/sh" > semester && echo "curl --head --silent https://missing.csail.mit.edu" >> semester
   ```

6. 尝试执行这个文件。例如，将该脚本的路径（`./semester`）输入到您的shell中并回车。如果程序无法执行，请使用 `ls` 命令来获取信息并理解其不能执行的原因。

   ```shell
   ./semester
   bash: ./semester: Permission denied
   ```

   使用下列命令查看文件权限：

   ```shell
   ls -l
   total 4
   -rw-rw-r-- 1 klose klose 62 Sep 16 16:01 semester
   ```

   可以看到没有执行权限，使用chmod来添加权限：

7. 查看 `chmod` 的手册(例如，使用 `man chmod` 命令)

   ```shell
   man chmod
   ```

8. 使用 `chmod` 命令改变权限，使 `./semester` 能够成功执行，不要使用 `sh semester` 来执行该程序。您的 shell 是如何知晓这个文件需要使用 `sh` 来解析呢？

   ```shell
   sudo chmod +x semester
   ```

   因为文件开头使用了`#!/bin/bash`，其指定了执行程序路径。

9. 使用 `|` 和 `>` ，将 `semester` 文件输出的最后更改日期信息，写入主目录下的 `last-modified.txt` 的文件中

   ```shell
   ./semester | grep "Last-Modified" > last-modified.txt
   ```

10. 写一段命令来从 `/sys` 中获取笔记本的电量信息，或者台式机 CPU 的温度。注意：macOS 并没有 sysfs，所以 Mac 用户可以跳过这一题。

    由于VMware虚拟机上的镜像并没有相关文件，所以还是去下载了WSL。

    ```shell
    cd /sys/class/power_supply/BAT1 && cat capacity
    ```

    得到结果：

    ```
    100
    ```

    

