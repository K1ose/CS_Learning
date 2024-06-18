---
title: Gopl_02_introduction
top: false
comment: false
lang: zh-CN
date: 2022-10-25 02:28:23
tags:
categories:
  - study
  - book
  - The Go Programming Language 
---

# 第一章 入门

先扯一下go mod的问题。

在1.13版本后引入了管理用的modules机制。

执行：

```
mkdir gopl
go mod init gopl
```

得到与工作目录gopl并列的go.mod，其内容如下：

```
module gopl

go 1.17
```

## hello

依旧是从hello,world开始：

```go
// main.go
package main

import "fmt"

func main() {
	fmt.Println("hello, 世界")
}
```

执行结果为：

```
hello, 世界
```

这里用中文是因为Go语言原生支持Unicode，它可以处理全世界任何语言的文本。

```bash
$ go build main.go
```

这个命令生成一个名为main的可执行的二进制文件（译注：Windows系统下生成的可执行文件是main.exe，增加了.exe后缀名），之后你可以随时运行它（译注：在Windows系统下在命令行直接输入main.exe命令运行），不需任何处理。

```bash
$ ./main
hello, 世界
```

Go语言的代码通过**包**（package）组织，包类似于其它语言里的库（libraries）或者模块（modules）。一个包由位于单个目录下的一个或多个.go源代码文件组成, 目录定义包的作用。每个源文件都以一条`package`声明语句开始，这个例子里就是`package main`, 表示该文件属于哪个包，紧跟着一系列导入（import）的包，之后是存储在这个文件里的程序语句。

Go的标准库提供了100多个包，以支持常见功能，如输入、输出、排序以及文本处理。比如`fmt`包，就含有格式化输出、接收输入的函数。`Println`是其中一个基础函数，可以打印以空格间隔的一个或多个值，并在最后添加一个换行符，从而输出一整行。

`main`包比较特殊。它定义了一个独立可执行的程序，而不是一个库。在`main`里的`main` *函数* 也很特殊，它是整个程序执行时的入口（译注：C系语言差不多都这样）。`main`函数所做的事情就是程序做的。当然了，`main`函数一般调用其它包里的函数完成很多工作, 比如, `fmt.Println`。

必须告诉编译器源文件需要哪些包，这就是跟随在`package`声明后面的`import`声明扮演的角色。hello world例子只用到了一个包，大多数程序需要导入多个包。

必须恰当导入需要的包，缺少了必要的包或者导入了不需要的包，程序都无法编译通过。这项严格要求避免了程序开发过程中引入未使用的包（译注：Go语言编译过程没有警告信息，争议特性之一）。

`import`声明必须跟在文件的`package`声明之后。随后，则是组成程序的函数、变量、常量、类型的声明语句（分别由关键字`func`, `var`, `const`, `type`定义）。这些内容的声明顺序并不重要（译注：最好还是定一下规范）。这个例子的程序已经尽可能短了，只声明了一个函数, 其中只调用了一个其他函数。为了节省篇幅，有些时候, 示例程序会省略`package`和`import`声明，但是，这些声明在源代码里有，并且必须得有才能编译。

一个函数的声明由`func`关键字、函数名、参数列表、返回值列表（这个例子里的`main`函数参数列表和返回值都是空的）以及包含在大括号里的函数体组成。第五章进一步考察函数。

Go语言不需要在语句或者声明的末尾添加分号，除非一行上有多条语句。实际上，编译器会主动把特定符号后的换行符转换为分号, 因此换行符添加的位置会影响Go代码的正确解析（译注：比如行末是标识符、整数、浮点数、虚数、字符或字符串文字、关键字`break`、`continue`、`fallthrough`或`return`中的一个、运算符和分隔符`++`、`--`、`)`、`]`或`}`中的一个）。举个例子, 函数的左括号`{`必须和`func`函数声明在同一行上, 且位于末尾，不能独占一行，而在表达式`x + y`中，可在`+`后换行，不能在`+`前换行（译注：以+结尾的话不会被插入分号分隔符，但是以x结尾的话则会被分号分隔符，从而导致编译错误）。

