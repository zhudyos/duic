<!--

    Copyright 2017-2018 the original author or authors

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<style lang="stylus" scoped>

    .login
        width 100%
        height 100%
        background url("../images/login_bg.jpg") center
        background-size cover
        position relative

</style>
<template>
    <v-container fluid class="login" @keydown.enter="login">
        <v-layout row wrap justify-center>
            <v-flex xs12 sm8>
                <v-container fluid>
                    <v-layout row wrap class="mt-5 pt-5">
                        <v-spacer></v-spacer>
                        <v-flex xs12 sm4>
                            <v-card>
                                <v-card-title primary-title class="title">欢迎登录</v-card-title>
                                <v-card-text>
                                    <v-form ref="form">
                                        <v-text-field
                                                v-model="email"
                                                ref="email"
                                                type="email"
                                                placeholder="邮箱"
                                                prepend-icon="fas fa-envelope"
                                                autofocus
                                                :rules="rules.email"></v-text-field>
                                        <v-text-field
                                                v-model="password"
                                                ref="password"
                                                placeholder="密码"
                                                type="password"
                                                prepend-icon="fas fa-key"
                                                :rules="rules.password"></v-text-field>
                                    </v-form>
                                </v-card-text>
                                <v-card-actions>
                                    <v-btn color="info" block @click="login">登 录</v-btn>
                                </v-card-actions>
                                <v-card-actions>
                                    <v-btn icon large href="https://github.com/zhudyos/duic" target="_blank">
                                        <v-icon large>fab fa-github</v-icon>
                                    </v-btn>
                                    <v-btn icon large href="https://hub.docker.com/r/zhudyos/duic" target="_blank">
                                        <v-icon large>fab fa-docker</v-icon>
                                    </v-btn>
                                    <v-btn icon large href="mailto:kevinz@weghst.com" target="_blank">
                                        <v-icon large>fas fa-envelope</v-icon>
                                    </v-btn>
                                </v-card-actions>
                                <v-card-text class="body-1" v-if="isSample">
                                    <div>默认登录邮箱帐户密码</div>
                                    <div>kevinz@weghst.com</div>
                                    <div>123456</div>
                                    <div style="color: #E53935"><span>注意</span>：演示环境请不要修改默认帐户密码</div>
                                </v-card-text>
                            </v-card>
                        </v-flex>
                    </v-layout>
                </v-container>
            </v-flex>
        </v-layout>
    </v-container>
</template>
<script>
    import axios from 'axios'
    import Cookies from 'js-cookie'

    export default {
        data: () => ({
            email: '',
            password: '',
            rules: {
                email: [
                    v => !!v || "邮箱不能为空",
                    v => /^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$/.test(v) || "邮箱格式不正确"
                ],
                password: [
                    v => !!v || "密码不能为空"
                ]
            }
        }),
        computed: {
            isSample() {
                let href = location.href
                return href.indexOf('duic.zhudy.io') >= 0 || href.indexOf('localhost') >= 0
            }
        },
        methods: {
            login() {
                if (!this.$refs.form.validate()) {
                    return
                }

                axios.post('/api/admins/login', {email: this.email, password: this.password}).then((response) => {
                    Cookies.set('token', response.data.token)
                    Cookies.set('email', this.email)
                    this.$router.push("/")
                }).catch((error) => {
                    var d = error.response.data || {}
                    var text = '登录失败'
                    if (d.code === 2000) {
                        text = '用户不存在'
                        this.$refs.email.focus()
                    } else if (d.code === 2001) {
                        text = '密码不匹配'
                        this.password = ''
                        this.$refs.password.focus()
                    }
                    this.$notice(text)
                })
            }
        }
    }
</script>