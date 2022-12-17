---
title: glibc2.23_ptmalloc
top: false
comment: false
lang: zh-CN
date: 2021-11-22 21:50:06
tags:
categories:
  - CTF
  - PWN
---

# glibc2.23 ptmalloc2分析

malloc

### __libc_malloc

跳过了初始化和定义的部分，先从`__libc_malloc`开始；

```c
void *
__libc_malloc (size_t bytes)
{
  mstate ar_ptr;
  void *victim;

  void *(*hook) (size_t, const void *)
    = atomic_forced_read (__malloc_hook);
  if (__builtin_expect (hook != NULL, 0))
    return (*hook)(bytes, RETURN_ADDRESS (0));
```

首先判断了是否定义了hook函数，如果`hook != NULL`，则return到该函数地址，如果没有hook函数，则执行：

```c
  arena_get (ar_ptr, bytes);

/* 
  thread_arena = &main_arena;

#define arena_get(ptr, size) do { \
      ptr = thread_arena;						      \
      arena_lock (ptr, size);						      \
  } while (0)
*/    
```

指针指向一个空闲的`main_arena`，然后将该区域标志为已分配；

```c
  victim = _int_malloc (ar_ptr, bytes);
  /* Retry with another arena only if we were able to find a usable arena
     before.  */
  if (!victim && ar_ptr != NULL)
    {
      LIBC_PROBE (memory_malloc_retry, 1, bytes);
      ar_ptr = arena_get_retry (ar_ptr, bytes);
      victim = _int_malloc (ar_ptr, bytes);
    }
```

`_int_malloc`函数调用后获得内存地址，如果内存地址申请失败，则会再次尝试获取空闲的`arena`，并malloc一个堆块；

```c
  if (ar_ptr != NULL)
    (void) mutex_unlock (&ar_ptr->mutex);
```

成功malloc后，将解锁分配的区域；

```c
  assert (!victim || chunk_is_mmapped (mem2chunk (victim)) ||
          ar_ptr == arena_for_chunk (mem2chunk (victim)));

/* check for mmap()'ed chunk */
#define chunk_is_mmapped(p) ((p)->size & IS_MMAPPED)

/* find the heap and corresponding arena for a given ptr */
#define arena_for_chunk(ptr) \
  (chunk_non_main_arena (ptr) ? heap_for_ptr (ptr)->ar_ptr : &main_arena)
```

如果申请内存失败，申请到的是mmap的内存，申请到的内存在分配的`arena`中；

```c
  return victim;
}
```

返回内存；

其中关键的`victim`来源于`__init_malloc`，下面分析`__init_malloc`；

### _int_malloc

成员变量

```c
static void *
_int_malloc (mstate av, size_t bytes)
{  
  INTERNAL_SIZE_T nb;               /* normalized request size */
  unsigned int idx;                 /* associated bin index */
  mbinptr bin;                      /* associated bin */

  mchunkptr victim;                 /* inspected/selected chunk */
  INTERNAL_SIZE_T size;             /* its size */
  int victim_index;                 /* its bin index */

  mchunkptr remainder;              /* remainder from a split */
  unsigned long remainder_size;     /* its size */

  unsigned int block;               /* bit map traverser */
  unsigned int bit;                 /* bit map traverser */
  unsigned int map;                 /* current word of binmap */

  mchunkptr fwd;                    /* misc temp for linking */
  mchunkptr bck;                    /* misc temp for linking */

  const char *errstr = NULL;       // 用于打印错误字符并返回
```

```c
  /*
     Convert request size to internal form by adding SIZE_SZ bytes
     overhead plus possibly more to obtain necessary alignment and/or
     to obtain a size of at least MINSIZE, the smallest allocatable
     size. Also, checked_request2size traps (returning 0) request sizes
     that are so large that they wrap around zero when padded and
     aligned.
   */

  checked_request2size (bytes, nb);

#define checked_request2size(req, sz)                             \
  if (REQUEST_OUT_OF_RANGE (req)) {					      \
      __set_errno (ENOMEM);						      \
      return 0;								      \
    }									      \
  (sz) = request2size (req);
```

检查申请的size是否符合要求；如果超出了范围，则返回，否则将将size转换为chunk_size；

```c
  /* There are no usable arenas.  Fall back to sysmalloc to get a chunk from
     mmap.  */
  if (__glibc_unlikely (av == NULL))
    {
      void *p = sysmalloc (nb, av);
      if (p != NULL)
	alloc_perturb (p, bytes);
      return p;
    }
```

