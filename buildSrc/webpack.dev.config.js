const merge = require('webpack-merge');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const webpackBaseConfig = require('./webpack.base.config.js');

module.exports = merge(webpackBaseConfig, {
    devtool: '#source-map',
    output: {
        filename: '[name].js',
        chunkFilename: '[name].chunk.js'
    },
    plugins: [
        new ExtractTextPlugin({
            filename: '[name].css',
            allChunks: true
        })
    ],
    devServer: {
        proxy: {
            '/api': {
                target: 'http://localhost:7777'
            }
        }
    }
});