Go语言在代码格式上采取了很强硬的态度。`gofmt`工具把代码格式化为标准格式（译注：这个格式化工具没有任何可以调整代码格式的参数，Go语言就是这么任性），并且`go`工具中的`fmt`子命令会对指定包, 否则默认为当前目录, 中所有.go源文件应用`gofmt`命令。本书中的所有代码都被gofmt过。你也应该养成格式化自己的代码的习惯。以法令方式规定标准的代码格式可以避免无尽的无意义的琐碎争执（译注：也导致了Go语言的TIOBE排名较低，因为缺少撕逼的话题）。更重要的是，这样可以做多种自动源码转换，如果放任Go语言代码格式，这些转换就不大可能了。

很多文本编辑器都可以配置为保存文件时自动执行`gofmt`，这样你的源代码总会被恰当地格式化。还有个相关的工具，`goimports`，可以根据代码需要, 自动地添加或删除`import`声明。这个工具并没有包含在标准的分发包中，可以用下面的命令安装：

```
$ go get golang.org/x/tools/cmd/goimports
```

对于大多数用户来说，下载、编译包、运行测试用例、察看Go语言的文档等等常用功能都可以用go的工具完成。

由于我使用的是Goland，可以在Tool里配置gofmt和goimports。

## 命令行参数

通常情况下，输入来自于程序外部：文件、网络连接、其它程序的输出、敲键盘的用户、命令行参数或其它类似输入源。下面几个例子会讨论其中几个输入源，首先是命令行参数。

`os`包以跨平台的方式，提供了一些与操作系统交互的函数和变量。程序的命令行参数可从os包的Args变量获取；os包外部使用os.Args访问该变量。

os.Args变量是一个字符串（string）的*切片*（slice）（译注：slice和Python语言中的切片类似，是一个简版的动态数组），切片是Go语言的基础概念，稍后详细介绍。现在先把切片s当作数组元素序列, 序列的长度动态变化, 用`s[i]`访问单个元素，用`s[m:n]`获取子序列(译注：和python里的语法差不多)。序列的元素数目为len(s)。和大多数编程语言类似，区间索引时，Go言里也采用左闭右开形式, 即，区间包括第一个索引元素，不包括最后一个, 因为这样可以简化逻辑。（译注：比如a = [1, 2, 3, 4, 5], a[0:3] = [1, 2, 3]，不包含最后一个元素）。比如s[m:n]这个切片，0 ≤ m ≤ n ≤ len(s)，包含n-m个元素。

os.Args的第一个元素，os.Args[0], 是命令本身的名字；其它的元素则是程序启动时传给它的参数。s[m:n]形式的切片表达式，产生从第m个元素到第n-1个元素的切片，下个例子用到的元素包含在os.Args[1:len(os.Args)]切片中。如果省略切片表达式的m或n，会默认传入0或len(s)，因此前面的切片可以简写成os.Args[1:]。

下面是Unix里echo命令的一份实现，echo把它的命令行参数打印成一行。程序导入了两个包，用括号把它们括起来写成列表形式, 而没有分开写成独立的`import`声明。两种形式都合法，列表形式习惯上用得多。包导入顺序并不重要；gofmt工具格式化时按照字母顺序对包名排序。

```go
// echo1.go
// 一个模拟echo的程序
package main

import (
	"fmt"
	"os"
)

func main() {
	var s, sep string
	for i := 1; i < len(os.Args); i++ {
		s += sep + os.Args[i] // 在第一次执行时，sep为 zero
		sep = " "
	}
	fmt.Println(s)
}
```

可以得到下列结果：

```bash
$ go run echo1.go love flora
love flora
```

var声明定义了两个string类型的变量s和sep。变量会在声明时直接初始化。如果变量没有显式初始化，则被隐式地赋予其类型的*零值*（zero value），数值类型是0，字符串类型是空字符串""。这个例子里，声明把s和sep隐式地初始化成空字符串。第2章再来详细地讲解变量和声明。

