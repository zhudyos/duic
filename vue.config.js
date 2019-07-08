const path = require("path")
const webpack = require("webpack")
const CopyWebpackPlugin = require("copy-webpack-plugin")
const MonacoWebpackPlugin = require("monaco-editor-webpack-plugin")

function resolve(dir) {
  return path.join(__dirname, dir)
}

module.exports = {
  configureWebpack: {
    resolve: {
      alias: {
        "@": resolve("src/main/web")
      }
    },
    plugins: [
      new webpack.DllReferencePlugin(
        {
          context: process.cwd(),
          manifest: require("./src/main/web/static/js/vue_poly-manifest.json")
        }
      ),
      new CopyWebpackPlugin([
        {
          from: "./src/main/web/assets",
          to: "public"
        },
        {
          from: "./src/main/web/static",
          to: "static"
        }
      ]),
      new MonacoWebpackPlugin(
        {
          languages: ["yaml"]
        }
      )
    ]
  },
  pluginOptions: {
    quasar: {
      treeShake: true
    }
  },
  transpileDependencies: [
    /[\\\/]node_modules[\\\/]quasar[\\\/]/
  ],
  pages: {
    index: {
      entry: "src/main/web/main.js",
      template: "src/main/web/index.html"
    }
  },
  devServer: {
    proxy: {
      "/api": {
        target: "http://127.0.0.1:7777"
      }
    }
  }
}
