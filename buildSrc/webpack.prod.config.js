const webpack = require('webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const cleanWebpackPlugin = require('clean-webpack-plugin');
const merge = require('webpack-merge');
const webpackBaseConfig = require('./webpack.base.config.js');
const fs = require('fs');
const path = require('path');
const package = require('../package.json');

module.exports = merge(webpackBaseConfig, {
    output: {
        publicPath: '/',
        filename: '[name].[hash].js',
        chunkFilename: '[name].[hash].chunk.js'
    },
    plugins: [
        new cleanWebpackPlugin(['dist/*'], {
            root: path.resolve(__dirname, '../')
        }),
        new ExtractTextPlugin({
            filename: '[name].[hash].css',
            allChunks: true
        }),
        new webpack.optimize.CommonsChunkPlugin({
            name: ['vender-exten', 'vender-base'],
            minChunks: Infinity
        }),
        new webpack.DefinePlugin({
            'process.env': {
                NODE_ENV: '"production"'
            }
        }),
        new webpack.optimize.UglifyJsPlugin({
            compress: {
                warnings: false
            }
        }),
        new HtmlWebpackPlugin({
            title: 'DuiC Admin v' + package.version,
            filename: './index.html',
            template: './src/main/web/template/index.ejs',
            inject: false
        })
    ]
});