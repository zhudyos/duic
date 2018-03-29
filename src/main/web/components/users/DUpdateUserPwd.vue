<template>
    <v-card @keydown.enter="submit()">
        <v-card-title>修改密码</v-card-title>
        <v-card-text>
            <v-container grid-list-md>
                <v-form ref="form">
                    <v-layout row>
                        <v-text-field required
                                      label="原密码"
                                      v-model="oldPassword"
                                      :rules="rules.oldPassword"
                                      :append-icon="visiblePwd ? 'visibility' : 'visibility_off'"
                                      :append-icon-cb="() => (visiblePwd =! visiblePwd)"
                                      :type="visiblePwd ? 'text' : 'password'"></v-text-field>
                    </v-layout>
                    <v-layout row>
                        <v-text-field required
                                      label="新密码"
                                      v-model="newPassword"
                                      :rules="rules.newPassword"
                                      :append-icon="visiblePwd ? 'visibility' : 'visibility_off'"
                                      :append-icon-cb="() => (visiblePwd =! visiblePwd)"
                                      :type="visiblePwd ? 'text' : 'password'"></v-text-field>
                    </v-layout>
                    <v-layout row>
                        <v-text-field required
                                      label="确认密码"
                                      v-model="confirmPassword"
                                      :rules="rules.confirmPassword"
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
        name: 'DUpdateUserPwd',
        data() {
            var that = this
            return {
                oldPassword: '',
                newPassword: '',
                confirmPassword: '',
                visiblePwd: false,
                rules: {
                    oldPassword: [
                        v => !!v || '原密码不能为空'
                    ],
                    newPassword: [
                        v => !!v || '新密码不能为空'
                    ],
                    confirmPassword: [
                        v => !!v || '确认密码不能为空',
                        v => that.newPassword == v || '确认密码与新密码不相同'
                    ]
                }
            }
        },
        methods: {
            submit() {
                if (!this.$refs.form.validate()) return

                axios.put(`/api/admins/users/password`, {
                    old_password: this.oldPassword,
                    new_password: this.newPassword
                }).then(() => {
                    this.$notice('密码修改成功', {top: true, color: 'success'})
                    this.$emit('finish')
                }).catch((error) => {
                    var d = error.response.data || {};
                    this.$notice(d.message || '密码修改失败')
                })
            }
        }
    }
</script>