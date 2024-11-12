globalThis.Java = { type: (clazz) => javet.package[clazz] }
const impl = Java.type("com.github.synnerz.akutz.engine.impl.Impl").INSTANCE
globalThis.print = (msg) => impl.print(msg)