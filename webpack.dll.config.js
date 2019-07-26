const path = require('path')
const webpack = require('webpack')
const { CleanWebpackPlugin } = require('clean-webpack-plugin')
const AssetsPlugin = require('assets-webpack-plugin')

const root = (pathName = '') => path.resolve(__dirname, pathName)
const dllPath = 'web/assets/dll'

module.exports = {
  entry: {
    // 所有在开发中不会更改的包
    vendor: ['vue', 'vue-router', 'whatwg-fetch']
  },
  output: {
    path: root('static/js'),
    filename: 'dll_[name]_[hash:6].js',
    library: '[name]_[hash:6]'
  },
  plugins: [
    new CleanWebpackPlugin({
      cleanOnceBeforeBuildPatterns: ['dll_*'],
    }),
    new webpack.DllPlugin({
      path: root(`${dllPath}/manifest.json`),
      name: '[name]_[hash:6]' // 必须与 output.library 一致
    }),
    new AssetsPlugin({
      filename: `${dllPath}/assets.json`, // 相对于该配置文件的相对路径
      fullPath: false,
      prettyPrint: true
    }),
  ]
}