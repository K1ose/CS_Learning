---
title: 'challenge6.2:init_IDT'
top: false
comment: false
lang: zh-CN
date: 2021-11-11 16:29:54
tags:
categories:
  - OS
  - lab1
  - challenge6:interrupt handle
---

# challenge6.2:init IDT

编程完善kern/trap/trap.c中对中断向量表进行初始化的函数idt_init。在idt_init函数中，依次对所有中断入口进行初始化。使用mmu.h中的SETGATE宏，填充idt数组内容。每个中断的入口由tools/vectors.c生成，使用trap.c中声明的vectors数组即可。

```C
/* idt_init - initialize IDT to each of the entry points in kern/trap/vectors.S */
void idt_init(void)
{
    /* LAB1 YOUR CODE : STEP 2 */
    /* (1) Where are the entry addrs of each Interrupt Service Routine (ISR)?
      *     All ISR's entry addrs are stored in __vectors. where is uintptr_t __vectors[] ?
      *     __vectors[] is in kern/trap/vector.S which is produced by tools/vector.c
      *     (try "make" command in lab1, then you will find vector.S in kern/trap DIR)
      *     You can use  "extern uintptr_t __vectors[];" to define this extern variable which will be used later.
      * (2) Now you should setup the entries of ISR in Interrupt Description Table (IDT).
      *     Can you see idt[256] in this file? Yes, it's IDT! you can use SETGATE macro to setup each item of IDT
      * (3) After setup the contents of IDT, you will let CPU know where is the IDT by using 'lidt' instruction.
      *     You don't know the meaning of this instruction? just google it! and check the libs/x86.h to know more.
      *     Notice: the argument of lidt is idt_pd. try to find it!
      */
    extern uintptr_t __vectors[];
    int i;
    // 入口数=idt大小/gatedesc结构化大小
    // idt = n * gatedesc
    for (i = 0; i < sizeof(idt) / sizeof(struct gatedesc); i++)
    {
        /*
        #define SETGATE(gate, istrap, sel, off, dpl) {            \
          (gate).gd_off_15_0 = (uint32_t)(off) & 0xffff;        \
          (gate).gd_ss = (sel);                                \
          (gate).gd_args = 0;                                    \
          (gate).gd_rsv1 = 0;                                    \
          (gate).gd_type = (istrap) ? STS_TG32 : STS_IG32;    \
          (gate).gd_s = 0;                                    \
          (gate).gd_dpl = (dpl);                                \
          (gate).gd_p = 1;                                    \
          (gate).gd_off_31_16 = (uint32_t)(off) >> 16;        \
        }
        */
        // 为每一个entry设置gate属性
        SETGATE(idt[i], 0, GD_KTEXT, __vectors[i], DPL_KERNEL);
    }
    // 设置用户态到内核的开关
    SETGATE(idt[T_SWITCH_TOK], 0, GD_KTEXT, __vectors[T_SWITCH_TOK], DPL_USER);
    // 装载
    lidt(&idt_pd);
    }
}
```


在kern/mm/memlayout.h中，对内核权限和用户态权限的定义；

```C
#define DPL_KERNEL  (0)
#define DPL_USER    (3)
```


在kern/trap/trap.h中，对开关的定义；

```C
#define T_SWITCH_TOU                120    // user/kernel switch
#define T_SWITCH_TOK                121    // user/kernel switch
```

