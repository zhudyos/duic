<template>
  <v-app dark>
    <v-toolbar app fixed>
      <div class="logo"></div>
      <v-toolbar-title>配置中心</v-toolbar-title>

      <v-spacer></v-spacer>

      <v-toolbar-items class="hidden-sm-and-down">
        <v-btn flat>用户</v-btn>
        <v-btn flat>应用</v-btn>
        <v-btn flat>集群</v-btn>
      </v-toolbar-items>

      <v-toolbar-items class="hidden-sm-and-down">
        <v-menu offset-y>
          <v-btn flat slot="activator">kevinz@weghst.com</v-btn>
          <v-list>
            <v-list-tile href @click="updatePwdDialog = !updatePwdDialog">
              <v-list-tile-content>
                <v-list-tile-title>修改密码</v-list-tile-title>
              </v-list-tile-content>
            </v-list-tile>
          </v-list>
        </v-menu>
      </v-toolbar-items>
      <v-toolbar-items>
        <v-btn flat style="min-width: 36px" @click="logout">
          <v-icon>fas fa-sign-out-alt</v-icon>
        </v-btn>
      </v-toolbar-items>
    </v-toolbar>

    <v-content>
      <v-container fluid fill-height>
        <v-layout justify-center row>
          <router-view></router-view>
        </v-layout>
      </v-container>
    </v-content>

    <v-footer app fixed>
      <span>&copy; 2019</span>
    </v-footer>
  </v-app>
</template>

<script>
export default {
  name: "Home"
};
</script>

<style lang="stylus" scoped>
.logo {
  background-image: url('../assets/duic200x60.png');
  background-repeat: no-repeat;
  background-position: center;
  height: 60px;
  width: 200px;
}
</style>