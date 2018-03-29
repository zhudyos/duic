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
        <v-dialog v-model="noLoginDialog" max-width="290">
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
            let state = !!Cookies.get('token')
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