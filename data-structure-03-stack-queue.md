---
title: data_structure_03_stack_queue
top: false
comment: false
lang: zh-CN
date: 2022-11-09 09:40:05
tags:
categories:
  - study
  - course
  - data structure
---

# Stack & Queue

重点内容：

- 栈和队列的基本概念
- 栈和队列的顺序存储结构
- 栈和队列的链式存储结构
- 栈和队列的应用
- 特殊矩阵的压缩存储

知识框架：

![](./data-structure-03-stack-queue\figure_01.jpg)

## 栈 stack

### 栈的基本概念

栈(stack) - 许在一段进行插入或删除操作的线性表

栈顶(top) - 允许进行插入和删除的一端

栈底(bottom) - 固定的，不允许进行插入和删除的一端

空栈(empty stack) - 不含任何元素的空表

### 栈的性质

- 后进先出(Last In First Out, LIFO)
- $n$ 个不同元素进栈，出栈元素不同排列的个数为 $\frac{1}{n+1}C {2n\choose n}$，该公式也被称为卡特兰(Catalan)数，可采用数学归纳法证明

### 栈的基本操作

- `InitStack(&S)` - 初始化空栈S
- `StackEmpty(S)` - 判断栈是否为空，空则返回`true`，不空则返回`false`
- `Push(&S, x)` - 进栈，若S未满，则将x加入
- `Pop(&S, &x)` - 出栈，若S非空，弹出并返回给x
- `GetTop(S, &x)` - 获取栈顶元素，用x返回
- `DestroyStack(&S)` - 销毁栈，释放空间

### 栈的顺序存储

采用顺序存储结构的栈称为**顺序栈**，利用一组地址连续的存储单元存放自栈底到栈顶的数据元素，用一个指针`top`指示当前栈顶元素的位置。

```cpp
typedef int ElemType;

#define MAXSIZE 50

typedef struct
{
    ElemType data[MAXSIZE]; /* 数组存放入栈元素 */
    int top;                /* 指针，指向栈顶 */
} SeqStack;
```

下面是一些基础操作：

```cpp
/* 栈初始化 */
void InitStack(SeqStack &S)
{
    /* 指针设置为-1 */
    S.top = -1;
}

/* 判空 */
bool isStackEmpty(SeqStack S)
{
    if (S.top == -1)
        return true;
    else
        return false;
}

/* 入栈 */
SeqStack Push(SeqStack &S, ElemType x)
{
    if (S.top >= MAXSIZE)
        return S;
    else
    {
        /* 指针+1后，数组对应索引赋值为x */
        S.top++;
        S.data[S.top] = x;
    }
    return S;
}

/* 弹出 */
SeqStack Pop(SeqStack &S, ElemType &x)
{
    if (!isStackEmpty(S))
    {
        x = S.data[S.top];
        S.top--;
    }
    return S;
}

/* 获取栈顶元素 */
ElemType GetTop(SeqStack S)
{
    if (!isStackEmpty(S))
        return S.data[S.top];
    else
        return NULL;
}
```

利用栈底位置相对不变的特性，两个顺序栈共享一个一维数组空间，将两个栈的栈底分别设置在共享空间的两端，两个栈顶想可共享空间的中间延伸。这种形式称为：**共享栈**。

![](./data-structure-03-stack-queue\figure_02.jpg)

- `topA=-1, topB=MAXSIZE` 时，A、B栈为空
- `topB - topA = 1` 时，共享栈满
- 进栈：
  - stackA：S.data[++S.top]
  - stackB：S.data[--S.top]
- 出栈：
  - stackA：S.data[S.top--]
  - stackB：S.data[S.top++]

### 栈的链式存储

链栈的优点时便于多个栈共享存储空间，提高效率，且不存在栈满溢出的情况。

规定单链表的表头为操作端，且规定链栈没有头结点。

```cpp
/* 链栈 */
typedef struct LNode
{
    ElemType data;
    LNode *next;

} LNode, *LinkStack;
```

### 课后题

