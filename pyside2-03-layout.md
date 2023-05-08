---
title: pyside2_03_layout
top: false
comment: false
lang: zh-CN
date: 2022-10-18 18:02:58
tags:
categories:
  - program language
  - python
  - pyside2
---

# 概要

QT提供了很多界面布局，最简单的是水平和垂直。为什么要使用布局？有些人可能遇到过， 窗口绘制好后，运行时偶然拖动窗口或控件位置大小，会破坏整个布局的美观。在Qt中使用布局功能， 能很好的控制控件的大小和位置随窗孔变化而变化。

# 绘制窗口

添加MainWindow，放置一个Vertical Layout，并拖入label和pushButton。组件会在layout框架中自动排列好，给label设置属性，在filter里搜索alignment（或者右键选择alignment），将水平和垂直都设置为Center。

<img src="./pyside2-03-layout\figure1.jpg" width=500 height=400/>

# 代码转换

使用ui2py工具，将ui转换为py代码：

```python
from PySide2.QtCore import *
from PySide2.QtGui import *
from PySide2.QtWidgets import *

class Ui_MainWindow(object):
    def setupUi(self, MainWindow):
        if not MainWindow.objectName():
            MainWindow.setObjectName(u"MainWindow")
        MainWindow.resize(800, 600)
        self.centralwidget = QWidget(MainWindow)
        self.centralwidget.setObjectName(u"centralwidget")
        self.label = QLabel(self.centralwidget)
        self.label.setObjectName(u"label")
        self.label.setGeometry(QRect(290, 200, 221, 81))
        font = QFont()
        font.setFamily(u"Arial")
        font.setPointSize(28)
        font.setBold(True)
        font.setWeight(75)
        self.label.setFont(font)
        MainWindow.setCentralWidget(self.centralwidget)
        self.menubar = QMenuBar(MainWindow)
        self.menubar.setObjectName(u"menubar")
        self.menubar.setGeometry(QRect(0, 0, 800, 26))
        MainWindow.setMenuBar(self.menubar)
        self.statusbar = QStatusBar(MainWindow)
        self.statusbar.setObjectName(u"statusbar")
        MainWindow.setStatusBar(self.statusbar)

        self.retranslateUi(MainWindow)

        QMetaObject.connectSlotsByName(MainWindow)
    # setupUi

    def retranslateUi(self, MainWindow):
        MainWindow.setWindowTitle(QCoreApplication.translate("MainWindow", u"MainWindow", None))
        self.label.setText(QCoreApplication.translate("MainWindow", u"Hello world!", None))
```

# 执行

添加`main.py`来运行：

```python
from ui_layout import Ui_MainWindow
from PySide2.QtWidgets import *
class MainWindow(QMainWindow):
    def __init__(self):
        super(MainWindow, self).__init__()
        self.ui = Ui_MainWindow()
        self.ui.setupUi(self)

if __name__ == '__main__':
    app = QApplication([])
    pha = MainWindow()
    pha.show()
    app.exec_()
```

效果为：

<img src="./pyside2-03-layout\figure2.jpg" width=500 height=400/>