对数值类型，Go语言提供了常规的数值和逻辑运算符。

第一次迭代之后，还会再插入一个空格，因此循环结束时每个参数中间都有一个空格。这是一种二次加工（quadratic process），当参数数量庞大时，开销很大，但是对于echo，这种情形不大可能出现。

Go语言只有for循环这一种循环语句。for循环有多种形式，其中一种如下所示：

```go
for initialization; condition; post {
    // zero or more statements
}
```

for循环三个部分不需括号包围。大括号强制要求, 左大括号必须和*post*语句在同一行。

*initialization*语句是可选的，在循环开始前执行。*initalization*如果存在，必须是一条*简单语句*（simple statement），即，短变量声明、自增语句、赋值语句或函数调用。`condition`是一个布尔表达式（boolean expression），其值在每次循环迭代开始时计算。如果为`true`则执行循环体语句。`post`语句在循环体执行结束后执行，之后再次对`condition`求值。`condition`值为`false`时，循环结束。

for循环的这三个部分每个都可以省略，如果省略`initialization`和`post`，分号也可以省略：

```go
// a traditional "while" loop
for condition {
    // ...
}
```

如果连`condition`也省略了，像下面这样：

```go
// a traditional infinite loop
for {
    // ...
}
```

这就变成一个无限循环，尽管如此，还可以用其他方式终止循环, 如一条`break`或`return`语句。

`for`循环的另一种形式, 在某种数据类型的区间（range）上遍历，如字符串或切片，如下：

```go
// echo2.go
package main

import (
	"fmt"
	"os"
)

func main() {
	s, sep := "", ""
	for _, arg := range os.Args[1:] {	// 原本_所得到的为对应的idx
		s += sep + arg
		sep = " "
	}
	fmt.Println(s)
}

```

每次循环迭代，`range`产生一对值；索引以及在该索引处的元素值。这个例子不需要索引，但`range`的语法要求, 要处理元素, 必须处理索引。一种思路是把索引赋值给一个临时变量, 如`temp`, 然后忽略它的值，但Go语言不允许使用无用的局部变量（local variables），因为这会导致编译错误。

Go语言中这种情况的解决方法是用`空标识符`（blank identifier），即`_`（也就是下划线）。空标识符可用于**任何语法需要变量名但程序逻辑不需要**的时候, 例如, 在循环里，丢弃不需要的循环索引, 保留元素值。大多数的Go程序员都会像上面这样使用`range`和`_`写`echo`程序，因为隐式地而非显式地索引os.Args，容易写对。

`echo`的这个版本使用一条短变量声明来声明并初始化`s`和`seps`，也可以将这两个变量分开声明，声明一个变量有好几种方式，下面这些都等价：

```go
s := ""
var s string
var s = ""
var s string = ""
```

用哪种不用哪种，为什么呢？第一种形式，是一条短变量声明，最**简洁**，但只能用在函数内部，而不能用于包变量。第二种形式依赖于字符串的默认初始化零值机制，被初始化为""。第三种形式用得很少，除非同时声明多个变量。第四种形式显式地标明变量的类型，当变量类型与初值类型相同时，类型冗余，但如果两者类型不同，变量类型就必须了。实践中一般使用前两种形式中的某个，**初始值重要的话就显式地指定变量的类型**，否则使用隐式初始化。

如果连接涉及的数据量很大，这种方式代价高昂。一种简单且高效的解决方案是使用`strings`包的`Join`函数：

```go
// echo3.go
func main() {
    fmt.Println(strings.Join(os.Args[1:], " "))
}
```

**练习 1.1：** 修改`echo`程序，使其能够打印`os.Args[0]`，即被执行命令本身的名字。

```go
package main

import (
	"fmt"
	"os"
	"strings"
)

func main() {
	fmt.Println(strings.Join(os.Args[0:], " "))

	/* s, sep := "", ""
	for i := 0; i < len(os.Args); i++ {
		s += sep + os.Args[i]
		sep = " "
	}
	fmt.Println(s) */

	/* s, sep := "", ""
	for _, arg := range os.Args[0:] {
		s += sep + arg
		sep = " "
	}
	fmt.Println(s) */
}

```

