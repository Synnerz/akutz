globalThis.Java = {
  type: (clazz) => javet.package[clazz]
}

const Launch = Java.type("net.minecraft.launchwrapper.Launch")
const isDevEnv = Launch.blackboard.getOrDefault("fml.deobfuscatedEnvironment", false)

const getClassName = path => path.substring(path.lastIndexOf(".") + 1)

const loadClass = (clazz, name = getClassName(clazz)) => globalThis[name] = Java.type(clazz)

const loadInstance = (clazz, name = getClassName(clazz)) => globalThis[name] = Java.type(clazz).INSTANCE

const getInstance = (clazz) => Java.type(clazz).INSTANCE

const impl = getInstance("com.github.synnerz.akutz.engine.impl.Impl")
const System = Java.type("java.lang.System")
globalThis.print = (msg) => System.out.println(msg === undefined ? "undefined" : msg === null ? "null" : msg)

const getRelFileName = () => {
  const stack = new Error().stack
  if (!stack) throw "cannot resolve path"
  /*
  Error
      at require (<anonymous>:7:15)
      at .\config\Akutz\test\index.js:12:1
  */
  return stack.split("\n")[3].slice("    at .\\config\\Akutz\\".length).split(":").slice(0, -2).join(":")
}

globalThis.$import = (path, rel = getRelFileName()) => {
  const stack = new Error().stack
  if (!stack) throw "cannot resolve path"

  return new Promise((res, rej) => {
    impl.loadModuleDynamic(rel, path.endsWith(".js") ? path : path + ".js", val => {
      if (val) res(val)
      else rej("failed to import file")
    })
  })
}

globalThis.require = () => {
  throw "CommonJS modules are not supported viz. `require` and `module.exports`. Please use ECMAScript Modules instead. If you need dynamic imports (as `import()` is also not supported), please use the polyfill `$import()` instead."
}

const Paths = Java.type("java.nio.file.Paths")
const configLocation = Paths.get(Java.type("com.github.synnerz.akutz.Akutz").getConfigLocation().getAbsolutePath())

Object.defineProperties(globalThis, {
  __filename: {
    configurable: false,
    enumerable: false,
    get() {
      return configLocation.resolve(getRelFileName()).normalize().toString()
    }
  },
  __dirname: {
    configurable: false,
    enumerable: false,
    get() {
      return configLocation.resolve(getRelFileName()).getParent().normalize().toString()
    }
  }
})

globalThis.net = Java.type("net")
globalThis.java = Java.type("java")

loadClass("com.github.synnerz.akutz.api.libs.FileLib")

