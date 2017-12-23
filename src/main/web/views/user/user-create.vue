<style type="less">
</style>
<template>
    <div>
        <breadcrumb class="ctn-breadcrumb-menu">
            <breadcrumb-item to="/">首页</breadcrumb-item>
            <breadcrumb-item to="/user">用户列表</breadcrumb-item>
            <breadcrumb-item>创建用户</breadcrumb-item>
        </breadcrumb>
        <card>
            <i-form :model="user" :rules="validation" label-position="right" :label-width="80">
                <form-item label="E-Mail" prop="email">
                    <i-input v-model="user.email" placeholder="邮箱"></i-input>
                </form-item>
                <form-item label="密码" prop="password">
                    <i-input v-model="user.password" type="password" placeholder="密码"></i-input>
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

    export default {
        name: 'user-create',
        data() {
            return {
                validation: {
                    email: [
                        {required: true, type: 'email', message: '邮箱不正确', trigger: 'blur'}
                    ],
                    password: [
                        {required: true, message: '密码不能为空', trigger: 'blur'},
                    ]
                },
                user: {
                    email: '',
                    password: '',
                }
            }
        },
        methods: {
            commit() {
                axios.post(`/api/admins/users`, this.user).then(() => {
                    this.$Message.success('用户添加成功');
                }).catch((err) => {
                    var d = err.response.data || {};
                    this.$Message.error(d.message || '用户添加失败');
                })
            }
        }
    }
</script>