关于`__glibc_unlikely`和`__builtin_except()`的内容，参考一篇[博客](https://blog.csdn.net/weixin_42157432/article/details/115805804?spm=1001.2101.3001.6650.2&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7Edefault-2.no_search_link&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7Edefault-2.no_search_link)；

这里再次判断是否没有申请到内存，如果没有申请到，将会调用`sysmalloc`尝试获取内存地址，如果获取到了，则调用`alloc_perturb`清理内存空间，返回内存地址；

```c
  /*
     If the size qualifies as a fastbin, first check corresponding bin.
     This code is safe to execute even if av is not yet initialized, so we
     can try it without checking, which saves some time on this fast path.
   */

  if ((unsigned long) (nb) <= (unsigned long) (get_max_fast ()))
    {
      idx = fastbin_index (nb);
      mfastbinptr *fb = &fastbin (av, idx);
      mchunkptr pp = *fb;
```

如果申请到了内存空间，判断申请的内存大小是否小于`get_max_fast`，即fastbin中有满足size的堆块，则根据大小获取索引`idx`。定义fastbin指针指向当前索引的fastbin链表；

```c
      do
        {
          victim = pp;
          if (victim == NULL)
            break;
        }
      while ((pp = catomic_compare_and_exchange_val_acq (fb, victim->fd, victim))
             != victim);
```

接着将内存地址指向该fastbin链表，并且`fastbin = fastbin -> fd`；

```c
      if (victim != 0)
        {
          if (__builtin_expect (fastbin_index (chunksize (victim)) != idx, 0))
            {
              errstr = "malloc(): memory corruption (fast)";
            errout:
              malloc_printerr (check_action, errstr, chunk2mem (victim), av);
              return NULL;
            }
          check_remalloced_chunk (av, victim, nb);
          void *p = chunk2mem (victim);
          alloc_perturb (p, bytes);
          return p;
        }
    }
```

如果申请到了内存空间，判断 `victim_size`对应的fastbin索引是否不等于size对应的fastbin索引，即是不是符合这个链表的大小，如果不符合则抛出异常，符合则检查chunk的合法性。随后将`victim chunk`指针转换为用户数据区指针，调用`alloc_perturb()`对用户数据区进行初始化；

```c
  /*
     If a small request, check regular bin.  Since these "smallbins"
     hold one size each, no searching within bins is necessary.
     (For a large request, we need to wait until unsorted chunks are
     processed to find best fit. But for small ones, fits are exact
     anyway, so we can check now, which is faster.)
   */

  if (in_smallbin_range (nb))
    {
      idx = smallbin_index (nb);
      bin = bin_at (av, idx);

      if ((victim = last (bin)) != bin)
        {
          if (victim == 0) /* initialization check */
            malloc_consolidate (av);
          else
            {
              bck = victim->bk;
```

如果是在`smallbin`的范围内，则获取对应的索引，定位索引所在的区域。接着让`victim = last(bin)`，即为`smallbin->bk`，判断是否为`smallbin`，即判断`smallbin`是否为空。接着判断`smallbin->bk`是否为`0`，是的话就进行初始化；

```c
	          if (__glibc_unlikely (bck->fd != victim))
                {
                  errstr = "malloc(): smallbin double linked list corrupted";
                  goto errout;
                }
              set_inuse_bit_at_offset (victim, nb);
              bin->bk = bck;
              bck->fd = bin;

              if (av != &main_arena)
                victim->size |= NON_MAIN_ARENA;
              check_malloced_chunk (av, victim, nb);
              void *p = chunk2mem (victim);
              alloc_perturb (p, bytes);
              return p;
            }
        }
    }.
```

如果`victim -> bk -> fd != victim`，则抛出异常。设置下一个chunk的PREV_INUSE为1，将`victim`从链表中取下，即`smallbin -> bk = smallbin -> bk -> bk`，`smallbin -> bk -> fd = smallbin`。然后判断分配的区域是否为`main_arena`，如果不是则设置`victim->size`为`NON_MAIN_ARENA`。接着检查合法性，将`victim chunk`指针转换为用户数据区指针，调用`alloc_perturb()`对用户数据区进行初始化，并返回该地址指针；

```c
  /*
     If this is a large request, consolidate fastbins before continuing.
     While it might look excessive to kill all fastbins before
     even seeing if there is space available, this avoids
     fragmentation problems normally associated with fastbins.
     Also, in practice, programs tend to have runs of either small or
     large requests, but less often mixtures, so consolidation is not
     invoked all that often in most programs. And the programs that
     it is called frequently in otherwise tend to fragment.
   */

  else
    {
      idx = largebin_index (nb);
      if (have_fastchunks (av))
        malloc_consolidate (av);
    }
```

如果属于`largebin`的大小范围，则获取`largebin`的索引，调用`have_fastchunks`查看当前分配区域内是否存在fastbin，如果存在则进行初始化；

```c
  /*
     Process recently freed or remaindered chunks, taking one only if
     it is exact fit, or, if this a small request, the chunk is remainder from
     the most recent non-exact fit.  Place other traversed chunks in
     bins.  Note that this step is the only place in any routine where
     chunks are placed in bins.

     The outer loop here is needed because we might not realize until
     near the end of malloc that we should have consolidated, so must
     do so and retry. This happens at most once, and only when we would
     otherwise need to expand memory to service a "small" request.
   */

  for (;; )
    {
      int iters = 0;
      while ((victim = unsorted_chunks (av)->bk) != unsorted_chunks (av))
        {
          bck = victim->bk;
          if (__builtin_expect (victim->size <= 2 * SIZE_SZ, 0)
              || __builtin_expect (victim->size > av->system_mem, 0))
            malloc_printerr (check_action, "malloc(): memory corruption",
                             chunk2mem (victim), av);
          size = chunksize (victim);
```

如果`unsortedbin`不为空，则令`bck = victim->bk = unsortedbin->bk`，判断`size`的合法性，如果不合法，则抛出异常；

```c
          /*
             If a small request, try to use last remainder if it is the
             only chunk in unsorted bin.  This helps promote locality for
             runs of consecutive small requests. This is the only
             exception to best-fit, and applies only when there is
             no exact fit for a small chunk.
           */

          if (in_smallbin_range (nb) &&
              bck == unsorted_chunks (av) &&
              victim == av->last_remainder &&
              (unsigned long) (size) > (unsigned long) (nb + MINSIZE))
            {
              /* split and reattach remainder */
              remainder_size = size - nb;
              remainder = chunk_at_offset (victim, nb);
              unsorted_chunks (av)->bk = unsorted_chunks (av)->fd = remainder;
              av->last_remainder = remainder;
              remainder->bk = remainder->fd = unsorted_chunks (av);
```

如果大小属于`smallbin`，而且只存在一个`unsortedbin`，并且当前的`unsorted chunk`属于`last_remainder`，`unsortedbin`的`size`还大于`申请的nb`+`MINSIZE`，则切分这个`unsorted chunk`，剩下的部分放回链表；

```c
              if (!in_smallbin_range (remainder_size))
                {
                  remainder->fd_nextsize = NULL;
                  remainder->bk_nextsize = NULL;
                }
```

如果剩下的chunk大小不属于`smallbin`范围，则将剩下的chunk的`fd_nextsize`和`bk_nextsize`置`NULL`；

```c
              set_head (victim, nb | PREV_INUSE |
                        (av != &main_arena ? NON_MAIN_ARENA : 0));
              set_head (remainder, remainder_size | PREV_INUSE);
              set_foot (remainder, remainder_size);

              check_malloced_chunk (av, victim, nb);
              void *p = chunk2mem (victim);
              alloc_perturb (p, bytes);
              return p;
            }
```

设置`victim`的`prer_inuse`和`main_arena`标志位，设置`last_remainder`的`prev_inuse`标志位和`last_remainder`下一个块的`prev_inuse`标志位，将`victim`转换为用户数据区地址，调用`alloc_perturb()`对用户数据区进行初始化；

```c
          /* remove from unsorted list */
          unsorted_chunks (av)->bk = bck;
          bck->fd = unsorted_chunks (av);
```

从`unsorted bin`中移除已经分配的`victim chunk`，`unsortedbin->bk = victim->bk`，`victim->bk = unsortedbin`；

```c
          /* Take now instead of binning if exact fit */

          if (size == nb)
            {
              set_inuse_bit_at_offset (victim, size);
              if (av != &main_arena)
                victim->size |= NON_MAIN_ARENA;
              check_malloced_chunk (av, victim, nb);
              void *p = chunk2mem (victim);
              alloc_perturb (p, bytes);
              return p;
            }
```

判断当前切割出来的`unsortedbin_size`是否等于申请的`chunk size`，如果是，则设置`victim`的下一个chunk的`prev_inuse`位以及`main_arena`位，检查chunk的合法性，将`victim`转换为用户数据区地址，调用`alloc_perturb()`对用户数据区进行初始化；

```c
          /* place chunk in bin */

          if (in_smallbin_range (size))
            {
              victim_index = smallbin_index (size);
              bck = bin_at (av, victim_index);
              fwd = bck->fd;
            }
```

如果切割出来的chunk属于`smallbin`的大小范围，则获得其前后堆块的指针；

```c
          else
            {
              victim_index = largebin_index (size);
              bck = bin_at (av, victim_index);
              fwd = bck->fd;
```

否则获得其前后堆块的指针；

```c
              /* maintain large bins in sorted order */
              if (fwd != bck)
                {
                  /* Or with inuse bit to speed comparisons */
                  size |= PREV_INUSE;
                  /* if smaller than smallest, bypass loop below */
                  assert ((bck->bk->size & NON_MAIN_ARENA) == 0);
```

如果`largebin`不为空，则设置`victim`的`prev_inuse`为`1`；

```c
                  if ((unsigned long) (size) < (unsigned long) (bck->bk->size))   
                    {
                      fwd = bck;       // bck->fd = bck
                      bck = bck->bk;   // bck = bck->bk

                      victim->fd_nextsize = fwd->fd;
                      victim->bk_nextsize = fwd->fd->bk_nextsize;
                      fwd->fd->bk_nextsize = victim->bk_nextsize->fd_nextsize = victim;
                    }
                  else
                    {
                      assert ((fwd->size & NON_MAIN_ARENA) == 0);
                      while ((unsigned long) size < fwd->size)
                        {
                          fwd = fwd->fd_nextsize;
                          assert ((fwd->size & NON_MAIN_ARENA) == 0);
                        }

                      if ((unsigned long) size == (unsigned long) fwd->size)
                        /* Always insert in the second position.  */
                        fwd = fwd->fd;
                      else
                        {
                          victim->fd_nextsize = fwd;
                          victim->bk_nextsize = fwd->bk_nextsize;
                          fwd->bk_nextsize = victim;
                          victim->bk_nextsize->fd_nextsize = victim;
                        }
                      bck = fwd->bk;
                    }
                }
              else
                victim->fd_nextsize = victim->bk_nextsize = victim;
            }
```

将`victim`按大小顺序插入到`largebin`中；

```c
          mark_bin (av, victim_index);
          victim->bk = bck;
          victim->fd = fwd;
          fwd->bk = victim;
          bck->fd = victim;
```

设置分配区的`binmap`，将`victim`插入目标位置；

```c
#define MAX_ITERS       10000
          if (++iters >= MAX_ITERS)
            break;
        }
```

迭代器+1，当迭代器数值大于`10000`时`break`；

```c
      /*
         If a large request, scan through the chunks of current bin in
         sorted order to find smallest that fits.  Use the skip list for this.
       */

      if (!in_smallbin_range (nb))
        {
          bin = bin_at (av, idx);
```

如果申请的`size`不在`smallbin`范围内，则定位到`largebin`；

```c
          /* skip scan if empty or largest chunk is too small */
          if ((victim = first (bin)) != bin &&
              (unsigned long) (victim->size) >= (unsigned long) (nb))
            {
              victim = victim->bk_nextsize;
              while (((unsigned long) (size = chunksize (victim)) <
                      (unsigned long) (nb)))
                victim = victim->bk_nextsize;
```

如果`largebin`不为空，并且`victim chunk size`大于申请的size，则`victim`等于下一个更小的chunk，直到大小正合适位置；

```c
              /* Avoid removing the first entry for a size so that the skip
                 list does not have to be rerouted.  */
              if (victim != last (bin) && victim->size == victim->fd->size)
                victim = victim->fd;
```

如果`victime`不是最后一个`largebin`，并且size等于前一个chunk的size，则让`victim`为前一个chunk，即大小相等的时候，挑后一个，这样避免了重新更新列表；

```c
              remainder_size = size - nb;
              unlink (av, victim, bck, fwd);
```

剩下的块减去申请的大小，然后调用`unlink`脱链`victim chunk`；

```c
              /* Exhaust */
              if (remainder_size < MINSIZE)
                {
                  set_inuse_bit_at_offset (victim, size);
                  if (av != &main_arena)
                    victim->size |= NON_MAIN_ARENA;
                }
```

判断剩下的大小是否小于`MINSIZE`最小可分配大小，即是否耗尽了这个chunk，如果是，则即将`victim chunk`的下一个chunk的`prev_inuse`设置为1，并设置`main_arena`标志位；

```c
              /* Split */
              else
                {
                  remainder = chunk_at_offset (victim, nb);
                  /* We cannot assume the unsorted list is empty and therefore
                     have to perform a complete insert here.  */
                  bck = unsorted_chunks (av);
                  fwd = bck->fd;
```

如果没有耗尽，则获取剩下的堆块，放到`unsortedbin`中；

```c
	              if (__glibc_unlikely (fwd->bk != bck))
                    {
                      errstr = "malloc(): corrupted unsorted chunks";
                      goto errout;
                    }
                  remainder->bk = bck;
                  remainder->fd = fwd;
                  bck->fd = remainder;
                  fwd->bk = remainder;
```

如果`bck->fd->bk != bck`，则抛出异常，否则将剩余堆块放入`unsortedbin`中；

```c
                  if (!in_smallbin_range (remainder_size))
                    {
                      remainder->fd_nextsize = NULL;
                      remainder->bk_nextsize = NULL;
                    }
                  set_head (victim, nb | PREV_INUSE |
                            (av != &main_arena ? NON_MAIN_ARENA : 0));
                  set_head (remainder, remainder_size | PREV_INUSE);
                  set_foot (remainder, remainder_size);
                }
              check_malloced_chunk (av, victim, nb);
              void *p = chunk2mem (victim);
              alloc_perturb (p, bytes);
              return p;
            }
        }
```

如果剩余堆块不属于`smallbin`范围，则将其`fd_nextsize`和`bk_nextsize`置为NULL，并设置`victim`的`prer_inuse`和`main_arena`标志位，设置`last_remainder`的`prev_inuse`标志位和`last_remainder`下一个块的`prev_inuse`标志位，将`victim`转换为用户数据区地址，调用`alloc_perturb()`对用户数据区进行初始化；

```c
     /*
         Search for a chunk by scanning bins, starting with next largest
         bin. This search is strictly by best-fit; i.e., the smallest
         (with ties going to approximately the least recently used) chunk
         that fits is selected.

         The bitmap avoids needing to check that most blocks are nonempty.
         The particular case of skipping all bins during warm-up phases
         when no chunks have been returned yet is faster than it might look.
       */

      ++idx;
      bin = bin_at (av, idx);
      block = idx2block (idx);
      map = av->binmap[block];
      bit = idx2bit (idx);

      for (;; )
        {
          /* Skip rest of block if there are no more set bits in this block.  */
          if (bit > map || bit == 0)
            {
              do
                {
                  if (++block >= BINMAPSIZE) /* out of bins */
                    goto use_top;
                }
              while ((map = av->binmap[block]) == 0);

              bin = bin_at (av, (block << BINMAPSHIFT));
              bit = 1;
            }

          /* Advance to bin with set bit. There must be one. */
          while ((bit & map) == 0)
            {
              bin = next_bin (bin);
              bit <<= 1;
              assert (bit != 0);
            }

          /* Inspect the bin. It is likely to be non-empty */
          victim = last (bin);

          /*  If a false alarm (empty bin), clear the bit. */
          if (victim == bin)
            {
              av->binmap[block] = map &= ~bit; /* Write through */
              bin = next_bin (bin);
              bit <<= 1;
            }

          else
            {
              size = chunksize (victim);

              /*  We know the first chunk in this bin is big enough to use. */
              assert ((unsigned long) (size) >= (unsigned long) (nb));

              remainder_size = size - nb;

              /* unlink */
              unlink (av, victim, bck, fwd);

              /* Exhaust */
              if (remainder_size < MINSIZE)
                {
                  set_inuse_bit_at_offset (victim, size);
                  if (av != &main_arena)
                    victim->size |= NON_MAIN_ARENA;
                }

              /* Split */
              else
                {
                  remainder = chunk_at_offset (victim, nb);

                  /* We cannot assume the unsorted list is empty and therefore
                     have to perform a complete insert here.  */
                  bck = unsorted_chunks (av);
                  fwd = bck->fd;
	  if (__glibc_unlikely (fwd->bk != bck))
                    {
                      errstr = "malloc(): corrupted unsorted chunks 2";
                      goto errout;
                    }
                  remainder->bk = bck;
                  remainder->fd = fwd;
                  bck->fd = remainder;
                  fwd->bk = remainder;

                  /* advertise as last remainder */
                  if (in_smallbin_range (nb))
                    av->last_remainder = remainder;
                  if (!in_smallbin_range (remainder_size))
                    {
                      remainder->fd_nextsize = NULL;
                      remainder->bk_nextsize = NULL;
                    }
                  set_head (victim, nb | PREV_INUSE |
                            (av != &main_arena ? NON_MAIN_ARENA : 0));
                  set_head (remainder, remainder_size | PREV_INUSE);
                  set_foot (remainder, remainder_size);
                }
              check_malloced_chunk (av, victim, nb);
              void *p = chunk2mem (victim);
              alloc_perturb (p, bytes);
              return p;
            }
        }
```

`largebin`索引+1，遍历`largebin`寻找满足要求的空闲块(使用`binmap`和`block`缩短检索时间)，如果找到则按照上诉方式分配`victim`；

```c
      /*
         If large enough, split off the chunk bordering the end of memory
         (held in av->top). Note that this is in accord with the best-fit
         search rule.  In effect, av->top is treated as larger (and thus
         less well fitting) than any other available chunk since it can
         be extended to be as large as necessary (up to system
         limitations).

         We require that av->top always exists (i.e., has size >=
         MINSIZE) after initialization, so if it would otherwise be
         exhausted by current request, it is replenished. (The main
         reason for ensuring it exists is that we may need MINSIZE space
         to put in fenceposts in sysmalloc.)
       */

      victim = av->top;
      size = chunksize (victim);

      if ((unsigned long) (size) >= (unsigned long) (nb + MINSIZE))
        {
          remainder_size = size - nb;
          remainder = chunk_at_offset (victim, nb);
          av->top = remainder;
          set_head (victim, nb | PREV_INUSE |
                    (av != &main_arena ? NON_MAIN_ARENA : 0));
          set_head (remainder, remainder_size | PREV_INUSE);

          check_malloced_chunk (av, victim, nb);
          void *p = chunk2mem (victim);
          alloc_perturb (p, bytes);
          return p;
        }
```

`victim`指向`top_chunk`，`size`为`victim`的`size`，从`top_chunk`中分配chunk， 判断`top_chunk_size`是否大于等于`(size+MINSIZE)`， 如果是则切割`top_chunk`赋值给`victim`,将`victim`转换为用户数据区地址，调用`alloc_perturb()`对用户数据区进行初始化，返回地址指针；

```c
      /* When we are using atomic ops to free fast chunks we can get
         here for all block sizes.  */
      else if (have_fastchunks (av))
        {
          malloc_consolidate (av);
          /* restore original bin index */
          if (in_smallbin_range (nb))
            idx = smallbin_index (nb);
          else
            idx = largebin_index (nb);
        }
```

如果存在`fastbin`，则初始化分配区域，如果申请的大小在`smallbin`范围内，则定位到`smallbin`，否则定位到`larginbin`；

```c
      /*
         Otherwise, relay to handle system-dependent cases
       */
      else
        {
          void *p = sysmalloc (nb, av);
          if (p != NULL)
            alloc_perturb (p, bytes);
          return p;
        }
    }
}
```

如果不存在`fastbin`，则使用`sysmalloc`分配空间，使用`alloc_perturb` 清空内存，返回地址指针；

## malloc_consolidate

`malloc_consolidate`是对堆的一种整理，从`fast bin`中删除每个chunk并将其合并，然后将其放入`unsorted bin`中；

```c
/*
  ------------------------- malloc_consolidate -------------------------

  malloc_consolidate is a specialized version of free() that tears
  down chunks held in fastbins.  Free itself cannot be used for this
  purpose since, among other things, it might place chunks back onto
  fastbins.  So, instead, we need to use a minor variant of the same
  code.

  Also, because this routine needs to be called the first time through
  malloc anyway, it turns out to be the perfect place to trigger
  initialization code.
*/
```

可以看到它对fastbin的操作；

```c
static void malloc_consolidate(mstate av)
{
  mfastbinptr*    fb;                 /* current fastbin being consolidated */
  mfastbinptr*    maxfb;              /* last fastbin (for loop control) */
  mchunkptr       p;                  /* current chunk being consolidated */
  mchunkptr       nextp;              /* next chunk to consolidate */
  mchunkptr       unsorted_bin;       /* bin header */
  mchunkptr       first_unsorted;     /* chunk to link to */

  /* These have same use as in free() */
  mchunkptr       nextchunk;
  INTERNAL_SIZE_T size;
  INTERNAL_SIZE_T nextsize;
  INTERNAL_SIZE_T prevsize;
  int             nextinuse;
  mchunkptr       bck;
  mchunkptr       fwd;
```

一些成员变量和定义；

```c
  /*
    If max_fast is 0, we know that av hasn't
    yet been initialized, in which case do so below
  */

  if (get_max_fast () != 0) {
    clear_fastchunks(av);

    unsorted_bin = unsorted_chunks(av);
```

如果`max_fast`不为0，则清空分配区`have_fastbin`标志，否则执行`malloc_init_state`进行初始化；

```c
    /*
      Remove each chunk from fast bin and consolidate it, placing it
      then in unsorted bin. Among other reasons for doing this,
      placing in unsorted bin avoids needing to calculate actual bins
      until malloc is sure that chunks aren't immediately going to be
      reused anyway.
    */

    maxfb = &fastbin (av, NFASTBINS - 1);
    fb = &fastbin (av, 0);
    do {
      p = atomic_exchange_acq (fb, 0);
      if (p != 0) {
        do {
          check_inuse_chunk(av, p);
          nextp = p->fd;
```

获得`unsorted bin`，`fastbin[0]`，`fastbin[MAX]`的地址，遍历fastbin中的所有chunk，对每个chunk做如下操作；

```c
          /* Slightly streamlined version of consolidation code in free() */
          size = p->size & ~(PREV_INUSE|NON_MAIN_ARENA);
          nextchunk = chunk_at_offset(p, size);
          nextsize = chunksize(nextchunk);
```

获得`chunk_size`,下一个块的地址与`next_chunk_size`；

```c
          if (!prev_inuse(p)) {
            prevsize = p->prev_size;
            size += prevsize;
            p = chunk_at_offset(p, -((long) prevsize));
            unlink(av, p, bck, fwd);
          }
```

如果上一个堆块是空闲的，则合并chunk，并使用`unlink`将上一个块从bin中释放；

```c
          if (nextchunk != av->top) {
            nextinuse = inuse_bit_at_offset(nextchunk, nextsize);

            if (!nextinuse) {
              size += nextsize;
              unlink(av, nextchunk, bck, fwd);
            } else
              clear_inuse_bit_at_offset(nextchunk, 0);
```

如果下一个堆块不是`top_chunk`，则判断下一个chunk是否空闲，如果是则合并chunk，并使用`unlink`将下一个块从bin中释放；

```c
            first_unsorted = unsorted_bin->fd;
            unsorted_bin->fd = p;
            first_unsorted->bk = p;
            if (!in_smallbin_range (size)) {
              p->fd_nextsize = NULL;
              p->bk_nextsize = NULL;
            }

            set_head(p, size | PREV_INUSE);
            p->bk = unsorted_bin;
            p->fd = first_unsorted;
            set_foot(p, size);
          }

```

将chunk插入unsortedbin头部，返回地址指针；

```c
          else {
            size += nextsize;
            set_head(p, size | PREV_INUSE);
            av->top = p;
	      } while ( (p = nextp) != 0);
      }
    } while (fb++ != maxfb);
  }
```

将chunk与top_chunk合并；

```c
  else {
    malloc_init_state(av);
    check_malloc_state(av);
  }
}
```

如果`max_fast`为`0`，则初始化堆块状态并检查；

## free

### __libc_free

```c
void
__libc_free (void *mem)
{
  mstate ar_ptr;
  mchunkptr p;                          /* chunk corresponding to mem */

  void (*hook) (void *, const void *)
    = atomic_forced_read (__free_hook);
  if (__builtin_expect (hook != NULL, 0))
    {
      (*hook)(mem, RETURN_ADDRESS (0));
      return;
    }
```

同样是从`__libc_free`开始，代入参数为要进行`free`操作的指针；

查看有无钩子函数，如果有则进行跳转执行；

```c
  if (mem == 0)                              /* free(0) has no effect */
    return;
```

如果内存地址指针为`0`，则直接返回，

```c
  p = mem2chunk (mem);

  if (chunk_is_mmapped (p))                       /* release mmapped memory. */
    {
      /* see if the dynamic brk/mmap threshold needs adjusting */
      if (!mp_.no_dyn_threshold
          && p->size > mp_.mmap_threshold
          && p->size <= DEFAULT_MMAP_THRESHOLD_MAX)
        {
          mp_.mmap_threshold = chunksize (p);
          mp_.trim_threshold = 2 * mp_.mmap_threshold;
          LIBC_PROBE (memory_mallopt_free_dyn_thresholds, 2,
                      mp_.mmap_threshold, mp_.trim_threshold);
        }
      munmap_chunk (p);
      return;
    }
```

将指针转换为用户内存chunk指针，并判断chunk是否是由mmap分配得到的，如果是，则判断：

1. 是否没有禁用动态阈值
2. chunk大小是否大于当前mmap阈值的大小
3. chunk大小是否小于等于mmap默认阈值的最大值

如果满足条件，则调整当前阈值为chunk的大小，并且mmap的紧缩阈值为该chunk大小的两倍。随后释放该mmap分配的chunk并返回；

```c
  ar_ptr = arena_for_chunk (p);
  _int_free (ar_ptr, p, 0);
}
```

如果chunk不是`mmap`分配的chunk，则获得chunk分配的区域`arena`，调用`_int_free`；

### _int_free

```c
/*
   ------------------------------ free ------------------------------
 */

static void
_int_free (mstate av, mchunkptr p, int have_lock)
{
  INTERNAL_SIZE_T size;        /* its size */
  mfastbinptr *fb;             /* associated fastbin */
  mchunkptr nextchunk;         /* next contiguous chunk */
  INTERNAL_SIZE_T nextsize;    /* its size */
  int nextinuse;               /* true if nextchunk is used */
  INTERNAL_SIZE_T prevsize;    /* size of previous contiguous chunk */
  mchunkptr bck;               /* misc temp for linking */
  mchunkptr fwd;               /* misc temp for linking */

  const char *errstr = NULL;
  int locked = 0;
  
  size = chunksize (p);
```

brk申请的堆块具体是由`_int_free`实现释放的，传入参数为分配区指针`av`，待free的chunk指针`p`以及锁标志`have_lock`；

```c
  /* Little security check which won't hurt performance: the
     allocator never wrapps around at the end of the address space.
     Therefore we can exclude some size values which might appear
     here by accident or by "design" from some intruder.  */
  if (__builtin_expect ((uintptr_t) p > (uintptr_t) -size, 0)
      || __builtin_expect (misaligned_chunk (p), 0))
    {
      errstr = "free(): invalid pointer";
    errout:
      if (!have_lock && locked)
        (void) mutex_unlock (&av->mutex);
      malloc_printerr (check_action, errstr, chunk2mem (p), av);
      return;
    }
```

判断指针p合法性以及是否对齐，如果不满足则抛出指针异常；

```c
  /* We know that each chunk is at least MINSIZE bytes in size or a
     multiple of MALLOC_ALIGNMENT.  */
  if (__glibc_unlikely (size < MINSIZE || !aligned_OK (size)))
    {
      errstr = "free(): invalid size";
      goto errout;
    }
```

如果chunk的大小小于分配的最小单位，并且也没有对齐，则抛出大小异常；

```c
  check_inuse_chunk(av, p);
```

检查chunk的状态是否合法；

```c
  /*
    If eligible, place chunk on a fastbin so it can be found
    and used quickly in malloc.
  */

  if ((unsigned long)(size) <= (unsigned long)(get_max_fast ())
```

如果`chunk_size`小于等于`fast bin`的最大值，则放到`fast bin`中；

```c
    if (__builtin_expect (chunk_at_offset (p, size)->size <= 2 * SIZE_SZ, 0)
	|| __builtin_expect (chunksize (chunk_at_offset (p, size))
			     >= av->system_mem, 0))
      {
```

如果chunk的下一个`chunk_size`小于等于`2*SIZE_SZ`，或者下一个chunk的`chunk_size`大于等于`arena`的`system_mem`；

```c
	if (have_lock
	    || ({ assert (locked == 0);
		  mutex_lock(&av->mutex);
		  locked = 1;
		  chunk_at_offset (p, size)->size <= 2 * SIZE_SZ
		    || chunksize (chunk_at_offset (p, size)) >= av->system_mem;
	      }))
	  {
	    errstr = "free(): invalid next size (fast)";
	    goto errout;
	  }
```

分配区上锁，判断分配区是否加锁或者`next_chunk_size`是否小于等于`2*sizeof(INTERNAL_SIZE_T)`或者`next_chunk_size`是否大于等于`av->system_mem`，如果满足，则抛出下一个`chunk_size`异常；

```c
	if (! have_lock)
	  {
	    (void)mutex_unlock(&av->mutex);
	    locked = 0;
	  }
      }
    free_perturb (chunk2mem(p), size - 2 * SIZE_SZ);
```

解锁；

```c
    set_fastchunks(av);
    unsigned int idx = fastbin_index(size);
    fb = &fastbin (av, idx);
```

设置分配区`av`的标志位flag的`FASTCHUNKS_BIT`为1，根据size寻找到`fastbin`的索引`idx`，然后根据分配区和索引获得对应的`&fastbin`指针；

```c
    /* Atomically link P to its fastbin: P->FD = *FB; *FB = P;  */
    mchunkptr old = *fb, old2;
    unsigned int old_idx = ~0u;
```

定义两个个chunk指针，`old`指向`&fastbin`；

```c
    do
      {
	/* Check that the top of the bin is not the record we are going to add
	   (i.e., double free).  */
	if (__builtin_expect (old == p, 0))
	  {
	    errstr = "double free or corruption (fasttop)";
	    goto errout;
	  }
```

如果判断指针所指向的chunk是否为要释放的chunk，这里很明显的漏洞就是`fastbin double free`，如果是则抛出`double free`异常；

```c
	/* Check that size of fastbin chunk at the top is the same as
	   size of the chunk that we are adding.  We can dereference OLD
	   only if we have the lock, otherwise it might have already been
	   deallocated.  See use of OLD_IDX below for the actual check.  */
	if (have_lock && old != NULL)
	  old_idx = fastbin_index(chunksize(old));
	p->fd = old2 = old;
      }
```

如果分配区上锁，且`&fastbin`不为空，则获取索引，并将chunk插入到`fastbin`头部；

```c
    while ((old = catomic_compare_and_exchange_val_rel (fb, p, old2)) != old2);

    if (have_lock && old != NULL && __builtin_expect (old_idx != idx, 0))
      {
	errstr = "invalid fastbin entry (free)";
	goto errout;
      }
  }
```

如果分配区上锁，并且`fastbin`不为空，而且索引不相等，则抛出`fastbin`插入的异常；

```c
 /*
    Consolidate other non-mmapped chunks as they arrive.
  */

  else if (!chunk_is_mmapped(p)) {
    if (! have_lock) {
      (void)mutex_lock(&av->mutex);
      locked = 1;
    }
```

如果chunk不是mmap分配的，分配区没有上锁，则上锁；

```c
    nextchunk = chunk_at_offset(p, size);

    /* Lightweight tests: check whether the block is already the
       top block.  */
    if (__glibc_unlikely (p == av->top))
      {
	errstr = "double free or corruption (top)";
	goto errout;
      }
```

获取下一个chunk的指针，如果chunk是top_chunk，则抛出异常；

```c
    /* Or whether the next chunk is beyond the boundaries of the arena.  */
    if (__builtin_expect (contiguous (av)
			  && (char *) nextchunk
			  >= ((char *) av->top + chunksize(av->top)), 0))
      {
	errstr = "double free or corruption (out)";
	goto errout;
      }
```

如果存在连续分配区并且`next_chunk`指针大于等于`top_chunk_addr + top_chunk_size`的地址，则抛出越界异常；

```c
    /* Or whether the block is actually not marked used.  */
    if (__glibc_unlikely (!prev_inuse(nextchunk)))
      {
	errstr = "double free or corruption (!prev)";
	goto errout;
      }
```

如果`next_chunk`的标志位`prev_inuse`为`0`，表示chunk已经被标记为free状态，则抛出异常；

```c
    nextsize = chunksize(nextchunk);
    if (__builtin_expect (nextchunk->size <= 2 * SIZE_SZ, 0)
	|| __builtin_expect (nextsize >= av->system_mem, 0))
      {
	errstr = "free(): invalid next size (normal)";
	goto errout;
      }
```

如果`next_chunk_size<=2*SIZE_SZ`或者`next_chunk_size>=av->system_mem`，抛出异常；

```c
    free_perturb (chunk2mem(p), size - 2 * SIZE_SZ);

    /* consolidate backward */
    if (!prev_inuse(p)) {
      prevsize = p->prev_size;
      size += prevsize;
      p = chunk_at_offset(p, -((long) prevsize));
      unlink(av, p, bck, fwd);
    }
```

判断`prev_chunk`是否是`free`状态，是的话，则合并并且`unlink`；

```c

    if (nextchunk != av->top) {
      /* get and clear inuse bit */
      nextinuse = inuse_bit_at_offset(nextchunk, nextsize);
```

如果`next_chunk`不是`top_chunk`则获取`next_chunk_inuse`；

```c
      /* consolidate forward */
      if (!nextinuse) {
	unlink(av, nextchunk, bck, fwd);
	size += nextsize;
      } else
	clear_inuse_bit_at_offset(nextchunk, 0);
```

如果`next_chunk`为空闲状态，则`unlink`后合并，否则设置其`prev_inuse`为`0`；

```c
      /*
	Place the chunk in unsorted chunk list. Chunks are
	not placed into regular bins until after they have
	been given one chance to be used in malloc.
      */

      bck = unsorted_chunks(av);
      fwd = bck->fd;
      if (__glibc_unlikely (fwd->bk != bck))
	{
	  errstr = "free(): corrupted unsorted chunks";
	  goto errout;
	}
```

获取分配区的`unsorted_chunks`链表指针，判断 `unsorted_bin->fd->bk`是否不等于`unsorted_bin`，否则抛出`unsorted bin`异常；

```c
      p->fd = fwd;
      p->bk = bck;
      if (!in_smallbin_range(size))
	{
	  p->fd_nextsize = NULL;
	  p->bk_nextsize = NULL;
	}
      bck->fd = p;
      fwd->bk = p;
      set_head(p, size | PREV_INUSE);
      set_foot(p, size);
      check_free_chunk(av, p);
    }
```

将`chunk`插入到`unsorted bin`中，然后判断其大小是否属于`small bin`范围，如果不是则清空`chunk`的`fd_nextsize`和`bk_nextsize`区域，设置`chunk`的`pre_inuse`和`size`，最后检查`chunk`的`free`状态是否正常；

```c
    /*
      If the chunk borders the current high end of memory,
      consolidate into top
    */

    else {
      size += nextsize;
      set_head(p, size | PREV_INUSE);
      av->top = p;
      check_chunk(av, p);
    }
```

如果其`next_chunk`已经是`top_chunk`，则将当前`chunk`合并到`top_chunk`中，设置`prev_inuse`，检查`top_chunk`的状态；

```c
   /*
      If freeing a large space, consolidate possibly-surrounding
      chunks. Then, if the total unused topmost memory exceeds trim
      threshold, ask malloc_trim to reduce top.

      Unless max_fast is 0, we don't know if there are fastbins
      bordering top, so we cannot tell for sure whether threshold
      has been reached unless fastbins are consolidated.  But we
      don't want to consolidate on each free.  As a compromise,
      consolidation is performed if FASTBIN_CONSOLIDATION_THRESHOLD
      is reached.
    */

    if ((unsigned long)(size) >= FASTBIN_CONSOLIDATION_THRESHOLD) {
      if (have_fastchunks(av))
	malloc_consolidate(av);
```

如果被释放的堆块的`chunk_size>=65536`，即`fastbin`合并的阈值，则触发`malloc_consolidate`合并分配区中的`fastbin`；

```c
      if (av == &main_arena) {
#ifndef MORECORE_CANNOT_TRIM
	if ((unsigned long)(chunksize(av->top)) >=
	    (unsigned long)(mp_.trim_threshold))
	  systrim(mp_.top_pad, av);
#endif
```

判断分配区是否为主线程区域，如果是则判断是否`top_chunk_size>=mp_.trim)threshold`收缩阈值，如果是的话，则系统调用搜索，将多余内存归还操作系统；

```c
      } else {
	/* Always try heap_trim(), even if the top chunk is not
	   large, because the corresponding heap might go away.  */
	heap_info *heap = heap_for_ptr(top(av));
	
    assert(heap->ar_ptr == av);
	heap_trim(heap, mp_.top_pad);
      }
    }
```

如果是非主线程区域，尝试找到该区域的`top_chunk`，并收缩`top_chunk`；

```c
    if (! have_lock) {
      assert (locked);
      (void)mutex_unlock(&av->mutex);
    }
  }
  /*
    If the chunk was allocated via mmap, release via munmap().
  */

  else {
    munmap_chunk (p);
  }
}
```

分配区解锁，执行`munmap_chunk`释放chunk；
