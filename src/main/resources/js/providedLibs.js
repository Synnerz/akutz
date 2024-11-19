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

// Events
loadInstance("com.github.synnerz.akutz.engine.impl.Register", "EventTrigger")

globalThis.GlStateManager = wrap(Java.type("net.minecraft.client.renderer.GlStateManager"))
loadClass("org.lwjgl.opengl.GL11")
globalThis.MCTessellator = wrap(Renderer.getTessellator())
globalThis.WorldRenderer = wrap(Renderer.getWorldRenderer())
globalThis.DefaultVertexFormats = wrap(Java.type("net.minecraft.client.renderer.vertex.DefaultVertexFormats"))

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
globalThis.Gui = class Gui {
  constructor() {
    this.gui = new implGui()
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