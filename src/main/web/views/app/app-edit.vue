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
        <pre id="editor">{{app.content}}</pre>
        <i-button class="commit-btn" type="primary" @click="commit()"> 提 交 </i-button>
    </div>
</template>
<script>
    import axios from 'axios';

    export default {
        data() {
            return {
                editor: null,
                app: {}
            }
        },
        mounted() {
            head.load('//cdnjs.cloudflare.com/ajax/libs/ace/1.2.9/ace.js', () => {
                var editor = ace.edit("editor");
                var session = editor.session;
                session.setMode("ace/mode/yaml");
                session.setTabSize(2);
                session.setUseSoftTabs(true);

                this.editor = editor;
            });

            var params = this.$route.params;
            axios.get(`/api/admin/apps/${params.name}/${params.profile}`).then((resp) => {
                this.app = resp.data
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
                }).catch((err) => {
                    var d = err.response.data || {};
                    this.$Message.error(d.message || '配置修改失败');
                });
            }
        }
    }
</script>