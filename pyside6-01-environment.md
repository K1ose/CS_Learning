---
title: pyside6_01_environment
top: false
comment: false
lang: zh-CN
date: 2023-05-11 09:55:52
tags:
categories:
  - program language
  - python
  - pyside6
---

# Environment

## Getting Started on Windows

The Qt library has to be built with the same version of MSVC as Python and PySide, this can be selected when using the online installer.

### Requirements

- [MSVC2022](https://visualstudio.microsoft.com/downloads/) or (MSVC2019) for Python 3 on Windows.
- [OpenSSL](https://sourceforge.net/projects/openssl/) (optional for SSL support, Qt must have been configured using the same SSL library).
- `sphinx` package for the documentation (optional).

> Python 3.8.0 was missing some API required for PySide/Shiboken so it’s not possible to use it for a Windows build.

