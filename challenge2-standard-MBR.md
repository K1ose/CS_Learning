---
title: challenge1.2-standard-MBR
top: false
comment: false
lang: zh-CN
date: 2021-11-03 16:25:36
tags:
categories:
  - OS
  - lab1	
  - challenge1:what is make
---

# challenge1.2-standard MBR

> 标准的硬盘主导扇区应符合什么条件？


## sign.c

在tools/sign.c中，存在：

```C
// sign.c
#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <sys/stat.h>

int
main(int argc, char *argv[]) {
    struct stat st;
    if (argc != 3) {
        fprintf(stderr, "Usage: <input filename> <output filename>\n");
        return -1;
    }
    if (stat(argv[1], &st) != 0) {
        fprintf(stderr, "Error opening file '%s': %s\n", argv[1], strerror(errno));
        return -1;
    }
    printf("'%s' size: %lld bytes\n", argv[1], (long long)st.st_size);
    if (st.st_size > 510) {
        fprintf(stderr, "%lld >> 510!!\n", (long long)st.st_size);
        return -1;
    }
**    char buf[512];
**     memset(buf, 0, sizeof(buf));
    FILE *ifp = fopen(argv[1], "rb");
    int size = fread(buf, 1, st.st_size, ifp);
    if (size != st.st_size) {
        fprintf(stderr, "read '%s' error, size is %d.\n", argv[1], size);
        return -1;
    }
    fclose(ifp);
**    buf[510] = 0x55;
    buf[511] = 0xAA;** 
    FILE *ofp = fopen(argv[2], "wb+");
    size = fwrite(buf, 1, 512, ofp);
    if (size != 512) {
        fprintf(stderr, "write '%s' error, size is %d.\n", argv[2], size);
        return -1;
    }
    fclose(ofp);
    printf("build 512 bytes boot sector: '%s' success!\n", argv[2]);
    return 0;
}



```


可以看到定义的缓冲区大小为512bytes；

标准的硬盘主导扇区应符合：

1. 大小为512bytes；

2. 倒数两个bytes分别是0x55, 0xaa；（组合起来是0x55aa，这是一个标志，表示该设备可以用于启动）

