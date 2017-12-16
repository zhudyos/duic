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
            <page ref="page" page-size="50" size="small" :total="total" show-total
                  @on-change="loadAppByUser()"
                  @on-page-size-change="loadAppByUser()"></page>
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
                    {title: '名称 (name)', key: 'name', sortable: true},
                    {title: '环境 (profile)', key: 'profile', sortable: true},
                    {title: '描述', key: 'description', width: 400},
                    {title: '创建时间', key: 'created_at', sortable: true},
                    {title: '更新时间', key: 'updated_at', sortable: true},
                    {
                        title: '操作',
                        width: 200,
                        align: 'center',
                        render: (h, params) => {
                            return h('div', [
                                h('i-button', {
                                    props: {
                                        type: 'primary',
                                        size: 'small'
                                    },
                                    style: {
                                        marginRight: '5px'
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
                                }, '配置'),
                                h('i-button', {
                                    props: {
                                        type: 'info',
                                        size: 'small'
                                    },
                                    style: {
                                        marginRight: '5px'
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
                                }, '编辑'),
                                h('i-button', {
                                    props: {
                                        type: 'error',
                                        size: 'small'
                                    },
                                    on: {
                                        click: () => {
                                            var r = params.row;
                                            this.$Modal.confirm({
                                                title: '删除应用',
                                                content: `删除应用 (<label style="color: red; font-weight: bold">${r.name}/${r.profile}</label>) 将清除配置及历史修改记录, 确认删除吗`,
                                                onOk: () => {
                                                    this.deleteApp(r.name, r.profile)
                                                }
                                            });
                                        }
                                    }
                                }, '删除')
                            ])
                        }
                    }
                ]
            };
        },
        mounted() {
            this.loadAppByUser()
        },
        methods: {
            loadAppByUser() {
                var p = this.$refs.page;
                axios.get(`/api/admins/apps/user?page=${p.currentPage}&size=${p.currentPageSize}`).then(resp => {
                    this.appData = resp.data.items;
                    this.total = resp.data.total
                })
            },
            createApp() {
                this.$router.push({path: '/app-create'})
            },
            deleteApp(name, profile) {
                axios.delete(`/api/admins/apps/${name}/${profile}`).then(() => {
                    this.$Notice.success({title: '删除成功'});
                    this.loadAppByUser()
                }).catch((error) => {
                    var d = error.response.data || {};
                    this.$Notice.error({title: '删除失败', desc: d.message});
                })
            }
        }
    };
</script>