**练习 1.2：** 修改`echo`程序，使其打印每个参数的索引和值，每个一行。

```go
package main

import (
	"fmt"
	"os"
)

func main() {
	for idx, arg := range os.Args[0:] {
		fmt.Println(idx, arg)
	}
}
```

**练习 1.3：** 做实验测量潜在低效的版本和使用了`strings.Join`的版本的运行时间差异。（1.6节讲解了部分`time`包，11.4节展示了如何写标准测试程序，以得到系统性的性能评测。）

```go
package main

import (
	"fmt"
	"os"
	"strings"
	"time"
)

func echo1_3_1() {
	start := time.Now()
	var s, sep string

	for i := 0; i < len(os.Args); i++ {
		s += sep + os.Args[i]
		sep = " "
	}
	fmt.Println(s)
	fmt.Printf("%.6fs \n", time.Since(start).Seconds())
}

func echo1_3_2() {
	start := time.Now()
	var s, sep string

	for _, arg := range os.Args {
		s += sep + arg
		sep = " "
	}
	fmt.Println(s)
	fmt.Printf("%.6fs \n", time.Since(start).Seconds())
}

func echo1_3_3() {
	start := time.Now()
	fmt.Println(strings.Join(os.Args[0:], " "))
	fmt.Printf("%.6fs \n", time.Since(start).Seconds())

}
```

## 查找重复的行

对文件做拷贝、打印、搜索、排序、统计或类似事情的程序都有一个差不多的程序结构：一个处理输入的循环，在每个元素上执行计算处理，在处理的同时或最后产生输出。我们会展示一个名为`dup`的程序的三个版本；灵感来自于Unix的`uniq`命令，其寻找相邻的重复行。该程序使用的结构和包是个参考范例，可以方便地修改。

现在，在 `main.go` 同一目录下新建

`dup`的第一个版本打印标准输入中多次出现的行，以重复次数开头。该程序将引入`if`语句，`map`数据类型以及`bufio`包。

*<u>dup1.go</u>*

```go
package main

import (
    "bufio"
    "fmt"
    "os"
)

func main() {
    counts := make(map[string]int)
    input := bufio.NewScanner(os.Stdin)
    for input.Scan() {
        counts[input.Text()]++
    }
    // NOTE: ignoring potential errors from input.Err()
    for line, n := range counts {
        if n > 1 {
            fmt.Printf("%d\t%s\n", n, line)
        }
    }
}
```

正如`for`循环一样，`if`语句条件两边也不加括号，但是主体部分需要加。`if`语句的`else`部分是可选的，在`if`的条件为`false`时执行。

**map**存储了键/值（key/value）的集合，对集合元素，提供常数时间的存、取或测试操作。键可以是任意类型，只要其值能用`==`运算符比较，最常见的例子是字符串；值则可以是任意类型。这个例子中的键是字符串，值是整数。内置函数`make`创建空`map`，此外，它还有别的作用。4.3节讨论`map`。

（译注：从功能和实现上说，`Go`的`map`类似于`Java`语言中的`HashMap`，Python语言中的`dict`，`Lua`语言中的`table`，通常使用`hash`实现。遗憾的是，对于该词的翻译并不统一，数学界术语为`映射`，而计算机界众说纷纭莫衷一是。为了防止对读者造成误解，保留不译。）

每次`dup`读取一行输入，该行被当做`map`，其对应的值递增。`counts[input.Text()]++`语句等价下面两句：

```go
line := input.Text()
counts[line] = counts[line] + 1
```