const objToMap = o => new Map(Object.entries(o))
/** @type {Map<string, Map<string, { t: 'f', n: string } | { t: 'm', n: string, s: string }>>} */
const mappings = JSON.parse(FileLib.readFromResource("mappings.json"), (k, v) => typeof v === "object" && typeof v.t !== "string" ? objToMap(v) : v)
const jObject = java.lang.Object
const reflPropCache = new Map()
function getField(className, c, n, o) {
  const prop = `${className}/${n}`
  let f = reflPropCache.get(prop)
  if (!f) {
    do {
      try {
        f = c.getDeclaredField(n)
        break
      } catch (_) { }
    } while (c = c.getSuperclass())
    if (!f) throw `Failed to get Field of field ${n} (${o}) in Class ${className}`
    m.setAccessible(true)
    reflPropCache.set(prop, f)
  }
  return f
}
const jClass = java.lang.Class
function getClass(c) {
  return jClass.isInstance(c) ? c : c.getClass?.()
}
const javaObjectKeysCache = new Map()
function javaObjectKeys(c) {
  if (javaObjectKeysCache.has(c)) return javaObjectKeysCache.get(c)
  const s = new Set()
  while (c) {
    c.getDeclaredFields().forEach(v => s.add(v.getName()))
    c.getDeclaredMethods().forEach(v => s.add(v.getName()))
    c = c.getSuperclass()
  }
  javaObjectKeysCache.set(c, s)
  return s
}
function $wrap(val) {
  if (isDevEnv) return val
  if (!jObject.isInstance(val)) return val

  const clazz = getClass(val)
  if (!clazz) return val
  const className = clazz.getName().replace(/\./g, "/")
  const propMap = mappings.get(className)
  if (!propMap) throw "Cannot find mappings for class: " + className
  {
    const s = new Set(javaObjectKeys(clazz))
    propMap.forEach((v, k) => s.add(k))
    var ownKeys = Array.from(s.keys())
  }

  return new Proxy({}, {
    get(t, p, r) {
      if (typeof p === "symbol") return Reflect.get(val, p, r)
      const d = propMap.get(p)
      if (!d) return Reflect.get(val, p, r)
      if (d.t === "m") return $wrapFunc(val, className, p)
      try {
        var v = Reflect.get(val, p, r)
      } catch (_) {
        v = getField(className, clazz, d.n, p).get(val)
      }
      return $wrap(v)
    },
    has(t, p) {
      if (typeof p === "symbol") return Reflect.has(val, p)
      return propMap.has(p) || Reflect.has(val, p)
    },
    ownKeys(t) {
      return ownKeys
    },
    getOwnPropertyDescriptor(t, p) {
      if (!this.has(t, p)) return
      return {
        value: this.get(t, p, t),
        configurable: true,
        enumerable: true
      }
    },
    set(t, p, v, r) {
      if (typeof p === "symbol") return Reflect.set(val, p, v, r)
      const d = propMap.get(p)
      if (!d) return Reflect.set(val, p, v, r)
      if (d.t !== "f") throw `Cannot set property ${p} as it is a method`
      try {
        Reflect.set(val, p, v, r)
      } catch (_) {
        getField(className, clazz, d.n, p).set(val, v)
      }
      return true
    }
  })
}
const reflMethCache = new Map()
const jMethodTypes = {
  Z: java.lang.Boolean.TYPE,
  B: java.lang.Byte.TYPE,
  C: java.lang.Character.TYPE,
  D: java.lang.Double.TYPE,
  F: java.lang.Float.TYPE,
  I: java.lang.Integer.TYPE,
  J: java.lang.Long.TYPE,
  S: java.lang.Short.TYPE,
}
function $wrapFunc(val, className, n) {
  const d = mappings.get(className).get(n)
  const meth = `${className}/${n}`
  let m = reflMethCache.get(meth)
  if (!m) {
    const desc = d.s.slice(d.s.indexOf("(") + 1, d.s.lastIndexOf(")"))
    const args = []
    let i = 0
    while (i < desc.length) {
      const c = desc[i]
      if (c in jMethodTypes) args.push(jMethodTypes[c])
      else if (c === "L") args.push(jClass.forName(desc.slice(i + 1, i = desc.indexOf(";", i + 1)).replace(/\//g, ".")))
      else throw "Unknown type " + c
      i++
    }
    let c = getClass(val)
    do {
      try {
        m = c.getDeclaredMethod(d.n, ...args)
        break
      } catch (_) { }
    } while (c = c.getSuperclass())
    if (!m) throw `Failed to get Method of method ${d.n} (${n}) in Class ${className}`
    m.setAccessible(true)
    reflMethCache.set(meth, m)
  }
  return new Proxy(Function.prototype, {
    apply(t, h, a) {
      return $wrap(m.invoke(val, ...a))
    }
  })
}
globalThis.wrap = val => $wrap(val)

// Wrappers
loadClass("com.github.synnerz.akutz.api.wrappers.Player")
loadClass("com.github.synnerz.akutz.api.wrappers.World")
loadClass("com.github.synnerz.akutz.api.wrappers.Client")
loadClass("com.github.synnerz.akutz.api.wrappers.entity.Entity")
loadClass("com.github.synnerz.akutz.api.wrappers.entity.TileEntity")
loadClass("com.github.synnerz.akutz.api.wrappers.entity.EntityLivingBase")
loadClass("com.github.synnerz.akutz.api.wrappers.entity.PlayerMP")
loadClass("com.github.synnerz.akutz.api.wrappers.world.Chunk")
loadClass("com.github.synnerz.akutz.api.wrappers.world.block.Block")
loadClass("com.github.synnerz.akutz.api.wrappers.world.block.Sign")
loadClass("com.github.synnerz.akutz.api.wrappers.PotionEffect")
loadClass("com.github.synnerz.akutz.api.wrappers.Team")
loadClass("com.github.synnerz.akutz.api.wrappers.inventory.Item")
loadClass("com.github.synnerz.akutz.api.wrappers.entity.Particle")
loadClass("com.github.synnerz.akutz.api.wrappers.inventory.Inventory")
loadClass("com.github.synnerz.akutz.api.wrappers.inventory.Slot")
loadClass("com.github.synnerz.akutz.api.wrappers.Scoreboard")
loadClass("com.github.synnerz.akutz.api.wrappers.TabList")

// Libs
loadClass("com.github.synnerz.akutz.api.libs.ChatLib")
loadClass("com.github.synnerz.akutz.api.libs.MathLib")
loadInstance("com.github.synnerz.akutz.api.libs.render.Renderer")
loadInstance("com.github.synnerz.akutz.api.libs.render.Tessellator")

// Objects
loadClass("com.github.synnerz.akutz.api.objects.render.Color")
loadClass("com.github.synnerz.akutz.api.objects.render.Image")
loadClass("com.github.synnerz.akutz.api.objects.state.StateVar")
loadClass("com.github.synnerz.akutz.api.objects.state.StateExp")
loadClass("com.github.synnerz.akutz.api.objects.gui.GuiHandler")
loadClass("com.github.synnerz.akutz.api.objects.data.PersistantData")
loadClass("com.github.synnerz.akutz.api.objects.data.PropertyArray")
loadClass("com.github.synnerz.akutz.api.objects.data.PropertyBoolean")
loadClass("com.github.synnerz.akutz.api.objects.data.PropertyColor")
loadClass("com.github.synnerz.akutz.api.objects.data.PropertyDouble")
loadClass("com.github.synnerz.akutz.api.objects.data.PropertyFlags")
loadClass("com.github.synnerz.akutz.api.objects.data.PropertyInteger")
loadClass("com.github.synnerz.akutz.api.objects.data.PropertyObject")
loadClass("com.github.synnerz.akutz.api.objects.data.PropertyOption")
loadClass("com.github.synnerz.akutz.api.objects.data.PropertyPercent")
loadClass("com.github.synnerz.akutz.api.objects.data.PropertyString")
loadClass("com.github.synnerz.akutz.api.objects.data.SettingsProperty")
loadClass("com.github.synnerz.akutz.api.objects.keybind.Keybind")
loadClass("com.github.synnerz.akutz.api.objects.sound.Sound")
loadClass("com.github.synnerz.akutz.api.wrappers.message.Message")
loadClass("com.github.synnerz.akutz.api.wrappers.message.TextComponent")

// Events
loadInstance("com.github.synnerz.akutz.engine.impl.Register", "EventTrigger")

globalThis.GlStateManager = wrap(Java.type("net.minecraft.client.renderer.GlStateManager"))
loadClass("org.lwjgl.opengl.GL11")
globalThis.MCTessellator = wrap(Renderer.getTessellator())
globalThis.WorldRenderer = wrap(Renderer.getWorldRenderer())
globalThis.DefaultVertexFormats = wrap(Java.type("net.minecraft.client.renderer.vertex.DefaultVertexFormats"))
globalThis.BlockPos = wrap(Java.type("net.minecraft.util.BlockPos"))

globalThis.register = (eventType, cb) => {
  if (typeof cb !== "function") return print(`${cb} is not a valid function, please make sure to pass in an actual function.`)
  if (jClass.isInstance(eventType)) return EventTrigger.register(eventType, (args) => cb(...args))
  return EventTrigger.register(eventType.includes(".") ? Java.type(eventType) : eventType, (args) => cb(...args))
}

globalThis.cancel = (event) => {
  try {
    EventTrigger.cancel(event)
    return true
  } catch (error) {
    if (event.isCancelable()) {
      event.setCanceled(true)
      return true
    }
    return false
  }
}

globalThis.toV8Value = val => impl.forceWrap(val)

const implGui = Java.type("com.github.synnerz.akutz.api.objects.gui.Gui")

// We need to make a wrapper for our own implementation
// due to the engine being a tad bit too funny and making
// the listeners' args into a single array
class AbstractGui {
  constructor() {
    this.gui = null
  }

  isOpen() {
    return this.gui.isOpen()
  }

  open() {
    this.gui.open()
    return this
  }

  close() {
    this.gui.close()
    return this
  }

  drawString(str, x, y) {
    this.gui.drawString(str, x, y)
  }

  drawCreativeTabHoveringString(str, x, y) {
    this.gui.drawCreativeTabHoveringString(str, x, y)
  }

  drawHoveringString(array, x, y) {
    this.gui.drawHoveringString(array, x, y)
  }

  isControlDown() {
    return this.gui.isControlDown()
  }

  isShiftDown() {
    return this.gui.isShiftDown()
  }

  isAltDown() {
    return this.gui.isAltDown()
  }

  onDraw(cb) {
    return this.gui.onDraw((args) => cb(...args))
  }

  onKeyTyped(cb) {
    return this.gui.onKeyTyped((args) => cb(...args))
  }

  onOpened(cb) {
    return this.gui.onOpened((args) => cb(...args))
  }

  onClosed(cb) {
    return this.gui.onClosed((args) => cb(...args))
  }

  onResize(cb) {
    return this.gui.onResize((args) => cb(...args))
  }

  onClick(cb) {
    return this.gui.onClick((args) => cb(...args))
  }

  onScroll(cb) {
    return this.gui.onScroll((args) => cb(...args))
  }

  onReleased(cb) {
    return this.gui.onReleased((args) => cb(...args))
  }

  onDragged(cb) {
    return this.gui.onDragged((args) => cb(...args))
  }
}

globalThis.Gui = class Gui extends AbstractGui {
  constructor() {
    super()
    this.gui = new implGui()
  }
}

const implDraggableGui = Java.type("com.github.synnerz.akutz.api.objects.gui.DraggableGui")

globalThis.DraggableGui = class DraggableGui extends AbstractGui {
  constructor(x = 0, y = 0, scale = 1) {
    super()
    this.gui = new implDraggableGui(x, y, scale)
  }

  getX() {
    return this.gui.getX()
  }

  getY() {
    return this.gui.getY()
  }

  getScale() {
    return this.gui.getScale()
  }
}

globalThis.JavaAdapter = function JavaAdapter() {
  if (arguments.length < 2) throw "Missing arguments for JavaAdapter(...clazzes, obj)"
  if (arguments.length > 2) throw "sorry, can only extend 1 class at a time :("
  return javet.extend(arguments[0], arguments[1])
  // return javet.extend(Array.from(arguments).slice(0, -1), arguments[arguments.length - 1])
}

const implDisplay = Java.type("com.github.synnerz.akutz.api.objects.render.Display")
const implDisplayLine = Java.type("com.github.synnerz.akutz.api.objects.render.DisplayLine")
const implBufferedText = Java.type("com.github.synnerz.akutz.api.objects.render.BufferedText")

class DisplayLine {
  constructor(_displayLine) {
    this.displayLine = _displayLine
  }

  onClick(cb) {
    return this.displayLine.onClick((args) => cb(...args))
  }

  onScroll(cb) {
    return this.displayLine.onScroll((args) => cb(...args))
  }

  onDragged(cb) {
    return this.displayLine.onDragged((args) => cb(...args))
  }

  getText() {
    return this.displayLine.getText()
  }

  getString() {
    return this.displayLine.getString()
  }

  setString(str) {
    this.displayLine.setString(str)
    return this
  }

  getX() {
    return this.displayLine.getX()
  }

  getY() {
    return this.displayLine.getY()
  }

  getScale() {
    return this.displayLine.getScale()
  }

  setScale(scale) {
    this.displayLine.setScale(scale)
    return this
  }

  getShadow() {
    return this.displayLine.getShadow()
  }

  setShadow(shadow) {
    this.displayLine.setShadow(shadow)
    return this
  }

  getResolution() {
    return this.displayLine.getResolution()
  }

  setResolution(res) {
    this.displayLine.setResolution(res)
    return this
  }

  getWidth() {
    return this.displayLine.getWidth()
  }

  getHeight() {
    return this.displayLine.getHeight()
  }

  getVisibleWidth() {
    return this.displayLine.getVisibleWidth()
  }

  getVisibleHeight() {
    return this.displayLine.getVisibleHeight()
  }

  update() {
    this.displayLine.update()
  }

  render(x, y, graph) {
    if (!graph) return this.displayLine.render(x, y)
    this.displayLine.render(x, y, graph)
  }
}

globalThis.Display = class Display {
  constructor(registerListeners = false, isBuffered = false) {
    this.display = new implDisplay(registerListeners, isBuffered)

    // Used for js intercept to the cb methods
    /** @private */
    this._isDirty = true
    /** @private */
    this._lines = []
  }

  onClick(cb) {
    return this.display.onClick((args) => cb(...args))
  }

  onScroll(cb) {
    return this.display.onScroll((args) => cb(...args))
  }

  onDragged(cb) {
    return this.display.onDragged((args) => cb(...args))
  }

  onCreateLine(cb) {
    return this.display.onCreateLine((line) => cb(new DisplayLine(line[0])))
  }

  onLineCreate(cb) {
    return this.onCreateLine(cb)
  }

  mark() {
    this.display.mark()
    return this
  }

  getLines() {
    if (this._isDirty) {
      this._lines = []
      this.display.getLines().forEach(it => {
        this._lines.push(new DisplayLine(it))
      })
      this._isDirty = false
    }
    return this._lines
  }

  getX() {
    return this.display.getX()
  }

  getY() {
    return this.display.getY()
  }

  setX(x) {
    this.display.setX(x)
    return this
  }

  setY(y) {
    this.display.setY(y)
    return this
  }

  getTopLeftX() {
    return this.display.getTopLeftX()
  }

  getTopLeftY() {
    return this.display.getTopLeftY()
  }

  getScale() {
    return this.display.getScale()
  }

  setScale(s) {
    this.display.setScale(s)
    return this
  }

  getGap() {
    return this.display.getGap()
  }

  setGap(g) {
    this.display.setGap(g)
    return this
  }

  getShadow() {
    return this.display.getShadow()
  }

  setShadow(s) {
    this.display.setShadow(s)
    return this
  }

  getResolution() {
    return this.display.getResolution()
  }

  setResolution(r) {
    this.display.setResolution(r)
    return this
  }

  getFont() {
    return this.display.getFont()
  }

  setFont(f) {
    this.display.setFont(f)
    return this
  }

  setLine(line) {
    this._isDirty = true
    this.display.setLine(line)
    return this
  }

  setLines(lines) {
    this._isDirty = true
    this.display.setLines(lines)
    return this
  }

  addLine(line) {
    this._isDirty = true
    this.display.addLine(line)
    return this
  }

  addLines(lines) {
    this._isDirty = true
    this.display.addLines(lines)
    return this
  }

  clearLines() {
    this._isDirty = true
    this._lines = []
    this.display.clearLines()
    return this
  }

  getWidth() {
    return this.display.getWidth()
  }

  getVisibleWidth() {
    return this.display.getVisibleWidth()
  }

  getLineHeight() {
    return this.display.getLineHeight()
  }

  getHeight() {
    return this.display.getHeight()
  }

  getVisibleHeight() {
    return this.display.getVisibleHeight()
  }

  getHorzAlign() {
    return this.display.getHorzAlign()
  }

  setHorzAlign(align) {
    this.display.setHorzAlign(align)
    return this
  }

  getVertAlign() {
    return this.display.getVertAlign()
  }

  setVertAlign(align) {
    this.display.setVertAlign(align)
    return this
  }

  getBackground() {
    return this.display.getBackground()
  }

  setBackground(bg) {
    this.display.setBackground(bg)
    return this
  }

  getBackgroundColor() {
    return this.display.getBackgroundColor()
  }

  setBackgroundColor(col) {
    this.display.setBackgroundColor(col)
    return this
  }

  render() {
    this.display.render()
  }

  getLineUnder(x, y) {
    return this.display.getLineUnder(x, y)
  }

  isInBounds(x, y) {
    return this.display.isInBounds(x, y)
  }

  static registerFont(name, font) {
    return implBufferedText.registerFont(name, font)
  }
}

function createBezier(x0, y0, xf, yf, ...p) {
  const n = p.length >> 1
  const c = []
  {
    let v = n + 1
    for (let i = 0; i < n; i++) {
      c.push(v)
      v *= (n - i) / (i + 2)
    }
  }
  return function(t) {
    const u = 1 - t
    let x = (u ** (n + 1)) * x0 + (t ** (n + 1)) * xf
    let y = (u ** (n + 1)) * y0 + (t ** (n + 1)) * yf
    for (let i = 0; i < n; i++) {
      x += c[i] * (u ** (n - i)) * (t ** (i + 1)) * p[(i << 1) + 0]
      y += c[i] * (u ** (n - i)) * (t ** (i + 1)) * p[(i << 1) + 1]
    }
    return [x, y]
  }
}
function invBezier(bz) {
  return function(x) {
    let pt = 0
    let t = x
    let px = 0
    for (let i = 0; i < 10; i++) {
      const p = bz(t)
      const dx = p[0] - px
      const dt = t - pt
      pt = t
      px = p[0]
      t += dx / dt * (x - p[0])
    }
    return bz(t)[1]
  }
}
function createEaser(cb) {
  return function(x) {
    x = +x
    if (Number.isNaN(x)) return x
    if (x <= 0) return 0
    if (x >= 1) return 1
    return cb(x)
  }
}
function createBezierEaser(...points) {
  return createEaser(invBezier(createBezier(0, 0, 1, 1, ...points)))
}
// https://easings.net/
const bounceFunc = x => {
  const n1 = 7.5625
  const d1 = 2.75
  if (x < 1 / d1) return n1 * x * x
  if (x < 2 / d1) return n1 * (x -= 1.5 / d1) * x + 0.75
  if (x < 2.5 / d1) return n1 * (x -= 2.25 / d1) * x + 0.9375
  return n1 * (x -= 2.625 / d1) * x + 0.984375
}
globalThis.Easing = {
  easeInSine: createEaser(x => 1 - Math.cos((x * Math.PI) / 2)),
  easeOutSine: createEaser(x => Math.sin((x * Math.PI) / 2)),
  easeInOutSine: createEaser(x => -(Math.cos(Math.PI * x) - 1) / 2),

  easeInQuad: createEaser(x => x * x),
  easeOutQuad: createEaser(x => 1 - (1 - x) * (1 - x)),
  easeInOutQuad: createEaser(x => x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2),

  easeInCubic: createEaser(x => x * x * x),
  easeOutCubic: createEaser(x => 1 - Math.pow(1 - x, 3)),
  easeInOutCubic: createEaser(x => x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2),

  easeInQuart: createEaser(x => x * x * x * x),
  easeOutQuart: createEaser(x => 1 - Math.pow(1 - x, 4)),
  easeInOutQuart: createEaser(x => x < 0.5 ? 8 * x * x * x * x : 1 - Math.pow(-2 * x + 2, 4) / 2),

  easeInQuint: createEaser(x => x * x * x * x * x),
  easeOutQuint: createEaser(x => 1 - Math.pow(1 - x, 5)),
  easeInOutQuint: createEaser(x => x < 0.5 ? 16 * x * x * x * x * x : 1 - Math.pow(-2 * x + 2, 5) / 2),

  easeInExpo: createEaser(x => Math.pow(2, 10 * x - 10)),
  easeOutExpo: createEaser(x => 1 - Math.pow(2, -10 * x)),
  easeInOutExpo: createEaser(x => x < 0.5 ? Math.pow(2, 20 * x - 10) / 2 : (2 - Math.pow(2, -20 * x + 10)) / 2),

  easeInCirc: createEaser(x => 1 - Math.sqrt(1 - Math.pow(x, 2))),
  easeOutCirc: createEaser(x => Math.sqrt(1 - Math.pow(x - 1, 2))),
  easeInOutCirc: createEaser(x => x < 0.5 ? (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2 : (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2),

  easeInBack: createEaser(x => {
    const c1 = 1.70158
    const c3 = c1 + 1
    return c3 * x * x * x - c1 * x * x
  }),
  easeOutBack: createEaser(x => {
    const c1 = 1.70158
    const c3 = c1 + 1
    return 1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2)
  }),
  easeInOutBack: createEaser(x => {
    const c1 = 1.70158
    const c2 = c1 * 1.525
    return x < 0.5
      ? (Math.pow(2 * x, 2) * ((c2 + 1) * 2 * x - c2)) / 2
      : (Math.pow(2 * x - 2, 2) * ((c2 + 1) * (x * 2 - 2) + c2) + 2) / 2
  }),

  easeInElastic: createEaser(x => {
    const c4 = (2 * Math.PI) / 3
    return -Math.pow(2, 10 * x - 10) * Math.sin((x * 10 - 10.75) * c4)
  }),
  easeOutElastic: createEaser(x => {
    const c4 = (2 * Math.PI) / 3
    return Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * c4) + 1
  }),
  easeInOutElastic: createEaser(x => {
    const c5 = (2 * Math.PI) / 4.5
    return x < 0.5
      ? -(Math.pow(2, 20 * x - 10) * Math.sin((20 * x - 11.125) * c5)) / 2
      : (Math.pow(2, -20 * x + 10) * Math.sin((20 * x - 11.125) * c5)) / 2 + 1
  }),

  easeInBounce: createEaser(x => 1 - bounceFunc(1 - x)),
  easeOutBounce: createEaser(bounceFunc),
  easeInOutBounce: createEaser(x => x < 0.5 ? (1 - bounceFunc(1 - 2 * x)) / 2 : (1 + bounceFunc(2 * x - 1)) / 2),

  cssLinear: createBezierEaser(0, 0, 1, 1),
  cssEase: createBezierEaser(0.25, 0.1, 0.25, 1),
  cssEaseIn: createBezierEaser(0.42, 0, 1, 1),
  cssEaseOut: createBezierEaser(0, 0, 0.58, 1),
  cssEaseInOut: createBezierEaser(0.42, 0, 0.58, 1),

  createBezier: createBezierEaser
}