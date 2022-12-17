---
title: MIT_Missing_Semester_03_vim
top: false
comment: false
lang: zh-CN
date: 2022-09-18 16:00:51
tags:
categories:
  - study
  - course
  - Missing Semester
---

# Vim

## 使用

### 模式

按下 `<ESC>` （退出键） 从任何其他模式返回正常模式。 在正常模式，键入 `i` 进入插入模式， `R` 进入替换模式， `v` 进入可视（一般）模式， `V` 进入可视（行）模式， `<C-v>` （Ctrl-V, 有时也写作 `^V`）进入可视（块）模式， `:` 进入命令模式。

在正常模式下键入 `:` 进入命令行模式。 在键入 `:` 后，你的光标会立即跳到屏幕下方的命令行。 这个模式有很多功能，包括打开，保存，关闭文件，以及 [退出 Vim](https://twitter.com/iamdevloper/status/435555976687923200)。

- `:q` 退出（关闭窗口）
- `:w` 保存（写）
- `:wq` 保存然后退出
- `:e {文件名}` 打开要编辑的文件
- `:ls` 显示打开的缓存
- `:help {标题}` 打开帮助文档
  - `:help :w` 打开 `:w` 命令的帮助文档
  - `:help w` 打开 `w` 移动的帮助文档

### 移动

多数时候你会在正常模式下，使用移动命令在缓存中导航。在 Vim 里面移动也被称为 “名词”， 因为它们指向文字块。

- 基本移动: `hjkl` （左， 下， 上， 右）
- 词： `w` （下一个词）， `b` （词初）， `e` （词尾）
- 行： `0` （行初）， `^` （第一个非空格字符）， `$` （行尾）
- 屏幕： `H` （屏幕首行）， `M` （屏幕中间）， `L` （屏幕底部）
- 翻页： `Ctrl-u` （上翻）， `Ctrl-d` （下翻）
- 文件： `gg` （文件头）， `G` （文件尾）
- 行数： `:{行数}<CR>` 或者 `{行数}G` ({行数}为行数)
- 杂项： `%` （找到配对，比如括号或者 /* */ 之类的注释对）
- 查找：`f{字符}`，`t{字符}`，`F{字符}`，`T{字符}`
  - 查找/到 向前/向后 在本行的{字符}
  - `,` / `;` 用于导航匹配
- 搜索: `/{正则表达式}`, `n` / `N` 用于导航匹配

### 选择

可视化模式:

- 可视化：`v`
- 可视化行： `V`
- 可视化块：`Ctrl+v`

可以用移动命令来选中。

### 编辑

所有你需要用鼠标做的事， 你现在都可以用键盘：采用编辑命令和移动命令的组合来完成。 这就是 Vim 的界面开始看起来像一个程序语言的时候。Vim 的编辑命令也被称为 “动词”， 因为动词可以施动于名词。

- `i`  进入插入模式
  - 但是对于操纵/编辑文本，不单想用退格键完成
- `O` / `o` 在之上/之下插入行
- `d{移动命令}` 删除 {移动命令}
  - 例如， `dw` 删除词, `d$` 删除到行尾, `d0` 删除到行头。
- `c{移动命令}` 改变 {移动命令}
  - 例如， `cw` 改变词
  - 比如 `d{移动命令}` 再 `i`
- `x` 删除字符（等同于 `dl`）
- `s` 替换字符（等同于 `xi`）
- 可视化模式 + 操作
  - 选中文字, `d` 删除 或者 `c` 改变
- `u` 撤销, `<C-r>` 重做
- `y` 复制 / “yank” （其他一些命令比如 `d` 也会复制）
- `p` 粘贴
- 更多值得学习的: 比如 `~` 改变字符的大小写

### 计数

你可以用一个计数来结合“名词”和“动词”，这会执行指定操作若干次	。

- `3w` 向前移动三个词
- `5j` 向下移动5行
- `7dw` 删除7个词

### 修饰语

你可以用修饰语改变“名词”的意义。修饰语有 `i`，表示“内部”或者“在内“，和 `a`， 表示”周围“。

- `ci(` 改变当前括号内的内容
- `ci[` 改变当前方括号内的内容
- `da'` 删除一个单引号字符串， 包括周围的单引号

## 练习演示

下面是关于python的错误用例，现在需要用vim来将其修复。

```python
def fizz_buzz(limit):
    for i in range(limit):
        if i % 3 == 0:
            print('fizz')
        if i % 5 == 0:
            print('fizz')
        if i % 3 and i % 5:
            print(i)

def main():
    fizz_buzz(10)
```

其问题为：

- 主函数没有被调用
- 从 0 而不是 1 开始
- 在 15 的整数倍的时候在不换行打印 “fizz” 和 “buzz”
- 在 5 的整数倍的时候打印 “fizz”
- 采用硬编码的参数 10 而不是从命令控制行读取参数

解决如下：

- 主函数没有被调用

  - `G` 文件末尾 -> `o` 新增一行，输入

    ```python
    if __name__ == "__main__":
    	main()
    ```

- 从 0 而不是 1 开始

  - `/range` 搜索到range
  - `ww` 前移两个词，
  - `i` 进入插入模式，输入 `1,` 
  - `ea` 在limit后插入 `+1`

- 在新的一行 “fizzbuzz”

  - `gg` 回到开头后，`jjj$i` 到`print('fizz')`的行尾，`i` 进入插入模式，输入 `“, end=’’”`
  - `jj.` 重复第二个打印，`.`表示重复其一次的编辑
  - `jjo` 新建一行，输入 `else: print()`

- 在 5 的整数倍的时候打印 “fizz”
  - `/fizz` 搜索到对应字符串，如果有多个相同字符串，可以使用 `n` 来循环遍历
  - `ci'` 修改当前光标到`'`之前的内容，这条命令将把`''`之间的 `fizz` 删除，之后填入 `buzz`
- 采用硬编码的参数 10 而不是从命令控制行读取参数
  - `gg` 回到开头
  - `O` 创建一个新行，`i` 进入插入模式，输入`import sys`
  - 退出插入模式后，输入 `/10` 查找到 `fizz_buzz(10)` 的位置，并输入 `ci(` ，修改括号前的内容为`int(sys.argv[1])`

最终修改后的代码为：

```python
import sys
def fizz_buzz(limit):
    for i in range(1, limit+1):
        if i % 3 == 0:
            print('fizz', end="")
        if i % 5 == 0:
            print('buzz', end="")
        if i % 3 and i % 5:
            print(i)
        else:
            print()

def main():
    fizz_buzz(int(sys.argv[1]))

if __name__ == "__main__":
    main()
```

## 自定义 Vim

Vim 由一个位于 `~/.vimrc` 的文本配置文件（包含 Vim 脚本命令）。 你可能会启用很多基本 设置。

我们提供一个文档详细的基本设置，你可以用它当作你的初始设置。我们推荐使用这个设置因为 它修复了一些 Vim 默认设置奇怪行为。 **在[这儿](https://missing-semester-cn.github.io/2020/files/vimrc) 下载我们的设置，然后将它保存成 `~/.vimrc`.**

Vim 能够被重度自定义，花时间探索自定义选项是值得的。你可以参考其他人的在GitHub 上共享的设置文件，比如，你的授课人的 Vim 设置 ([Anish](https://github.com/anishathalye/dotfiles/blob/master/vimrc), [Jon](https://github.com/jonhoo/configs/blob/master/editor/.config/nvim/init.vim) (uses [neovim](https://neovim.io/)), [Jose](https://github.com/JJGO/dotfiles/blob/master/vim/.vimrc))。 有很多好的博客文章也聊到了这个话题。尽量不要复制粘贴别人的整个设置文件， 而是阅读和理解它，然后采用对你有用的部分。

## 扩展 Vim

Vim 有很多扩展插件。跟很多互联网上已经过时的建议相反，你_不_需要在 Vim 使用一个插件 管理器（从 Vim 8.0 开始）。你可以使用内置的插件管理系统。只需要创建一个 `~/.vim/pack/vendor/start/` 的文件夹，然后把插件放到这里（比如通过 `git clone`）。

以下是一些我们最爱的插件：

- [ctrlp.vim](https://github.com/ctrlpvim/ctrlp.vim): 模糊文件查找
- [ack.vim](https://github.com/mileszs/ack.vim): 代码搜索
- [nerdtree](https://github.com/scrooloose/nerdtree): 文件浏览器
- [vim-easymotion](https://github.com/easymotion/vim-easymotion): 魔术操作

我们尽量避免在这里提供一份冗长的插件列表。你可以查看讲师们的开源的配置文件 ([Anish](https://github.com/anishathalye/dotfiles), [Jon](https://github.com/jonhoo/configs), [Jose](https://github.com/JJGO/dotfiles)) 来看看我们使用的其他插件。 浏览 [Vim Awesome](https://vimawesome.com/) 来了解一些很棒的插件。 这个话题也有很多博客文章：搜索 “best Vim plugins”。

## 其他程序的 Vim 模式

很多工具提供了 Vim 模式。这些 Vim 模式的质量参差不齐；取决于具体工具， 有的提供了 很多酷炫的 Vim 功能，但是大多数对基本功能支持的很好。

### Shell

如果你是一个 Bash 用户，用 `set -o vi`。如果你用 Zsh：`bindkey -v`。Fish 用 `fish_vi_key_bindings`。另外，不管利用什么 shell，你可以 `export EDITOR=vim`。 这是一个用来决定当一个程序需要启动编辑时启动哪个的环境变量。 例如，`git` 会使用这个编辑器来编辑 commit 信息。

### Readline

很多程序使用 [GNU Readline](https://tiswww.case.edu/php/chet/readline/rltop.html) 库来作为 它们的命令控制行界面。Readline 也支持基本的 Vim 模式， 可以通过在 `~/.inputrc` 添加如下行开启：

```
set editing-mode vi
```

比如，在这个设置下，Python REPL 会支持 Vim 快捷键。

### 其他

甚至有 Vim 的网页浏览快捷键 [browsers](http://vim.wikia.com/wiki/Vim_key_bindings_for_web_browsers), 受欢迎的有 用于 Google Chrome 的 [Vimium](https://chrome.google.com/webstore/detail/vimium/dbepggeogbaibhgnhhndojpepiihcmeb?hl=en) 和用于 Firefox 的 [Tridactyl](https://github.com/tridactyl/tridactyl)。 你甚至可以在 [Jupyter notebooks](https://github.com/lambdalisue/jupyter-vim-binding) 中用 Vim 快捷键。 [这个列表](https://reversed.top/2016-08-13/big-list-of-vim-like-software) 中列举了支持类 vim 键位绑定的软件。

## Vim 进阶

这里我们提供了一些展示这个编辑器能力的例子。我们无法把所有的这样的事情都教给你，但是你 可以在使用中学习。一个好的对策是: 当你在使用你的编辑器的时候感觉 “一定有更好的方法来做这个”， 那么很可能真的有：上网搜寻一下。

### 搜索和替换

`:s` （替换）命令（[文档](http://vim.wikia.com/wiki/Search_and_replace)）。

- ```plaintext
  %s/foo/bar/g
  ```

  - 在整个文件中将 foo 全局替换成 bar

- ```plaintext
  %s/\[.*\](\(.*\))/\1/g
  ```

  - 将有命名的 Markdown 链接替换成简单 URLs

### 多窗口

- 用 `:sp` / `:vsp` 来分割窗口
- 同一个缓存可以在多个窗口中显示。

### 宏

- `q{字符}` 来开始在寄存器`{字符}`中录制宏

- `q`停止录制

- `@{字符}` 重放宏

- 宏的执行遇错误会停止

- `{计数}@{字符}`执行一个宏{计数}次

- 宏可以递归

  - 首先用`q{字符}q`清除宏
  - 录制该宏，用 `@{字符}` 来递归调用该宏 （在录制完成之前不会有任何操作）

- 例子：将 xml 转成 json (

  file

  )

  - 一个有 “name” / “email” 键对象的数组

  - 用一个 Python 程序？

  - 用 sed / 正则表达式

    - `g/people/d`
    - `%s/<person>/{/g`
    - `%s/<name>\(.*\)<\/name>/"name": "\1",/g`
    - …

  - Vim 命令 / 宏

    - `Gdd`, `ggdd` 删除第一行和最后一行

    - 格式化最后一个元素的宏 （寄存器

       

      ```plaintext
      e
      ```

      ）

      - 跳转到有 `<name>` 的行
      - `qe^r"f>s": "<ESC>f<C"<ESC>q`

    - 格式化一个

      的宏

      - 跳转到有 `<person>` 的行
      - `qpS{<ESC>j@eA,<ESC>j@ejS},<ESC>q`

    - 格式化一个

      标签然后转到另外一个的宏

      - 跳转到有 `<person>` 的行
      - `qq@pjq`

    - 执行宏到文件尾

      - `999@q`

    - 手动移除最后的 `,` 然后加上 `[` 和 `]` 分隔符

## 课后练习

1. 完成 `vimtutor`。 备注：它在一个 [80x24](https://en.wikipedia.org/wiki/VT100)（80 列，24 行） 终端窗口看起来效果最好。

   ```shell
   # terminal中输入下指令
   vimtutor
   ```

2. 下载我们提供的 [vimrc](https://missing-semester-cn.github.io/2020/files/vimrc)，然后把它保存到 `~/.vimrc`。 通读这个注释详细的文件 （用 Vim!）， 然后观察 Vim 在这个新的设置下看起来和使用起来有哪些细微的区别。

   

3. 安装和配置一个插件：

   ctrlp.vim

   1. 用 `mkdir -p ~/.vim/pack/vendor/start` 创建插件文件夹
   2. 下载这个插件： `cd ~/.vim/pack/vendor/start; git clone https://github.com/ctrlpvim/ctrlp.vim`
   3. 阅读这个插件的 [文档](https://github.com/ctrlpvim/ctrlp.vim/blob/master/readme.md)。 尝试用 CtrlP 来在一个工程文件夹里定位一个文件， 打开 Vim, 然后用 Vim 命令控制行开始 `:CtrlP`.
   4. 自定义 CtrlP： 添加 [configuration](https://github.com/ctrlpvim/ctrlp.vim/blob/master/readme.md#basic-options) 到你的 `~/.vimrc` 来用按 Ctrl-P 打开 CtrlP

4. 练习使用 Vim, 在你自己的机器上重做 [演示](https://missing-semester-cn.github.io/2020/editors/#demo)。

5. 下个月用 Vim 完成_所有的_文件编辑。每当不够高效的时候，或者你感觉 “一定有一个更好的方式”时， 尝试求助搜索引擎，很有可能有一个更好的方式。如果你遇到难题，可以来我们的答疑时间或者给我们发邮件。

6. 在其他工具中设置 Vim 快捷键 （见上面的操作指南）。

7. 进一步自定义你的 `~/.vimrc` 和安装更多插件。

8. （高阶）用 Vim 宏将 XML 转换到 JSON ([例子文件](https://missing-semester-cn.github.io/2020/files/example-data.xml))。 尝试着先完全自己做，但是在你卡住的时候可以查看上面[宏](https://missing-semester-cn.github.io/2020/editors/#macros) 章节。

   1. Gdd, ggdd 删除第一行和最后一行

   2. 格式化最后一个元素的宏 （寄存器 e）
      跳转到有 `<name>` 的行 `qe^r"f>s": "<ESC>f<C"<ESC>q`

   3. 格式化一个人的宏
      跳转到有 `<person>` 的行 `qpS{<ESC>j@eA,<ESC>j@ejS},<ESC>q`

   4. 格式化一个人然后转到另外一个人的宏
      跳转到有 `<person>` 的行`qq@pjq`

   5. 执行宏到文件尾 `999@q`

   6. 手动移除最后的 , 然后加上 [ 和 ] 分隔符