`map`中不含某个键时不用担心，首次读到新行时，等号右边的表达式`counts[line]`的值将被计算为其类型的零值，对于int`即0。

为了打印结果，我们使用了基于 `range` 的循环，并在 `counts` 这个 `map` 上迭代。跟之前类似，每次迭代得到两个结果，键和其在`map`中对应的值。`map`的迭代顺序并不确定，从实践来看，该顺序随机，每次运行都会变化。这种设计是有意为之的，因为能防止程序依赖特定遍历顺序，而这是无法保证的。

继续来看 `bufio` 包，它使处理输入和输出方便又高效。

`Scanner` 类型是该包最有⽤的特性之⼀，它读取输⼊并将其拆成行或单词；通常是处理⾏形式的输⼊最简单的⽅法。

程序使⽤短变量声明创建 `bufio.Scanner` 类型的变量 `input` 。

```go
input := bufio.NewScanner(os.Stdin)
```

该变量从程序的标准输入中读取内容，每次调用 `input.Scanner` ，即读入下一行，并移除行末的换行符，读取的内容可以用 `input.Text()` 得到。 `scan` 函数在读到一行时返回 `true` ，在无输入时返回 `false` 。 

对于格式化输出，`Printf` 里的转换在 `Go` 中被称为 verb 。

```
%d 			⼗进制整数
%x, %o, %b 	⼗六进制，⼋进制，⼆进制整数。
%f, %g, %e 	浮点数： 3.141593 3.141592653589793 3.141593e+00
%t 			布尔：true或false
%c 			字符（rune） (Unicode码点)
%s 			字符串
%q 			带双引号的字符串"abc"或带单引号的字符'c'
%v 			变量的⾃然形式（natural format）
%T 			变量的类型
%% 			字⾯上的百分号标志（⽆操作数）
```

dup的另一个版本，从文件中读取每行数据，计算重复行。

```go
func Dup2() {
	counts := make(map[string]int)
	files := os.Args[1]
	if len(files) == 0 {
		countLines(os.Stdin, counts)
	} else {
		for _, arg := range files {
			f, err := os.Open(arg)
			if err != nil {
				fmt.Fprintf(os.Stderr, "dup2: %v\n", err)
				continue
			}
			countLines(f, counts)
			f.Close()
		}
	}
	for line, n := range counts {
		if n > 1 {
			fmt.Printf("%d\t%s\n", n, line)
		}
	}
}

func countLines(f *os.File, counts map[string]int) {
	input := bufio.NewScanner(f)
	for input.Scan() {
		counts[input.Text()]++
	}
}
```

`countLines` 允许读入文件每行数据。

接着还有一个先整体读入为字符串，然后通过关键字 `\n` 来进行split的方法。

```go
func Dup3() {
	counts := make(map[string]int)
	files := os.Args[1:]
	for _, file := range files {
		data, err := os.ReadFile(file)
		if err != nil {
			fmt.Fprintf(os.Stderr, "dup3: %v\n", err)
			continue
		}

		// read and split it by "\n"
		for _, line := range strings.Split(string(data), "\n") {
			counts[line]++
		}
	}
	for line, n := range counts {
		if n > 1 {
			fmt.Printf("%d\t%s\n", n, line)
		}
	}
}
```

最后，完成练习1-4：修改 `dup1` ，当出现重复行的时候打印文件名称。

```go
func Dup_p1() {
	counts := make(map[string]int)
	files := os.Args[1:]
	if len(files) == 0 {
		countAndPrintFilename(os.Stdin, counts)
	} else {
		for _, arg := range files {
			f, err := os.Open(arg)
			if err != nil { // catch exception
				fmt.Fprintf(os.Stderr, "dup2: %v\n", err)
				continue
			}
			countAndPrintFilename(f, counts)
			f.Close()
		}
	}
}

func countAndPrintFilename(f *os.File, counts map[string]int) {
	// read every lines
	input := bufio.NewScanner(f)
	for input.Scan() {
		counts[input.Text()]++
		if counts[input.Text()] > 1 {
			fmt.Println(f.Name())
		}
	}
}
```

## GIF动画

有问题，跳过。

## 获取URL

为了最简单地展示基于HTTP获取信息的方式，下面给出一个示例程序 `fetch` ，该程序将获取对应的 url， 并将其源文本打印出来。其灵感来源于 curl 。

```go
package url

