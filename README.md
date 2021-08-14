# AXAnimation
<p align="center"><img src="/images/AXAnimation.jpg"></p>

[![Platform](https://img.shields.io/badge/platform-android-green.svg)](http://developer.android.com/index.html)
[![API](https://img.shields.io/badge/API-19%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=19)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.aghajari/AXAnimation.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.aghajari/AXAnimation/1.0.1/aar)
[![Join the chat at https://gitter.im/Aghajari/community](https://badges.gitter.im/Aghajari/community.svg)](https://gitter.im/Aghajari/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

 AXAnimation is an Android Library which can simply animate views and everything!
 
 ## Table of Contents  
- [Introduction](#introduction)
- [Installation](#installation)
- [Usage](#usage)
- [LiveVar](#livevar)
- [Better Debug](#better-debug)
- [AXAnimatorData](#axanimatordata)
- [Methods](#methods)
- [Author](#author)
- [License](#license)
 
 ## Introduction
 
 This library is made up of three main sections that you should be familiar with before you begin.
 1. Rule
 2. RuleSection
 3. PreRule
 
### Rule :
Each Rule does a specific job for the Animation. Rules will create the Animators of animation methods.

There are different types of rules :
- **PropertyRule** : Uses `ObjectAnimator` for `alpha()`, `rotation()`, `scale()` and etc. Also It has a subclass called **PropertyValueRule** which uses `ValueAnimator` for more customiztions.
- **RuleSet** : Can create multi rules in just one rule.
- **NotAnimatedRule** : Some Rules have no Animators but they can update the target's state such as `bringViewToFront` or `sendViewToBack`.
- **DrawRule** : Will draw Lines, Arcs, Shapes, Texts and etc on a `DrawableLayout` by canvas.

### RuleSection :
Each section contains some Rules which will play together. but the sections will play sequentially. You can have several sections in an Animation.

There is also a `WaitRule` which can add delay between each section.

### PreRule :
PreRule will prepare target for an Animation just before starting it. For Example `copyOfView(...)` makes a Placeholder of view.

### Thats it!
This was a quick introduction for AXAnimation.

*Good News:* You don't need to create rules or anything by yourself, they are already made and waiting for your command to be executed.

## Installation

AXAnimation is available in the `mavenCentral()`, so you just need to add it as a dependency (Module gradle)

Gradle
```gradle
implementation 'io.github.aghajari:AXAnimation:1.0.1'
```

Maven
```xml
<dependency>
  <groupId>io.github.aghajari</groupId>
  <artifactId>AXAnimation</artifactId>
  <version>1.0.1</version>
  <type>pom</type>
</dependency>
```

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

Let's see a few more custom animations.

<br>

<img src="/images/activity_1.gif" alt="sample" title="sample" width="270" height="480" align="right" />

-   **`drawPath(...)`**

```java
Paint paint = new Paint();
paint.setColor(Color.RED);
paint.setStrokeWidth(20);
paint.setStyle(Paint.Style.STROKE);

Path path = new Path();
path.moveTo(100, 100);
path.lineTo(600, 100);
path.lineTo(600, 500);
path.lineTo(100, 500);
path.lineTo(100, 1000);

AXAnimation.create().dp()
        .duration(1000)
        .drawPath("path", true, Gravity.CENTER, paint, path)
        .backgroundColor(Color.BLUE)
        .textColor(Color.WHITE)
        .unlockY().unlockX()
        .toTop(150)
        .toRight(130)
        .nextSectionWithDelay(500)
        .reversePreviousRuleSection()
        .start(findViewById(R.id.view2));
```

---

<img src="/images/activity_2.gif" alt="sample" title="sample" width="270" height="480" align="right" />

-   **`matrix(...)`**

```java
Matrix matrix = new Matrix();
matrix.setSkew(0.15f, 0.15f);
matrix.postScale(2f,2f);
matrix.postTranslate(-150,-100);

AXAnimation.create()
        .duration(1000)
        .toCenterOf(AXAnimation.PARENT_ID)
        .nextSectionWithDelay(500)
        .matrix(matrix)
        .start(findViewById(R.id.view2));
```

<br>
<br>
<br>
<br>
<br>
<br>
<br>
<br>

---

<img src="/images/activity_3.gif" alt="sample" title="sample" width="270" height="480" align="right" />

-   **`AXAnimationSet`**

```java
AXAnimation.create()
        .duration(1000)
        .toBottom(AXAnimation.MATCH_PARENT)
        .nextSection()
        .toLeft(0)
        .nextSectionWithDelay(500)
        .backToFirstPlace()
        .save("v1");

AXAnimation.create()
        .duration(1000)
        .toTop(0)
        .nextSection()
        .toRight(AXAnimation.MATCH_PARENT)
        .nextSectionWithDelay(500)
        .backToFirstPlace()
        .save("v2");

AXAnimationSet.delay(1000)
        .andAnimate("v1", findViewById(R.id.view1))
        .andAnimate("v2", findViewById(R.id.view2))
        .start();
```

---

<img src="/images/activity_4.gif" alt="sample" title="sample" width="270" height="480" align="right" />

-   **`copyOfView(...)` (PreRule)**

```java
AXAnimation.create()
        .waitBefore(1000)
        .duration(1000)
        .toCenterOf(AXAnimation.PARENT_ID)
        .scale(2f)
        .nextSectionWithDelay(500)
        .reversePreviousRuleSection()
        .copyOfView(true, true,
                AXAnimation.create()
                        .waitBefore(1000)
                        .duration(1000)
                        .scale(0.5f)
                        .delay(250).duration(750)
                        .backgroundColor(Color.MAGENTA)
                        .nextSectionWithDelay(500)
                        .reversePreviousRuleSection()
        )
        .start(findViewById(R.id.view1));
```

<br>
<br>

---

<img src="/images/activity_5.gif" alt="sample" title="sample" width="270" height="480" align="right" />

-   **`drawText(...)`** Using LiveVar

```java
LiveVar<CharSequence> text = LiveVar.ofValue("");
LiveVar<Integer[]> startColor = LiveVar.ofArray();
LiveVar<Integer[]> endColor = startColor.reverseArray();

Paint textPaint = new Paint();
Paint paint = new Paint();
paint.setColor(Color.RED);
paint.setStrokeWidth(20);
paint.setStyle(Paint.Style.STROKE);

int cx = AXAnimation.ORIGINAL | Gravity.CENTER_HORIZONTAL;
int cy = AXAnimation.ORIGINAL | Gravity.CENTER_VERTICAL;

AXAnimation.create()
        .updateLiveVar(LiveVarUpdater.forEachSection(text,
                "", "Hello 1", "Hello 2", "Hello 3", "Hello 4", "Hello 5"))
        .updateLiveVar(new LiveVarUpdater(startColor) {
            final Random rnd = new Random();

            @Override
            public void update(AXAnimation animation, int sectionIndex, int realSectionIndex, RuleSection section) {
                if (!(section instanceof WaitRule)) {
                    int color = Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                    target.update(Color.TRANSPARENT, color);
                    paint.setColor(color);
                }
            }
        })
        .duration(7500)
        .drawCircle("circle", true, paint, cx, cy, 200, false, -90)
        .nextSectionImmediate()
        .duration(1500).firstValueFromView(false)
        .drawText("text", true, false, textPaint, Gravity.CENTER, cx, cy, text)
        .duration(1000)
        .drawSetPaint(textPaint, "textSize", false, 50f, 100f)
        .duration(500)
        .drawSetPaint(textPaint, "color", false, ArgbEvaluator.getInstance(), startColor)
        .delay(1000).duration(500)
        .drawSetPaint(textPaint, "color", false, ArgbEvaluator.getInstance(), endColor)
        .nextSectionWithDelay(100)
        .repeatPreviousRuleSection(4, AXAnimation.RESTART, 100)
        .nextSection()
        .applyAnimatorForReverseRules(true)
        .duration(2000)
        .reverseRuleSection(0)
        .start(view);

LiveSize liveSize = LiveSize.create(AXAnimation.PARENT_HEIGHT).minus(100);

AXAnimation.create()
        .duration(1000)
        .drawLine("line", true, Gravity.CENTER, paint,
                0, 100, AXAnimation.MATCH_PARENT, 100)
        .drawLine("line2", true, Gravity.CENTER, paint,
                LiveSize.create(), liveSize, LiveSize.create(AXAnimation.MATCH_PARENT), liveSize)
        .animationRepeatMode(AXAnimation.REVERSE)
        .animationRepeatCount(AXAnimation.INFINITE)
        .start(view);
```

---

<img src="/images/activity_6.gif" alt="sample" title="sample" width="270" height="480" align="right" />

-   **`relativeMove(...)`** (+ inspection for a better debug) 

```java
AXAnimation.create().dp()
        .waitBefore(1000)
        .inspect(true).clearOldInspect(true)
        .repeatCount(1)
        .repeatMode(AXAnimation.REVERSE)
        .duration(1500)
        .relativeMove(R.id.view1, Gravity.TOP | Gravity.END,
                Gravity.BOTTOM | Gravity.START, -100, 100)
        .nextSectionWithDelay(500)
        .repeatCount(0)
        .toBottomOf(R.id.view1, Gravity.TOP, 100)
        .toLeftOf(R.id.view1, Gravity.RIGHT, -100)
        .withEndAction(animation -> {
            Toast.makeText(this, "Double click to clear inspection", Toast.LENGTH_SHORT).show();
        })
        .start(findViewById(R.id.view2));
```

<br>
<br>
<br>
<br>

---
<p align="center"><b>There is no limitation in AXAnimation! You can do whatever you want :)</b></p>

---

## LiveVar
**LiveVar** is a data holder class helps you to update animator value during animation.

**LiveSize** is a subclass of LiveVar, Helps you to move view base on it's original size, target size, the parent size or a related view.

For Example : `target.left = target.top / 2`

```java
AXAnimation.create()
        .duration(800)
        .toLeft(LiveSize.create(AXAnimation.TARGET|Gravity.TOP).divide(2))
        .start(View)
 ```

## Better Debug
Well, You just saw inspection, an Awesome way to debug animating layout.

There is also another way to debug all rules & rule sections. (Inspired by [OkHttp Logging Interceptor](https://github.com/square/okhttp/tree/master/okhttp-logging-interceptor))

```java
AXAnimation.create()
        .wrap(DebugRuleWrapper.class)
        .wrap(DebugRuleSectionWrapper.class, true)
        ...
```

This will debug everything like this example :
```
    --> PropertyRule@139593355
       Data: [2.0]
       TmpData: [1.0, 2.0]
       PropertyName: scaleY
       AnimatorValues: [scaleY:  1.0  2.0  ]
       Duration: 1000
       StartDelay: 0
       Interpolator: AccelerateDecelerateInterpolator
       RepeatCount: 0
    --> END RULE DEBUG PropertyRule@139593355
```

**LiveSizes** will also be translated, example : `target.left = target.top / 2`

## AXAnimatorData
All the rules have duration, interpolator, startDelay, repeat and reverse options.

## Methods
<details><summary><b>Click to expand</b></summary>
<p>
 
### Transformation & Property & Smart Rules
 
| Method Name | Param Types | Param Names |
| ----------------------- | ---------------------- | ---------------------- |
| translationX | Float... \| LiveVar<Float[]> | x |
| translationY | Float... \| LiveVar<Float[]> | y |
| translationZ | Float... \| LiveVar<Float[]> | z |
| translation | float, float | x, y |
| x | Float... \| LiveVar<Float[]> | x |
| y | Float... \| LiveVar<Float[]> | y |
| z | Float... \| LiveVar<Float[]> | z |
| xyz | float, float, float \| Float[], Float[], Float[] | x, y, z |
| pivotX | Float... \| LiveVar<Float[]> | pivotX |
| pivotY | Float... \| LiveVar<Float[]> | pivotY |
| alpha | Float... \| LiveVar<Float[]> | alpha |
| scale | Float... \| LiveVar<Float[]> | scale |
| scaleX | Float... \| LiveVar<Float[]> | scaleX |
| scaleY | Float... \| LiveVar<Float[]> | scaleY |
| skew | float, float \| PointF... | kx, ky \| skewValues |
| imageSkew | float, float \| PointF... | kx, ky \| skewValues |
| rotation | Float... \| LiveVar<Float[]> | rotation |
| rotationX | Float... \| LiveVar<Float[]> | rotationX |
| rotationY | Float... \| LiveVar<Float[]> | rotationY |
| cameraDistance | Float... \| LiveVar<Float[]> | distance |
| visibility | int | visibility |
| backgroundColor | Integer... \| LiveVar<Integer[]> | colors |
| background | Drawable... | backgrounds |
| backgroundFade | Drawable... | backgrounds |
| textColor | Integer... \| LiveVar<Integer[]> | colors |
| textSize | Float... \| LiveVar<Float[]> | sizes |
| textSize | int, Float... \| int, LiveVar<Float[]> | unit, sizes |
| matrix | Matrix... | matrices |
| imageMatrix | Matrix... | matrices |
| flipHorizontal | Void \| float | Void \| finalRotation |
| flipHorizontalToHide | Void \| int | Void \| visibility |
| flipHorizontalToShow | Void | |
| flipVertical | Void \| float | Void \| finalRotation |
| flipVerticalToHide | Void \| int | Void \| visibility |
| flipVerticalToShow | Void | |
| flash | Void | |
| bounceIn | Void | |
| bounceOut | Void | |
| fadeInt | Void | |
| fadeOut | Void | |
| shake | Void \| float, float | Void \| nbShake, translation |
| shakeY | Void \| float, float | Void \| nbShake, translation |
| press | Void \| float | Void \| depth |
 
### Layout Rules
 
| Method Name | Param Types | Param Names |
| ----------------------- | ---------------------- | ---------------------- |
| setTargetLayoutParams | ViewGroup.LayoutParams | targetLayoutParams |
| fromLayoutParams | ViewGroup.LayoutParams | layoutParams |
| toLayoutParams | ViewGroup.LayoutParams | layoutParams |
| toLayoutParams | ViewGroup.LayoutParams, boolean | layoutParams, markAsTarget |
| backToFirstPlace | Void | |
| backToFirstPlace | boolean | markAsTarget |
| backToPreviousPlace | Void | |
| backToSectionPlace | int | sectionIndex |
| moveOnPath | Path | path |
| move | int, int, int \| int, LiveSize, LiveSize | gravity, x, y |
| move | int, Point... | gravity, points |
| relativeMove | int, int, int, Point \| View, int, int, Point | view, sourceGravity, targetGravity, delta |
| relativeMove | int, int, int, int, int \| View, int, int, int, int | view, sourceGravity, targetGravity, dx, dy |
| toLeft | int \| LiveSize | left |
| toTop | int \| LiveSize | top |
| toRight | int \| LiveSize | right |
| toBottom | int \| LiveSize | bottom |
| toCenterHorizontal | int \| LiveSize | center |
| toCenterVertical | int \| LiveSize | center |
| toLeftOf | int, int, int \| View, int, int | view, gravity, delta |
| toTopOF | int, int, int \| View, int, int | view, gravity, delta |
| toRightOf | int, int, int \| View, int, int | view, gravity, delta |
| toBottomOf | int, int, int \| View, int, int | view, gravity, delta |
| toCenterHorizontalOf | int, int, int \| View, int, int | view, gravity, delta |
| toCenterVerticalOf | int, int, int \| View, int, int | view, gravity, delta |
| toCenterOf | int \| View | view |
| toCenterOf | int, int \| View, int | view, gravity |
| toCenterOf | int, int, int, int \| View, int, int, int | view, gravity, horizontalDelta, verticalData |
| toPosition | int, int | gravity, position |
| toPosition | int, int, int | gravity, x, y |
| toPositionOf | int, int, int, int \| View, int, int, int | view, sourceGravity, targetGravity, delta |
| resize | int[4] \| LiveSize[4] | left, top, right, bottom |
| resize | Rect... \| LayoutSize... | values |
| resizeHorizontal | int[2] \| LiveSize[2] | left, right |
| resizeHorizontal | Rect... \| LayoutSize... | values |
| resizeVertical | int[2] \| LiveSize[2] | top, bottom |
| resizeVertical | Rect... \| LayoutSize... | values |
| resizeWidth | int, int... \| int, LiveSize... | gravity, width |
| resizeHeight | int, int... \| int, LiveSize... | gravity, height |
| resize | int, int, int \| int, LiveSize, LiveSize | gravity, width, height |
| padding | int[4] \| Rect... | left, top, right, bottom \| values |
 
### Draw Rules
 
| Method Name | Param Types | Param Names |
| ----------------------- | ---------------------- | ---------------------- |
| drawSetPaint | Paint, String, boolean, T... | target, propertyName, reset, values |
| drawSetPaint | Paint, String, boolean, TypeEvaluator\<?\>, T... | target, propertyName, reset, evaluator, values |
| drawSetPaint | Paint, String, boolean, LiveVar\<T[]\> | target, propertyName, reset, values |
| drawSetPaint | Paint, String, boolean, TypeEvaluator\<?\>, LiveVar\<T[]\> | target, propertyName, reset, evaluator, values |
| drawSetMatrix | String, boolean, Matrix... | key, drawOnFront, values |
| drawPath | String, boolean, Paint, Path | key, drawOnFront, paint, path |
| drawPath | String, boolean, int, Paint, Path | key, drawOnFront, lineGravity, paint, path |
| drawLine | String, boolean, int, Paint, float, float, float, float | key, drawOnFront, lineGravity, paint, startX, startY, stopX, stopY |
| drawLine | String, boolean, int, Paint, LiveSize, LiveSize, LiveSize, LiveSize | key, drawOnFront, lineGravity, paint, startX, startY, stopX, stopY |
| drawLine | String, boolean, int, Paint, PointF[]... | key, drawOnFront, lineGravity, paint, values |
| drawLine | String, boolean, int, Paint, LiveSizePoint[]... | key, drawOnFront, lineGravity, paint, values |
| drawArc | String, boolean, Paint, float, float, float, boolean, float, float... | key, drawOnFront, paint, cx, cy, radius, useCenter, startAngle, sweepAngles |
| drawArc | String, boolean, Paint, LiveSize, LiveSize, float, boolean, float, float... | key, drawOnFront, paint, cx, cy, radius, useCenter, startAngle, sweepAngles |
| drawArc | String, boolean, Paint, RectF, float, boolean, float, float... | key, drawOnFront, paint, oval, radius, useCenter, startAngle, sweepAngles |
| drawArc | String, boolean, Paint, LiveSize, float, boolean, float, float... | key, drawOnFront, paint, oval, radius, useCenter, startAngle, sweepAngles |
| drawCircle | String, boolean, Paint, float, float, float, boolean, float | key, drawOnFront, paint, cx, cy, radius, useCenter, startAngle |
| drawCircle | String, boolean, Paint, LiveSize, LiveSize, float, boolean, float | key, drawOnFront, paint, cx, cy, radius, useCenter, startAngle |
| drawOval | String, boolean, Paint, RectF, float, boolean, float | key, drawOnFront, paint, oval, radius, useCenter, startAngle |
| drawOval | String, boolean, Paint, LiveSize, float, boolean, float | key, drawOnFront, paint, oval, radius, useCenter, startAngle |
| drawOvalRect | String, boolean, Paint, int, RectF...  | key, drawOnFront, paint, gravity, values |
| drawOvalRect | String, boolean, Paint, int, LayoutSize...  | key, drawOnFront, paint, gravity, values |
| drawOvalRect | String, boolean, Paint, int, float[4]  | key, drawOnFront, paint, gravity, left, top, right, bottom |
| drawOvalRect | String, boolean, Paint, int, LiveSize[4]  | key, drawOnFront, paint, gravity, left, top, right, bottom |
| drawRect | String, boolean, Paint, int, RectF...  | key, drawOnFront, paint, gravity, values |
| drawRect | String, boolean, Paint, int, LayoutSize...  | key, drawOnFront, paint, gravity, values |
| drawRect | String, boolean, Paint, int, float[4]  | key, drawOnFront, paint, gravity, left, top, right, bottom |
| drawRect | String, boolean, Paint, int, LiveSize[4]  | key, drawOnFront, paint, gravity, left, top, right, bottom |
| drawRoundRect | String, boolean, Paint, int, float, float, RectF...  | key, drawOnFront, paint, gravity, rx, ry, values |
| drawRoundRect | String, boolean, Paint, int, float, float, LayoutSize...  | key, drawOnFront, paint, gravity, rx, ry, values |
| drawRoundRect | String, boolean, Paint, int, float, float, float[4]  | key, drawOnFront, paint, gravity, rx, ry, left, top, right, bottom |
| drawRoundRect | String, boolean, Paint, int, float, float, LiveSize[4]  | key, drawOnFront, paint, gravity, rx, ry, left, top, right, bottom |
| drawText | String, boolean, boolean, Paint, int, float, float, CharSequence | key, drawOnFront, typing, paint, gravity, x, y, text |
| drawText | String, boolean, boolean, Paint, int, LiveSize, LiveSize, CharSequence | key, drawOnFront, typing, paint, gravity, x, y, text |
| drawText | String, boolean, boolean, Paint, int, float, float, LiveVar\<CharSequence\> | key, drawOnFront, typing, paint, gravity, x, y, text |
| drawText | String, boolean, boolean, Paint, int, LiveSize, LiveSize, LiveVar\<CharSequence\> | key, drawOnFront, typing, paint, gravity, x, y, text |
| removeDrawRule | String | key |

### Custom Rules

| Method Name | Param Types | Param Names |
| ----------------------- | ---------------------- | ---------------------- |
| property | String, float... \| String, int... | propertyName, values |
| propertySize | String, float... \| String, int... | propertyName, values |
| propertyColor | String, int... | propertyName, colors |
| property | String, TypeEvaluator\<T\>, T... | propertyName, evaluator, values |
| custom | AXAnimatorUpdateListener\<Float\>, float... | listener, values |
| custom | AXAnimatorUpdateListener\<Integer\>, int... | listener, values |
| customArgb | AXAnimatorUpdateListener\<Integer\>, int... | listener, values |
| custom | TypeEvaluator\<T\>, AXAnimatorUpdateListener\<T\>, T... | evaluator, listener, values |
| customMatrix | AXAnimatorUpdateListener\<Matrix\>, Matrix... | listener, matrices |
| invoke | String, Object... | methodName, args |
| invoke | int, String, Object... \| View, String, Object... | view, methodName, args |
| fieldSet | String, Object | fieldName, value |
| fieldSet | int, String, Object \| View, String, Object | view, fieldName, value |
| fieldAnimatorSet | String, boolean, TypeEvaluator\<T\>, T... | fieldName, invalidate, evaluator, values |
| fieldAnimatorSet | String, AXAnimatorUpdateListener\<T\>, boolean, TypeEvaluator\<T\>, T... | fieldName, listener, invalidate, evaluator, values |

### Other Methods

| Method Name | Param Types | Param Names |
| ----------------------- | ---------------------- | ---------------------- |
| addRule | Rule... | rules |
| addReverseRule | Rule... | rules |
| addRuleSection | RuleSection... | ruleSections |
| bringViewToFront | Void | |
| bringViewToFront | int \| View | view |
| sendViewToBack | Void | |
| sendViewToBack | int \| View | view |
| startOtherAnimation | AXAnimation \| AXAnimation, View  \| AXAnimation, int | animation \| animation, view |
| startOtherAnimation | String \| String, View \| String, int | animation \| animationName, view |
| reverseOtherAnimation | AXAnimation \| AXAnimation, View  \| AXAnimation, int | animation \| animation, view |
| reverseOtherAnimation | String \| String, View \| String, int | animation \| animationName, view |
| reversePreviousRule | Void | |
| reverseRule | int \| Rule | index \| rule |
| reverseRuleOnSection | int, int \| int, RuleSection \| Rule, RuleSection | rule, ruleSection |
| reversePreviousRuleSection | Void | |
| reverseRuleSection | int \| RuleSection | index \| ruleSection |
| updateLiveVar | LiveVarUpdater | liveVarUpdater |
| updateLiveVar | LiveVar, Object | var, value |
| repeatPreviousRuleSection | int, int, long | repeatCount, repeatMode, delay |
| repeatRuleSection | int, int, long, int \| int, int, long, RuleSection | repeatCount, repeatMode, delay, ruleSection |
| nextSection | Void | |
| nextSectionImmediate | Void | |
| nextSectionWithDelay | long | delay |
| nextSectionWithReverseDelay | long | delay |
| waitBefore | long | duration |
| waitNotifyBefore | WaitNotifyRule.Listener \| long, WaitNotifyRule.Listener | listener \| delay, listener |
| requiresApi | int | api |
| wrap | Class\<? extends RuleWrapper\> | wrapper |
| wrap | Class\<? extends RuleSectionWrapper\>, boolean | wrapper, wrapDelays |
| copyOfView | boolean \| boolean, boolean | focusOnCopy \| removeCopyAtTheEnd, focusOnCopy |
| copyOfView | boolean, boolean, AXAnimation | removeCopyAtTheEnd, focusOnCopy, placeholderAnimation |
| addPreRule | PreRule | preRule |
| start | View | view |
| reverse | View | view |
| end | View | view |
| pause | Void | |
| resume | Void | |
| cancel | Void | |
| end | Void | |
| getTotalDuration | Void | |
| getCurrentPlayTime | Void | |
| setCurrentPlayTime | long | playTime |
| getAnimatedFraction | Void | |
| addAnimatorListener | AXAnimatorListener | listener |
| removeAnimatorListener | AXAnimatorListener | listener |
| withSectionStartAction | AXAnimatorStartListener | listener |
| withSectionEndAction | AXAnimatorEndListener | listener |
| withStartAction | AXAnimatorStartListener | listener |
| withEndAction | AXAnimatorEndListener | listener |
| addStartAction | AXAnimatorStartListener | listener |
| addEndAction | AXAnimatorEndListener | listener |
| removeStartAction | AXAnimatorStartListener | listener |
| removeEndAction | AXAnimatorEndListener | listener |
| resetAnimatorValues | Void | |
| resetAnimation | Void | |
| importAnimation | AXAnimation | animation |
| setAnimation | AXAnimation | animation |
| createSimpleAnimator : AXSimpleAnimator | View | target |
| save | String | animationName |
| getAnimation | String | animationName |
| getAnimationsOfView | View | view |
| clear | View | view |
| measureUnit | float | density |
| dp | Void | |
| px | Void | |

</p></details>

## Author
Amir Hossein Aghajari

### SUPPORT ❤️
If you find this library useful, Support it by joining [**stargazers**](https://github.com/aghajari/AXAnimation/stargazers) for this repository ⭐️


License
=======

    Copyright 2021 Amir Hossein Aghajari
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

<br>
<div align="center">
  <img width="64" alt="LCoders | AmirHosseinAghajari" src="https://user-images.githubusercontent.com/30867537/90538314-a0a79200-e193-11ea-8d90-0a3576e28a18.png">
  <br><a>Amir Hossein Aghajari</a> • <a href="mailto:amirhossein.aghajari.82@gmail.com">Email</a> • <a href="https://github.com/Aghajari">GitHub</a>
</div>
