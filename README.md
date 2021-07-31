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
        .duration(500)
        .resize(Gravity.CENTER, width, height)
        .start(target);
```

<br>
<br>

<img src="/images/1_1.gif" alt="sample" title="sample" width="250" height="180" align="right" />

```java
AXAnimation.create().dp()
        .duration(500)
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
        .duration(500)
        .resize(left, top, right, bottom)
        .start(target);
```

<br>

---

<img src="/images/3.gif" alt="sample" title="sample" width="250" height="180" align="right" />

-   **`skew(kx, ky)`** OR **`skew(PointF... values)`** & **`imageSkew(...)`**

```java
AXAnimation.create()
        .duration(500)
        .skew(0.3f, 0.3f)
        .start(target);
```

<br>

---

<img src="/images/4.gif" alt="sample" title="sample" width="250" height="180" align="right" />

-   **`matrix(Matrix... matrices)`** & **`imageMatrix(...)`**

```java
Matrix matrix = new Matrix();
matrix.setSkew(0.15f, 0.15f);
matrix.postScale(1.5f, 1.5f);
matrix.postTranslate(-100, -100);

AXAnimation.create()
        .duration(1000)
        .matrix(matrix)
        .nextSectionWithDelay(500)
        .reversePreviousRule()
        .start(target);
```

---

<img src="/images/5.gif" alt="sample" title="sample" width="250" height="180" align="right" />

-   **`backgroundColor(int... colors)`**

```java
AXAnimation.create()
        .duration(1000)
        .backgroundColor(Color.MAGENTA)
        .nextSectionWithDelay(500)
        .reversePreviousRule()
        .start(target);
```

---

<img src="/images/6.gif" alt="sample" title="sample" width="250" height="200" align="right" />

-   **`background(Drawable... drawables)`**

```java
GradientDrawable gd1 = new GradientDrawable();
gd1.setColors(new int[]{Color.RED, Color.BLUE});
gd1.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);

GradientDrawable gd2 = new GradientDrawable();
gd2.setColors(new int[]{Color.BLUE, Color.GREEN});
gd2.setOrientation(GradientDrawable.Orientation.TL_BR);
gd2.setCornerRadius(100);
gd2.setStroke(20, Color.RED, 0, 0);

ColorDrawable cd = new ColorDrawable(Color.MAGENTA);

AXAnimation.create()
        .duration(4000)
        .background(gd1, gd2, cd)
        .start(target);
```

---

<img src="/images/7.gif" alt="sample" title="sample" width="250" height="200" align="right" />

-   **`flipHorizontal`** And **`flipVertical`**

```java
AXAnimation.create()
        .duration(1000)
        .flipHorizontalToHide()
        .nextSectionWithDelay(600)
        .flipHorizontalToShow()
        .nextSectionWithDelay(600)
        .flipVerticalToHide()
        .nextSectionWithDelay(600)
        .flipVerticalToShow()
        .start(target);
```

---

<img src="/images/8.gif" alt="sample" title="sample" width="250" height="200" align="right" />

-   **`drawLine(...)`**

```java
Paint paint = new Paint();
paint.setStyle(Paint.Style.STROKE);
paint.setStrokeWidth(20);
paint.setColor(Color.WHITE);

LiveSize y = LiveSize.create(AXAnimation.CONTENT_HEIGHT).divide(2);
LiveSize left = LiveSize.create(16);
LiveSize right = LiveSize.create(AXAnimation.CONTENT_HEIGHT).minus(16);

AXAnimation.create().dp()
        .duration(1000)
        .repeatCount(AXAnimation.INFINITE)
        .repeatMode(AXAnimation.REVERSE)
        .drawLine("line_key", true, Gravity.CENTER, paint, left, y, right, y)
        .start(target);
```

---

<img src="/images/9.gif" alt="sample" title="sample" width="250" height="200" align="right" />

-   **`drawArc(...)`**

```java
Paint paint = new Paint();
paint.setStyle(Paint.Style.STROKE);
paint.setStrokeWidth(20);
paint.setColor(Color.WHITE);

LiveSize cx = LiveSize.create(AXAnimation.CONTENT_WIDTH).divide(2);
LiveSize cy = LiveSize.create(AXAnimation.CONTENT_HEIGHT).divide(2);

AXAnimation.create().dp()
        .waitBefore(1000)
        .duration(2500)
        .drawArc("arc_key", true, paint, cx, cy, 56, false, -90, 270, 200, 320, 270, 360)
        .nextSectionWithDelay(500)
        .reversePreviousRuleSection()
        .start(target);
```

---
<p align="center"><b>And many other interesting animations!</b></p>

---

s