import (
	"fmt"
	"io"
	"net/http"
	"os"
)

func Url1() {
	// get url by Command-Line arguments
	for _, url := range os.Args[1:] {
		res, err := http.Get(url)
		if err != nil {
			fmt.Fprintf(os.Stderr, "fetch: %v\n", err)
			os.Exit(1)
		}
		b, err := io.ReadAll(res.Body)
		res.Body.Close()
		if err != nil {
			fmt.Fprintf(os.Stderr, "fetch: reading %s: %v\n", url, err)
			os.Exit(1)
		}
		fmt.Printf("%s", b)
	}
}
```

`io.ReadAll()` 从 res 中读取全部内容，而 `res.Body` 字段包括一个可读的服务器响应流。

执行：

```
$ go build main.go
$ .\main.exe https://www.baidu.com
<!--STATUS OK--><html> <head><meta http-equiv=content-type content=text/html;charset=utf-8><meta http-equiv=X-UA-Compatible content=IE=Edge><meta content=always name=referrer><link rel=stylesheet type=text/css href=https://ss1.bdstatic.com/5eN1bjq8AAUYm2zgoY3K/r/www/cache/bdorz/baidu.min.css><title>百度一下，你就知道</title></head>
...
```

现在开始练习：

*Exercise 1.7*

```go
func Url_p1() {
	for _, url := range os.Args[1:] {
		res, err := http.Get(url)
		if err != nil {
			fmt.Fprintf(os.Stderr, "fetch (exercise): %v\n", err)
			os.Exit(1)
		}
		_, err = io.Copy(os.Stdout, res.Body)
		res.Body.Close()
		if err != nil {
			fmt.Fprintf(os.Stderr, "fetch (exercise) %s: %v\n", url, err)
			os.Exit(1)
		}
	}
}
```

*Exercise 1.8*

```go
func Url_p2() {
	for _, url := range os.Args[1:] {
		if !strings.HasPrefix(url, "http://") {
			url = "http://" + url
		}
		res, err := http.Get(url)
		if err != nil {
			fmt.Fprintf(os.Stderr, "fetch (exercise): %v\n", err)
			os.Exit(1)
		}
		_, err = io.Copy(os.Stdout, res.Body)
		res.Body.Close()
		if err != nil {
			fmt.Fprintf(os.Stderr, "fetch (exercise) %s: %v\n", url, err)
			os.Exit(1)
		}
	}
}
```

*Exercise 1.9*

```go
func Url_p3() {
	for _, url := range os.Args[1:] {
		if !strings.HasPrefix(url, "http://") {
			url = "http://" + url
		}
		res, err := http.Get(url)
		if err != nil {
			fmt.Fprintf(os.Stderr, "fetch (exercise): %v\n", err)
			os.Exit(1)
		}
		_, err = io.Copy(os.Stdout, res.Body)
		res.Body.Close()
		if err != nil {
			fmt.Fprintf(os.Stderr, "fetch (exercise) %s: %v", url, err)
			os.Exit(1)
		}
		_, err = io.WriteString(os.Stdout, res.Status)
		if err != nil {
			fmt.Fprintf(os.Stderr, "fetch (exercise) %s: %v", url, err)
			os.Exit(1)
		}
	}
}
```

## 并发获取URL

```go
package urls

import (
	"fmt"
	"io"
	"net/http"
	"os"
	"time"
)

func Urls1() {
	start := time.Now()
	ch := make(chan string)
	for _, url := range os.Args[1:] {
		go fetch(url, ch) // start a goroutine
	}

	for range os.Args[1:] {		// print channel data
		fmt.Println(<-ch)
	}

	fmt.Printf("%.2fs elapsed\n", time.Since(start).Seconds())
}

