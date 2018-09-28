/*
 * Copyright 2017-2018 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
const path = require('path')
const CopyWebpackPlugin = require('copy-webpack-plugin')

function resolve(dir) {
    return path.join(__dirname, dir)
}

module.exports = {
    configureWebpack: {
        resolve: {
            alias: {
                '@': resolve('src/main/web')
            }
        },
        plugins: [
            new CopyWebpackPlugin([{
                from: './node_modules/monaco-editor/min',
                to: 'monaco-editor/min'
            }, {
                from: './node_modules/monaco-editor/min-maps',
                to: 'monaco-editor/0.11.1/min-maps'
            }, {
                from: './src/main/web/statics/fontawesome-free-5.0.10',
                to: 'fontawesome-free-5.0.10/'
            }, {
                from: './src/main/web/assets',
                to: 'public'
            }])
        ]
    },
    runtimeCompiler: true,
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