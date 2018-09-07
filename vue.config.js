const path = require('path')
const CopyWebpackPlugin = require('copy-webpack-plugin')

function resolve(dir) {
    return path.join(__dirname, dir)
}

module.exports = {
    chainWebpack: config => {
        config.resolve.alias
            .set('@', resolve('src/main/web'))

        config.plugin('copy')
            .use(CopyWebpackPlugin, [[{
                from: './node_modules/monaco-editor/min',
                to: 'monaco-editor/min'
            }, {
                from: './node_modules/monaco-editor/min-maps',
                to: 'monaco-editor/0.11.1/min-maps'
            }, {
                from: './src/main/web/statics/fontawesome-free-5.0.10',
                to: 'fontawesome-free-5.0.10/'
            }]])
    },
    pages: {
        index: {
            entry: 'src/main/web/index.js',
            template: 'src/main/web/templates/index.html',
            filename: 'index.html'
        },
        oai: {
            entry: 'src/main/web/oai.js',
            template: 'src/main/web/oai.html',
            filename: 'oai.html'
        }
    },
    devServer: {
        proxy: {
            '/api': {
                target: 'http://localhost:7777'
            }
        }
    }
}