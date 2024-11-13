globalThis.Java = { type: (clazz) => javet.package[clazz] }
const impl = Java.type("com.github.synnerz.akutz.engine.impl.Impl").INSTANCE
globalThis.print = (msg) => impl.print(msg === undefined ? 'undefined' : msg === null ? 'null' : msg)

function getRelFileName() {
  const stack = new Error().stack;
  if (!stack) throw 'cannot resolve path';
  /*
  Error
      at require (<anonymous>:7:15)
      at .\config\Akutz\test\index.js:12:1
  */
  return stack.split('\n')[3].slice('    at .\\config\\Akutz\\'.length).split(':').slice(0, -2).join(':');
}

const Paths = Java.type('java.nio.file.Paths');
const configLocation = Paths.get(Java.type('com.github.synnerz.akutz.Akutz').Companion.getConfigLocation().getAbsolutePath());
Object.defineProperties(globalThis, {
  __filename: {
    configurable: false,
    enumerable: false,
    get() {
      return configLocation.resolve(getRelFileName()).normalize().toString();
    }
  },
  __dirname: {
    configurable: false,
    enumerable: false,
    get() {
      return configLocation.resolve(getRelFileName()).getParent().normalize().toString();
    }
  }
});