const path = require('path')
const webpack = require('webpack')
const CopyWebpackPlugin = require('copy-webpack-plugin')
const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin')

const src = (pathName = '') => path.join(__dirname, 'web/src', pathName)
const dll = (pathName = '') => src(`assets/dll/${pathName}`)

module.exports = {
  configureWebpack: {
    resolve: {
      alias: {
        '@': src(),
        'quasar-variables-styl': 'quasar/src/css/variables.styl',
        'quasar-styl': 'quasar/dist/quasar.styl',
        'quasar-addon-styl': 'quasar/src/css/flex-addon.styl'
      }
    },
    plugins: [
      new CopyWebpackPlugin([
        { from: src('../static'), to: 'static' }
      ]),
      new MonacoWebpackPlugin({
        languages: ['yaml']
      }),
      new webpack.DllReferencePlugin({
        manifest: require(dll('manifest.json'))
      }),
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
      entry: src('main.ts'),
      template: src('index.html'),
      // 自定义数据 可以在 template 中使用
      dllJs: `/static/js/${require(dll('file.json')).vendor.js}`
    }
  },
  devServer: {
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:7777'
      }
    }
  }
}
