<template>
  <q-layout view="hHh lpR fFf">
    <q-header elevated class="bg-black">
      <q-toolbar>
        <q-toolbar-title>DUIC 配置中心</q-toolbar-title>

        <q-space />

        <q-btn stretch flat icon="mdi-content-save" :disable="submitBtnDisabled" label="保 存" />
      </q-toolbar>
    </q-header>

    <q-page-container>
      <q-page>
        <div ref="codeEditor" style="min-height: calc(100vh - 50px)"></div>
      </q-page>
    </q-page-container>
  </q-layout>
</template>
<script>
import "monaco-editor/esm/vs/editor/browser/controller/coreCommands.js";
import "monaco-editor/esm/vs/editor/browser/widget/diffEditorWidget.js";
import "monaco-editor/esm/vs/editor/contrib/find/findController.js";
import * as monaco from "monaco-editor/esm/vs/editor/editor.api.js";
import "monaco-editor/esm/vs/basic-languages/yaml/yaml.contribution.js";

export default {
  name: "ConfigEdit",
  data: () => ({
    submitBtnDisabled: true,
    editor: null,
    originalContent: null
  }),
  mounted() {
    this.initEditor();
  },
  destroyed() {
    this.editor.dispose();
  },
  methods: {
    initEditor() {
      this.originalContent = "a: b";
      const editor = monaco.editor.create(this.$refs.codeEditor, {
        value: this.originalContent,
        theme: "vs-dark",
        language: "yaml"
      });

      editor.getModel().updateOptions({
        tabSize: 2,
        indentSize: 2,
        insertSpaces: true,
        trimAutoWhitespace: true
      });

      editor.onDidChangeModelContent(this.changeContent);
      editor.focus();

      this.editor = editor;
    },
    changeContent(v) {
      this.submitBtnDisabled = this.originalContent === this.editor.getValue();
    }
  }
};
</script>
