---
title: CS61A_lab_00_intro
top: false
comment: false
lang: zh-CN
date: 2022-11-04 20:00:05
tags:
categories:
  - study
  - course
  - CS 61A:Structure and Interpretation of Computer Programs
  - lab
---

# Introduction

[lab00](https://cs61a.org/lab/lab00/lab00.zip)

```
wget https://cs61a.org/lab/lab00/lab00.zip
unzip lab00.zip
```

实验目录下有如下文件：

```
$ ls
__pycache__  lab00.ok  lab00.py  ok  tests
```

下面是解释：

- `lab00.py`: the starter file you just edited
- `ok`: our testing program
- `lab00.ok`: a configuration file for Ok

关于 `ok` 的命令生成器：

[okpy Command Generator](https://ok-help.cs61a.org/)



`lab00` 要求很简单：

```python
def twenty_twenty_two():
    """Come up with the most creative expression that evaluates to 2022,
    using only numbers and the +, *, and - operators.

    >>> twenty_twenty_two()
    2022
    """
    return 2022
```

用表达式返回值 2022 即可，这里我就直接返回了:smile_cat:。

```shell
python3 ok
```

执行，如果想submit，可以换成 `python3 ok submit` 。
