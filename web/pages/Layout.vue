<template>
  <q-layout view="hHh lpR fFf" class>
    <q-header elevated class="bg-black">
      <q-toolbar>
        <q-toolbar-title>
          <q-avatar>
            <img src="../assets/images/duic60x60.png" />
          </q-avatar>配置中心
        </q-toolbar-title>
      </q-toolbar>
    </q-header>

    <q-page-container>
      <router-view class="q-pa-sm" />
    </q-page-container>

    <q-footer elevated class="bg-grey-8 text-white">
      <q-toolbar>
        <q-toolbar-title>
          <q-avatar>
            <img src="../assets/images/duic60x60.png" />
          </q-avatar>Title
        </q-toolbar-title>
      </q-toolbar>
    </q-footer>
  </q-layout>
</template>

<script lang="ts">
export default {
  data() {
    return {}
  },
}
</script>