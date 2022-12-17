---
title: pyside2_01_helloworld
top: false
comment: false
lang: zh-CN
date: 2022-10-17 06:11:37
tags:
categories:
  - program language
  - python
  - pyside2
---

直接看代码吧。

```python
import sys
import random
from PySide2 import QtCore, QtWidgets, QtGui


class MyWidget(QtWidgets.QWidget):
    def __init__(self):
        super().__init__()
        self.hello = ["Hello, Klose", "Hi, Flora"]

        self.button = QtWidgets.QPushButton("Click me", self)

        self.text = QtWidgets.QLabel("Hello World")
        self.text.setAlignment(QtCore.Qt.AlignCenter)

        self.layout = QtWidgets.QVBoxLayout()
        self.layout.addWidget(self.text)
        self.layout.addWidget(self.button)
        self.setLayout(self.layout)

        self.button.clicked.connect(self.foo)

    def foo(self):
        self.text.setText(random.choice(self.hello))


if __name__ == '__main__':
    app = QtWidgets.QApplication([])
    widget = MyWidget()
    widget.resize(800, 600)
    widget.show()
    sys.exit(app.exec_())
```

![](./pyside2-01-helloworld/helloworld.jpg)
