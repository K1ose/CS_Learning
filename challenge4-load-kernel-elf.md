---
title: 'challenge4:load_kernel_elf'
top: false
comment: false
lang: zh-CN
date: 2021-11-11 16:24:46
tags:
categories:
  - OS
  - lab1
  - challenge4:load kernel elf
---

# challenge4:load kernel ELF

通过阅读bootmain.c，了解bootloader如何加载ELF文件。通过分析源代码和通过qemu来运行并调试bootloader&OS，

- bootloader如何读取硬盘扇区的？

- bootloader是如何加载ELF格式的OS？

提示：可阅读“硬盘访问概述”，“ELF执行文件格式概述”这两小节。

## 读取硬盘扇区

CPU进入保护模式后，bootLoader会从硬盘上加载并运行OS，考虑到实现的简单性，bootloader的访问硬盘都是LBA模式的PIO（Program IO）方式，即所有的IO操作是通过CPU访问硬盘的IO地址寄存器完成。

已知，硬盘数据存储在硬盘扇区中，一个扇区大小为512bytes，来看`boot/bootmain.c`中和读取硬盘数据的相关代码；

```C
// x86.h

// inb(port) 获取端口数据
static inline uint8_t inb(uint16_t port) __attribute__((always_inline));
// outb(port, data) 对端口输出数据的操作
static inline void outb(uint16_t port, uint8_t data) __attribute__((always_inline));

static inline void insl(uint32_t port, void *addr, int cnt) __attribute__((always_inline));


```


```C
// bootmain.c
#include <defs.h>
#include <x86.h>
#include <elf.h>

#define SECTSIZE        512                             // per sectors 512 bytes
#define ELFHDR          ((struct elfhdr *)0x10000)      // scratch space

/* waitdisk - wait for disk ready */
static void
waitdisk(void) {
// 0x1f7为状态和命令寄存器，先给命令再读取，如果不是忙状态就从0x1f0读数据
// if inb(0x1f7) == 0 then break, means disk is ready
// 0xc0 = 1100 0000
// 0x40 = 0100 0000
    while ((inb(0x1F7) & 0xC0) != 0x40)   
        /* do nothing */;
}

/* readsect - read a single sector at @secno into @dst */
static void
readsect(void *dst, uint32_t secno) {
    // wait for disk to be ready
    waitdisk();

    // 联合指定扇区号
    // 在这4个字节线联合构成的32位参数中
    // 29-31位强制设为1
    // 28位(=0)表示访问"Disk 0"
    // 0-27位是28位的偏移量
    outb(0x1F2, 1);                         // count = 1, 0x1f2为要读取的扇区数
    outb(0x1F3, secno & 0xFF);              // LBA args0-args7
    outb(0x1F4, (secno >> 8) & 0xFF);       // LBA args8-args15
    outb(0x1F5, (secno >> 16) & 0xFF);      // LBA args16-args23
    // LBA args24-args27, 5th bit (0->master, 1->slave), 6th & 8th bit = 1, 7th bit [if 1 then LBA, if 0 then CHS)
    outb(0x1F6, ((secno >> 24) & 0xF) | 0xE0);  
    outb(0x1F7, 0x20);                      // cmd 0x20 - read sectors, 0x20命令读取扇区

    // wait for disk to be ready
    waitdisk();

    // read a sector
    // 读取到dst位置，除数4因为这里以DW为单位
    insl(0x1F0, dst, SECTSIZE / 4);
}
```


## ELF载入

readseg包装了readsect，可以从设备读取任意长度的内容；

```C
/* *
 * readseg - read @count bytes at @offset from kernel into virtual address @va,
 * might copy more than asked.
 * */
static void
readseg(uintptr_t va, uint32_t count, uint32_t offset) {
    uintptr_t end_va = va + count;

    // round down to sector boundary
    va -= offset % SECTSIZE;

    // translate from bytes to sectors; kernel starts at sector 1
    // 扇区0作为引导扇区已经被占用，从扇区1开始加载ELF文件
    uint32_t secno = (offset / SECTSIZE) + 1;
  
    // If this is too slow, we could read lots of sectors at a time.
    // We'd write more to memory than asked, but it doesn't matter --
    // we load in increasing order.
    for (; va < end_va; va += SECTSIZE, secno ++) {
        readsect((void *)va, secno);
    }
}
```


在bootmain()中，对ELF文件的读取载入进行了详细描述和操作；

```C
/* bootmain - the entry of bootloader */
void
bootmain(void) {
    // read the 1st page off disk
    // 读取ELF header
    readseg((uintptr_t)ELFHDR, SECTSIZE * 8, 0);

    // is this a valid ELF?
    // 通过读取header的magic参数来判断ELF文件是否合法
    if (ELFHDR->e_magic != ELF_MAGIC) {
        goto bad;
    }

    struct proghdr *ph, *eph;

    // load each program segment (ignores ph flags)
    // ELF头部描述了ELF文件应该被加载到什么位置的描述表
    // 先将描述表的头地址存在ph中，ph = *elfheader + offset
    ph = (struct proghdr *)((uintptr_t)ELFHDR + ELFHDR->e_phoff);
    eph = ph + ELFHDR->e_phnum;
    // 按照描述表将ELF文件中的数据载入内存
    for (; ph < eph; ph ++) {
        readseg(ph->p_va & 0xFFFFFF, ph->p_memsz, ph->p_offset);
    }
    // ELF文件0x1000位置后面的0xd1ec bytes被载入内存0x00100000
    // ELF文件0xf000位置后面的0x1d20 bytes被载入内存0x0010e000
    
    // 根据入口信息，找到内核入口
    // call the entry point from the ELF header
    // note: does not return
    ((void (*)(void))(ELFHDR->e_entry & 0xFFFFFF))();

bad:
    outw(0x8A00, 0x8A00);
    outw(0x8A00, 0x8E00);

    /* do nothing */
    while (1);
}
```


至此ELF文件载入内存，内核可以开始工作；

