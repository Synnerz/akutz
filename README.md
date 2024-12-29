# Akutz

## About
Akutz is a framework for minecraft that allows for javascript execution.<br>
This uses the [Javet Engine](https://github.com/caoccao/Javet) which uses JS V8 engine.<br>
We aim to be a better alternative for [ChatTriggers](https://github.com/ChatTriggers/ChatTriggers/) currently only supporting minecraft 1.8.9.<br>

### Downside
Currently, since we use V8 engine the interop (js to java and vice) is pretty slow in comparison to [CT Rhino](https://github.com/ChatTriggers/rhino).<br>
However, native javascript should be faster than it.

### Upsides
Since this is being developed by people who used the apis we kind of have a general idea of how people would like apis to be, and so we centralize more in this aspect.<br>
As well as having built in mappings, so you don't need to do those weird obfuscated method/field names (some that are not mapped to real values are still obfuscated but the majority is mapped).<br>
We hope to keep improving our api based on feedback and add new features as well as improve our interop in the future.

### Comparison
Here we're testing how good is Akutz against Chattriggers on each side.<br>
<br>
Testing native js:<br>
Amount | Akutz | Chattriggers
--- | --- | ---
100k | ~3ms | ~230ms
1M | ~60ms | ~4,500ms
10M | ~1,284ms | ~112,569ms

code:<br>
```js
const isPrime = (num) => {
    if (num <= 1) return false
    if (num === 2) return true
    if (num % 2 === 0) return false

    for (let i = 3; i <= Math.sqrt(num); i += 2) {
        if (num % i === 0) {
            return false
        }
    }

    return true
}
```
<br>

Testing interop (js to java and vice):<br>
Amount | Akutz | Chattriggers
--- | --- | ---
1k | ~93ms | ~0ms
8k | ~589ms | ~5ms
27k | ~1,773ms | ~16ms
64k | ~4,180ms | ~17ms

code: <br>
```js
const [ x, y, z ] = [-1674, 4, 1495]
let scanned = []
for (let x1 = x; x1 < x + n; x1++) {
    for (let z1 = z; z1 < z + n; z1++) {
        for (let y1 = y; y1 < y + n; y1++) {
            scanned.push(new BlockPos(x1, y1, z1))
        }
    }
}
```
<br>
Yes, sadly this little code that seems to do nothing takes a lot to process in Akutz because of the interop which we hope to improve in the future.
<br>

### Differences
If you are a developer coming from [ChatTriggers](https://github.com/ChatTriggers/ChatTriggers/) we tried to make our apis as similar as possible, so you wouldn't struggle migrating much.<br>
That being said, we have removed quite a few of methods that we deemed unnecessary as well as some events (triggers).<br>
A list of events that we have removed are:
```
ScreenshotTaken
ChatComponentClicked
ChatComponentHovered
GuiDrawBackground
RenderPlayerList
RenderBossHealth
RenderDebug
RenderCrosshair
RenderHotbar
RenderExperience
RenderArmor
RenderHealth
RenderFood
RenderMountHealth
RenderAir
RenderPortal
RenderJumpBar
RenderHelmet
RenderHand
RenderHotbar
PlayerJoin
PlayerLeave
NoteBlockPlay
NoteBlockChange
HitBlock
```
If you needed any of these events we are sure you can come up with a good workaround for them!<br>
As for the methods, you'll have to find that out (for now, in a near future we'll highly likely list them).<br>
We also currently do not have a good way to print out errors, so sadly they go to your minecraft logs which is not as good as having a console like [ChatTriggers](https://github.com/ChatTriggers/ChatTriggers/) does, we hope to fix this soon.

## Special Thanks
Massive thanks to [ChatTriggers](https://github.com/ChatTriggers/ChatTriggers/) for being the motivation behind this project as well as the (_mostly_) base api of it.