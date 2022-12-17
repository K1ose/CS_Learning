---
title: Qt5.9_cpp_programming_guide_02_GUI_Base
top: false
comment: false
lang: zh-CN
date: 2022-11-01 07:59:39
tags:
categories:
  - study
  - book
  - Qt5.9 c++ 开发指南

---

# GUI应用程序设计基础

Qt Creator 设计 GUI 应用程序的基本方法：

- Qt创建的应用程序项目和基本组织结构
- 可视化设计的UI界面文件的原理和运行机制
- 信号与槽的使用方法
- 窗体可视化设计的底层原理
- 应用程序的窗体、组件布局、菜单、工具栏、Actions等的使用

## UI文件设计与运行机制

在Qt Creator中新建一个 Widget Application 项目 ，基类中选择 QWidget 作为窗体基类，选中 Generate form 。

### 项目文件

`samp2_1.pro` 文件内容如下：

```
QT       += core gui

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

CONFIG += c++11

DEFINES += QT_DEPRECATED_WARNINGS

SOURCES += \
    main.cpp \
    widget.cpp

HEADERS += \
    widget.h

FORMS += \
    widget.ui

qnx: target.path = /tmp/$${TARGET}/bin
else: unix:!android: target.path = /opt/$${TARGET}/bin
!isEmpty(target.path): INSTALLS += target

```

项目管理文件用于记录项目的一些设置，以及项目包含文件的组织管理。



`QT       += core gui` 表示项目中加入了 core gui 模块，core gui 是 Qt 用于 GUI 设计的类库模块，如果创建的是控制台应用程序，就不需要添加 core gui 。

Qt类库以模块的形式组织各种功能的类，根据功能需求可以适当添加类库模块支持，例如，如果用到了数据库操作，则需要用到 sql 模块，在 pro 文件中需要加入 `QT       += sql` 。



`greaterThan(QT_MAJOR_VERSION, 4): QT += widgets` 这是一个条件执行语句，表示当Qt主版本大于 4 时，才加入 widgets 模块。 



除此外，加入 `TARGET = samp2_1` 表示生成的目标可执行文件的名称， `TEMPLATE = app` 表示项目使用的模板是 app，是一般的应用程序。

### 界面文件

- 组件面板：位于左侧，设计的常见组件都能在其中找到；

- 设计窗口：位于中间，拖放即可；

- Signals 和 Slots编辑器 与 Action 编辑器 位于下方，用于可视化进行信号与槽的关联，以及可视化设计Action；

- 布局和界面设计工具栏：上方工具栏；

- 对象浏览器：右上方 Object Inspector，用树状视图显示窗体上各组件之间的布局包含关系，视图有两列，显示每个组件的对象名称和类名称；

- 属性编辑器：右下方 Property Editor，显示某个选中组件或窗体的各种属性和取值；

  ![](./Qt5-9-cpp-programming-guide-02-GUI-Base\figure_01.jpg)

  `label : QLabel` 表示这个组件是一个QLabel类的组件；

  QLabel 的继承关系为 QObject -> QWidge -> QFrame -> QLabel ；



放置一个 Push Button 组件，并为按钮增加一个功能：单击此按钮，关闭窗口退出程序。在信号与槽编辑器的工具栏上点击 Add 按钮，在出现条目中，Sender 选择按钮的 ObjectName ， Signal 选择 clicked() ， Receiver 选择窗体 Widget，Slot 选择 close()，这样设置后：当按钮被点击时，就执行 Widget 的 close() 函数，关闭窗口。

![](./Qt5-9-cpp-programming-guide-02-GUI-Base\figure_02.jpg)

### 主函数文件

```cpp
#include "widget.h"

#include <QApplication>

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);	// 定义并创建应用程序
    Widget w;					// 定义并创建窗口
    w.show();					// 窗口显示
    return a.exec();			// 应用程序运行
}
```

### 窗体相关文件

项目编译后，会生成ui_widget.h文件，因此共有四个文件：

- widget.h - 定义窗体类的头文件，定义了类 Widget
- widget.cpp - Widget类的功能实现源程序文件
- widget.ui - 窗体界面文件，由UI设计器自动生成，存储了窗体上各个组件的属性设置和布局
- ui_widget.h - 编译后，根据窗体上的组件、信号和槽的关联等自动生成一个类的定义文件

#### widget.h

```cpp
#ifndef WIDGET_H
#define WIDGET_H

#include <QWidget>

QT_BEGIN_NAMESPACE
namespace Ui { class Widget; }	// 一个命名空间Ui， 包括一个类Widget
QT_END_NAMESPACE

class Widget : public QWidget
{
    Q_OBJECT

public:
    Widget(QWidget *parent = nullptr);
    ~Widget();

private:
    Ui::Widget *ui;		// 使用Ui::Widget 定义了指针 *ui
};
#endif // WIDGET_H

```

