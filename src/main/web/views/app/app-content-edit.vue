<style type="less">
    #editor {
        position: absolute;
        top: 30px;
        right: 0;
        bottom: 10%;
        left: 0;
        font-size: 12px;
    }

    .commit-btn {
        position: fixed;
        bottom: 5%;
        margin-left: 30px;
    }
</style>
<template>
    <div>
        <breadcrumb class="ctn-breadcrumb-menu">
            <breadcrumb-item>首页</breadcrumb-item>
            <breadcrumb-item to="/app">应用列表</breadcrumb-item>
            <breadcrumb-item>修改配置(name: {{app.name}}, profile: {{app.profile}})</breadcrumb-item>
        </breadcrumb>
        <pre id="editor"></pre>
        <i-button ref="commitBtn" class="commit-btn" type="primary" @click="commit()" disabled>提交</i-button>
    </div>
</template>
<script>
    import axios from 'axios';

    export default {
        name: 'app-edit',
        data() {
            return {
                editor: null,
                app: {}
            }
        },
        mounted() {
            var query = this.$route.query;
            axios.get(`/api/admin/apps/${query.name}/${query.profile}`).then((resp) => {
                this.app = resp.data;
                this.initEditor();
            });
        },
        methods: {
            commit() {
                axios.put(`/api/admin/apps`, {
                    name: this.app.name,
                    profile: this.app.profile,
                    v: this.app.v,
                    content: this.editor.getValue()
                }).then(() => {
                    this.$Message.success('配置修改成功');
                    this.$refs.commitBtn.disabled = true;
                }).catch((err) => {
                    var d = err.response.data || {};
                    var msg = d.message;
                    if (d.code === 1006) {
                        msg = '文件格式错误';
                    }
                    this.$Message.error(msg || '配置修改失败');
                });
            },
            initEditor() {
                head.load('//cdnjs.cloudflare.com/ajax/libs/ace/1.2.9/ace.js', () => {
                    var editor = ace.edit("editor");
                    var session = editor.session;
                    session.setMode("ace/mode/yaml");
                    session.setTabSize(2);
                    session.setUseSoftTabs(true);

                    editor.setValue(this.app.content, 1);
                    editor.focus();
                    editor.clearSelection();
                    editor.on('change', this.changeContent);

                    this.editor = editor;
                });
            },
            changeContent() {
                this.$refs.commitBtn.disabled = false;
            }
        }
    }
</script>