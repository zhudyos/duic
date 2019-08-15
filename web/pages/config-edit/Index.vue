<template>
  <q-layout>
    <q-header elevated class="bg-grey-9">
      <q-toolbar>
        <d-banner />

        <q-space />
        <q-btn
          stretch
          flat
          icon="mdi-content-save"
          size="lg"
          :disable="submitBtnDisabled"
          @click="save"
        />
      </q-toolbar>
    </q-header>
    <q-page-container>
      <q-page>
        <div ref="codeEditor" style="min-height: calc(100vh - 54px)"></div>
      </q-page>
    </q-page-container>
  </q-layout>
</template>
<script>
import DBanner from "../../components/DBanner.vue";
import axios from "axios";
import "monaco-editor/esm/vs/editor/browser/controller/coreCommands.js";
import "monaco-editor/esm/vs/editor/browser/widget/diffEditorWidget.js";
import "monaco-editor/esm/vs/editor/contrib/find/findController.js";
import * as monaco from "monaco-editor/esm/vs/editor/editor.api.js";
import "monaco-editor/esm/vs/basic-languages/yaml/yaml.contribution.js";

export default {
  components: {
    DBanner
  },
  data: () => ({
    submitBtnDisabled: true,
    editor: null,
    app: {}
  }),
  mounted() {
    const { name, profile } = this.$route.query;
    document.title = `${profile}/${name} 配置编辑 - DUIC`;

    // 拉取数据
    axios.get(`/api/admins/apps/${name}/${profile}`).then(response => {
      this.app = response.data;
      this.initEditor();
    });
  },
  destroyed() {
    this.editor.dispose();
  },
  methods: {
    initEditor() {
      const editor = monaco.editor.create(this.$refs.codeEditor, {
        value: this.app.content,
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
    changeContent() {
      this.submitBtnDisabled = this.app.content === this.editor.getValue();
    },
    save() {
      const content = this.editor.getValue();

      axios
        .put(`/api/admins/apps/contents`, {
          name: this.app.name,
          profile: this.app.profile,
          v: this.app.v,
          content: content
        })
        .then(response => {
          this.app.content = content;
          this.app.v = response.data.v;
          this.submitBtnDisabled = true;

          this.$q.notify({
            color: "positive",
            message: "配置修改成功",
            position: "top"
          });
        })
        .catch(error => {
          let d = error.response.data || {};
          let text = d.message || "配置修改失败";

          this.$q.notify({
            color: "negative",
            message: text,
            position: "top"
          });
        });
    }
  }
};
</script>
