<template>
  <q-dialog ref="dialog" @keyup.enter="onOk">
    <q-card class="full-width" style="min-width: 700px;">
      <q-card-section>
        <div class="text-h6">编辑应用</div>
      </q-card-section>

      <q-card-section>
        <q-form ref="form">
          <q-input
            v-model="app.name"
            label="应用名称"
            :rules="[v => !!v || '名称不能为空', checkName]"
            hint="格式：^[a-zA-Z0-9\-_]+$"
            readonly
          ></q-input>
          <q-input
            v-model="app.profile"
            label="应用环境"
            :rules="[v => !!v || '环境不能为空', checkName]"
            hint="格式：^[a-zA-Z0-9\-_]+$"
            readonly
          ></q-input>
          <q-input
            type="textarea"
            v-model="app.description"
            label="应用描述"
            rows="3"
            :rules="[v => !!v || '描述不能为空']"
          ></q-input>
          <q-input v-model="app.token" label="访问令牌">
            <template v-slot:append>
              <q-icon
                name="mdi-autorenew"
                class="cursor-pointer"
                @click="genToken"
              />
            </template>
          </q-input>
          <q-input
            v-model="app.ip_limit"
            label="IP 限制"
            hint="示例：192.168.1.36,192.168.1.1-192.168.1.244"
          ></q-input>
          <d-user-email-select v-model="app.users" />
        </q-form>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat @click="onOk">保存</q-btn>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>
<script>
import axios from "axios";
import { generateToken, nameValidate } from "./app.js";
import DUserEmailSelect from "../../components/DUserEmailSelect.vue";

export default {
  name: "AppAdd",
  props: ["name", "profile"],
  components: [DUserEmailSelect],
  data: () => ({
    app: {
      name: "",
      profile: "",
      description: "",
      token: "",
      ip_limit: "",
      users: []
    }
  }),
  mounted() {
    axios
      .get(`/api/admins/apps/${this.name}/${this.profile}`)
      .then(response => {
        Object.assign(this.app, response.data);
      });
  },
  methods: {
    show() {
      this.$refs.dialog.show();
    },
    hide() {
      this.$refs.dialog.hide();
    },
    onOk() {
      this.$refs.form.validate().then(valid => {
        if (!valid) {
          return;
        }
        this.submit();
      });
    },
    genToken() {
      this.app.token = generateToken();
    },
    checkName(v) {
      return new Promise(resolve => {
        resolve(nameValidate(v) || "格式错误");
      });
    },
    submit() {
      const app = this.app;
      const newApp = {
        description: app.description,
        token: app.token,
        ip_limit: app.ip_limit,
        v: app.v,
        users: app.users
      };

      axios
        .put(`/api/admins/apps/${app.name}/${app.profile}`, newApp)
        .then(() => {
          this.$q.notify({
            color: "positive",
            message: "配置修改成功",
            position: "top"
          });

          this.$emit("ok");
          this.hide();
        })
        .catch(error => {
          const d = error.response.data || {};
          this.$q.notify({
            color: "negative",
            message: `配置修改失败：${d.message}`,
            position: "top"
          });
        });
    }
  }
};
</script>
