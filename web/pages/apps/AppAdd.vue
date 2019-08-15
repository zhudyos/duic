<template>
  <q-dialog ref="dialog" @keyup.enter="onOk">
    <q-card dark class="full-width bg-grey-9" style="min-width: 700px;">
      <q-card-section>
        <div class="text-h6">创建应用</div>
      </q-card-section>

      <q-card-section>
        <q-form ref="form">
          <q-input
            dark
            v-model="app.name"
            label="应用名称"
            :rules="[ v => !!v || '名称不能为空', checkName]"
            hint="格式：^[a-zA-Z0-9\-_]+$"
          ></q-input>
          <q-input
            dark
            v-model="app.profile"
            label="应用环境"
            :rules="[ v=> !!v || '环境不能为空', checkName]"
            hint="格式：^[a-zA-Z0-9\-_]+$"
          ></q-input>
          <q-input
            dark
            type="textarea"
            v-model="description"
            label="应用描述"
            rows="3"
            :rules="[ v=> !!v || '描述不能为空' ]"
          ></q-input>
          <q-input dark v-model="app.token" label="访问令牌">
            <template v-slot:append>
              <q-icon name="mdi-autorenew" class="cursor-pointer" @click="genToken" />
            </template>
          </q-input>
          <q-input
            dark
            v-model="app.ip_limit"
            label="IP 限制"
            hint="示例：192.168.1.36,192.168.1.1-192.168.1.244"
          ></q-input>
          <q-select
            dark
            options-dark
            multiple
            use-input
            use-chips
            v-model="app.users"
            :options="filteredUsers"
            input-debounce="0"
            @filter="filterUser"
          />
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

export default {
  name: "AppAdd",
  data: () => ({
    app: {
      name: "",
      profile: "",
      description: "",
      token: "",
      ip_limit: "",
      users: []
    },
    users: [],
    filteredUsers: []
  }),
  mounted() {
    axios.get(`/api/admins/users/emails`).then(response => {
      this.users = response.data;
      this.filteredUsers = response.data;
    });

    const email = this.$q.cookies.get("email");
    this.app.users.push(email);
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
        this.save();
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
    filterUser(val, update) {
      if (val === "") {
        update(() => {
          this.filteredUsers = this.users;
        });
        return;
      }

      update(() => {
        const needle = val.toLowerCase();
        this.filteredUsers = this.users.filter(
          v => v.toLowerCase().indexOf(needle) > -1
        );
      });
    },
    save() {}
  }
};
</script>

