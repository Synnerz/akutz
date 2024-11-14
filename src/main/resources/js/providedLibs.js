globalThis.Java = {
  type: (clazz) => javet.package[clazz]
}

const getClassName = path => path.substring(path.lastIndexOf(".") + 1)

const loadClass = (clazz) => globalThis[getClassName(clazz)] = Java.type(clazz)

const loadInstance = (clazz) => globalThis[getClassName(clazz)] = Java.type(clazz).INSTANCE

const getInstance = (clazz) => Java.type(clazz).INSTANCE

const impl = getInstance("com.github.synnerz.akutz.engine.impl.Impl")
globalThis.print = (msg) => impl.print(msg === undefined ? "undefined" : msg === null ? "null" : msg)

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

const objToMap = o => new Map(Object.entries(o))
/** @type {Map<string, Map<string, { t: 'f', n: string } | { t: 'm', n: string, s: string }>>} */
const mappings = JSON.parse(impl.readMappingsFile(), (k, v) => typeof v === "object" && typeof v.t !== "string" ? objToMap(v) : v)
const jObject = java.lang.Object
const reflPropCache = new Map()
function getField(className, c, n) {
  const prop = `${className}/${n}`
  let f = reflPropCache.get(prop)
  if (!f) {
    f = c.getField(d.n)
    if (!f) throw `Failed to get Field of field ${d.n} (${p}) in Class ${className}`
    m.setAccessible(true)
    reflPropCache.set(prop, f)
  }
  return f
}
function $wrap(val) {
  if (!(val instanceof jObject)) return val

  const className = val.getClass().getName().replace(/\./g, "/")
  const propMap = mappings.get(className)
  if (!propMap) throw "Cannot find mappings for class: " + className

  return new Proxy(val, {
    get(t, p, r) {
      const d = propMap.get(p)
      if (!d) throw "Unknown property: " + p
      if (d.t === "f") return $wrapFunc(t, className, p)
      try {
        var val = Reflect.get(t, p, r)
      } catch (_) {
        val = getField(className, t.getClass(), d.n).get(t)
      }
      return $wrap(val)
    },
    has(t, p) {
      return propMap.has(p)
    },
    ownKeys(t) {
      return propMap.keys()
    },
    getOwnPropertyDescriptor(t, p) {
      return {
        value: this.get(t, p, t),
        configurable: true,
        enumerable: true
      }
    },
    set(t, p, v, r) {
      const d = propMap.get(p)
      if (!d) throw "Unknown property: " + p
      if (d.t !== "f") throw `Cannot set property ${p} as it is a method`
      try {
        Reflect.set(t, p, v, r)
      } catch (_) {
        getField(className, t.getClass(), d.n).set(t, v)
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
const jClass = java.lang.Class
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
      else if (c === "L") args.push(jClass.forName(desc.slice(i + 1, i = desc.indexOf(";", i + 1))))
      else throw "Unknown type " + c
      i++
    }
    m = c.getMethod(d.n, ...args)
    if (!m) throw `Failed to get Method of method ${d.n} (${p}) in Class ${className}`
    m.setAccessible(true)
    reflMethCache.set(meth, m)
  }
  return new Proxy(val, {
    apply(t, h, a) {
      return $wrap(m.invoke(h, ...a))
    }
  })
}
globalThis.wrap = val => $wrap(val)


loadClass("com.github.synnerz.akutz.api.wrappers.Player")
loadClass("com.github.synnerz.akutz.api.wrappers.World")

loadClass("com.github.synnerz.akutz.api.libs.ChatLib")

const ForgeEvent = Java.type("com.github.synnerz.akutz.api.events.ForgeEvent")

const registerForge = (clazz, fn) => new ForgeEvent(fn, clazz)
