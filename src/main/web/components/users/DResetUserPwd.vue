<template>
    <v-card @keydown.enter="submit()">
        <v-card-title>重置密码</v-card-title>
        <v-card-text>
            <v-container grid-list-md>
                <v-form ref="form">
                    <v-layout row>
                        <v-text-field required
                                      label="密码"
                                      v-model="password"
                                      :rules="rules.password"
                                      :append-icon="visiblePwd ? 'visibility' : 'visibility_off'"
                                      :append-icon-cb="() => (visiblePwd =! visiblePwd)"
                                      :type="visiblePwd ? 'text' : 'password'"></v-text-field>
                    </v-layout>
                </v-form>
            </v-container>
        </v-card-text>
        <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="blue darken-1" flat @click="submit"> 保 存 </v-btn>
        </v-card-actions>
    </v-card>
</template>
<script>
    import axios from 'axios'

    export default {
        name: 'DResetUserPwd',
        data: () => ({
            password: '',
            rules: {
                password: [
                    v => !!v || '密码不能为空'
                ]
            }
        }),
        props: ['email'],
        methods: {
            submit() {
                if (!this.$refs.form.validate()) {
                    return
                }

                axios.patch(`/api/admins/users/password`, {
                    email: this.email,
                    password: this.password
                }).then(() => {
                    this.$notice('重置密码成功', {top: true, color: 'success'})
                    this.$emit('finish')
                }).catch((error) => {
                    var d = error.response.data || {}
                    this.$notice(d.message || '重置密码失败')
                })
            }
        }
    }
</script>