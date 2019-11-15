<template>
  <q-layout>
    <q-page-container>
      <router-view></router-view>
    </q-page-container>
  </q-layout>
</template>

<script>
export default {
  name: "Index",

  created() {
    // 设置为 dark 模式
    this.$q.dark.set(true);
  }
};
</script>