创建项目时，选择窗体基类为 QWidget ，因此在其中定义了一个继承自 QWidget 的类 Widget。

- namespace 声明

  `namespace Ui{	class Widget;	}`

  这里声明了一个名称为 Ui 的命名空间，其中包含一个类 Widget，这个类并不是本文件内定义的类，而是 ui_widget.h 中定义的类，用于描述界面组件。这个声明相当于一个外部类型声明。

- Widget 类的定义

  widget.h 中定义了一个继承于 QWidget 的类 Widget，是本实例的窗体类。

  在类中使用了宏 Q_OBJECT，这是使用Qt signal和slot机制的类必须加入的一个宏。

  在 public 部分定义了类的构造函数和析构函数

  在 private 部分定义了一个指针 `Ui::Widget *ui` ，这个指针式用前面声明的 namespace Ui 里的 Widget 类定义的，所以指针ui式只想可视化界面的，通过该指针访问界面上的组件。

#### widget.cpp

```cpp
#include "widget.h"
#include "ui_widget.h"

Widget::Widget(QWidget *parent)
    : QWidget(parent)
    , ui(new Ui::Widget)
{
    ui->setupUi(this);
}

Widget::~Widget()
{
    delete ui;
}

```

在 widget.cpp 中，目前只有构造函数和析构函数。

其中构造函数为：

```cpp
Widget::Widget(QWidget *parent)
    : QWidget(parent)
    , ui(new Ui::Widget)
{
    ui->setupUi(this);
}
```

执行父类 `QWidget` 的构造函数，创建一个 `Ui::Widget` 类的对象 `ui`。这个 `ui` 就是 `Widget` 的 `private` 部分所定义的指针变量 `ui` 。而后，执行 `Ui::Widget` 类中的 `setupUi()` 函数，这个函数实现窗口的生成和各种属性的设置，包括信号与槽的关联。

其中析构函数只是简单地删除了用 `new` 创建的指针 `ui` 。

#### widget.ui

窗口界面定义文件，是一个XML文件，定义了窗口上所有组件的属性设置、布局，及其信号与槽函数的关联。

#### ui_widget.h

实在对 widget.ui 文件编译后生成的一个文件，并不会出现在 Qt Creator 项目文件目录树中，但可以手工添加。 [右键项目名称节点] - [Add Existing Files ...] ，添加即可。

`ui_widget.h` 文件主要做了以下的工作：

- 定义了一个类 `Ui_Widget` ，用于封装可视化设计的界面。

- 自动生成界面各个组件的类成员变量定义。例如，在可视化界面添加了按钮组件，并把它的 `objectName` 设置为 `btnClose` ，那么生成的定义就是：

  ```
  QPushButton *btnClose;
  ```

- 定义了函数`setupUi()`用于创建各个界面组件，并设置其位置、大小、文字内容等；

  ```cpp
  void setupUi(QWidget *Widget)
      {
          if (Widget->objectName().isEmpty())
              Widget->setObjectName(QString::fromUtf8("Widget"));
          Widget->resize(800, 600);
          QFont font;
          font.setPointSize(20);
          Widget->setFont(font);
          label = new QLabel(Widget);
          label->setObjectName(QString::fromUtf8("label"));
          label->setGeometry(QRect(290, 250, 281, 61));
          pushButton = new QPushButton(Widget);
          pushButton->setObjectName(QString::fromUtf8("pushButton"));
          pushButton->setGeometry(QRect(290, 430, 181, 41));
  
          retranslateUi(Widget);
          QObject::connect(pushButton, SIGNAL(clicked()), Widget, SLOT(close()));
  
          QMetaObject::connectSlotsByName(Widget);
      } // setupUi
  ```

  - 根据可视化设计的界面内容，以代码形式创建界面上的各组件，并设置属性；

  - 调用函数`retranslateUi(Widget)`，设置各组件的文字内容。

  - 设置信号和槽的关联：

    ```cpp
    QObject::connect(pushButton, SIGNAL(clicked()), Widget, SLOT(close()));
    QMetaObject::connectSlotsByName(Widget);
    ```

    第一行调用 `connect()` 函数，将在UI设计器里的设置的信号与槽的关联转换成语句，在这里将 `btnClose` 按键的 `clicked()` 信号与窗体 `Widget` 的 `close()` 槽函数关联起来。

    第二行设置了关联的方式，用于将UI设计器自动生成的组件信号的槽函数与组件信号相关联。