```cpp
#include <stdio.h>
#include <string.h>
#include <malloc.h>
#include <stack>

typedef int ElemType;

/* 链表结点 */
typedef struct LNode
{
    /* data */
    ElemType data;      // 数据单元
    struct LNode *next; // 指向下一个 node 的指针
} LNode, *Linklist;     // 可以表示单结点或是指向整个链表的指针

void PrintLinkList(Linklist L)
{
    while (L->next)
    {
        L = L->next; /* 适用于带头结点时，越过头结点直接指向第一个元素域 */
        printf("%d ", L->data);
    }
    printf("\n");
}

Linklist InsertListFromTail(Linklist &L, int n)
{
    /* 头结点 */
    L = (Linklist)malloc(sizeof(LNode));
    L->next = NULL;

    /* 引入一个尾结点，初始化为与头结点相同的域 */
    LNode *tail = L;
    for (int i = 0; i < n; i++)
    {
        /* 获取数据域 */
        ElemType x;
        scanf_s("%c", &x);
        LNode *p = (LNode *)malloc(sizeof(LNode));
        p->data = x;
        /* 添加在 tail 后 */
        p->next = tail->next;
        tail->next = p;
        /* tail = p，使得下一个插入结点能够在 p 之后 */
        tail = p;
    }
    tail->next = NULL; // 尾结点指针置空
    return L;
}

/* 顺序栈 */
#define MAXSIZE 50
typedef struct
{
    ElemType data[MAXSIZE]; /* 数组存放入栈元素 */
    int top;                /* 指针，指向栈顶 */
} SeqStack;

/* 链栈 */
typedef struct LSNode
{
    ElemType data;
    LSNode *next;

} LSNode, *LinkStack;

/* 栈初始化 */
void InitStack(SeqStack &S)
{
    /* 指针设置为-1 */
    S.top = -1;
}

/* 判空 */
bool isStackEmpty(SeqStack S)
{
    if (S.top == -1)
        return true;
    else
        return false;
}

/* 入栈 */
SeqStack Push(SeqStack &S, ElemType x)
{
    if (S.top >= MAXSIZE)
        return S;
    else
    {
        /* 指针+1后，数组对应索引赋值为x */
        S.top++;
        S.data[S.top] = x;
    }
    return S;
}

/* 弹出 */
SeqStack Pop(SeqStack &S, ElemType &x)
{
    if (!isStackEmpty(S))
    {
        x = S.data[S.top];
        S.top--;
    }
    return S;
}

/* 获取栈顶元素 */
ElemType GetTop(SeqStack S)
{
    if (!isStackEmpty(S))
        return S.data[S.top];
    else
        return NULL;
}

/* Q3.判定操作是否合法 */
bool isLegalOperate_Q3(char op[])
{
    int i = 0;
    int j = 0;
    while (op[i] != '\0')
    {
        switch (op[i])
        {
        case 'I':
            j++;
            break;
        case 'O':
            j--;
            if (j < 0)
            {
                printf("illegal\n");
                return false;
            }
        default:
            break;
        }
        i++;
    }
    if (j != 0)
    {
        printf("illegal\n");
        return false;
    }
    else
    {
        printf("legal\n");
        return true;
    }
}

/* Q4.单链表表头指针为L，data为字符型，设计算法判断链表的全部n个字符是否中心对称 */
bool isSymmetry_Q4(Linklist L, int n)
{
    /* 使用顺序栈，压入单链表前1/2的元素，然后逐一判断后续元素和栈顶元素是否一一相等 */
    int i;
    ElemType *s = (ElemType *)malloc(sizeof(ElemType) * (n / 2));
    LNode *p = L->next;
    for (i = 0; i < n / 2; i++)
    {
        s[i] = p->data;
        p = p->next;
    }
    i--;
    if (n % 2 == 1)
        p = p->next;
    while (p && s[i] == p->data)
    {
        i--;
        p = p->next;
    }
    if (i == -1)
        return true;
    else
        return false;
}

/* Q5. 两个栈s1，s2 都采用顺序栈，共享一个存储区[0,...,maxsize-1]，采用栈顶相向，迎面增长的方式存储，设计s1、s2有关的入栈和出栈算法 */
typedef struct
{
    /* data */
    ElemType data[MAXSIZE];
    int top[2];
} shareS;

bool sharePush(shareS ss, int i, ElemType x)
{
    if (i != 0 || i != 1)
        return false;
    if (ss.top[1] - ss.top[0] == 1)
        return false;
    else
    {
        if (i == 0)
            ss.data[++ss.top[0]] = x;
        else
            ss.data[--ss.top[1]] = x;
    }
    return true;
}

bool sharePop(shareS ss, int i, ElemType &x)
{
    if (i != 0 || i != 1)
        return false;
    switch (i)
    {
    case 0:
        if (ss.top[0] != -1)
        {
            x = ss.data[ss.top[0]--];
            return true;
        }
    case 1:
        if (ss.top[1] != MAXSIZE)
        {
            x = ss.data[ss.top[1]++];
            return true;
        }
    }
    return false;
}

int main()
{
    /* Q3 */
    // char op[8];
    // for (int i = 0; i < 8; i++)
    // {
    //     scanf_s("%c", &op[i]);
    // }
    // isLegalOperate_Q3(op);

    /* Q4 */
    /* Linklist L;
    InsertListFromTail(L, 5);
    if (isSymmetry_Q4(L, 5))
    {
        printf("is symmetry.\n");
    }
    else
    {
        printf("not symmetry.\n");
    } */

    /* Q5 */

    return 0;
}
```

