<style type="less">
</style>
<template>
    <div>
        <breadcrumb class="ctn-breadcrumb-menu">
            <breadcrumb-item to="/">首页</breadcrumb-item>
            <breadcrumb-item to="/app">应用列表</breadcrumb-item>
            <breadcrumb-item>编辑应用(name: <label style="color: red">{{app.name}}</label>, profile: <label style="color: red">{{app.profile}}</label>)</breadcrumb-item>
        </breadcrumb>
        <card>
            <i-form :model="app" :rules="validation" label-position="right" :label-width="80">
                <form-item label="应用描述" prop="description">
                    <i-input v-model="app.description" type="textarea" placeholder="应用描述 description"></i-input>
                </form-item>
                <form-item label="访问令牌" prop="token">
                    <i-input v-model="app.token" placeholder="访问令牌, 不填写该值则不进行令牌校验">
                        <i-button slot="append" @click="generateToken()">生成</i-button>
                    </i-input>
                </form-item>
                <form-item label="IP 限制" prop="token">
                    <i-input v-model="app.ip_limit" placeholder="127.0.0.1,192.168.1.1-192.168.1.255">
                        <i-button slot="append" @click="localNetIps()">局域网</i-button>
                    </i-input>
                </form-item>
                <form-item label="所属用户">
                    <i-select v-model="app.users" multiple filterable>
                        <i-option v-for="item in users" :value="item" :key="item">{{item}}</i-option>
                    </i-select>
                </form-item>
                <form-item>
                    <i-button type="primary" @click="commit()">提交</i-button>
                </form-item>
            </i-form>
        </card>
    </div>
</template>
<script>
    import axios from 'axios';
    import deepExtend from 'deep-extend';

    function _generateToken() {
        return ([1e7] + 1e3 + 4e3 + 8e3 + 1e11).replace(/[018]/g, c =>
            (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
        )
    }

    export default {
        name: 'app-create',
        data() {
            return {
                validation: {
                    description: [
                        {required: true, message: '应用描述不能为空', trigger: 'blur'}
                    ]
                },
                app: {
                    name: '',
                    profile: '',
                    description: '',
                    token: '',
                    ip_limit: '',
                    users: []
                },
                users: []
            }
        },
        mounted() {
            axios.get(`/api/admins/users/emails`).then((resp) => {
                this.users = resp.data;
            }).catch((err) => {
                var d = err.response.data || {};
                this.$Message.error(d.message || '获取用户邮箱失败');
            });

            var query = this.$route.query;
            axios.get(`/api/admins/apps/${query.name}/${query.profile}`).then((resp) => {
                deepExtend(this.app, resp.data)
            });
        },
        methods: {
            commit() {
                axios.put(`/api/admins/apps`, this.app).then(() => {
                    this.$Notice.success({title: '修改成功'});
                    this.$router.push({path: `/app`,})
                }).catch((err) => {
                    var d = err.response.data || {};
                    this.$Notice.error({title: '修改失败', desc: d.message});
                });
            },
            generateToken() {
                this.app.token = _generateToken()
            },
            localNetIps() {
                this.app.ip_limit = "10.0.0.0-10.255.255.255,172.16.0.0-172.31.255.255,192.168.0.0-192.168.255.255"
            }
        }
    }
</script>