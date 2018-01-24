<style type="less">
</style>
<template>
    <div>
        <breadcrumb class="ctn-breadcrumb-menu">
            <breadcrumb-item to="/">首页</breadcrumb-item>
            <breadcrumb-item to="/app">应用列表</breadcrumb-item>
            <breadcrumb-item>创建应用</breadcrumb-item>
        </breadcrumb>
        <card>
            <i-form :model="app" :rules="validation" label-position="right" :label-width="80">
                <form-item label="应用名称" prop="name">
                    <i-input v-model="app.name" placeholder="应用名称 name"></i-input>
                </form-item>
                <form-item label="应用环境" prop="profile">
                    <i-input v-model="app.profile" placeholder="应用环境 profile"></i-input>
                </form-item>
                <form-item label="应用描述" prop="description">
                    <i-input v-model="app.description" type="textarea" placeholder="应用描述 description"></i-input>
                </form-item>
                <form-item label="访问令牌" prop="token">
                    <i-input v-model="app.token" placeholder="访问令牌, 不填写该值则不进行令牌校验">
                        <i-button slot="append" @click="generateToken()">生成</i-button>
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
    import Cookies from 'js-cookie';

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
                    name: [
                        {required: true, message: '应用名称不能为空', trigger: 'blur'},
                        {pattern: /^[\w-]+$/, message: '名称只能由数字、字母、下划线、中划线组成', trigger: 'blur'}
                    ],
                    profile: [
                        {required: true, message: '应用环境不能为空', trigger: 'blur'},
                        {pattern: /^[\w-]+$/, message: '环境只能由数字、字母、下划线、中划线组成', trigger: 'blur'}
                    ],
                    description: [
                        {required: true, message: '应用描述不能为空', trigger: 'blur'}
                    ]
                },
                app: {
                    name: '',
                    profile: '',
                    description: '',
                    token: '',
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

            this.app.users.push(Cookies.get('email'));
        },
        methods: {
            commit() {
                axios.post(`/api/admins/apps`, this.app).then(() => {
                    this.$Notice.success({title: '应用添加成功'});
                    this.$router.push({
                        path: `/app-content-edit`,
                        query: {
                            name: this.app.name,
                            profile: this.app.profile
                        }
                    })
                }).catch((err) => {
                    var d = err.response.data || {};
                    if (d.code === 995) {
                        this.$Notice.error({title: '应用/环境已经存在不能重复添加'});
                        return
                    }
                    this.$Notice.error({title: '应用添加失败', desc: d.message});
                })
            },
            generateToken() {
                this.app.token = _generateToken()
            }
        }
    }
</script>