- 定义 `namepspace Ui` ，定义一个从 `Ui_Widget` 继承的类 `Widget` 。

  ```cpp
  namespace Ui{
  	class Widget: public Ui_Widget();
  }
  ```

  由此，`widget.h` 引入该文件后，定义的 `Widget` 就继承了 `ui_widget.h` 中 `Ui_Widget` 类。


## 可视化UI设计

### 组件与功能

程序的主功能是对中间一个文本框的文字字体样式和颜色进行设置。

![](./Qt5-9-cpp-programming-guide-02-GUI-Base\figure_03.png)

对于界面组件的属性设置，需要注意：

- `objectName` 是窗体上创建的组件的实例名称，界面上的每个组件需要有一个唯一的 `objectName` ，程序里访问界面组件时都是通过其 `objectName` 进行访问，自动生成槽函数名称里也有 `objectName` 。所以，组件的 `objectName` 需要在设计程序之前设置好，设置好之后一般不要再改动。若设计程序之后再改动 `objectName` ，涉及的代码需要相应改动。
- 窗体的 `objectName` 就是窗体的类名称，在UI设计器里不要修改，其实例名称需要在使用窗体的代码里定义。

### 组件布局

Qt界面设计使用了布局 Layout 功能，所谓布局，就是界面上组件的排列方式，使用布局可以使组件有规则地而非内部，并且随着窗体大小变化自动地调整大小和相对位置。布局很重要！

- 界面组件的层次关系

  为了将界面上各个组件分布设计更加美观，经常使用一些容器类，比如 `QgroupBox` 、`OtabWidget`、`QFrame`等等。

  ![](./Qt5-9-cpp-programming-guide-02-GUI-Base\figure_04.jpg)

  ![](./Qt5-9-cpp-programming-guide-02-GUI-Base\figure_05.jpg)

  可以看到三类按钮被分别放置在了不同的三个`groupBox`里，这样布局就比较美观。除此之外，我还将窗口整体布局设置为水平布局。

- 布局管理

  ![](./Qt5-9-cpp-programming-guide-02-GUI-Base\figure_06.jpg)

- 伙伴关系与Tab顺序

  UI设计工具栏中的 `Edit Buddies` 按钮可以进入伙伴关系编辑状态，伙伴关系指的是界面上一个Label和一个组件相关联。例如，表示“姓名”的标签`nameLabel` 和 输入框可以进行伙伴关系绑定； 

  ![](./Qt5-9-cpp-programming-guide-02-GUI-Base\figure_09.jpg)

  设置伙伴关系；

  ![](./Qt5-9-cpp-programming-guide-02-GUI-Base\figure_08.jpg)

  在Label的Text属性的“姓名”后加上 `(&N)`，在“年龄”后加上 `(&A)` ，`&` 会自动隐藏，在用户输入 `Alt+N` 后自动锁定到姓名后面的输入框。

  UI设计工具栏中的 `Edit Tab Order` 按钮可以进入 Tab 顺序编辑状态。按Tab键时输入焦点的移动顺序，在按Tab键时，焦点应该以合理的顺序在界面上移动，而不是随意地移动。

### 信号与槽

`Signal & Slot` 是 Qt 编程的基础，也是 Qt 的一大创新。因为有了信号与槽的编程机制，在 Qt 中处理界面各个组件的交互操作时变得更加直观和简单。

**信号(Signal)**就是在特定情况下被发射的事件，例如 `PushButton` 最常见的信号就是鼠标单击时发射的 `clicked()` 信号，一个 `ComboBox` 最常见的信号是选择的列表项变化时发射的 `CurrentIndexChanged()` 信号。GUI 程序设计的主要内容就是对界面上各组件的信号的响应，只需要知道什么情况下发射哪些信号，合理地去响应和处理这些信号就可以了。

**槽(Slot)**就是对信号响应的函数，槽就是一个函数，与一般的C++函数是一样的，可以定义在类的任何部分（public、private或protected），可以具有任何参数，也可以被直接调用。槽函数与一般的函数不同的是：槽函数可以与一个信号关联，当信号被发射时，关联的槽函数被自动执行。

```cpp
QObject::connect(sender, SIGNAL(signal()), receiver, SLOT(slot()));
```

`connect()` 是 `QObject` 类的一个静态函数，而 `QObject` 是所有Qt类的基类，在实际调用时可以忽略前面的限定符，所以可以直接写为：

```cpp
connect(sender, SIGNAL(signal()), receiver, SLOT(slot()));
```

其中，`sender` 是发射信号的对象的名称，`signal()` 是信号名称。信号可以看作是特殊的函数，需要带括号，有参数时还需要指明参数。`receiver` 时接收信号的对象名称，`slot()` 是槽函数的名称，需要带括号，有参数时还需要指明参数。