## 队列 queue

### 队列的基本概念

Queue也是操作受限的线性表，只允许在表的一端进行插入，在表的另一端进行删除。

### 队列的性质

- 先进先出 First In First Out，FIFO
- 队头 Front，允许删除的一端
- 队尾 Rear，允许插入的一端
- 空队列，不含任何元素的空表

### 队列的基本操作

- `InitQueue(&Q)` - 初始化队列，构造一个空队列Q
- `QueueEmpty(Q)` - 队列判空
- `EnQueue(&Q, x)` - 入队，若队列Q未满则将x加入，x称为新的队尾
- `DeQueue(&Q, &x)` - 出队，若队列Q非空，删除队头元素，用x返回
- `GetHead(Q, &x)` - 读队头元素，返回给x

不能随意读取栈或队列中间的某个数据。

### 队列的顺序存储

一般采用的是循环队列：

- 入队时，使用`(front+1)%MAXSIZE`更新rear的值
- 出队时，使用`(front+1)%MAXSIZE`更新front的值
- 牺牲一个元素的空间，用于区分队空和队满：
  - `(rear + 1) % MAXSIZE == CQ.front` 时，表示队满
  - `rear == front` 时，表示队空

数据结构

```cpp
typedef struct
{
    /* data */
    ElemType data[MAXSIZE];
    int front, rear;
} CircleQueue;
```

初始化

```cpp
CircleQueue InitCirQueue(CircleQueue &CQ)
{
    CQ.front = 0;
    CQ.rear = 0;
    return CQ;
}
```

判空

```cpp
bool isCirQueueEmpty(CircleQueue CQ)
{
    if (CQ.front == CQ.rear)
        return true;
    else
        return false;
}
```

入队和出队

```cpp
bool EnCirQueue(CircleQueue &CQ, ElemType x)
{
    if ((CQ.rear + 1) % MAXSIZE == CQ.front)
        return false;

    CQ.data[CQ.rear] = x;
    CQ.rear = (CQ.rear + 1) % MAXSIZE;
    return true;
}

bool DeCirQueue(CircleQueue &CQ, ElemType &x)
{
    if (CQ.rear == CQ.front)
        return false;

    x = CQ.data[CQ.front];
    CQ.front = (CQ.front + 1) % MAXSIZE;
    return true;
}
```

### 队列的链式存储

实际上，链式队列是一个带有头指针和尾指针的单链表。

数据结构

```cpp
typedef struct
{
    /* data */
    ElemType data;
    struct LinkNode *next;
} LinkNode;

typedef struct
{
    /* data */
    LinkNode *front, *rear;
} LinkQueue;

```

