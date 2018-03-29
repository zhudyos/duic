<template>
    <v-card @keydown.enter="submit()">
        <v-card-title>创建应用</v-card-title>
        <v-card-text>
            <v-container grid-list-md>
                <v-form ref="form">
                    <v-layout row>
                        <v-flex>
                            <v-text-field required
                                          label="应用名称"
                                          v-model="app.name"
                                          :rules="rules.name"></v-text-field>
                        </v-flex>
                    </v-layout>
                    <v-layout row>
                        <v-flex>
                            <v-text-field required
                                          label="应用环境"
                                          v-model="app.profile"
                                          :rules="rules.profile"></v-text-field>
                        </v-flex>
                    </v-layout>
                    <v-layout row>
                        <v-flex>
                            <v-text-field required
                                          multi-line
                                          :rows="2"
                                          label="应用描述 description"
                                          v-model="app.description"
                                          :rules="rules.description"></v-text-field>
                        </v-flex>
                    </v-layout>
                    <v-layout row>
                        <v-flex>
                            <v-text-field label="访问令牌"
                                          clearable
                                          append-icon="fas fa-random"
                                          :append-icon-cb="generateToken"
                                          v-model="app.token"></v-text-field>
                        </v-flex>
                    </v-layout>
                    <v-layout row>
                        <v-flex>
                            <v-text-field label="IP 限制"
                                          hint="示例：127.0.0.1,192.168.1.1-192.168.1.255"
                                          persistent-hint
                                          clearable
                                          append-icon="fas fa-lock"
                                          :append-icon-cb="localNetIps"
                                          v-model="app.ip_limit"></v-text-field>
                        </v-flex>
                    </v-layout>
                    <v-layout row>
                        <v-flex>
                            <v-select required
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
    import Cookies from 'js-cookie'

    function _generateToken() {
        return ([1e7] + 1e3 + 4e3 + 8e3 + 1e11).replace(/[018]/g, c =>
            (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
        )
    }

    export default {
        name: 'DCreationApp',
        data: () => ({
            app: {
                name: '',
                profile: '',
                description: '',
                token: '',
                ip_limit: '',
                users: []
            },
            users: [],
            rules: {
                name: [
                    v => !!v || "应用名称不能为空"
                ],
                profile: [
                    v => !!v || "应用配置不能为空"
                ],
                description: [
                    v => !!v || "应用描述不能为空"
                ],
                users: [
                    v => v.length > 0 || "所属用户不能为空"
                ]
            }
        }),
        mounted() {
            axios.get(`/api/admins/users/emails`).then((response) => {
                this.users = response.data
            })

            this.app.users.push(Cookies.get('email'))
        },
        methods: {
            submit() {
                if (!this.$refs.form.validate()) {
                    return
                }

                axios.post(`/api/admins/apps`, this.app).then(() => {
                    this.$notice('应用添加成功', {top: true, color: 'success'})
                    this.$emit('created')
                }).catch(error => {
                    var d = error.response.data || {}
                    if (d.code === 995) {
                        this.$notice('应用/环境已经存在不能重复添加')
                        return
                    }
                    this.$notice('应用添加失败：' + d.message)
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