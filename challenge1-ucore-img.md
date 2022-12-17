---
title: challenge1.1-ucore.img
top: false
comment: false
lang: zh-CN
date: 2021-11-03 16:18:42
tags:
categories:
  - OS
  - lab1
  - challenge1:what is make
---

# challenge1.1-ucore.img

> ucore.img从何而来？

## make

&ensp;首先在`lab1`目录中使用以下指令：

```纯文本
make "V=" | tee -a make.info
```


这样就成功把`make "V="`的输出信息写入到`make.info`文件中，方便查看原始命令执行；

## Makefile

看一下`toobj`、`totarget`、`liftf`等具体定义，在`tools/function.mk`中：

`toobj`

```Makefile
# get .o obj files: (#files[, packet])
toobj = $(addprefix $(OBJDIR)$(SLASH)$(if $(2),$(2)$(SLASH)),\
    $(addsuffix .o,$(basename $(1))))
```

`todep`、`totarget`

```Makefile
# get .d dependency files: (#files[, packet])
todep = $(patsubst %.o,%.d,$(call toobj,$(1),$(2)))

totarget = $(addprefix $(BINDIR)$(SLASH),$(1))
```


`liftf`：

```Makefile
listf = $(filter $(if $(2),$(addprefix %.,$(2)),%),\
      $(wildcard $(addsuffix $(SLASH)*,$(1))))
```

`cc_compile`：

```Makefile
# compile file: (#files, cc[, flags, dir])
cc_compile = $(eval $(call do_cc_compile,$(1),$(2),$(3),$(4)))

# compile file: (#files, cc[, flags, dir])
define do_cc_compile
$$(foreach f,$(1),$$(eval $$(call cc_template,$$(f),$(2),$(3),$(4))))
endef

define cc_template
$$(call todep,$(1),$(4)): $(1) | $$$$(dir $$$$@)
  @$(2) -I$$(dir $(1)) $(3) -MM $$< -MT "$$(patsubst %.d,%.o,$$@) $$@"> $$@
$$(call toobj,$(1),$(4)): $(1) | $$$$(dir $$$$@)
  @echo + cc $$<
  $(V)$(2) -I$$(dir $(1)) $(3) -c $$< -o $$@
ALLOBJS += $$(call toobj,$(1),$(4))
endef

```

### not the time to create ucore.img

在`Makefile`文件中，存在：

```Makefile
# create ucore.img
UCOREIMG  := $(call totarget,ucore.img)

$(UCOREIMG): $(kernel) $(bootblock)
  $(V)dd if=/dev/zero of=$@ count=10000
  $(V)dd if=$(bootblock) of=$@ conv=notrunc
  $(V)dd if=$(kernel) of=$@ seek=1 conv=notrunc

$(call create_target,ucore.img)
```


可以看到，`ucore.img`作为`target`，需要依赖`kernel`和`bootlock`；

### create kernel

在`Makefile`文件中，存在：

```Makefile
# kernel

KINCLUDE  += kern/debug/ \
         kern/driver/ \
         kern/trap/ \
         kern/mm/

KSRCDIR    += kern/init \
         kern/libs \
         kern/debug \
         kern/driver \
         kern/trap \
         kern/mm

KCFLAGS    += $(addprefix -I,$(KINCLUDE))

$(call add_files_cc,$(call listf_cc,$(KSRCDIR)),kernel,$(KCFLAGS))

KOBJS  = $(call read_packet,kernel libs)

# create kernel target
kernel = $(call totarget,kernel)

$(kernel): tools/kernel.ld

$(kernel): $(KOBJS)
  @echo + ld $@
  $(V)$(LD) $(LDFLAGS) -T tools/kernel.ld -o $@ $(KOBJS)
  @$(OBJDUMP) -S $@ > $(call asmfile,kernel)
  @$(OBJDUMP) -t $@ | $(SED) '1,/SYMBOL TABLE/d; s/ .* / /; /^$$/d' > $(call symfile,kernel)

$(call create_target,kernel)
```


可以看到，生成`kernel`依赖于`kern文件夹`下的许多目标文件和`kernel.ld`工具；

1. 这些文件通过一下指令来生成：

```Makefile
$(call add_files_cc,$(call listf_cc,$(KSRCDIR)),kernel,$(KCFLAGS))

# obj集合
KOBJS  = $(call read_packet,kernel libs)

$(V)$(LD) $(LDFLAGS) -T tools/kernel.ld -o $@ $(KOBJS)

```

2. `kernel.ld`

而实际上的命令为：

