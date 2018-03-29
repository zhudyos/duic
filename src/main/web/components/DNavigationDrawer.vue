<style lang="stylus" scoped>
    .logo
        background-image url("../images/duic200x60.png")
        background-repeat no-repeat
        background-position center
        height 64px
</style>
<template>
    <v-navigation-drawer
            v-model="value"
            absolute
            overflow
            app>
        <div class="logo mt-3">
        </div>
        <div class="title text-xs-center pa-3">配置中心</div>
        <v-divider></v-divider>
        <v-list>
            <v-list-tile to="/apps">
                <v-list-tile-action>
                    <v-icon>fas fa-cogs</v-icon>
                </v-list-tile-action>
                <v-list-tile-content>
                    <v-list-tile-title>配置管理</v-list-tile-title>
                </v-list-tile-content>
            </v-list-tile>
            <v-list-tile to="/users" v-if="isRoot">
                <v-list-tile-action>
                    <v-icon>fas fa-users</v-icon>
                </v-list-tile-action>
                <v-list-tile-content>
                    <v-list-tile-title>用户管理</v-list-tile-title>
                </v-list-tile-content>
            </v-list-tile>
        </v-list>
    </v-navigation-drawer>
</template>
<script>
    import axios from 'axios'

    export default {
        name: 'DNavigationDrawer',
        data: () => ({
            isRoot: false
        }),
        props: {
            value: Boolean
        },
        mounted() {
            axios.get(`/api/admins/user/root`).then(response => {
                this.isRoot = (response.data.root === this.$store.state.loginEmail)
            })
        }
    }
</script>