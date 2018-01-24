<style lang="less">
    @import "../../styles/common";

    .diff-editor {
        position: absolute;
        top: 80px;
        right: 0;
        bottom: 10px;
        left: 0;
        font-size: 12px;
    }
</style>
<template>
    <div>
        <breadcrumb class="ctn-breadcrumb-menu">
            <breadcrumb-item to="/">首页</breadcrumb-item>
            <breadcrumb-item to="/app">应用列表</breadcrumb-item>
            <breadcrumb-item>修改历史比较(name: <label style="color: red">{{$route.query.name}}</label>, profile: <label
                    style="color: red">{{$route.query.profile}}</label>)
            </breadcrumb-item>
        </breadcrumb>

        <row class="margin-bottom-10">
            <i-col span="12">
                <i-select v-model="left.version" style="width: 200px" @on-change="renderDiffContent()">
                    <i-option v-for="(item, idx) in left.versions" :value="idx" :key="idx">{{item}}</i-option>
                </i-select>
                <span v-show="left.revisedBy">修改者：<span style="font-weight: bold;">{{left.revisedBy}}</span></span>
                <span v-show="left.updatedAt">修改时间：<span style="font-weight: bold;">{{left.updatedAt}}</span></span>
            </i-col>
            <i-col span="12">
                <i-select v-model="right.version" style="width: 200px;" @on-change="renderDiffContent()">
                    <i-option v-for="(item, idx) in right.versions" :value="idx" :key="idx">{{item}}</i-option>
                </i-select>
                <span v-show="right.revisedBy">修改者：<span style="font-weight: bold;">{{right.revisedBy}}</span></span>
                <span v-show="right.updatedAt">修改时间：<span style="font-weight: bold;">{{right.updatedAt}}</span></span>
            </i-col>
        </row>

        <div id="editor" class="diff-editor"></div>
    </div>
</template>

<script>
    import axios from 'axios';

    export default {
        name: 'app',
        data() {
            return {
                left: {
                    version: 0,
                    versions: [],
                    revisedBy: '',
                    updatedAt: ''
                },
                right: {
                    version: 0,
                    versions: [],
                    revisedBy: '',
                    updatedAt: ''
                },
                app: {},
                historyItems: [],
                editor: null
            };
        },
        mounted() {
            var query = this.$route.query;
            // 加载 app 最新信息
            axios.get(`/api/admins/apps/${query.name}/${query.profile}`).then((resp) => {
                this.app = resp.data;
                this.left.versions.push("HEAD");
                this.right.versions.push("HEAD");
                this.historyItems.push(resp.data);

                axios.get(`/api/admins/apps/${query.name}/${query.profile}/histories`).then((resp) => {
                    resp.data.forEach((e) => {
                        if (e.hid) {
                            this.left.versions.push(e.hid);
                            this.right.versions.push(e.hid);
                            this.historyItems.push(e);
                        }
                    });

                    if (this.historyItems.length > 1) {
                        this.left.version = 1
                    }
                    this.initEditor();
                });
            });
        },
        methods: {
            initEditor() {
                var that = this;
                window.require.config({paths: {'vs': 'https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.10.1/min/vs'}});
                window.require(['vs/editor/editor.main'], function () {
                    var e = monaco.editor.createDiffEditor(document.getElementById('editor'), {
                        readOnly: true
                    });
                    that.editor = e;
                    that.renderDiffContent();
                });
            },
            renderDiffContent() {
                var leftItem = this.historyItems[this.left.version];
                var rightItem = this.historyItems[this.right.version];

                this.left.revisedBy = leftItem.revised_by;
                this.left.updatedAt = leftItem.updated_at;

                this.right.revisedBy = rightItem.revised_by;
                this.right.updatedAt = rightItem.updated_at;

                this.editor.setModel({
                    original: monaco.editor.createModel(leftItem.content, 'yaml'),
                    modified: monaco.editor.createModel(rightItem.content, 'yaml'),
                });
            }
        }
    };
</script>
