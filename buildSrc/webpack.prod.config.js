const webpack = require('webpack')
const cleanWebpackPlugin = require('clean-webpack-plugin')
const merge = require('webpack-merge')
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const webpackBaseConfig = require('./webpack.base.config.js')
const path = require('path')

module.exports = merge(webpackBaseConfig, {
    output: {
        filename: '[name].[hash].js',
        chunkFilename: '[name].chunk.[hash].js'
    },
    plugins: [
        new cleanWebpackPlugin(['dist/*'], {
            root: path.resolve(__dirname, '../')
        }),
        new webpack.DefinePlugin({
            'process.env': {
                NODE_ENV: '"production"'
            }
        }),
        new ExtractTextPlugin({
            filename: '[name].[hash].css',
            allChunks: true
        }),
        new webpack.optimize.UglifyJsPlugin({
            compress: {
                warnings: false
            }
        })
    ]
})