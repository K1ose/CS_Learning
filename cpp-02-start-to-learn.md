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

# 02 start to learn

## 2.1 进入C++

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

使用 `cout << "message" << endl` 来显示消息。`<<` 符号把字符串发送给 `cout` ，其指出了信息流动的路径。 `cout` 是一个预定义的对象。从概念上看，输出是一个流，













