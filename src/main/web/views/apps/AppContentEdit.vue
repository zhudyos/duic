<template>
  <v-flex ref="editor" class="editor"></v-flex>
</template>

<script>
import * as monaco from "monaco-editor";

export default {
  name: "AppContentEdit",
  mounted() {
    monaco.editor.create(this.$refs.editor, {
      value: "a: bcd",
      language: "yaml",
      theme: "vs-dark"
    });
  }
};
</script>

<style type="stylus" scoped>
.editor {
  height: calc(100vh - 64px - 48px - 36px);
}
</style>

