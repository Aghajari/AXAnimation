# AXAnimation
<p align="center"><img src="/images/AXAnimation.jpg"></p>

[![Platform](https://img.shields.io/badge/platform-android-green.svg)](http://developer.android.com/index.html)
[![API](https://img.shields.io/badge/API-19%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=19)

 AXAnimation is an Android Library which can simply animate views and everything!
 
 ## Introduction
 
 This library is made up of three main sections that you should be familiar with before you begin.
 1. Rule
 2. RuleSection
 3. PreRule
 
### Rule :
Each Rule does a specific job for the Animation. Rules will create the Animators of animation methods.

There are different types of rules :
- **PropertyRule** : It uses `ObjectAnimator` for some methods such as `alpha()`, `rotation()`, `scale()` and etc. Also It has a subclass called **PropertyValueRule** which uses `ValueAnimator` for more customiztions.
- **RuleSet** : It can create multi rules in just one rule.
- **NotAnimatedRule** : Some Rules have no Animators but they can update the target view's state such as `bringViewToFront` or `sendViewToBack`.
- **DrawRule** : Will draw Lines, Arcs, Shapes, Texts and etc on a `DrawableLayout` by canvas.

### RuleSection :
Each section contains some Rules which will play together. but the sections will play sequentially. You can have several sections in an Animation.

There is also a `WaitRule` which can add delay between each section.

### PreRule :
PreRule will prepare target for an Animation just before starting it. For Example `copyOfView()` makes a Placeholder of view.

### Thats it!
This was a quick introduction for AXAnimation. I will tell you more about the library after showing you some sample codes & previews..

*Good News:* You don't need to create rules or anything by yourself, they are already made and waiting for your command to be executed.

## USAGE

<img src="/images/0.gif" alt="sample" title="sample" width="250" height="180" align="right" />

-   Let's start with **`alpha()`** And **`scale()`**

```java
AXAnimation.create()
        .duration(1000)
        .alpha(1f)
        .nextSection()
        .scale(1.5f, 1.25f, 1.8f)
        .start(target);
```

---

<img src="/images/1_0.gif" alt="sample" title="sample" width="250" height="180" align="right" />

-   **`resize(gravity, width, height)`**

```java
AXAnimation.create().dp()
        .duration(2000)
        .resize(Gravity.CENTER, width, height)
        .start(target);
```

<br>
<br>

<img src="/images/1_1.gif" alt="sample" title="sample" width="250" height="180" align="right" />

```java
AXAnimation.create().dp()
        .duration(2000)
        .resize(Gravity.TOP | Gravity.RIGHT, width, height)
        .start(target);
```

<br>
<br>

---

<img src="/images/2.gif" alt="sample" title="sample" width="250" height="180" align="right" />

-   **`resize(left, top, right, bottom)`** OR **`resize(Rect... layouts)`**

```java
AXAnimation.create().dp()
        .duration(2000)
        .resize(left, top, right, bottom)
        .start(target);
```

<br>

---

<img src="/images/3.gif" alt="sample" title="sample" width="250" height="180" align="right" />

-   **`skew(kx, ky)`** OR **`skew(PointF... values)`** & **`imageSkew(...)`**

```java
AXAnimation.create()
        .duration(2000)
        .skew(0.3f, 0.3f)
        .start(target);
```

<br>

---
