const path = require("path")
const webpack = require("webpack")

const dllPath = "src/main/web/static/js"

module.exports = {
    entry: {
        vue_poly: ["vue", "vue-router"]
    },
    output: {
        path: path.join(__dirname, dllPath),
        filename: "[name].dll.js",
        // 保持与 webpack.DllPlugin 中名称一致
        library: "[name]_[hash]"
    },
    plugins: [
        new webpack.DllPlugin({
            path: path.join(__dirname, dllPath, '[name]-manifest.json'),
            // 保持与 output.library 中名称一致
            name: '[name]_[hash]',
            context: process.cwd()
        })
    ]
}