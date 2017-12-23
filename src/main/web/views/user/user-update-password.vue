<style type="less">
</style>
<template>
    <div>
        <breadcrumb class="ctn-breadcrumb-menu">
            <breadcrumb-item to="/">首页</breadcrumb-item>
            <breadcrumb-item>修改密码</breadcrumb-item>
        </breadcrumb>
        <card>
            <i-form :model="user" :rules="validation" label-position="right" :label-width="80">
                <form-item label="当前密码" prop="oldPassword">
                    <i-input v-model="user.oldPassword" type="password" placeholder="当前密码"></i-input>
                </form-item>
                <form-item label="新密码" prop="newPassword">
                    <i-input v-model="user.newPassword" type="password" placeholder="新密码"></i-input>
                </form-item>
                <form-item label="确认密码" prop="confirmPassword">
                    <i-input v-model="user.confirmPassword" type="password" placeholder="确认密码"></i-input>
                </form-item>
                <form-item>
                    <i-button type="primary" @click="commit()">提交</i-button>
                </form-item>
            </i-form>
        </card>
    </div>
</template>
<script>
    import axios from 'axios';

    export default {
        name: 'user-update-password',
        data() {
            const validatePass = (rule, value, callback) => {
                if (this.user.newPassword !== value) {
                    callback(new Error('确认密码与新密码不相同'));
                } else {
                    callback();
                }
            };

            return {
                validation: {
                    oldPassword: [
                        {required: true, message: '密码不能为空', trigger: 'blur'}
                    ],
                    newPassword: [
                        {required: true, message: '新密码不能为空', trigger: 'blur'},
                    ],
                    confirmPassword: [
                        {required: true, message: '确认密码不能为空', trigger: 'blur'},
                        {validator: validatePass, trigger: 'blur'}
                    ],
                },
                user: {
                    oldPassword: '',
                    newPassword: '',
                    confirmPassword: ''
                }
            }
        },
        methods: {
            commit() {
                axios.put(`/api/admins/users/password`, {
                    old_password: this.user.oldPassword,
                    new_password: this.user.newPassword
                }).then(() => {
                    this.$Message.success('密码修改成功');
                }).catch((err) => {
                    var d = err.response.data || {};
                    this.$Message.error(d.message || '密码修改失败');
                })
            }
        }
    }
</script>