```纯文本
+ ld bin/kernel
ld -m    elf_i386 -nostdlib -T tools/kernel.ld -o bin/kernel  obj/kern/init/init.o obj/kern/libs/stdio.o obj/kern/libs/readline.o obj/kern/debug/panic.o obj/kern/debug/kdebug.o obj/kern/debug/kmonitor.o obj/kern/driver/clock.o obj/kern/driver/console.o obj/kern/driver/picirq.o obj/kern/driver/intr.o obj/kern/trap/trap.o obj/kern/trap/vectors.o obj/kern/trap/trapentry.o obj/kern/mm/pmm.o  obj/libs/string.o obj/libs/printfmt.o
dd if=bin/kernel of=bin/ucore.img seek=1 conv=notrunc

```

### create bootlock

在`Makefile`文件中，存在：

```Makefile
# create bootblock

# type(boot) = dir
bootfiles = $(call listf_cc,boot)
# gcc -Os -nostdinc <bootfiles>
$(foreach f,$(bootfiles),$(call cc_compile,$(f),$(CC),$(CFLAGS) -Os -nostdinc))

bootblock = $(call totarget,bootblock)

# bootfiles = bootasm.S + bootmain.c => toobj
$(bootblock): $(call **toobj** ,$(bootfiles)) | $(call **totarget** ,sign)
  @echo + ld $@
  $(V)$(LD) $(LDFLAGS) -N -e start -Ttext 0x7C00 $^ -o $(call toobj,bootblock)
  @$(OBJDUMP) -S $(call objfile,bootblock) > $(call asmfile,bootblock)
  @$(OBJCOPY) -S -O binary $(call objfile,bootblock) $(call outfile,bootblock)
  @$(call totarget,sign) $(call outfile,bootblock) $(bootblock)

$(call create_target,bootblock)
```


可以看到这一句：

```Makefile
$(bootblock): $(call toobj,$(bootfiles)) | $(call totarget,sign)

```


由蓝色背景部分可以得知，该步将依赖：

1. boot文件夹中的编译而成目标文件，即bootasm.o和bootmain.o；

```Makefile
# type(boot) = dir
bootfiles = $(call listf_cc,boot)
# gcc -Os -nostdinc <bootfiles>
$(foreach f,$(bootfiles),$(call cc_compile,$(f),$(CC),$(CFLAGS) -Os -nostdinc))
```


实际的命令为：

```纯文本
// cat make.info | grep "bootasm.S"
gcc -Iboot/ -fno-builtin -Wall -ggdb -m32 -gstabs -nostdinc  -fno-stack-protector -Ilibs/ -Os -nostdinc -c boot/bootasm.S -o obj/boot/bootasm.o

// cat make.info | grep "bootmain.c"
gcc -Iboot/ -fno-builtin -Wall -ggdb -m32 -gstabs -nostdinc  -fno-stack-protector -Ilibs/ -Os -nostdinc -c boot/bootmain.c -o obj/boot/bootmain.o

```

2. target sign；

```Makefile
# create 'sign' tools
$(call add_files_host,tools/sign.c,sign,sign)
$(call create_target_host,sign,sign)
```


实际的命令为：

```纯文本
// cat make.info | grep "sign"
gcc -Itools/ -g -Wall -O2 -c tools/sign.c -o obj/sign/tools/sign.o
gcc -g -Wall -O2 obj/sign/tools/sign.o -o bin/sign

```


依赖文件都生成后，将生成`bootblock.o`；

```纯文本
// cat make.info | grep "bootblock.o"

+ ld bin/bootblock
ld -m    elf_i386 -nostdlib -N -e start -Ttext 0x7C00 **obj/boot/bootasm.o**  **obj/boot/bootmain.o**  -o **obj/bootblock.o** 
'**obj/bootblock.out** ' size: 488 bytes
build 512 bytes boot sector: '**bin/bootblock** ' success!
```


可以看到生成了依赖的二进制文件`bootblock`

### create ucore.img now

对依赖文件进行写入，生成镜像文件；

```纯文本
// make V=

dd if=**/dev/zero**  of=bin/ucore.img count=10000
10000+0 records in
10000+0 records out
5120000 bytes (5.1 MB, 4.9 MiB) copied, 0.0340812 s, 150 MB/s

dd if=**bin/bootblock**  of=bin/ucore.img conv=notrunc
1+0 records in
1+0 records out
512 bytes copied, 0.000153858 s, 3.3 MB/s

dd if=**bin/kernel**  of=bin/ucore.img seek=1 conv=notrunc
146+1 records in
146+1 records out
74828 bytes (75 kB, 73 KiB) copied, 0.00263467 s, 28.4 MB/s


```


可以看到生成了一个有10000个块的文件，每个块默认512bytes，使用0填充，共5120000bytes；

将bootblock写入第一个块，**小于等于512bytes** 才符合要求；

从第二个块开始，写入kernel，共74828bytes；

