<style type="less">
    #editor {
        position: absolute;
        top: 40px;
        right: 0;
        bottom: 50px;
        left: 0;
        font-size: 12px;
    }

    .commit-btn {
        position: fixed;
        bottom: 8px;
        margin-left: 35px;
    }
</style>
<template>
    <div>
        <breadcrumb class="ctn-breadcrumb-menu">
            <breadcrumb-item to="/">首页</breadcrumb-item>
            <breadcrumb-item to="/app">应用列表</breadcrumb-item>
            <breadcrumb-item>编辑配置(name: {{app.name}}, profile: {{app.profile}})</breadcrumb-item>
        </breadcrumb>
        <div id="editor"></div>
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
            axios.get(`/api/admins/apps/${query.name}/${query.profile}`).then((resp) => {
                this.app = resp.data;
                this.initEditor();
            });
        },
        methods: {
            commit() {
                axios.put(`/api/admins/apps/contents`, {
                    name: this.app.name,
                    profile: this.app.profile,
                    v: this.app.v,
                    content: this.editor.getValue()
                }).then((resp) => {
                    this.$Notice.success({title: '配置修改成功'});
                    this.app.v = resp.data.v;
                    this.$refs.commitBtn.disabled = true;
                }).catch((err) => {
                    var d = err.response.data || {};
                    var msg = d.message;

                    this.$Notice.error({title: '配置修改失败', desc: msg});
                });
            },
            initEditor() {
                var e = ace.edit('editor');
                e.setOptions({
                    printMarginColumn: 120
                });

                var session = e.session;
                session.setMode("ace/mode/yaml");
                session.setTabSize(2);
                session.setUseSoftTabs(true);

                e.setValue(this.app.content || '', 1);
                e.focus();
                e.on('change', this.changeContent);
                e.clearSelection();

                this.editor = e;
            },
            changeContent() {
                this.$refs.commitBtn.disabled = false;
            }
        }
    }
</script>