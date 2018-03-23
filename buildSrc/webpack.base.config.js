const path = require('path')
const os = require('os')
const webpack = require('webpack')
const ExtractTextPlugin = require('extract-text-webpack-plugin')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const CopyWebpackPlugin = require('copy-webpack-plugin')
const HappyPack = require('happypack')
var happyThreadPool = HappyPack.ThreadPool({size: os.cpus().length})
const package = require('../package.json')
const vuetifyCSS = new ExtractTextPlugin('vertify.[hash].css')

function resolve(dir) {
    return path.join(__dirname, dir)
}

module.exports = {
    entry: {
        main: '@/index',
        'vue-stack': '@/vendors/vue.stack.js',
        'common-libs': '@/vendors/common.libs.js'
    },
    output: {
        path: path.resolve(__dirname, '../dist/dist'),
        publicPath: '/',
        filename: '[name].[hash].js',
        chunkFilename: '[name].chunk.[hash].js'
    },
    module: {
        rules: [
            {
                test: /\.vue$/,
                loader: 'vue-loader',
                options: {
                    loaders: {
                        stylus: ExtractTextPlugin.extract({
                            use: ['css-loader', 'stylus-loader'],
                            fallback: 'vue-style-loader'
                        })
                    }
                }
            },
            {
                test: /\.js$/,
                loader: 'babel-loader',
                exclude: /node_modules/
            },
            {
                test: /\.js[x]?$/,
                include: [resolve('src')],
                exclude: /node_modules/,
                loader: 'happypack/loader?id=happybabel'
            },
            {
                test: /vuetify\.css$/,
                use: vuetifyCSS.extract(['css-loader?minimize=true'])
            },
            {
                test: /\.(gif|jpg|png|woff|svg|eot|ttf)\??.*$/,
                loader: 'url-loader?limit=1024'
            }
        ]
    },
    plugins: [
        new HappyPack({
            id: 'happybabel',
            loaders: ['babel-loader'],
            threadPool: happyThreadPool,
            cache: true,
            verbose: true
        }),
        new webpack.optimize.CommonsChunkPlugin({
            name: ['vue-stack', 'common-libs'],
            minChunks: Infinity
        }),
        new HtmlWebpackPlugin({
            title: 'DuiC Admin v' + package.version,
            favicon: './src/main/web2/images/favicon.ico',
            filename: './index.html',
            template: './src/main/web2/templates/index.html',
        }),
        new CopyWebpackPlugin([{
            from: './node_modules/monaco-editor/min',
            to: 'monaco-editor/0.11.1/min'
        }, {
            from: './node_modules/monaco-editor/min-maps',
            to: 'monaco-editor/0.11.1/min-maps'
        }]),
        vuetifyCSS
    ],
    resolve: {
        extensions: ['.js', '.vue', '.styl', '.css'],
        alias: {
            'vue': 'vue/dist/vue.esm.js',
            '@': resolve('../src/main/web2'),
        }
    },
    externals: {
        require: 'require',
        monaco: 'monaco'
    }
}
