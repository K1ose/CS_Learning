---
title: AFL_basic
top: false
comment: false
lang: zh-CN
date: 2021-11-16 13:23:51
tags:
categories:
  - PWN
  - Fuzz
  - AFL
---

# AFL基础

## 模糊测试

> 将自动或半自动生成的随机数据输入到一个程序中，并监视程序异常（崩溃、断言等）失败，以发现可能的程序错误，如内存泄漏等；

### 分类

根据数据生成方式分类：

1. 基于生成的测试（generation-based）
2. 基于变异的测试（mutation-based） （AFL）

根据对目标的理解程度分类：

1. 白盒测试
2. 灰盒测试 （AFL）
3. 黑盒测试

根据fuzz过程的反馈处理分类：

1. 盲测
2. 反馈制导 （AFL）

## 原理

### 使用

#### 安装

```
git clone git://github.com/google/AFL
cd AFL
make
```

### 测试用例

#### 代码

```c
#include <stdio.h> 
#include <stdlib.h> 
#include <unistd.h> 
#include <string.h> 
#include <signal.h> 

int vuln(char *str)
{
    int len = strlen(str);
    if(str[0] == 'A' && len == 66)
    {
        raise(SIGSEGV);
        //如果输入的字符串的首字符为A并且长度为66，则异常退出
    }
    else if(str[0] == 'F' && len == 6)
    {
        raise(SIGSEGV);
        //如果输入的字符串的首字符为F并且长度为6，则异常退出
    }
    else
    {
        printf("it is good!\n");
    }
    return 0;
}

int main(int argc, char *argv[])
{
    char buf[100]={0};
    gets(buf);//存在栈溢出漏洞
    printf(buf);//存在格式化字符串漏洞
    vuln(buf);

    return 0;
}
```

#### 插桩编译

```
./afl-gcc -g afl_test.c -o afl_test
```

#### 输入输出

需要输入文件夹和输出文件夹用于存放输入输出，并在输入文件夹中建立`testcase`文件，用于存放`init seed corpus`，其中随便填写`aaaa`作为输入；

```
mkdir fuzz_in fuzz_out
cd fuzz_in
echo aaaa > testcase
```

#### fuzz

开始fuzz

```
./afl-fuzz -i fuzz_in -o fuzz_out ./afl_test
```

报错

```
$ afl-fuzz -i fuzz_in -o fuzz_out ./afl_test
The program 'afl-fuzz' is currently not installed. To run 'afl-fuzz' please ask your administrator to install the package 'afl'
klose@ubuntu:~/AFL$ ./afl-fuzz -i fuzz_in -o fuzz_out ./afl_test
afl-fuzz 2.57b by <lcamtuf@google.com>
[+] You have 4 CPU cores and 3 runnable tasks (utilization: 75%).
[+] Try parallel jobs - see docs/parallel_fuzzing.txt.
[*] Checking CPU core loadout...
[+] Found a free CPU core, binding to #0.
[*] Checking core_pattern...

[-] Hmm, your system is configured to send core dump notifications to an
    external utility. This will cause issues: there will be an extended delay
    between stumbling upon a crash and having this information relayed to the
    fuzzer via the standard waitpid() API.

    To avoid having crashes misinterpreted as timeouts, please log in as root
    and temporarily modify /proc/sys/kernel/core_pattern, like so:

    echo core >/proc/sys/kernel/core_pattern

[-] PROGRAM ABORT : Pipe at the beginning of 'core_pattern'
         Location : check_crash_handling(), afl-fuzz.c:7347
```

根据错误提示，修改一下`core_pattern`；

```
sudo su
echo core > /proc/sys/kernel/core_pattern
```

再次运行即可；



### 组件与运行流程

基于路径覆盖反馈制导的fuzzer组件：

1. Seed Selector
2. Mutator/Generator
3. Monitor/Filter
4. Program Instrucmentor

```
1. 用户准备Init Seed Corpus，初始的种子集合；
2. Seed selector从Init Seed Corpus中按照一定规则选择一个seed；
3. 将seed送至Mutator/Generator，按照一定规则生成数据；
4. 将数据喂到目标程序中；
5. Monitor/Filter监视程序运行状态，并将结果划分为：Interesting testcases、Report crash或者boring testcases
6. 如果是Intersting testcases，则放入动态种子集中；
7. 如果是Report crash，将结果收集分析，则有可能获得漏洞；
8. 如果是Boring test cases，则discard掉；
```

### 流程

> 以完成一次SEED到FUZZ产生crash为例，AFL中间都做了什么？

- 种子选择与队列管理
  - 种子选择器
- 种子变异
  - 变异器
- 目标程序改造
  - 插桩
- 插桩Testcase的筛选与过滤
  - Monitor/Filter

