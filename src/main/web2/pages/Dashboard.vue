<style lang="stylus" scoped>
    main
        width 100%
        height 100%
</style>
<template>
    <v-layout fill-height wrap>
        <v-navigation-drawer
                v-model="primaryDrawer.model"
                :clipped="primaryDrawer.clipped"
                :floating="primaryDrawer.floating"
                :mini-variant="primaryDrawer.mini"
                absolute
                overflow
                app>
            <div class="logo mt-3">
                <!--<img src="../images/duic200x60.png"/>-->
            </div>
            <div class="title text-xs-center pa-3">配置中心</div>
            <v-divider></v-divider>
            <v-list>
                <v-list-tile to="/apps" prepend-icon="restaurant">
                    <v-list-tile-action>
                        <v-icon>fas fa-cogs</v-icon>
                    </v-list-tile-action>
                    <v-list-tile-content>
                        <v-list-tile-title>配置管理</v-list-tile-title>
                    </v-list-tile-content>
                </v-list-tile>
                <v-list-tile to="/users">
                    <v-list-tile-action>
                        <v-icon>fas fa-users</v-icon>
                    </v-list-tile-action>
                    <v-list-tile-content>
                        <v-list-tile-title>用户管理</v-list-tile-title>
                    </v-list-tile-content>
                </v-list-tile>
            </v-list>
        </v-navigation-drawer>
        <v-toolbar
                :clipped-left="primaryDrawer.clipped"
                absolute
                app>
            <v-toolbar-side-icon
                    @click.stop="primaryDrawer.model = !primaryDrawer.model"></v-toolbar-side-icon>
            <!--<v-toolbar-title>配置管理中心</v-toolbar-title>-->
            <v-spacer></v-spacer>
            <v-toolbar-items class="hidden-sm-and-down">
                <v-menu offset-y>
                    <v-btn flat slot="activator">Link One</v-btn>
                    <v-list>
                        <v-list-tile to="hello">
                            <v-list-tile-content>
                                <v-list-tile-title>修改密码</v-list-tile-title>
                            </v-list-tile-content>
                        </v-list-tile>
                    </v-list>
                </v-menu>
            </v-toolbar-items>
        </v-toolbar>

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

    export default {
        data: () => ({
            primaryDrawer: {
                model: null,
                clipped: false,
                floating: false,
                mini: false
            }
        }),
        computed: {
            ...mapState(['loginState']),
            noLoginDialog() {
                return !this.loginState
            }
        },
        created() {
            this.$store.commit('loginState', !!Cookies.get('token'))
        }
    }
</script>