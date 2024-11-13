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

globalThis.require = (path) => $import(path, getRelFileName())

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

loadClass("com.github.synnerz.akutz.api.wrappers.Player")
loadClass("com.github.synnerz.akutz.api.wrappers.World")

loadClass("com.github.synnerz.akutz.api.libs.ChatLib")

const ForgeEvent = Java.type("com.github.synnerz.akutz.api.events.ForgeEvent")

const registerForge = (clazz, fn) => new ForgeEvent(fn, clazz)
