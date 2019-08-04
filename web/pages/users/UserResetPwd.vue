<template>
  <q-dialog ref="dialog" style="sm" @keyup.enter="onOk">
    <q-card class="full-width">
      <q-card-section>
        <div class="text-h6">重置密码</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form ref="form">
          <q-input placeholder="邮箱" readonly :value="email">
            <template v-slot:prepend>
              <q-icon name="mdi-email" />
            </template>
          </q-input>

          <q-input
            :type="isPwd ? 'password' : 'text'"
            autofocus
            v-model="password"
            placeholder="密码"
            :rules="[ v=> !!v || '密码不能为空' ]"
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
        <q-btn flat @click="onOk">重 置</q-btn>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>
<script>
import axios from "axios";
export default {
  name: "UserRestPwd",
  data: () => ({
    isPwd: true,
    password: null
  }),
  props: ["email"],
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
    save() {
      const u = { email: this.email, password: this.password };
      axios
        .patch(`/api/admins/users/password`, u)
        .then(() => {
          this.$q.notify({
            color: "positive",
            message: "重置密码成功",
            position: "top"
          });

          this.$emit("ok");
          this.hide();
        })
        .catch(error => {
          const d = error.response.data || {};
          this.$q.notify({
            color: "negative",
            message: d.message || "重置密码失败",
            position: "top"
          });
        });
    }
  }
};
</script>

            
        