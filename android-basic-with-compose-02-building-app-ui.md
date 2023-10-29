---
title: Unit2 - Building App UI
top: false
comment: false
lang: zh-CN
date: 2023-10-20 15:00:55
tags:
categories:	
  - Android
  - Android Basic with Compose
---

# Unit 2: Building App UI

Continue learning the fundamentals of Kotlin, and start building more interactive apps.

## Kotlin fundamentals

### Write conditionals

In Kotlin, when you deal with multiple branches, you can use the `when` statement instead of the `if/else` statement because it improves readability, which refers to how easy it is for human readers, typically developers, to read the code. It's very important to consider readability when you write your code because it's likely that other developers need to review and modify your code throughout its lifetime. Good readability ensures that developers can correctly understand your code and don't inadvertently introduce bugs into it.

`when` statements are preferred when there are more than two branches to consider.

<img src="./android-basic-with-compose-02-building-app-ui/when.png" height=300 width=500>

<center><i>when statement</i></center>

Now, we have an example about traffic light color.

```kotlin
fun main() {
    val trafficLightColor = "Black"
    when(trafficLightColor){
        "Red" -> println("Stop")
        "Yello" -> println("Slow")
        "Green" -> println("Go")
        else -> println("Invalid traffic light color.")
    }
}
```

And more examples...

```kotlin
fun main() {
    val x: Any = 14

    when (x) {
        2, 3, 5, 7 -> println("x is a prime number between 1 and 10.")
        in 1..10 -> println("x is a number between 1 and 10, but not a prime number.")
        is Int -> println("x is an integer number, but not between 1 and 10.")
        else -> println("x isn't an integer number.")
    }
}
```

### Use nullability

In Kotlin, there's a distinction between nullable and non-nullable types:

- Nullable types are variables that *can* hold `null`.
- Non-null types are variables that *can't* hold `null`.

A type is only nullable if you explicitly let it hold `null`. As the error message says, the `String` data type is a non-nullable type, so you can't reassign the variable to `null`.
