const path = require("path")
const CopyWebpackPlugin = require("copy-webpack-plugin")
const MonacoWebpackPlugin = require("monaco-editor-webpack-plugin")
const VuetifyLoaderPlugin = require("vuetify-loader/lib/plugin")

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
            new CopyWebpackPlugin([{
                from: "./src/main/web/assets",
                to: "public"
            }]),
            new MonacoWebpackPlugin(),
            new VuetifyLoaderPlugin()
        ]
    },
    assetsDir: "public",
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