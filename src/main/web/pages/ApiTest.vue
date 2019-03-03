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
<style lang="stylus" scoped>
    .response-text
        border 1px solid #cacaca
        line-height 1.4em
        padding 10px
        overflow auto
        background-color #444
        margin 2em 0

    .c-code
        background transparent !important
        box-shadow none !important
        display block !important

        &:before
            content '' !important
</style>
<template>
    <v-card>
        <v-card-title>配置 API 测试</v-card-title>
        <v-card-text>
            <v-layout>
                <v-flex md4>
                    <v-container grid-list-md>
                        <v-form ref="form">
                            <v-layout row>
                                <v-select
                                        :items="operableNames"
                                        name="name"
                                        v-model="name"
                                        label="NAME"
                                        required
                                        autocomplete
                                        clearable
                                        @input="loadProfiles"
                                        :rules="rules.name"
                                ></v-select>
                            </v-layout>
                            <v-layout row>
                                <v-select
                                        :items="operableProfiles"
                                        name="profiles"
                                        v-model="profiles"
                                        label="PROFILES"
                                        required
                                        autocomplete
                                        multiple
                                        chips
                                        clearable
                                        :rules="rules.profile"
                                ></v-select>
                            </v-layout>
                            <v-layout row>
                                <v-text-field
                                        label="KEY"
                                        name="key"
                                        v-model.trim="key"
                                        persistent-hint
                                        hint="示例：user.default.avatar"
                                        clearable
                                ></v-text-field>
                            </v-layout>
                            <v-layout row>
                                <v-text-field
                                        label="TOKENS"
                                        name="tokens"
                                        v-model.trim="tokens"
                                        persistent-hint
                                        hint="示例：token1,token2,token3"
                                        multi-line
                                        clearable
                                        :rows="3"
                                ></v-text-field>
                            </v-layout>

                            <v-layout row>
                                <v-btn block color="primary" @click="execute"> 执 行 </v-btn>
                            </v-layout>
                        </v-form>
                    </v-container>
                </v-flex>
                <v-flex offset-xs1>
                    <div class="response-text" v-if="testRequestText">
                        <code class="c-code white--text">{{testRequestText}}</code>
                        <code class="c-code green--text lighten-1">{{testResponseText}}</code>
                    </div>
                </v-flex>
            </v-layout>
        </v-card-text>
    </v-card>
</template>
<script>
    import axios from 'axios'

    export default {
        data: () => ({
            operableNames: [],
            operableProfiles: [],
            name: '',
            profiles: [],
            key: '',
            tokens: '',
            rules: {
                name: [
                    v => !!v || "应用名称不能为空"
                ],
                profile: [
                    v => v.length > 0 || "应用配置不能为空"
                ]
            },
            testRequestText: '',
            testResponseText: ''
        }),
        mounted() {
            this.loadNames()
        },
        methods: {
            loadNames() {
                // 加载所有应用名称
                axios.get("/api/admins/tests/apps/names").then((response) => {
                    this.operableNames = response.data
                })
            },
            loadProfiles() {
                this.profiles = []
                this.key = ''

                axios.get(`/api/admins/tests/apps/${this.name}/profiles`).then((response) => {
                    this.operableProfiles = response.data
                })
            },
            execute() {
                if (!this.$refs.form.validate()) {
                    return
                }

                //
                let url = `/api/v1/apps/${this.name}`
                let p = ''
                this.profiles.forEach((v, i) => {
                    p += v
                    if (i < (this.profiles.length - 1)) {
                        p += ','
                    }
                })
                url += `/${p}`
                if (this.key) {
                    url += `/${this.key}`
                }

                let config = {apiTest: true}
                if (this.tokens) {
                    config.headers = {'x-config-token': this.tokens}
                }

                axios.get(url, config).then((response) => {
                    this.renderTestResult(response)
                }).catch((error) => {
                    this.renderTestResult(error.response)
                })
            },
            renderTestResult(response) {
                let text = 'curl'
                if (this.tokens) {
                    text += ` -H "x-config-token: ${this.tokens}"`
                }
                text += ` ${response.request.responseURL}`
                this.testRequestText = text

                text = `HTTP/1.1 ${response.status} ${response.statusText}\r\n`
                Object.keys(response.headers).forEach((k) => {
                    text += `\r\n${k}: ${response.headers[k]}`
                })
                text += `\r\n\r\n${JSON.stringify(response.data, null, 2)}`

                this.testResponseText = text
            }
        }
    }
</script>