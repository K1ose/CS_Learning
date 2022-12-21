---
title: Linux_kernel_development_03_process_management
top: false
comment: false
lang: zh-CN
date: 2022-12-20 02:40:37
tags:
categories:
  - study
  - book
  - Linux Kernel Development

---

# 进程管理

操作系统为用户程序服务，因此，进程管理是所有操作系统的心脏所在。

## 进程

进程是处于执行期的程序及其相关的资源的总称，它包含其他资源（如打开的文件、挂起的信号等），内核需要有效、透明地管理所有细节。进程还有另一个名称：任务(task)

线程(thread)是在进程中的活动对象，每个线程都拥有一个独立的程序计数器、进程栈和一组进程寄存器。内核调度的是线程，对Linux而言，线程不与进程区分，它只是一种特殊的进程。

进程提供两种虚拟机制：

- 虚拟处理器：许多进程共享一个处理器，但是虚拟处理器使得进程认为自己在独享处理器。
- 虚拟内存：许多进程共享一个内存资源，但是虚拟内存使得进程认为自己独享整个内存资源。

在Linux中，通常使用 `fork()` 系统调用，通过复制一个现有进程来创建一个新进程。

- 父进程：调用 `fork()` 的进程
- 子进程：新生成的进程

在 `fork()` 调用结束后，父进程恢复执行，子进程也开始执行。因此，fork() 系统调用会从内核返回两次，一次回到父进程，一次回到子进程。

在 `fork()` 之后，通常会使用 `exec()` 来创建新的地址空间，并把新的程序载入其中。而在现代Linux中，`fork()` 实际上是由 `clone()` 系统调用实现的。程序最终通过 `exit()` 系统调用退出执行，进程终结，占用资源被释放。父进程可以通过 `wait4()` 系统调用查询子进程是否终结，进程退出执行后被设置为僵死状态，直到父进程的调用 `wait()` 或 `waitpid()` 为止。

## 进程描述符和任务结构

内核把进程的列表存放在任务队列(task list)或者是任务数组(task array)的双向循环链表中。链表中的每一项都是类型为 `task_struct` 的结构，该结构被称为**进程描述符**(process descriptor)，在 `<linux/sched.h>` 中被定义。该结构包含一个具体进程的所有信息。

### 进程描述符的分配

Linux 通过 `slab` 分配器分配 `task_struct` 结构。在 2.6 版本前的内核中，各个进程的 `task_struct` 存放在它们内核栈的末尾。而现在则使用 slab 动态生成 `task_struct` ，所以只需要往栈中压入一个新的结构 `struct thread_info` 即可。这个结构可以使得在汇编代码中能很容易计算出其偏移。

在内核源码树中的 `arch` 文件夹中，存放了不同架构所对应的代码，例如 `x86` 的 `struct thread_info` 在文件 `arch/x86/include/asm/thread_info.h` 中定义。

```c
struct thread_info {
	struct task_struct	*task;		/* main task structure */
	struct exec_domain	*exec_domain;	/* execution domain */
	__u32			flags;		/* low level flags */
	__u32			status;		/* thread synchronous flags */
	__u32			cpu;		/* current CPU */
	int			preempt_count;	/* 0 => preemptable,
						   <0 => BUG */
	mm_segment_t		addr_limit;
	struct restart_block    restart_block;
	void __user		*sysenter_return;
#ifdef CONFIG_X86_32
	unsigned long           previous_esp;   /* ESP of the previous stack in
						   case of nested (IRQ) stacks
						*/
	__u8			supervisor_stack[0];
#endif
	int			uaccess_err;
};

```

每个任务的 `thread_info` 结构在它的内核栈的末尾分配，结构中 `task` 域中存放的是指向该任务的 `task_struct` 的指针。

### 进程描述符的存放

内核通过唯一的<u>进程标识值</u>(process identification value)或 `PID` 来标识每个进程，内核把每个进程的 `PID` 存放在它们各自的 `task_struct` 中。

`PID` 的数据类型是 `pid_t` ，这是一个隐含类型（表示该数据类型是未知或不相关的），而实际上它就是一个 `int` 整型数据，其默认最大值设置为 `32768` （`short int` 短整型的最大值）， `<linux/threads.h>` 中限制 `PID` 的最大值为 400 万。可以通过修改 `/proc/sys/kernel/pid_max` 来提高上限。





## 进程创建

## 线程实现

## 进程终结
