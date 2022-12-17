---
title: Tai-e_A0_Set_up
top: false
comment: false
lang: zh-CN
date: 2022-06-02 16:32:30
tags:
categories:
  - study
  - course
  - static_program_analysis
---

# Set Up Tai-e Assignments

Tai-e实验指导书：https://tai-e.pascal-lab.net/intro/setup.html

Github：https://github.com/pascal-lab/Tai-e-assignments

java 17：https://jdk.java.net/17/ or Intellij IDEA：https://www.jetbrains.com/idea/download/#section=windows

## Preface

目前，Tai-e 利用 Soot 前端解析 Java 程序并帮助构建 Tai-e IR。Soot 有两个前端，分别处理 Java 源代码文件（`.java`）和字节码文件（`.class`）。其中，前者可以将源代码中的变量名保留至 IR 中，从而使得生成的 IR 更贴近源码，比后者的更易于理解。因此，**在实验作业中**，测试用例（即待分析的程序）都以 Java 源文件的格式提供。然而，Soot 的 Java 源文件前端已经过时（只对最高 Java 7 版本提供部分支持）且不够健壮。与之相比，尽管 Soot 的字节码文件前端不能保持原先的变量名，但它更加健壮（对最高 Java 17 版本编译生成的 `.class` 文件都提供支持）。因此，分析**真实世界的程序**时，Tai-e 往往分析字节码。

Tai-e 利用 Gradle 构建，并符合一般 Gradle 项目的结构，所有实验作业项目都具有如下结构：

- `build.gradle.kts`，`gradlew`，`gradlew.bat`，`gradle/`：Gradle 脚本和 Tai-e 项目配置文件。
- `src/main/java`：Tai-e 源代码文件夹。你需要修改该文件夹中的文件以完成作业。
- `src/test/java`：运行测试用例所需的测试驱动程序（test drivers）所在文件夹。
- `src/test/resources`：测试用例（待分析的程序）文件夹。
- `lib/`：包含 Tai-e 类的文件夹。
- `plan.yml`：Tai-e 配置文件，设定了作业中需要执行的分析。
- `COPYING`, `COPYING.LESSER`：Tai-e 许可文件。

## SDK version

File > Project Structure... > Add SDK > Download JDK > 17 > Oracle OenJDK > 17 - Sealed typed, always-strict floating-point semantics

## Build

由于 Tai-e 是一个 Gradle 项目，IntelliJ IDEA 默认使用 Gradle 构建并运行它，这使得构建较慢且总会输出一些烦人的 Gradle 信息。为解决这些问题，可以使用 IntelliJ IDEA 而非 Gradle 来构建和运行 Tai-e。打开 `File > Settings`，将 Build and run 设置中的构建和运行工具从 Gradle 改为 IntelliJ IDEA。

或者...

在命令行模式下，用下面语句使用Gradle进行构建：

```bash
$ gradlew compileJava
```

## run Tai-e as Software

Tai-e中提供了一个特殊的类:`pascal.taie.Assignment`

提供了一种简单的使用方式来分析java程序：

```bash
-cp <CLASS_PATH> -m <CLASS_NAME>
```

其中，`<CLASS_PATH>` 是 .class 文件所在文件夹的路径，`<CLASS_NAME>` 是待分析类的类名。Tai-e 会在路径给定的文件夹中寻找该类。可以在`Run Configuration`中配置。

或者：

```sh
$ gradlew run --args="-cp <CLASS_PATH> -m <CLASS_NAME>"
```

## use JUnit to test

在 `src/test/resources/` 文件夹中有一些 Java 类和测试输入，每个类都对应于一个名为 `*-expected.txt` 的期望测试结果文件。可以（用 JUnit）运行测试类来分析 `src/test/java/` 中的测试输入。不同实验作业有不同测试用例和测试驱动程序，在各实验作业的文档中会详细说明。

测试驱动程序会对 `src/test/resources/` 下所有测试用例执行分析，并将其输出与期望结果进行比较。如果实现正确，你会通过测试，否则测试驱动程序会失败并输出期望结果和执行结果的不同之处。

同样，也可以使用 Gradle 运行测试：

```bash
$ gradlew clean test
```

该命令会清空构建目录，重新构建 Tai-e 并执行测试。
