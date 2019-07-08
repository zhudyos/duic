<template>
  <q-page class="login row">
    <div class="offset-7 col-3" style="margin-top: 8%;">
      <q-card dark class="bg-grey-9">
        <q-card-section>
          <div class="text-h6">
            <q-avatar>
              <img src="../assets/duic60x60.png" />
            </q-avatar>
            <span>登录</span>
          </div>
        </q-card-section>

        <q-card-section>
          <q-form>
            <q-input
              dark
              v-model="email"
              placeholder="邮箱"
              :rules="[ v => !!v || '邮箱不能为空', checkEmail ]"
            >
              <template v-slot:prepend>
                <q-icon name="fas fa-envelope" />
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
                <q-icon name="fas fa-key" />
              </template>
            </q-input>
          </q-form>
        </q-card-section>

        <q-card-actions>
          <q-btn color="black" class="full-width" label="登 录" />
        </q-card-actions>

        <q-card-actions>
          <q-btn
            flat
            round
            icon="fab fa-github"
            type="a"
            href="https://github.com/zhudyos/duic"
            target="_blank"
          />
          <q-btn
            flat
            round
            icon="fab fa-docker"
            type="a"
            href="https://hub.docker.com/r/zhudyos/duic"
            target="_blank"
          />
          <q-btn
            flat
            round
            icon="fas fa-envelope"
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
import { Promise } from "q";
export default {
  name: "Login",
  data: () => ({
    email: null,
    password: null
  }),
  methods: {
    checkEmail(v) {
      return new Promise(resolve => {
        resolve(
          /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/.test(
            v
          ) || "错误的邮箱"
        );
      });
    }
  }
};
</script>
<style lang="stylus" scoped>
.login {
  background: url('../assets/login_bg.jpg') center;
  background-size: cover;
  position: relative;
}
</style>