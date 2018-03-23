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
                        <v-flex xs12 sm6>
                            项目描述
                        </v-flex>

                        <v-spacer></v-spacer>

                        <v-flex xs12 sm4>
                            <v-card>
                                <v-card-title primary-title class="title">欢迎登录</v-card-title>
                                <v-card-text>
                                    <v-form>
                                        <v-text-field
                                                v-model="email"
                                                ref="email"
                                                placeholder="邮箱"
                                                prepend-icon="fas fa-envelope"
                                                autofocus
                                                @blur="$v.email.$touch"
                                                :error-messages="emailErrors"></v-text-field>
                                        <v-text-field
                                                v-model="password"
                                                ref="password"
                                                placeholder="密码"
                                                type="password"
                                                prepend-icon="fas fa-key"
                                                @blur="$v.password.$touch"
                                                :error-messages="passwordErrors"></v-text-field>
                                    </v-form>
                                </v-card-text>
                                <v-card-actions>
                                    <v-btn color="info" block @click="login">登 录</v-btn>
                                </v-card-actions>
                                <v-card-text class="body-1">
                                    <div>默认登录邮箱帐户密码</div>
                                    <div>kevinz@weghst.com</div>
                                    <div>123456</div>
                                    <div style="color: #E53935"><span>注意</span>：演示环境请不要修改默认帐户密码</div>
                                </v-card-text>
                            </v-card>
                        </v-flex>

                        <!-- 错误提示 -->
                        <v-snackbar
                                top
                                :timeout="error.timeout"
                                color="error"
                                v-model="error.snackbar">
                            {{error.text}}
                        </v-snackbar>

                    </v-layout>
                </v-container>
            </v-flex>
        </v-layout>
    </v-container>
</template>
<script>
    import axios from 'axios'
    import Cookies from 'js-cookie'
    import {required, email} from 'vuelidate/lib/validators'

    export default {
        data: () => ({
            email: '',
            password: '',
            error: {
                snackbar: false,
                timeout: 3000,
                text: ''
            }
        }),
        validations: {
            email: {required, email},
            password: {required}
        },
        computed: {
            emailErrors() {
                const errors = []
                if (!this.$v.email.$dirty) return errors
                !this.$v.email.email && errors.push('邮箱错误')
                !this.$v.email.required && errors.push('请输入邮箱')
                return errors
            },
            passwordErrors() {
                const errors = []
                if (!this.$v.password.$dirty) return errors
                !this.$v.password.required && errors.push('请输入密码')
                return errors
            }
        },
        methods: {
            login() {
                this.$v.$touch()
                if (this.$v.$error) {
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
                    this.error = {snackbar: true, text: text}
                });
            }
        }
    }
</script>