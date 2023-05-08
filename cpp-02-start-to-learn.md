---
title: cpp_02_start_to_learn
top: false
comment: false
lang: zh-CN
date: 2022-06-18 12:56:27
tags:
categories:
  - program language
  - cpp
---

# Ch2 开始学习C++

## 进入C++

第一个程序

```cpp
// myfirst.cpp--displays a message

#include <iostream>                           // a PREPROCESSOR directive
int main()                                    // function header
{                                             // start of function body
    using namespace std;                      // make definitions visible
    cout << "Come up and C++ me some time.";  // message
    cout << endl;                             // start a new line
    cout << "You won't regret it!" << endl;   // more output
// If the output window closes before you can read it,
// add the following code:
    // cout << "Press any key to continue." <<endl;
	// cin.get();                                                   
    return 0;                                 // terminate main()
}  
```

----

C++使用**预处理器**(preprocessor)，在程序进行主编译之前对源文件进行处理，它处理名称以 `#` 开头的编译指令。 `#include <iostream>` 会导致预处理器将 `iostream` 文件的内容添加到程序中。 `iostream` 中的 `io` 指的是 `input` 和 `output` ，使用 `cin` 和 `cout` 时必须包含 `iostream` 。像 `iostream` 这样的文件叫**包含文件**(include file)，也叫**头文件**(header file)。

-----

使用 `using namespace std;` 名称空间编译指令来使得 `iostream` 中的定义对程序可用，这叫做 `using` 编译指令。这里简单介绍，详细看第9章。如果只想让所需的名称可用，可以这样声明：

```c++
using std:cout;
using std:cin;
using std:endl;
```

----

使用 `cout << "message" << endl` 来显示消息。`<<` 符号把字符串发送给 `cout` ，其指出了信息流动的路径。从概念上看，输出是一个流， `cout` 对象表示这种流，其属性在 `iostream` 中定义。属性中包括一个插入运算符 `<<` ，它把右侧的信息插入到了流中。对于 `cin` 来说也一样，信息从 `cin` 流到了变量。

`cout` 对象的属性 `<<` 是一个重载运算符的例子。通过重载，同一个运算将有不同的含义，在标准的定义中， `<<` 表示按位左移运算符。编译器通过上下文来确定运算符的含义。

`endl` 是一个特殊的 `c++` 符号，和 `cout` 一样，它在 `iostream` 中定义，并位于名称空间 `std` 中。将其插入输出流时，将导致屏幕光标移动到下一行开头。这类符号被称为 <u>控制符(manipulator)</u> 。另一种输出换行的旧式方法：C语言符号 `\n` 。

## C++语句

要将信息存储在计算机中，必须指出信息的<u>存储位置</u>和所需的<u>内存空间</u>，C++使用了**声明语句**来指出<u>存储类型</u>并且提供<u>位置标签</u>。声明语句叫作定义声明(defining declaration) ，简称为定义(definition) ，这意味着编译器将为变量分配内存空间。

**赋值语句**将值赋值给存储单元，赋值从右向左进行。例如：

```cpp
int a, b, c;
a = b = c = 6;
```

首先，6 会被赋值给 c ，然后 c 的值被赋值给 b， 最后 b 的值被赋值给 a。

## 函数

被调用的函数叫作 called function， 包含函数调用的函数叫作 calling function。

在有些语言中，有返回值的函数被称为函数 function ，没有返回值的函数被称为过程 procedure 。 

在多函数程序中使用 `using` 编译指令，可以避免名称空间的混乱：

```cpp
// ourfunc.cpp -- defining your own function
#include <iostream>
void simon(int);    // function prototype for simon()

int main()
{
    using namespace std;
    simon(3);       // call the simon() function
    cout << "Pick an integer: ";
    int count;
    cin >> count;
    simon(count);   // call it again
    cout << "Done!" << endl;
	// cin.get();
    // cin.get();
    return 0;
}

void simon(int n)   // define the simon() function
{
    using namespace std;

    cout << "Simon says touch your toes " << n << " times." << endl;
}                   // void functions don't need return statements

```

## 总结

C++程序由一个或多个函数组成。程序从 `main()` 函数开始执行。

C++语句的类型有：

- 声明语句：定义函数中使用的变量的名称和类型；
- 赋值语句：使用赋值运算符 `=` 给变量赋值；
- 消息语句：将消息发送给对象；
- 函数调用：执行函数，called function执行完毕后，程序返回到函数调用语句后面的语句；
- 函数原型：声明函数的返回类型、函数接受的参数数量和类型；
- 返回语句：将一个值从 called function 那里返回到调用函数中；

类是用户定义的数据类型规范，描述了如何表示信息以及可对数据执行的操作。对象是根据类规范创建的实体。

C++提供了两个用于处理输入输出的预定义对象（ `cin` 和 `cout` ），它们是类 `istream` 和 类`ostream` 的实例，这两个类在 `iostream` 文件中定义。`ostream`  类将运算符 `<<` 重载为（重新定义为）插入运算符，使得数据插入输出流。 `istream` 类将运算符 `>>` 重载为抽取运算符，能够从输入流中抽取信息。

## 编程练习

```cpp
#include <iostream>
using namespace std;

void q1()
{
    cout << "K1ose"
         << " "
         << "Earth" << endl;
}

void q2()
{
    int input;
    cout << "long: ";
    cin >> input;
    cout << input << " long = " << 220 * input << "yard(s)" << endl;
}

void q3_1()
{
    cout << "Three blind mice" << endl;
}

void q3_2()
{
    cout << "See how they run" << endl;
}

void q4()
{
    cout << "Enter your age: ";
    int age;
    cin >> age;
    cout << age * 12 << endl;
}

double q5()
{
    cout << "Please enter a Celsius value: ";
    int celsius;
    cin >> celsius;
    double fahrenheit = celsius * 1.8 + 32;
    cout << celsius << "degree(s) Celsius is " << fahrenheit << " degree(s) Fahrenheit" << endl;
    return fahrenheit;
}

double q6()
{
    cout << "Enter the number of light years: ";
    int light_year;
    cin >> light_year;
    double astronomical_unit;
    astronomical_unit = 63240 * light_year;
    cout << light_year << " light year(s) = " << astronomical_unit << " astronomical unit(s)." << endl;
    return astronomical_unit;
}

void q7(int hour, int minute)
{
    /* main() */
    // int h, m;
    // cout << "Enter the number of hours: ";
    // cin >> h;
    // cout << "Enter the number of minutes: ";
    // cin >> m;
    // q7(h, m);
    cout << "Time: " << hour << ":" << minute << endl;
}

int main(int argc, char const *argv[])
{

    return 0;
}

```