func fetch(url string, ch chan<- string) {
	start := time.Now()
	res, err := http.Get(url)
	if err != nil {
		ch <- fmt.Sprint(err) // send to channel ch
		return
	}

	nbytes, err := io.Copy(io.Discard, res.Body)
	res.Body.Close() // do not leak resources
	if err != nil {
		ch <- fmt.Sprintf("while reading %s: %v", url, err)
		return
	}

	secs := time.Since(start).Seconds()
	ch <- fmt.Sprintf("%.2fs  %7d  %s", secs, nbytes, url)
} 
```

`goroutine` 是一种函数的并发执行方式，而 `channel` 用来在 `goroutine` 之间进行参数传递。

`Urls1` 函数本身也运行在一个`goroutine` 中，而 `go` 则表示创建一个新的 `goroutine` 。

`Urls1` 函数中用 `make` 函数创建了一个传递 `string` 类型参数的 `channel` ，对每个命令行参数，我们都用 `go` 关键字来创建一个 `goroutine` ，并且让函数在这个 `goroutine` 中一步执行 `http.Get()` 方法。这个程序的`io.Copy` 会把响应的 `Body` 内容拷贝到 `io.Discard` 输出流中。可以把这个变量看作一个垃圾桶，向里面写一些不想要的内容。每当请求返回内容时，`fetch` 函数会往 `ch` 这个 `channel` 里 写入一个字符串，由 `Urls1` 函数力度的第二个 `for` 循环来处理并打印出来。

当一个 `goroutine` 尝试在一个 `channel` 上做 `send` 或者 `receive` 操作时，这个 `goroutine` 会阻塞在调用处，直到另一个 `goroutine` 往这个 `channel` 里写入或接受数据时，这两个 `goroutine` 才会继续执行 `channel` 操作之后的逻辑。在这个例子中，每一个 `fetch` 函数在执行时都会往 `channel` 里发送一个值 (`ch <- expression`)。这个程序中，我们用 `Urls1` 函数来接收所有 `fetch` 函数传回的字符串，可以避免在 `goroutine` 异步执行还没有完成时，`Urls1` 就提前退出。

*Exercise 1.10*

```go
package urls

import (
	"fmt"
	"io"
	"net/http"
	"os"
	"time"
)

func UrlsExercise_p1() {
	start := time.Now()
	ch := make(chan string)
	for _, url := range os.Args[1:] {
		go fetchExercise(url, ch) // start a goroutine
	}

	for range os.Args[1:] {
		fmt.Println(<-ch)
	}

	fmt.Printf("%.2fs elapsed\n", time.Since(start).Seconds())
}

func fetchExercise(url string, ch chan<- string) {
	start := time.Now()
	res, err := http.Get(url)
	if err != nil {
		ch <- fmt.Sprint(err) // send to channel ch
		return
	}

	_, err = io.Copy(os.Stdout, res.Body)
	res.Body.Close() // do not leak resources
	if err != nil {
		ch <- fmt.Sprintf("while reading %s: %v", url, err)
		return
	}

	secs := time.Since(start).Seconds()
	ch <- fmt.Sprintf("%.2fs %s", secs, url)
}

```

## Web服务

```go
// web_server.go
package server

import (
	"fmt"
	"log"
	"net/http"
)

func WebServer1() {
	http.HandleFunc("/", handler)
	log.Fatal(http.ListenAndServe("localhost:8000", nil))
}

func handler(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintf(w, "URL.Path = %q\n", r.URL.Path)
}
```

我们只用了几行代码就实现了一个Web服务程序。 `WebServer1()` 函数将所有发送到 `/` 路径下的请求和 `handler` 函数关联起来，`/` 开头的请求其实就是所有发送到当前站点的请求，服务监听 `8000` 端口。发到这个服务的“请求”是一个 `http.Request` 类型的对象，其内部包含了一系列相关字段，包括我们需要的URL。

当请求到达服务器时，这个请求会被传入 `handler` 进行处理，这个函数会将 `/hello` 这个路径从请求的UL中解析出来，然后发到响应中。

现在执行以下命令，在 Windos 下跑起web服务：

```bash
$ go run ..../main.go # use & to run in the background.
```

访问 `localhost:8000` 就可以看到 `URL.Path = "/"` ，也可以用之前写的 fetch 程序去获取。

在这个服务的基础上叠加特性是很容易的，一种实用的修改是为访问的url添加某种状态，比如，对请求的次数进行统计、对URL的请求结果会包含各种URL被访问的总次数，直接对`/count` 的访问除外。

```go
package server

