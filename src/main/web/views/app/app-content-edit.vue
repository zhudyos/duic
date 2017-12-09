<style type="less">
    #editor {
        position: absolute;
        top: 0;
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
        <pre id="editor"></pre>
        <i-button ref="commitBtn" class="commit-btn" type="primary" @click="commit()" disabled> 提 交 </i-button>
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
                this.$refs.commitBtn.loading = true;
                axios.put(`/api/admin/apps`, {
                    name: this.app.name,
                    profile: this.app.profile,
                    v: this.app.v,
                    content: this.editor.getValue()
                }).then(() => {
                    this.$Message.success('配置修改成功');
                    this.$router.go({path: this.$route.path, force: true});
                }).catch((err) => {
                    var d = err.response.data || {};
                    this.$Message.error(d.message || '配置修改失败');
                    this.$refs.commitBtn.loading = false;
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