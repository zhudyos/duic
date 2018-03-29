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
                    <span v-show="left.revisedBy">修改者：<span class="red--text">{{left.revisedBy}}</span></span>
                    <span v-show="left.updatedAt">修改时间：<span class="red--text">{{left.updatedAt}}</span></span>
                </v-flex>
                <v-spacer></v-spacer>
                <v-flex xs3>
                    <span v-show="right.revisedBy">修改者：<span class="red--text">{{right.revisedBy}}</span></span>
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
                revisedBy: '',
                updatedAt: ''
            },
            right: {
                version: 'HEAD',
                versions: [],
                revisedBy: '',
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
                window.require.config({paths: {'vs': '/monaco-editor/0.11.1/min/vs'}})
                window.require(['vs/editor/editor.main'], () => {
                    var e = monaco.editor.createDiffEditor(this.$refs.codeEditor, {
                        theme: 'vs-dark',
                        readOnly: true
                    })
                    this.editor = e
                    this.renderDiffContent()
                });
            },
            renderDiffContent() {
                let left = this.left
                let right = this.right

                let leftItem = this.historyItems[left.versions.indexOf(left.version)]
                let rightItem = this.historyItems[right.versions.indexOf(right.version)]

                left.revisedBy = leftItem.revised_by
                left.updatedAt = leftItem.updated_at

                right.revisedBy = rightItem.revised_by
                right.updatedAt = rightItem.updated_at

                this.editor.setModel({
                    original: monaco.editor.createModel(leftItem.content, 'yaml'),
                    modified: monaco.editor.createModel(rightItem.content, 'yaml'),
                })
            }
        }
    }
</script>