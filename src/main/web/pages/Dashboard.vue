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
    main
        width 100%
        height 100%
</style>
<template>
    <v-layout fill-height wrap>
        <d-navigation-drawer v-model="primaryDrawer.model"></d-navigation-drawer>
        <d-toolbar v-model="primaryDrawer.model"></d-toolbar>

        <v-content>
            <v-container fluid>
                <router-view></router-view>
            </v-container>
        </v-content>

        <!-- 未认证模式框 -->
        <v-dialog v-model="noLoginDialog" max-width="290" @input="$store.commit('loginState', true)">
            <v-card>
                <v-card-title class="headline">登录过期/未登录</v-card-title>
                <v-card-text>跳转至登录页面进行重新登录，会丢失当前页面操作</v-card-text>
                <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn color="error" flat @click="$router.push('/login')">跳转</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
    </v-layout>
</template>
<script>
    import Cookies from 'js-cookie'
    import {mapState} from 'vuex'
    import DNavigationDrawer from "../components/DNavigationDrawer.vue"
    import DToolbar from "../components/DToolbar.vue"

    export default {
        components: {
            DToolbar,
            DNavigationDrawer
        },
        data: () => ({
            primaryDrawer: {
                model: true
            }
        }),
        computed: {
            ...mapState(['loginState']),
            noLoginDialog() {
                return !this.loginState
            }
        },
        created() {
            let state = !!Cookies.get('email')
            if (state) {
                let email = Cookies.get('email')
                this.$store.commit('loginState', state)
                this.$store.commit('loginEmail', email)
            } else {
                this.$router.push('/login')
            }
        }
    }
</script>