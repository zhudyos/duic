<!--

    Copyright 2017-2018 the original author or authors

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<style lang="stylus" scoped>
    .editor
        position absolute
        top 191px
        right 0
        bottom 0
        left 0
</style>
<template>
    <v-card tile style="overflow: hidden" @keydown.esc="$emit('input')">
        <v-toolbar card dark color="primary">
            <v-btn icon @click="$emit('input')" dark>
                <v-icon>close</v-icon>
            </v-btn>
            <v-toolbar-title>{{name}}/{{profile}}</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
            <v-layout row>
                <v-flex xs3>
                    <v-select
                            v-model="left.version"
                            :items="left.versions"
                            label="HID"
                            single-line
                            @input="renderDiffContent()"
                    ></v-select>
                </v-flex>
                <v-spacer></v-spacer>
                <v-flex xs3>
                    <v-select
                            v-model="right.version"
                            :items="right.versions"
                            label="HID"
                            single-line
                            @input="renderDiffContent()"
                    ></v-select>
                </v-flex>
            </v-layout>
            <v-layout row>
                <v-flex xs3>
                    <span v-show="left.updatedBy">修改者：<span class="red--text">{{left.updatedBy}}</span></span>
                    <span v-show="left.updatedAt">修改时间：<span class="red--text">{{left.updatedAt}}</span></span>
                </v-flex>
                <v-spacer></v-spacer>
                <v-flex xs3>
                    <span v-show="right.updatedBy">修改者：<span class="red--text">{{right.updatedBy}}</span></span>
                    <span v-show="right.updatedAt">修改时间：<span class="red--text">{{right.updatedAt}}</span></span>
                </v-flex>
            </v-layout>
        </v-card-text>

        <div ref="codeEditor" class="editor"></div>
    </v-card>
</template>
<script>
    import axios from 'axios'

    export default {
        name: 'DAppHistoryDiff',
        data: () => ({
            left: {
                version: 'HEAD',
                versions: [],
                updatedBy: '',
                updatedAt: ''
            },
            right: {
                version: 'HEAD',
                versions: [],
                updatedBy: '',
                updatedAt: ''
            },
            app: {},
            historyItems: [],
            editor: null
        }),
        props: ['name', 'profile', 'hid'],
        mounted() {
            axios.get(`/api/admins/apps/${this.name}/${this.profile}`).then((response) => {
                this.app = response.data
                this.left.versions.push("HEAD")
                this.right.versions.push("HEAD")
                this.historyItems.push(response.data)

                axios.get(`/api/admins/apps/${this.name}/${this.profile}/histories`).then((response2) => {
                    response2.data.forEach((e) => {
                        if (e.hid) {
                            this.left.versions.push(e.hid)
                            this.right.versions.push(e.hid)
                            this.historyItems.push(e)
                        }
                    });

                    if (this.historyItems.length > 1) {
                        this.left.version = this.hid
                    }
                    this.initEditor();
                })
            })
        },
        methods: {
            initEditor() {
                let e = monaco.editor.createDiffEditor(this.$refs.codeEditor, {
                    theme: 'vs-dark',
                    readOnly: true
                })
                this.editor = e
                this.renderDiffContent()
            },
            renderDiffContent() {
                let left = this.left
                let right = this.right

                let leftItem = this.historyItems[left.versions.indexOf(left.version)]
                let rightItem = this.historyItems[right.versions.indexOf(right.version)]

                left.updatedBy = leftItem.updated_by
                left.updatedAt = leftItem.updated_at

                right.updatedBy = rightItem.updated_by
                right.updatedAt = rightItem.updated_at

                this.editor.setModel({
                    original: monaco.editor.createModel(leftItem.content, 'yaml'),
                    modified: monaco.editor.createModel(rightItem.content, 'yaml'),
                })
            }
        }
    }
</script>