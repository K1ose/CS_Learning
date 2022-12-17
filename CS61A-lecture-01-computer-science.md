---
title: CS61A_lecture_01_computer_science
top: false
comment: false
lang: zh-CN
date: 2022-11-04 05:45:24
tags:
categories:
  - study
  - course
  - CS 61A:Structure and Interpretation of Computer Programs
  - lecture
---

# Computer Science

[Online textbook](http://composingprograms.com)



## What is CS?

The study of:

- What problems can be solved using computation
- How to solve those problems
- What techniques lead to effective solutions



Systems - Artificial Intelligence - Graphics - Security - Networking - Programming Languages - Theory - Scientific Computing



Artificial Intelligence:

- Decision Making
- Robotics
- Natural Language Processing
  - Answering Questions
  - Dialog
  - Translation
- ...



## What is this Course about?

about managing complexity

- Mastering abstraction
- Programming paradigms

An introduction to programming

- Full understanding of Python fundamentals
- Combining multiple ideas in large projects
- How computers interpret programing languages

Different types of languages: Scheme & SQL

## Course Policies

Learning - Community - Course Staff



## Code

```python
# Numeric expressions
2022
2000 + 22
1 + 2 + 3 + 4 * ((5 // 6) + 7 * 8 * 9)

# Functions
abs(-2)

# Values
"Go Bears"

# Objects
# Note: Download from http://composingprograms.com/shakespeare.txt
shakes = open('shakespeare.txt')
text = shakes.read().split()
len(text)
text[:25]
text.count('the')
text.count('thou')
text.count('you')
text.count('forsooth')
text.count(',')

# Sets
words = set(text)
len(words)

# Combinations 
'draw'
'draw'[0]
{w[0] for w in words}

# Data
'draw'[::-1]
{w for w in words if w == w[::-1] and len(w)>4}
{w for w in words if w[::-1] in words and len(w) == 4}
{w for w in words if w[::-1] in words and len(w) > 6}
```

得到结果：

```
>>> 2022
2022
>>> 2022+22
2044
>>> 1 + 2 + 3 + 4 * ((5 // 6) + 7 * 8 * 9)
2022
>>> abs(-2)
2
>>> "Go Bears"
'Go Bears'
>>> shakes = open("shakespeare.txt")
>>> text=shakes.read().split()
>>> len(text)
980637
>>> text[:25]
['A', "MIDSUMMER-NIGHT'S", 'DREAM', 'Now', ',', 'fair', 'Hippolyta', ',', 'our', 'nuptial', 'hour', 'Draws', 'on', 'apace', ':', 'four', 'happy', 'days', 'bring', 'in', 'Another', 'moon', ';', 'but', 'O']
>>> text.count("you")
12361
>>> text.count("thou")
4501
>>> text.count("smile")
70
>>> text.count("missing")
7
>>> text.count("golden")
82
>>> words=set(text)
>>> len(words)
33505
>>> 'draw'
'draw'
>>> 'draw'[0]
'd'
>>> {w[0] for w in words}
{'s', 'B', 'J', '!', 'T', '1', 'w', 'D', '2', 'U', ',', 'O', 'I', ';', 'm', '"', '7', 'F', 'H', 'h', 'N', 'i', 'P', 'A', '[', '9', 'p', '4', '?', 'S', 'M', 'c', 'K', '&', 'e', 'z', 'Y', 'E', 'o', 'g', 'a', 'C', '.', '6', 'l', 'W', 'u', 'V', 'v', '5', 'Q', 'R', 'G', 'L', 'Z', 'f', 'd', 'r', ':', '8', 'q', ']', '3', 'y', 'X', 'n', 'k', 'j', 'b', 't', "'"}
>>> 'draw'[::-1]
'ward'
>>> {w for w in words if w == w[::-1] and len(w)>4}
{'redder', 'rever', 'minim', 'level', 'refer', 'madam'}
>>> {w for w in words if w[::-1] in words and len(w) == 4}
{'maws', 'rail', 'pots', 'meed', 'deed', 'ward', 'trow', 'rats', 'swam', 'pool', 'lees', 'nips', 'trop', 'bats', 'keel', 'seel', 'ecce', 'doom', 'esse', 'spot', 'draw', 'pins', 'wolf', 'evil', 'liar', 'tops', 'drab', 'poop', 'live', 'brag', 'meet', 'reed', 'flow', 'stab', 'pooh', 'hoop', 'peep', 'dial', 'spin', 'gums', 'rood', 'laid', 'teem', 'stop', 'snip', 'moor', 'spit', 'deer', 'star', 'sees', 'bard', 'gnat', 'wort', 'leek', 'garb', 'part', 'smug', 'leer', 'deem', 'port', 'reel', 'trap', 'mood', 'tang', 'elle', 'tips', 'noon', 'loop', 'room', 'door'}
>>> {w for w in words if w[::-1] in words and len(w) > 6}
set()
```

