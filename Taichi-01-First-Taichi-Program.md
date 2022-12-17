---
title: Taichi_01_First_Taichi_Program
top: false
comment: false
lang: zh-CN
date: 2022-05-04 13:45:17
tags:
categories:
  - study
  - course
  - Taichi
---

# First Taichi Program

## Taichi - "Hello world"

- Data

- Computation

- Visualization

## Init

```python
import taichi as ti

ti.init(arch="gpu")	 # 计算硬件选择
# ti.init(ti.cpu)
# ti.init(ti.gpu)

def foo1():
    print("this is a normal python function")


@ti.kernel  # 修饰函数，在taichi作用域中
def foo2():
    print("this is a taichi function")

```

得到结果：

```
> python -u "d:\K1ose\code\Taichi\sample.py"
[Taichi] version 1.0.1, llvm 10.0.0, commit 1c3619d9, win, python 3.10.2
[W 05/04/22 13:51:54.435 20080] [misc.py:adaptive_arch_select@781] Arch=['gpu'] is not supported, falling back to CPU
[Taichi] Starting on arch=x64
this is a normal python function
this is a taichi function
```

应为我是核显，没有gpu，所以回滚到了cpu进行运行；

## Data Type

### Primitive types

- signed integers: ti.i8, ti.i16, ti,i32, ti.i64

- unsigned integers: ti.u8, ti.u16, ti,u32, ti.u64

- floating points: ti.f32, ti.f64

### Default types

- using int or  float for default types

- can be changed via ti.init

  ```python
  ti.init(default_fp=ti.f32)	# float = ti.f32
  ti.init(default_fp=ti.f64)	# float = ti.f64
  
  ti.init(default_fp=ti.i32)	# int = ti.i32
  ti.init(default_fp=ti.i64)	# int = ti.i64
  ```

### Type promotions

- pick more precious type

  ```
  i32 + f32 = f32
  i32 + i64 = i64
  ```

### Type cast

```python
def foo3():
    a = 1       # integer
    a = 3.14    # float
    print(a)    # float


@ti.kernel  # 修饰函数，在taichi作用域中
def foo4():
    a = 1       # integer
    a = 3.14    # float
    print(a)    # integer
```

得到的结果为：

```
> python -u "d:\K1ose\code\Taichi\sample.py"
[Taichi] version 1.0.1, llvm 10.0.0, commit 1c3619d9, win, python 3.10.2
[Taichi] Starting on arch=x64
3.14
1.700000
1
1.000000
```

存在隐式转换；

可以使用`ti.cast()`对不同类型的数据进行类型转换；

```python
@ti.kernel  # 修饰函数，在taichi作用域中
def foo2():
    a = 1.7       # integer
    print(a)    # integer

    b = ti.cast(a, ti.i32)
    print(b)
    c = ti.cast(b, ti.f32)
    print(c)
```

## Vector & matrix & struct

向量、矩阵和结构

```python
# 向量和矩阵来构建结构
vec3f = ti.types.vector(3, ti.f32)  						# 3d float
mat2f = ti.types.matrix(2, 2, ti.f32)   					# 2x2 float
ray = ti.types.struct(ro=vec3f, rd=vec3f, l=ti.f32)			# 用结构搭建起ray的起点ro，方向rd，长度l


@ti.kernel
def vector_matrix_struct():
    a = vec3f(0.0)
    # a = ti.Vector([0.0, 0.0, 0.0])
    print(a)
    
    d = vec3f(0.0, 1.0, 0.0)
    # d = ti.Vector([0.0, 1.0, 0.0])
    print(d)
    
    B = mat2f([[1.5, 1.4], [1.3, 1.2]])
    # B = ti.Matrix([[1.5,1.4], [1.3, 1.2]])
    print("B = ", B)
    
    r = ray(ro=a, rd=d, l=1)
    # r = ti.Struct(ro=a, rd=d, l=1)
    print("r.ro = ", r.ro)
    print("r.rd = ", r.rd)

    # visit element
    print("vec3f a[1] => ", a[1])
    print("mat2f B[1, 0] => ", B[1, 0])
```

得到的结果为：