import (
	"fmt"
	"log"
	"net/http"
	"sync"
)

var mu sync.Mutex
var count int

func WebServer2() {
	http.HandleFunc("/", handlerRoot)
	http.HandleFunc("/count", counter)
	log.Fatal(http.ListenAndServe("localhost:8000", nil))
}

func handlerRoot(w http.ResponseWriter, r *http.Request) {
	mu.Lock()
	count++
	mu.Unlock()
	fmt.Fprintf(w, "URL.Path = %q\n", r.URL.Path)
}

func counter(w http.ResponseWriter, r *http.Request) {
	mu.Lock()
	fmt.Fprintf(w, "Count %d\n", count)
	mu.Unlock()
}
```

这个服务器有两个请求处理函数，根据请求的url不同会调用不同的函数：

- 对 `/count` 这个 url 的请求，会调用到 `counter()` 函数。
- 如果请求 `pattern` 是以 `/` 结尾，那么所有以该 url 为前缀的 url 都会被匹配默认的处理函数 `handlerRoot()`。

服务器每次接收到请求处理时都会另起一个 `goroutine` ，这样服务器就可以在同一个时间处理多个请求。然而在并发的情况下，加入真的有两个请求同一时刻去更新 `count`  ，那么这个值可能不会被正确地增加，从而引发“条件竞争”的问题。

为了保证每次修改变量的时候最多只能有一个 `goroutine` ，使用 `mu.Lock()` 和 `mu.Unlock()` 函数来将修改 `count` 的所有行为包含在中间。

下面用一个更加鲜明的例子，处理函数将打印请求的http头和请求的form数据。

```go
package server

import (
	"fmt"
	"log"
	"net/http"
)

func WebServer3() {
	http.HandleFunc("/", handleRoot)
	log.Fatal(http.ListenAndServe("localhost:8000", nil))
}

func handleRoot(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintf(w, "%s %s %s\n", r.Method, r.URL, r.Proto)
	for k, v := range r.Header {
		fmt.Fprintf(w, "Header[%q] = %q\n", k, v)
	}

	fmt.Fprintf(w, "Host = %q\n", r.Host)
	fmt.Fprintf(w, "RemoteAddr = %q\n", r.RemoteAddr)
	if err := r.ParseForm(); err != nil {
		log.Print(err)
	}
	for k, v := range r.Form {
		fmt.Fprintf(w, "Form[%q] = %q\n", k, v)
	}
}
```

可以得到下面的结果：

![](./Gopl-02-introduction\print_head.jpg)

可以看到，变量定义和条件判断北方到了一起，这样结合可以限制 `err` 这个变量的作用域。

```go
if err := r.ParseForm(); err != nil {
		log.Print(err)
	}
```

在这些程序中，有很多不同类型被输出到了标准输出流中：

- 在 fetch URL 时，我们把 HTTP 相应数据拷贝到了 `os.Stdout` ；
- 在 gif 章节，我们输出了一个文件。；
- 在 fetchall 的时候则完全忽略了 `http.Body` ，转而只计算 `Body` 的大小， 把 `Body` 拷贝到了 `io.Discard` 中；
- 在 server 中，我们使用 `fmt.Fprintf` 直接写到了 `http.ResponseWriter` 中；

尽管这几种实现流程不一样，但是都实现了一个共同的借口—— `io.Writer` 。

现在我们实现在web页面显示1.4的动画：

```go
package server

import (
	gif "gopl/ch1/1.4_gif"
	"log"
	"net/http"
)

func WebServer4() {
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		gif.Lissajous(w)
	})
	log.Fatal(http.ListenAndServe("localhost:8000", nil))
}

```

![](./Gopl-02-introduction\lissajous.jpg)

*exercise 1.12*

```

```

