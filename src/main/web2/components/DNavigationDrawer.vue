<style lang="stylus" scoped>
    .logo
        background-image url("../images/duic200x60.png")
        background-repeat no-repeat
        background-position center
        height 64px
</style>
<template>
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
</template>
<script>
    export default {
        name: 'DNavigationDrawer'
    }
</script>