```
> python -u "d:\K1ose\code\Taichi\sample.py"
[Taichi] version 1.0.1, llvm 10.0.0, commit 1c3619d9, win, python 3.10.2
[Taichi] Starting on arch=x64
[0.000000, 0.000000, 0.000000]
[0.000000, 1.000000, 0.000000]
B =  [[1.500000, 1.400000], [1.300000, 1.200000]]
r.ro =  [0.000000, 0.000000, 0.000000]
r.rd =  [0.000000, 1.000000, 0.000000]
vec3f a[1] =>  0.000000
mat2f B[1, 0] =>  1.300000
```

## Field

```python
# ti.field
#   - a global N-d array of elements N维数组
#   - can be used both Taichi-scope and Python-scope
#   - N-d (Scalar: N=0), (Vector: N=1), (Matrix: N=2), ...
#   - elements: scalar, vecotr, matrix, struct
#   - access elements

# 例如一个256x256面积的场中所有点的温度(float)数组
heat_field = ti.field(dtype=ti.f32, shape=(256, 256))
# access element, set point of [10,10] temperature = 100.0
heat_field[10, 10] = 100.0

# 标量数组
zero_d_scalar = ti.field(ti.f32, shape=())
zero_d_scalar[None] = 1.5

# 向量数组
vf = ti.Vector.field(3, ti.f32, shape=4)
v = ti.Vector([1, 2, 3])
vf[0] = v   # access element, set vf[0] = ti.Vector([1, 2, 3])

# Examples

# 256x256x128的房间中，充满所有点的三维向量场，例如重力场
gravitaition_field = ti.Vector.field(n=3, dtype=ti.f32, shape=(256, 256, 128))

# 64x64的网格中的二维形变张量场
strain_tensor_field = ti.Matrix.field(n=2, m=2, dtype=ti.f32, shape=(64, 64))

# A global scalar that i want to access in a Taichi kernel
global_scalar = ti.field(dtype=ti.f32, shape=())
```

## ti.kernel & ti.func

### ti.kernel

在ti.kernel修饰后，就被识别编译成taichi作用域中的程序；

```python
# about ti.kernel
def python_func():
    ti_kernel()


@ti.kernel
def kernel_call_kernel():
    ti_kernel()


@ti.kernel
def ti_kernel():
    print("ti.kenrel")


if __name__ == "__main__":
    kernel_call_kernel()
```

在执行上面代码的时候会报错；

```
Kernels cannot call other kernels. I.e., nested kernels are not allowed. Please check if you have direct/indirect invocation of kernels within kernels. Note that some methods provided by the Taichi standard library may invoke kernels, and please move their invocations to Python-scope.
```

但是执行python_func()可运行；

#### Parallelize

在ti.kernel修饰后，当检测到程序是在并行环境中运行时，最外层的域（循环或是条件）会自动并行；

```python
@ti.kernel
def fill():
    for i in range(10):     # parallelized
        x[i] += i

        s = 0
        for j in range(5):  # Seriallized in each parallel thread
            s += j
        y[i] = s
    for k in range(20):     # parallelized
        z[k] = k
```

因此如果要最大化优化，需要考虑如何处理循环和条件的并行串行关系，可以把多层循环拆分，用ti.kernel去修饰可以并行的循环或条件

#### Race condition

有两种用法可以避免data race的情况：

- 操作符连用，例如`total+=x[i]`
- atomic函数，例如`ti.atomic_add(total,x[i])`

如果使用total = total + x[i]，则可能因为并行而发生data race

#### pass argument

```python
def argument(a: ti.f32, b: ti.f64)
	print(a, b)
    
argument(1.0, 2.0)	# 1.0 2.0
```

#### return value

```python
def returnVal() -> ti.i32	# to return int32 value
	return 233.666

print(returnVal())	# 233
```

### ti.func

与ti.kernel不同的是，只能从ti.kernel中被调用，必须处在ti.kernel作用域中

帮助重复使用某个函数，强制内联而不支持递归

## visualize

GUI

```python
gui = ti.GUI("sample", (512, 512))
    while gui.running:
        # ...
        gui.show()
```

## Sample

init - data - computaiton - visualizaiton