初始化

```cpp
LinkQueue InitLQueue(LinkQueue &LQ)
{
    LQ.front = LQ.rear = (LinkNode *)malloc(sizeof(LinkNode));
    LQ.front->next = NULL;
}
```

判空

```cpp
bool IsLQueueEmpty(LinkQueue LQ)
{
    if (LQ.front == LQ.rear)
        return true;
    else
        return false;
}
```

入队和出队

```cpp
bool EnLQueue(LinkQueue &LQ, ElemType x)
{
    LinkNode *newElement = (LinkNode *)malloc(sizeof(LinkNode));
    newElement->data = x;
    newElement->next = NULL;

    LQ.rear->next = newElement;
    LQ.rear = newElement;
    return true;
}

bool DeLQueue(LinkQueue &LQ, ElemType &x)
{
    if (LQ.rear == LQ.front)
        return false;

    LinkNode *delElement = LQ.front->next;
    x = delElement->data;

    LQ.front->next = delElement->next;
    if (delElement == LQ.rear)
        LQ.front == LQ.rear;
    delete (delElement);

    return true;
}
```

### 双端队列

双端队列：允许两端都可以入队和出队的队列，分为前端和后端。

输入受限的双端队列：在一端进行插入和删除，而另一端只允许删除。（只有一端可以输入，两端都可以输出）

输出受限的双端队列：在一端进行插入和删除，而另一端只允许插入。（只有一端可以输出，两端都可以输入）

### 课后题

```cpp
/* Q1. 使用tag来最大化利用循环队列的空间 */
#include <stdio.h>
#include <malloc.h>
#define MAXSIZE 50

typedef int ElemType;

typedef struct
{
    /* data */
    ElemType data[MAXSIZE];
    int front, rear;
    int tag; /* 用于区分队满还是队空 0空 1满 */
} CircleQueue;

CircleQueue InitCirQueue(CircleQueue &CQ)
{
    CQ.front = 0;
    CQ.rear = 0;
    CQ.tag = 0;
    return CQ;
}

bool EnCirQueue(CircleQueue &CQ, ElemType x)
{
    if (CQ.rear == CQ.front && CQ.tag == 1)
        return false;

    CQ.data[CQ.rear] = x;
    CQ.rear = (CQ.rear + 1) % MAXSIZE;
    CQ.tag = 1;
    return true;
}

bool DeCirQueue(CircleQueue &CQ, ElemType &x)
{
    if (CQ.rear == CQ.front)
        return false;

    x = CQ.data[CQ.front];
    CQ.front = (CQ.front + 1) % MAXSIZE;
    CQ.tag = 0;
    return true;
}
```



```cpp
/* Q2. 逆置队列 */
CircleQueue ReverseQueue(CircleQueue &CQ, SeqStack &S)
{
    /* 其实就是利用队列和栈的性质，将队列的元素依次压入栈中，将栈中的元素依次弹出到队列中 */
    ElemType x;
    while (!isCirQueueEmpty)
    {
        DeCirQueue(CQ, x);
        printf("%d", x);
        Push(S, x);
    }
    while (!isStackEmpty)
    {
        Pop(S, x);
        printf("%d", x);
        EnCirQueue(CQ, x);
    }
}
```

## 栈和队列的应用

- 栈：括号匹配、表达式求值、递归
- 队列：层次遍历

### 括号匹配

```cpp
bool isMatch(char *c, int n)
{
    SeqStack S;
    InitStack(S);
    char ch;
    for (int i = 0; i < n; i++)
    {
        switch (c[i])
        {
        case '{' || '[' || '(':
            Push(S, c[i]);
            break;

        case '}':
            if (GetTop(S) == '{')
                Pop(S, ch);
            break;

        case ']':
            if (GetTop(S) == '[')
                Pop(S, ch);
            break;
        case '(':
            if (GetTop(S) == ')')
                Pop(S, ch);
            break;
        default:
            break;
        }
    }

    if (isStackEmpty(S))
    {
        return true;
    }
    else
        return false;
}
```

