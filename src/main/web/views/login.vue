<style lang="less">
    @import './login.less';
</style>

<template>
    <div class="login" @keydown.enter="handleSubmit">
        <div class="login-con">
            <card :bordered="false">
                <p slot="title">
                    <icon type="log-in"></icon>
                    欢迎登录
                </p>
                <div class="form-con">
                    <i-form ref="loginForm" :model="form" :rules="rules">
                        <form-item prop="email">
                            <i-input v-model="form.email" placeholder="email" autofocus>
                                <span slot="prepend">
                                    <icon :size="16" type="person"></icon>
                                </span>
                            </i-input>
                        </form-item>
                        <form-item prop="password">
                            <i-input type="password" v-model="form.password" placeholder="password">
                                <span slot="prepend">
                                    <icon :size="14" type="locked"></icon>
                                </span>
                            </i-input>
                        </form-item>
                        <form-item>
                            <i-button @click="handleSubmit" type="primary" long>登录</i-button>
                        </form-item>
                    </i-form>
                </div>
            </card>
        </div>
    </div>
</template>

<script>
    import axios from 'axios';
    import Cookies from 'js-cookie';

    export default {
        data() {
            return {
                form: {
                    email: '',
                    password: ''
                },
                rules: {
                    email: [
                        {required: true, type: 'email', message: '邮箱错误', trigger: 'blur'}
                    ],
                    password: [
                        {required: true, message: '密码不能为空', trigger: 'blur'}
                    ]
                }
            };
        },
        methods: {
            handleSubmit() {
                this.$refs.loginForm.validate((valid) => {
                    if (!valid) {
                        return
                    }

                    axios.post('/api/admins/login', this.form).then((resp) => {
                        Cookies.set('token', resp.data.token);
                        Cookies.set('email', this.form.email);
                        this.$router.push({path: '/'});
                    }).catch((err) => {
                        var d = err.response.data || {};
                        var msg = '登录失败';
                        if(d.code === 2000) {
                            msg = '用户不存在'
                        } else if (d.code === 2001) {
                            msg = '密码不匹配'
                        }
                        this.$Message.error(msg);
                    });
                });
            }
        }
    };
</script>

<style>

</style>
