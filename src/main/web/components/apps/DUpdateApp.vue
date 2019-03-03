<!--

    Copyright 2017-2019 the original author or authors

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
        <v-card-title>修改应用</v-card-title>
        <v-card-text>
            <v-container grid-list-md>
                <v-form ref="form">
                    <v-layout row>
                        <v-flex>
                            <v-text-field required
                                          name="name"
                                          label="应用名称"
                                          v-model="app.name"
                                          disabled></v-text-field>
                        </v-flex>
                    </v-layout>
                    <v-layout row>
                        <v-flex>
                            <v-text-field required
                                          name="profile"
                                          label="应用环境"
                                          v-model="app.profile"
                                          disabled></v-text-field>
                        </v-flex>
                    </v-layout>
                    <v-layout row>
                        <v-flex>
                            <v-text-field required
                                          name="description"
                                          multi-line
                                          :rows="2"
                                          placeholder="应用描述"
                                          v-model.trim="app.description"
                                          :rules="rules.description"></v-text-field>
                        </v-flex>
                    </v-layout>
                    <v-layout row>
                        <v-flex>
                            <v-text-field placeholder="访问令牌"
                                          name="token"
                                          clearable
                                          append-icon="fas fa-random"
                                          :append-icon-cb="generateToken"
                                          v-model.trim="app.token"></v-text-field>
                        </v-flex>
                    </v-layout>
                    <v-layout row>
                        <v-flex>
                            <v-text-field label="IP 限制"
                                          name="ip_limit"
                                          hint="示例：127.0.0.1,192.168.1.1-192.168.1.255"
                                          persistent-hint
                                          clearable
                                          append-icon="fas fa-lock"
                                          :append-icon-cb="localNetIps"
                                          v-model.trim="app.ip_limit"></v-text-field>
                        </v-flex>
                    </v-layout>
                    <v-layout row>
                        <v-flex>
                            <v-select required
                                      name="users"
                                      autocomplete
                                      tags
                                      multiple
                                      clearable
                                      label="所属用户"
                                      v-model="app.users"
                                      :items="users"
                                      :rules="rules.users">
                            </v-select>
                        </v-flex>
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
    import deepExtend from 'deep-extend'

    function _generateToken() {
        return ([1e7] + 1e3 + 4e3 + 8e3 + 1e11).replace(/[018]/g, c =>
            (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
        )
    }

    export default {
        name: 'DUpdateApp',
        data: () => ({
            app: {
                name: '',
                profile: '',
                description: '',
                token: '',
                ip_limit: '',
                users: []
            },
            rules: {
                description: [
                    v => !!v || "应用描述不能为空"
                ],
                users: [
                    v => v.length > 0 || "所属用户不能为空"
                ]
            },
            users: []
        }),
        props: ['name', 'profile'],
        mounted() {
            axios.get(`/api/admins/users/emails`).then((resp) => {
                this.users = resp.data
            })

            axios.get(`/api/admins/apps/${this.name}/${this.profile}`).then((resp) => {
                deepExtend(this.app, resp.data)
            })
        },
        methods: {
            submit() {
                if (!this.app.token) {
                    this.app.token = ''
                }
                if (!this.app.ip_limit) {
                    this.app.ip_limit = ''
                }

                axios.put(`/api/admins/apps`, this.app).then(() => {
                    this.$notice('修改成功', {top: true, color: 'success'})
                    this.$emit('updated')
                }).catch((error) => {
                    console.error(error.response)
                    this.$notice('修改失败')
                })
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