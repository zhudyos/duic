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
            <i-button type="primary" @click="createApp()">创建</i-button>
        </div>

        <i-table border :columns="appColumns" :data="appData">
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
                appData: [],
                appColumns: [
                    {title: '名称 (Name)', key: 'name'},
                    {title: '环境 (Profile)', key: 'profile'},
                    {title: '创建时间', key: 'created_at'},
                    {title: '更新时间', key: 'updated_at'},
                    {
                        title: '操作',
                        width: 150,
                        align: 'center',
                        render: (h, params) => {
                            return h('div', [
                                h('i-button', {
                                    props: {
                                        type: 'primary',
                                        size: 'small'
                                    },
                                    on: {
                                        click: () => {
                                            var r = params.row;
                                            this.$router.push({
                                                path: `/app-content-edit`,
                                                query: {
                                                    name: r.name,
                                                    profile: r.profile
                                                }
                                            })
                                        }
                                    }
                                }, '编辑')
                            ])
                        }
                    }
                ]
            };
        },
        mounted() {
            this.loadUsers()
        },
        methods: {
            loadUsers() {
                var p = this.$refs.page;
                axios.get(`/api/admins/apps/user?page=${p.currentPage}&size=${p.currentPageSize}`).then(resp => {
                    this.appData = resp.data.items;
                    this.total = resp.data.total
                })
            },
            createApp() {
                this.$router.push({path: '/app-create'})
            }
        }
    };
</script>
