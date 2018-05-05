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
<template>
    <v-card @keydown.enter="submit()">
        <v-card-title>创建用户</v-card-title>
        <v-card-text>
            <v-container grid-list-md>
                <v-form ref="form">
                    <v-layout row>
                        <v-text-field required
                                      name="email"
                                      label="邮箱"
                                      v-model="user.email"
                                      :rules="rules.email"></v-text-field>
                    </v-layout>
                    <v-layout row>
                        <v-text-field required
                                      name="password"
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