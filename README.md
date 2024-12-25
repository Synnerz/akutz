# Akutz

## About
Akutz is a framework for minecraft that allows for javascript execution.<br>
This uses the [Javet Engine](https://github.com/caoccao/Javet) which uses JS V8 engine.<br>
We aim to be a better alternative for [ChatTriggers](https://github.com/ChatTriggers/ChatTriggers/) currently only supporting minecraft 1.8.9.<br>

### Downside
Currently, since we use V8 engine the interop (js to java and vice) is pretty slow in comparison to [CT Rhino](https://github.com/ChatTriggers/rhino).<br>
However, native javascript should be faster than it.

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
```
If you needed any of these events we are sure you can come up with a good workaround for them!<br>
As for the methods, you'll have to find that out (for now, in a near future we'll highly likely list them).<br>
We also currently do not have a good way to print out errors, so sadly they go to your minecraft logs which is not as good as having a console like [ChatTriggers](https://github.com/ChatTriggers/ChatTriggers/) does, we hope to fix this soon.

## Special Thanks
Massive thanks to [ChatTriggers](https://github.com/ChatTriggers/ChatTriggers/) for being the motivation behind this project as well as the (_mostly_) base api of it.