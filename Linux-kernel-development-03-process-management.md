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

内核通过唯一的<u>进程标识值</u>(process identification value)或 `PID` 来标识每个进程，内核把每个进程的 `PID` 存放在它们各自的 `task_struct` 中。`task_struct` 在 `include/linux/sched.h` 中定义，其结构体内还有许多其他的数据：

```c
struct task_struct {
	volatile long state;	/* -1 unrunnable, 0 runnable, >0 stopped */
	void *stack;
	atomic_t usage;
	unsigned int flags;	/* per process flags, defined below */
	unsigned int ptrace;

	int lock_depth;		/* BKL lock depth */

    /* ... */
    
	pid_t pid;
	pid_t tgid;

    /* ... */
};
```

`PID` 的数据类型是 `pid_t` ，这是一个隐含类型（表示该数据类型是未知或不相关的），而实际上它就是一个 `int` 整型数据，其默认最大值设置为 `32768` （`short int` 短整型的最大值）。

```c
/* include/linux/types.h */
typedef __kernel_pid_t		pid_t;

/* include/asm-generic/posix_types.h */
#ifndef __kernel_pid_t
typedef int		__kernel_pid_t;
#endif
```

 `<linux/threads.h>` 中限制 `PID` 的最大值为 400 万，可以通过修改 `/proc/sys/kernel/pid_max` 来提高上限。这个最大值实际上就是系统允许同时存在的进程的最大数目。

```c
/* include/linux/threads.h */

/*
 * This controls the default maximum pid allocated to a process
 */
#define PID_MAX_DEFAULT (CONFIG_BASE_SMALL ? 0x1000 : 0x8000)

/*
 * A maximum of 4 million PIDs should be enough for a while.
 * [NOTE: PID/TIDs are limited to 2^29 ~= 500+ million, see futex.h.]
 */
#define PID_MAX_LIMIT (CONFIG_BASE_SMALL ? PAGE_SIZE * 8 : \
	(sizeof(long) > 4 ? 4 * 1024 * 1024 : PID_MAX_DEFAULT))

#endif
```

```bash
$ cat /proc/sys/kernel/pid_max
4194304
```

在内核中，访问任务通常需要获得指向该任务 `task_struct` 的指针，通过 current 宏查找到当前正在运行的进程的 `task_struct` 。这个宏与硬件体系结构相关，需要专门处理。在寄存器多的体系结构中，有专门的寄存器用于存放指向当前进程的 `task_struct` 的指针，例如在 MIPS 架构中，获取 current 宏只需要直接访问寄存器：

```c
// arch/mips/include/asm/thread_info.h
/* How to get the thread information struct from C.  */
register struct thread_info *__current_thread_info __asm__("$28");
#define current_thread_info()  __current_thread_info
```

但在 `x86` 这种寄存器较少的架构中，只能通过在内核栈的尾部压入 `thread_info` 结构，通过计算偏移来简介查找对应的 `task_struct` 。具体操作由 `current_thread_info()` 函数完成：

```c
// arch/x86/include/asm/thread_info.h
/* how to get the thread information struct from C */
static inline struct thread_info *current_thread_info(void)
{
	return (struct thread_info *)
		(current_stack_pointer & ~(THREAD_SIZE - 1));
    /*
     * PAGE_SHIFT determines the page size 
     * #define PAGE_SHIFT 12
     * #define PAGE_SIZE		(1UL << PAGE_SHIFT)     // => PAGE_SIZE = 1000000000000(4096)
     * 
     * #ifdef CONFIG_4KSTACKS
     * #define THREAD_ORDER	0
     * #else
     * #define THREAD_ORDER	1
     * #endif
     *
     * #define THREAD_SIZE 	(PAGE_SIZE << THREAD_ORDER) // => THREAD_SIZE = 10000000000000(8192)
     * ~(THREAD_SIZE - 1) = 1111111111111111111111111111111111111111111111111110000000000000（-8192）
     */
}

// arch/mn10300/include/asm/thread_info.h
/* how to get the current stack pointer from C */
static inline unsigned long current_stack_pointer(void)
{
	unsigned long sp;
	asm("mov sp,%0; ":"=r" (sp));
	return sp;
}
```