#### 种子选择与队列管理

> AFL是基于变异的Fuzzer，用户对目标进行测试时，需要准备一系列的初始种子文件——seed corpus

两种seed corps

- initial seed corpus——用户准备的初始种子集
- dynamic seed  corpus——fuzz过程中发现的interesting testcase

##### 种子加载

- 读取输入文件夹中的seeds
- 添加到队列中

###### read_testcases()

在`afl-fuzz.c`中有如下函数：

```c
/* Read all testcases from the input directory, then queue them for testing.
   Called at startup. */

static void read_testcases(void) {

  struct dirent **nl;
  s32 nl_cnt;
  u32 i;
  u8* fn;

  /* Auto-detect non-in-place resumption attempts. */

  fn = alloc_printf("%s/queue", in_dir);
  if (!access(fn, F_OK)) in_dir = fn; else ck_free(fn);

  ACTF("Scanning '%s'...", in_dir);

  /* We use scandir() + alphasort() rather than readdir() because otherwise,
     the ordering  of test cases would vary somewhat randomly and would be
     difficult to control. */

  nl_cnt = scandir(in_dir, &nl, NULL, alphasort);

  if (nl_cnt < 0) {

    if (errno == ENOENT || errno == ENOTDIR)

      SAYF("\n" cLRD "[-] " cRST
           "The input directory does not seem to be valid - try again. The fuzzer needs\n"
           "    one or more test case to start with - ideally, a small file under 1 kB\n"
           "    or so. The cases must be stored as regular files directly in the input\n"
           "    directory.\n");

    PFATAL("Unable to open '%s'", in_dir);

  }

  if (shuffle_queue && nl_cnt > 1) {

    ACTF("Shuffling queue...");
    shuffle_ptrs((void**)nl, nl_cnt);

  }

  for (i = 0; i < nl_cnt; i++) {

    struct stat st;

    u8* fn = alloc_printf("%s/%s", in_dir, nl[i]->d_name);
    u8* dfn = alloc_printf("%s/.state/deterministic_done/%s", in_dir, nl[i]->d_name);

    u8  passed_det = 0;

    free(nl[i]); /* not tracked */
 
    if (lstat(fn, &st) || access(fn, R_OK))
      PFATAL("Unable to access '%s'", fn);

    /* This also takes care of . and .. */

    if (!S_ISREG(st.st_mode) || !st.st_size || strstr(fn, "/README.testcases")) {

      ck_free(fn);
      ck_free(dfn);
      continue;

    }

    if (st.st_size > MAX_FILE) 
      FATAL("Test case '%s' is too big (%s, limit is %s)", fn,
            DMS(st.st_size), DMS(MAX_FILE));

    /* Check for metadata that indicates that deterministic fuzzing
       is complete for this entry. We don't want to repeat deterministic
       fuzzing when resuming aborted scans, because it would be pointless
       and probably very time-consuming. */

    if (!access(dfn, F_OK)) passed_det = 1;
    ck_free(dfn);

    add_to_queue(fn, st.st_size, passed_det);

  }

  free(nl); /* not tracked */

  if (!queued_paths) {

    SAYF("\n" cLRD "[-] " cRST
         "Looks like there are no valid test cases in the input directory! The fuzzer\n"
         "    needs one or more test case to start with - ideally, a small file under\n"
         "    1 kB or so. The cases must be stored as regular files directly in the\n"
         "    input directory.\n");

    FATAL("No usable test cases in '%s'", in_dir);

  }

  last_path_time = 0;
  queued_at_start = queued_paths;

}
```

###### struct queue_entry

种子队列有如下的结构体：

```c
struct queue_entry {

  u8* fname;                          /* File name for the test case      */
  u32 len;                            /* Input length                     */

  u8  cal_failed,                     /* Calibration failed?              */
      trim_done,                      /* Trimmed?                         */
      was_fuzzed,                     /* Had any fuzzing done yet?        */
      passed_det,                     /* Deterministic stages passed?     */
      has_new_cov,                    /* Triggers new coverage?           */
      var_behavior,                   /* Variable behavior?               */
      favored,                        /* Currently favored?               */
      fs_redundant;                   /* Marked as redundant in the fs?   */

  u32 bitmap_size,                    /* Number of bits set in bitmap     */
      exec_cksum;                     /* Checksum of the execution trace  */

  u64 exec_us,                        /* Execution time (us)              */
      handicap,                       /* Number of queue cycles behind    */
      depth;                          /* Path depth                       */

  u8* trace_mini;                     /* Trace bytes, if kept             */
  u32 tc_ref;                         /* Trace bytes ref count            */

  struct queue_entry *next,           /* Next element, if any             */
                     *next_100;       /* 100 elements ahead               */

};
```

