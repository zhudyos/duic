<template>
    <v-card @keydown.enter="submit()">
        <v-card-title>创建用户</v-card-title>
        <v-card-text>
            <v-container grid-list-md>
                <v-form ref="form">
                    <v-layout row>
                        <v-text-field required
                                      label="邮箱"
                                      v-model="user.email"
                                      :rules="rules.email"></v-text-field>
                    </v-layout>
                    <v-layout row>
                        <v-text-field required
                                      label="密码"
                                      v-model="user.password"
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
        name: 'DCreationUser',
        data: () => ({
            visiblePwd: false,
            user: {
                email: '',
                password: '',
            },
            rules: {
                email: [
                    v => !!v || '邮箱不能为空',
                    v => /^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$/.test(v) || "邮箱格式不正确"
                ],
                password: [
                    v => !!v || '密码不能为空'
                ]
            }
        }),
        methods: {
            submit() {
                if (!this.$refs.form.validate()) {
                    return
                }

                axios.post(`/api/admins/users`, this.user).then(() => {
                    this.$notice('用户添加成功', {top: true, color: 'success'})
                    this.$emit('finish')
                }).catch((error) => {
                    console.error(error.response)

                    var d = error.response.data || {}
                    this.$notice(d.message || '用户添加失败')
                })
            }
        }
    }
</script>