`current_stack_pointer & ~(THREAD_SIZE - 1)` 相当于屏蔽了 `sp` 的后13个有效位，汇编代码为：

```assembly
movl $-8192, %eax
andl %esp, %eax
```

上述代码中，默认的栈大小是 8 KB，如果栈大小是 4 KB ，那么 `THREAD_ORDER` 将为 0 ，最终进行 `andl` 的数值是 -4096 ，即屏蔽后 `sp` 的12 位。

最后，在得到指向当前进程的 `thread_info` 结构体的指针后，返回其 `task` 域，即 `task_struct` 结构体的地址。

### 进程的几种状态

task_struct 的 state 域描述了进程的当前状态，它必然是五种状态的其中一种：

- `TASK_RUNNING` - 运行，进程是可执行的，两种情形：
  - 正在执行；
  - 在运行队列中等待执行；
- `TASK_INTERRUPTIBLE` - 可中断，进程在睡眠（被阻塞），需要等待某些条件达成才能进入运行状态。在接收到信号时，可被提前唤醒并准备进入运行态。
- `TASK_UNINTERRUPTIBLE` - 不可中断，进程在睡眠（被阻塞），需要等待某些条件达成才能进入运行状态。不接受信号的影响。
- `__TASK_TRACED` - 正在被其他进程跟踪的进程，如通过 `ptrace` 对被调试程序进行跟踪时，被调试程序出于该状态。
- `__TASK_STOPPED` - 停止，没有进入运行态，也不能进入运行态。在接收到 `SIGSTOP` 、 `SIGTSTP` 、 `SIGTTIN` 、 `SIGTTOU` 后进入该状态。此外，在调试期间的程序接收到任何信号，也会进入该状态。

### 设置当前进程状态

内核经常需要调整进程的状态，这是它会使用 `set_task_state(task, state)` 函数，将指定的 `task` 的状态设置为 `state` 参数所指示的状态。等价于 `task->state = state` 。

```c
// include/linux/sched.h
/*
 * set_current_state() includes a barrier so that the write of current->state
 * is correctly serialised wrt the caller's subsequent test of whether to
 * actually sleep:
 *
 *	set_current_state(TASK_UNINTERRUPTIBLE);
 *	if (do_i_need_to_sleep())
 *		schedule();
 *
 * If the caller does not need such serialisation then use __set_current_state()
 */
#define __set_current_state(state_value)			\
	do { current->state = (state_value); } while (0)
#define set_current_state(state_value)		\
	set_mb(current->state, (state_value))
```

### 进程上下文

代码从硬盘上的可执行文件中载入到进程的地址空间，一般程序在用户空间执行。当程序进行系统调用或出发某个异常后，该程序就陷入了内核。内核代表该进程执行，并处于进程上下文中，上下文中的 `current` 宏是有效的。除非有更高优先级的进程需要执行并由调度器做出调整，否则内核退出后，程序会在用户空间中恢复执行。

用户程序对内核提供的所有服务的访问必须通过接口：系统调用、异常处理。

### 进程家族树

所有进程都是 `PID` 为 1 的 `init` 进程的后代。 内核在系统启动的最后阶段启动 `init` 进程，其读取系统的初始化脚本 `initscript` 并执行相关程序，最终完成系统启动的过程。因此：

- 每个进程必有一个父进程。

- 每个进程可以拥有零个或多个子进程。

- 拥有同一个父进程的所有进程被称为兄弟。

可以看到，在 `task_struct` 结构体中，包含了一个指向其父进程 `task_struct` 结构体地址的指针 `parent` ，还包含一个指向其子进程 `task_struct` 结构体的链表指针 `children` 。

TODO

## 进程创建

## 线程实现

## 进程终结
