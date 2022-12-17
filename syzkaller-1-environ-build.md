---
title: syzkaller_1_environ_build
top: false
comment: false
lang: zh-CN
date: 2021-11-20 20:44:05
tags:
categories:
  - PWN
  -	kernel pwn
  - FUZZ
  - Syzkaller

---

# Syzkaller(1):环境搭建

> Syzkaller是Google的安全研究人员开发的内核fuzz工具，也是当前最强大的内核fuzz工具之一。其基本语言是Go，支持akaros/fuchsia/linux/android/freebsd/netbsd/openbsd/windows等系统，不支持Darwin/XNU。

## 环境搭建

采用`ubuntu1804`搭建；

### 安装依赖

```
sudo apt-get install debootstrap
sudo apt install qemu-kvm
sudo apt-get install subversion
sudo apt-get install git
sudo apt-get install make
sudo apt-get install qemu
sudo apt install libssl-dev libelf-dev
sudo apt-get install flex bison libc6-dev libc6-dev-i386 linux-libc-dev linux-libc-dev:i386 libgmp3-dev libmpfr-dev libmpc-dev
apt-get install g++
apt-get install build-essential
apt install golang-go
apt install gcc
```

### golang版本

```
$ go version
go version go1.10.4 linux/amd64
```

根据官方的说明，`syzkaller`需要`1.13+`的go版本支持，所以需要进行更新；

```
sudo apt-get remove golang-go
sudo apt-get remove --auto-remove golang-go
sudo wget https://dl.google.com/go/go1.14.2.linux-amd64.tar.gz
sudo tar -xf go1.14.2.linux-amd64.tar.gz
mkdir gopath && cd
mkdir src && cd
mkdir github.com && cd
mkdir google && cd
export GOPATH=/root/gopath
export GOROOT=/root/goroot
export PATH=$GOPATH/bin:$PATH
export PATH=$GOROOT/bin:$PATH
```

再次查看版本；

```
# go version
go version go1.14.2 linux/amd64
```

### gcc版本

```
$ gcc --version
gcc (Ubuntu 7.5.0-3ubuntu1~18.04) 7.5.0
```

### linux

```
~ # mkdir source
~ # cd source
~/source # git clone https://mirrors.tuna.tsinghua.edu.cn/git/linux.git
```

#### 编译

下载完毕后，会有一个`linux`文件夹

```
cd linux
make CC="/usr/bin/gcc" defconfig
make CC="/usr/bin/gcc" kvmconfig 
```

在.config文件中添加，重要的是，修改`# CONFIG_KCOV is not be set`，直接修改这一行，取消注释，其他config选项类似；

```
CONFIG_KCOV=y
CONFIG_DEBUG_INFO=y
CONFIG_KASAN=y          # KASAN这两项没有，添加也不要放最后
CONFIG_KASAN_INLINE=y
CONFIG_CONFIGFS_FS=y
CONFIG_SECURITYFS=y
```

再执行

```
make CC="/usr/bin/gcc" olddefconfig
make CC="/usr/bin/gcc" -j4         #我给虚拟机配了2个处理器，所以这里用了-j4
```

#### image

```
sudo apt-get install debootstrap
mkdir image
cd image
sudo gedit create-image.sh
```

输入

