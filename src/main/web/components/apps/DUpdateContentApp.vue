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
        top 64px
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
            <v-spacer></v-spacer>
            <v-toolbar-items>
                <v-btn dark flat :disabled="submitBtnDisabled" @click="submit"> 保 存 </v-btn>
            </v-toolbar-items>
        </v-toolbar>

        <div ref="codeEditor" class="editor"></div>
    </v-card>
</template>
<script>
    import axios from 'axios'

    export default {
        name: 'DUpdateContentApp',
        data: () => ({
            editor: null,
            submitBtnDisabled: true,
            app: {}
        }),
        props: ['name', 'profile'],
        mounted() {
            axios.get(`/api/admins/apps/${this.name}/${this.profile}`).then((response) => {
                this.app = response.data
                this.initEditor()
            })
        },
        methods: {
            submit() {
                axios.put(`/api/admins/apps/contents`, {
                    name: this.app.name,
                    profile: this.app.profile,
                    v: this.app.v,
                    content: this.editor.getValue()
                }).then((response) => {
                    this.$notice('配置修改成功', {top: true, color: 'success'})
                    this.app.v = response.data.v
                    this.submitBtnDisabled = true
                }).catch((error) => {
                    var d = error.response.data || {}
                    var msg = d.message
                    this.$notice('配置修改失败：' + msg)
                })
            },
            initEditor() {
                window.require.config({paths: {'vs': '/monaco-editor/0.11.1/min/vs'}})
                window.require(['vs/editor/editor.main'], () => {
                    var e = monaco.editor.create(this.$refs.codeEditor, {
                        value: this.app.content,
                        theme: 'vs-dark',
                        language: 'yaml'
                    })

                    e.onDidChangeModelContent(this.changeContent);
                    e.focus()
                    this.editor = e
                })
            },
            changeContent() {
                this.submitBtnDisabled = false
            }
        }
    }
</script>