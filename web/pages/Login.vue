<template>
  <q-page class="login row">
    <div class="offset-7 col-3" style="margin-top: 8%;">
      <q-card dark class="bg-grey-9">
        <q-card-section>
          <div class="text-h6">
            <q-avatar>
              <img src="../assets/images/duic60x60.png" />
            </q-avatar>
            <span>登录</span>
          </div>
        </q-card-section>

        <q-card-section @keyup.enter="login">
          <q-form ref="loginForm">
            <q-input
              dark
              v-model="email"
              placeholder="邮箱"
              :rules="[ v => !!v || '邮箱不能为空', checkEmail ]"
            >
              <template v-slot:prepend>
                <q-icon name="mdi-email" />
              </template>
            </q-input>

            <q-input
              dark
              type="password"
              v-model="password"
              placeholder="密码"
              :rules="[ v=> !!v || '密码不能为空' ]"
            >
              <template v-slot:prepend>
                <q-icon name="mdi-key" />
              </template>
            </q-input>
          </q-form>
        </q-card-section>

        <q-card-actions>
          <q-btn
            :loading="loginBtnLoading"
            color="black"
            class="full-width"
            label="登 录"
            @click="login"
          />
        </q-card-actions>

        <q-card-actions>
          <q-btn
            flat
            round
            icon="mdi-github-box"
            type="a"
            href="https://github.com/zhudyos/duic"
            target="_blank"
          />
          <q-btn
            flat
            round
            icon="mdi-docker"
            type="a"
            href="https://hub.docker.com/r/zhudyos/duic"
            target="_blank"
          />
          <q-btn
            flat
            round
            icon="mdi-email"
            type="a"
            href="mailto:kevinz@weghst.com"
            target="_blank"
          />
        </q-card-actions>

        <q-card-section>
          <div>默认登录邮箱帐户密码</div>
          <div>kevinz@weghst.com</div>
          <div>123456</div>
          <div class="text-red">
            <span>注意</span>：演示环境请不要修改默认帐户密码
          </div>
        </q-card-section>
      </q-card>
    </div>
  </q-page>
</template>
<script>
import axios from "axios";
import { validEmail } from "../util/validator";
export default {
  name: "Login",
  data: () => ({
    email: null,
    password: null,
    loginBtnLoading: false
  }),
  methods: {
    checkEmail(v) {
      return new Promise(resolve => {
        resolve(validEmail(v) || "错误的邮箱");
      });
    },
    login() {
      this.$refs.loginForm.validate().then(valid => {
        if (!valid) {
          return;
        }
        this.loginBtnLoading = true;

        axios
          .post("/api/admins/login", {
            email: this.email,
            password: this.password
          })
          .then(() => {
            this.loginBtnLoading = false;
            this.$router.push("/");
          })
          .catch(error => {
            this.loginBtnLoading = false;

            const d = error.response.data || {};
            let text = "登录失败";
            if (d.code === 2000) {
              text = "用户不存在";
            } else if (d.code === 2001) {
              text = "密码错误";
            }

            this.$q.notify({
              color: "negative",
              message: text,
              position: "top"
            });
          });
      });
    }
  }
};
</script>
<style lang="stylus" scoped>
.login {
  background: url('../assets/images/login_bg.jpg') center;
  background-size: cover;
  position: relative;
}
</style>