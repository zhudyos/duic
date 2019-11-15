<template>
  <q-dialog ref="dialog" @keyup.enter="onOk">
    <q-card class="full-width bg-grey-9">
      <q-card-section>
        <div class="text-h6">创建用户</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form ref="form">
          <q-input
            v-model="email"
            autofocus
            placeholder="邮箱"
            :rules="[v => !!v || '邮箱不能为空', checkEmail]"
          >
            <template v-slot:prepend>
              <q-icon name="mdi-email" />
            </template>
          </q-input>

          <q-input
            :type="isPwd ? 'password' : 'text'"
            v-model="password"
            placeholder="密码"
            :rules="[v => !!v || '密码不能为空']"
          >
            <template v-slot:prepend>
              <q-icon name="mdi-key" />
            </template>
            <template v-slot:append>
              <q-icon
                :name="isPwd ? 'mdi-eye-off' : 'mdi-eye'"
                class="cursor-pointer"
                @click="isPwd = !isPwd"
              />
            </template>
          </q-input>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right">
        <q-btn flat @click="onOk">保存</q-btn>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>
<script>
import axios from "axios";
import { validEmail } from "../../util/validator";

export default {
  name: "UserAdd",
  data: () => ({
    isPwd: true,
    email: null,
    password: null
  }),
  methods: {
    show() {
      this.$refs.dialog.show();
    },
    hide() {
      this.$refs.dialog.hide();
    },
    checkEmail(v) {
      return new Promise(resolve => {
        resolve(validEmail(v) || "错误的邮箱");
      });
    },
    onOk() {
      this.$refs.form.validate().then(valid => {
        if (!valid) {
          return;
        }
        this.save();
      });
    },
    save() {
      const u = { email: this.email, password: this.password };
      axios
        .post(`/api/admins/users`, u)
        .then(() => {
          this.$q.notify({
            color: "positive",
            message: "用户添加成功",
            position: "top"
          });

          this.$emit("ok");
          this.hide();
        })
        .catch(error => {
          const d = error.response.data || {};
          this.$q.notify({
            color: "negative",
            message: d.message || "用户添加失败",
            position: "top"
          });
        });
    }
  }
};
</script>