```shell
#!/usr/bin/env bash
# Copyright 2016 syzkaller project authors. All rights reserved.
# Use of this source code is governed by Apache 2 LICENSE that can be found in the LICENSE file.

# create-image.sh creates a minimal Debian Linux image suitable for syzkaller.

set -eux

# Create a minimal Debian distribution in a directory.
DIR=chroot
PREINSTALL_PKGS=openssh-server,curl,tar,gcc,libc6-dev,time,strace,sudo,less,psmisc,selinux-utils,policycoreutils,checkpolicy,selinux-policy-default,firmware-atheros,debian-ports-archive-keyring

# If ADD_PACKAGE is not defined as an external environment variable, use our default packages
if [ -z ${ADD_PACKAGE+x} ]; then
    ADD_PACKAGE="make,sysbench,git,vim,tmux,usbutils,tcpdump"
fi

# Variables affected by options
ARCH=$(uname -m)
RELEASE=stretch
FEATURE=minimal
SEEK=2047
PERF=false

# Display help function
display_help() {
    echo "Usage: $0 [option...] " >&2
    echo
    echo "   -a, --arch                 Set architecture"
    echo "   -d, --distribution         Set on which debian distribution to create"
    echo "   -f, --feature              Check what packages to install in the image, options are minimal, full"
    echo "   -s, --seek                 Image size (MB), default 2048 (2G)"
    echo "   -h, --help                 Display help message"
    echo "   -p, --add-perf             Add perf support with this option enabled. Please set envrionment variable \$KERNEL at first"
    echo
}

while true; do
    if [ $# -eq 0 ];then
	echo $#
	break
    fi
    case "$1" in
        -h | --help)
            display_help
            exit 0
            ;;
        -a | --arch)
	    ARCH=$2
            shift 2
            ;;
        -d | --distribution)
	    RELEASE=$2
            shift 2
            ;;
        -f | --feature)
	    FEATURE=$2
            shift 2
            ;;
        -s | --seek)
	    SEEK=$(($2 - 1))
            shift 2
            ;;
        -p | --add-perf)
	    PERF=true
            shift 1
            ;;
        -*)
            echo "Error: Unknown option: $1" >&2
            exit 1
            ;;
        *)  # No more options
            break
            ;;
    esac
done

# Handle cases where qemu and Debian use different arch names
case "$ARCH" in
    ppc64le)
        DEBARCH=ppc64el
        ;;
    aarch64)
        DEBARCH=arm64
        ;;
    arm)
        DEBARCH=armel
        ;;
    x86_64)
        DEBARCH=amd64
        ;;
    *)
        DEBARCH=$ARCH
        ;;
esac

# Foreign architecture

FOREIGN=false
if [ $ARCH != $(uname -m) ]; then
    # i386 on an x86_64 host is exempted, as we can run i386 binaries natively
    if [ $ARCH != "i386" -o $(uname -m) != "x86_64" ]; then
        FOREIGN=true
    fi
fi

if [ $FOREIGN = "true" ]; then
    # Check for according qemu static binary
    if ! which qemu-$ARCH-static; then
        echo "Please install qemu static binary for architecture $ARCH (package 'qemu-user-static' on Debian/Ubuntu/Fedora)"
        exit 1
    fi
    # Check for according binfmt entry
    if [ ! -r /proc/sys/fs/binfmt_misc/qemu-$ARCH ]; then
        echo "binfmt entry /proc/sys/fs/binfmt_misc/qemu-$ARCH does not exist"
        exit 1
    fi
fi

# Double check KERNEL when PERF is enabled
if [ $PERF = "true" ] && [ -z ${KERNEL+x} ]; then
    echo "Please set KERNEL environment variable when PERF is enabled"
    exit 1
fi

# If full feature is chosen, install more packages
if [ $FEATURE = "full" ]; then
    PREINSTALL_PKGS=$PREINSTALL_PKGS","$ADD_PACKAGE
fi

sudo rm -rf $DIR
sudo mkdir -p $DIR
sudo chmod 0755 $DIR

# 1. debootstrap stage

DEBOOTSTRAP_PARAMS="--arch=$DEBARCH --include=$PREINSTALL_PKGS --components=main,contrib,non-free $RELEASE $DIR"
if [ $FOREIGN = "true" ]; then
    DEBOOTSTRAP_PARAMS="--foreign $DEBOOTSTRAP_PARAMS"
fi

# riscv64 is hosted in the debian-ports repository
# debian-ports doesn't include non-free, so we exclude firmware-atheros
if [ $DEBARCH == "riscv64" ]; then
    DEBOOTSTRAP_PARAMS="--keyring /usr/share/keyrings/debian-ports-archive-keyring.gpg --exclude firmware-atheros $DEBOOTSTRAP_PARAMS http://deb.debian.org/debian-ports"
fi
sudo debootstrap $DEBOOTSTRAP_PARAMS

# 2. debootstrap stage: only necessary if target != host architecture

if [ $FOREIGN = "true" ]; then
    sudo cp $(which qemu-$ARCH-static) $DIR/$(which qemu-$ARCH-static)
    sudo chroot $DIR /bin/bash -c "/debootstrap/debootstrap --second-stage"
fi

# Set some defaults and enable promtless ssh to the machine for root.
sudo sed -i '/^root/ { s/:x:/::/ }' $DIR/etc/passwd
echo 'T0:23:respawn:/sbin/getty -L ttyS0 115200 vt100' | sudo tee -a $DIR/etc/inittab
printf '\nauto eth0\niface eth0 inet dhcp\n' | sudo tee -a $DIR/etc/network/interfaces
echo '/dev/root / ext4 defaults 0 0' | sudo tee -a $DIR/etc/fstab
echo 'debugfs /sys/kernel/debug debugfs defaults 0 0' | sudo tee -a $DIR/etc/fstab
echo 'securityfs /sys/kernel/security securityfs defaults 0 0' | sudo tee -a $DIR/etc/fstab
echo 'configfs /sys/kernel/config/ configfs defaults 0 0' | sudo tee -a $DIR/etc/fstab
echo 'binfmt_misc /proc/sys/fs/binfmt_misc binfmt_misc defaults 0 0' | sudo tee -a $DIR/etc/fstab
echo -en "127.0.0.1\tlocalhost\n" | sudo tee $DIR/etc/hosts
echo "nameserver 8.8.8.8" | sudo tee -a $DIR/etc/resolve.conf
echo "syzkaller" | sudo tee $DIR/etc/hostname
ssh-keygen -f $RELEASE.id_rsa -t rsa -N ''
sudo mkdir -p $DIR/root/.ssh/
cat $RELEASE.id_rsa.pub | sudo tee $DIR/root/.ssh/authorized_keys

# Add perf support
if [ $PERF = "true" ]; then
    cp -r $KERNEL $DIR/tmp/
    BASENAME=$(basename $KERNEL)
    sudo chroot $DIR /bin/bash -c "apt-get update; apt-get install -y flex bison python-dev libelf-dev libunwind8-dev libaudit-dev libslang2-dev libperl-dev binutils-dev liblzma-dev libnuma-dev"
    sudo chroot $DIR /bin/bash -c "cd /tmp/$BASENAME/tools/perf/; make"
    sudo chroot $DIR /bin/bash -c "cp /tmp/$BASENAME/tools/perf/perf /usr/bin/"
    rm -r $DIR/tmp/$BASENAME
fi

# Add udev rules for custom drivers.
# Create a /dev/vim2m symlink for the device managed by the vim2m driver
echo 'ATTR{name}=="vim2m", SYMLINK+="vim2m"' | sudo tee -a $DIR/etc/udev/rules.d/50-udev-default.rules

# Build a disk image
dd if=/dev/zero of=$RELEASE.img bs=1M seek=$SEEK count=1
sudo mkfs.ext4 -F $RELEASE.img
sudo mkdir -p /mnt/$DIR
sudo mount -o loop $RELEASE.img /mnt/$DIR
sudo cp -a $DIR/. /mnt/$DIR/.
sudo umount /mnt/$DIR
```

