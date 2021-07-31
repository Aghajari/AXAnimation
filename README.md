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
- **PropertyRule** : Uses `ObjectAnimator` for some methods such as `alpha()`, `rotation()`, `scale()` and etc. Also It has a subclass called **PropertyValueRule** which uses `ValueAnimator` for more customiztions.
- **RuleSet** : Can create multi rules in just one rule.
- **NotAnimatedRule** : Some Rules have no Animators but they can update the target view's state such as `bringViewToFront` or `sendViewToBack`.
- **DrawRule** : Will draw Lines, Arcs, Shapes, Texts and etc on a `DrawableLayout` by canvas.

### RuleSection :
Each section contains some Rules which will play together. but the sections will play sequentially. You can have several sections in an Animation.

There is also a `WaitRule` which can add delay between each section.

### PreRule :
PreRule will prepare target for an Animation just before starting it. For Example `copyOfView(...)` makes a Placeholder of view.

### Thats it!
This was a quick introduction for AXAnimation.

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

Let's see a few more custom animations (Full Activity).

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
