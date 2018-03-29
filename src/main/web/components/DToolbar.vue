<template>
    <v-toolbar absolute app>
        <v-toolbar-side-icon @click.stop="value = !value"></v-toolbar-side-icon>
        <v-spacer></v-spacer>
        <v-toolbar-items class="hidden-sm-and-down">
            <v-menu offset-y>
                <v-btn flat slot="activator">{{$store.state.loginEmail}}</v-btn>
                <v-list>
                    <v-list-tile href @click="updatePwdDialog = !updatePwdDialog">
                        <v-list-tile-content>
                            <v-list-tile-title>修改密码</v-list-tile-title>
                        </v-list-tile-content>
                    </v-list-tile>
                </v-list>
            </v-menu>
        </v-toolbar-items>
        <v-toolbar-items>
            <v-btn flat style="min-width: 36px" @click="logout">
                <v-icon>fas fa-sign-out-alt</v-icon>
            </v-btn>
        </v-toolbar-items>

        <v-dialog v-if="updatePwdDialog" v-model="updatePwdDialog" max-width="800px">
            <d-update-user-pwd @finish="updatePwdDialog = false"></d-update-user-pwd>
        </v-dialog>

    </v-toolbar>
</template>
<script>
    import Cookies from 'js-cookie'
    import DUpdateUserPwd from "./users/DUpdateUserPwd.vue"

    export default {
        components: {DUpdateUserPwd},
        name: 'DToolbar',
        data: () => ({
            updatePwdDialog: false
        }),
        props: {
            value: Boolean
        },
        watch: {
            value(v) {
                this.$emit('input', v)
            }
        },
        methods: {
            logout() {
                Cookies.remove('email')
                Cookies.remove('token')
                this.$router.push('/login')
            }
        }
    }
</script>