赋予执行权限，并执行

```
chmod +x create-image.sh
./create-image.sh
```

等待许久，生成了几个文件

```
~/source/linux/image# ls
chroot  create-image.sh  stretch.id_rsa  stretch.id_rsa.pub  stretch.img
```

### qemu环境

```
sudo apt-get install qemu-system-x86
```

### boot.sh

```
gedit boot.sh
```

输入：

```
qemu-system-x86_64 \
 -kernel /root/source/linux/arch/x86/boot/bzImage \
 -append "console=ttyS0 root=/dev/sda debug earlyprintk=serial slub_debug=QUZ"\
 -hda ./stretch.img \
 -net user,hostfwd=tcp::10021-:22 -net nic \
 -enable-kvm \
 -nographic \
 -m 256M \
 -smp 2 \
 -pidfile vm.pid \
 2>&1 | tee vm.log
```

### syzkaller安装

```
unzip syzkaller-master.zip 
mv syzkaller-master syzkaller
sudo gedit syz-manager/html.go            # 这里把Value: fmt.Sprint(head[:8])中的8改成4
make
```

在`bin`文件中有；

```
~/gopath/src/github.com/google/syzkaller/bin# ls
linux_amd64  syz-manager  syz-prog2c  syz-runtest  syz-upgrade
syz-db       syz-mutate   syz-repro   syz-sysgen
```

#### workdir

创建`workdir`目录；

```
mkdir workdir
```

#### my.cfg

创建my.cfg配置文件；

```
{
	"target": "linux/amd64",
	"http": "127.0.0.1:56741",
	"workdir": "../syzkaller/workdir",                # 注意路径，可以看报错设置
	"kernel_obj": "/root/source/linux",               # 注意路径，可以看报错设置
	"image": "/root/source/linux/image/stretch.img",   # 注意路径，可以看报错设置
	"sshkey": "/root/source/linux/image/stretch.id_rsa",   # 注意路径，可以看报错设置
	"syzkaller": "../syzkaller",    # 注意路径，可以看报错设置
	"procs": 8,
	"type": "qemu",
	"vm": {
		"count": 4,
		"kernel": "/root/source/linux/arch/x86/boot/bzImage",    # 注意路径，可以看报错设置
		"cpu": 2,
		"mem": 2048
	}
}
```

启动；

```
.bin/syz-manager -config=my.cfg
```

访问`http://127.0.0.1:65741`，即可以获得fuzz信息；

```
Stats: revision 	HEAD
config 	config
uptime 	18m37s
fuzzing 	18m0s
corpus 	540
triage queue 	0
signal 	31492
coverage 	25148
syscalls 	2062
crash types 	0 (0/hour)
crashes 	0 (0/hour)
exec candidate 	330 (18/min)
exec fuzz 	0 (0/hour)
exec gen 	0 (0/hour)
exec hints 	0 (0/hour)
exec minimize 	35776 (33/sec) 
...
```

