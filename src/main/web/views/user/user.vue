<style lang="less">
    .toolbar {
        text-align: right;
        margin-bottom: 5px;
    }

    .page {
        text-align: right;
        margin-top: 5px;
    }
</style>
<template>
    <div>
        <div class="toolbar">
            <i-button type="primary" @click="createUser()">创建</i-button>
        </div>

        <i-table border :columns="userColumns" :data="userData">
        </i-table>

        <div class="page">
            <page ref="page" :total="total" show-sizer show-total
                  @on-change="loadUsers()"
                  @on-page-size-change="loadUsers()"></page>
        </div>
    </div>
</template>

<script>
    import axios from 'axios';

    export default {
        name: 'app',
        data() {
            return {
                total: 0,
                userData: [],
                userColumns: [
                    {title: 'E-Mail', key: 'email'},
                    {title: '创建时间', key: 'created_at'},
                    {title: '更新时间', key: 'updated_at'},
                    {
                        title: '操作',
                        width: 200,
                        align: 'center',
                        render: (h, params) => {
                            return h('div', [
                                h('i-button', {
                                    props: {
                                        type: 'warning',
                                        size: 'small'
                                    },
                                    style: {
                                        marginRight: '5px'
                                    },
                                    on: {
                                        click: () => {
                                            this.resetPassword(params.row.email);
                                        }
                                    }
                                }, '重置密码'),

                                h('i-button', {
                                    props: {
                                        type: 'error',
                                        size: 'small'
                                    },
                                    on: {
                                        click: () => {
                                            this.deleteUser(params.row.email);
                                        }
                                    }
                                }, '删除')
                            ])
                        }
                    }
                ],
                newPassword: ''
            };
        },
        mounted() {
            this.loadUsers()
        },
        methods: {
            loadUsers() {
                var p = this.$refs.page;
                axios.get(`/api/admin/users?page=${p.currentPage}&size=${p.currentPageSize}`).then(resp => {
                    this.userData = resp.data.items;
                    this.total = resp.data.total
                })
            },
            createUser() {
                this.$router.push({path: '/user-create'})
            },
            deleteUser(email) {
                this.$Modal.confirm({
                    title: '删除用户',
                    content: `确认删除用户 ${email} 吗?`,
                    onOk: () => {
                        axios.delete(`/api/admin/users/${email}`).then(resp => {
                            this.$Message.success('删除成功');
                            this.loadUsers();
                        })
                    }
                });
            },
            resetPassword(email) {
                this.$Modal.confirm({
                    title: `重置密码`,
                    render: (h) => {
                        return h('i-input', {
                            props: {
                                autofocus: true,
                                placeholder: `重置${email}用户密码`
                            },
                            on: {
                                input: (v) => {
                                    this.newPassword = v;
                                }
                            }
                        })
                    },
                    onOk: () => {
                        axios.patch(`/api/admin/users/password`, {
                            email: email,
                            password: this.newPassword
                        }).then(() => {
                            this.$Message.success('重置密码成功');
                        }).catch((err) => {
                            var d = err.response.data || {};
                            this.$Message.error(d.message || '重置密码失败');
                        })
                    }
                })
            }
        }
    };
</script>
