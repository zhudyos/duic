<style lang="less">
    .toolbar {
        text-align: right;
    }
</style>
<template>
    <div>
        <row>
            <i-col span="12">
                <breadcrumb class="ctn-breadcrumb-menu">
                    <breadcrumb-item to="/">首页</breadcrumb-item>
                    <breadcrumb-item>应用列表</breadcrumb-item>
                </breadcrumb>
            </i-col>
            <i-col span="12" class="toolbar">
                <i-button type="primary" @click="createApp()">创建</i-button>
            </i-col>
        </row>

        <i-table border :columns="appColumns" :data="appData">
        </i-table>

        <div class="page">
            <page ref="page" :page-size="pageSize" size="small" :total="total" show-total
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
                pageSize: 50,
                appData: [],
                appColumns: [
                    {title: '名称 (name)', key: 'name', sortable: true},
                    {title: '环境 (profile)', key: 'profile', sortable: true},
                    {title: '描述', key: 'description', width: 400},
                    {
                        title: '令牌',
                        render: (h, params) => {
                            var token = params.row.token;
                            if (token) {
                                return h('a', {
                                    on: {
                                        click: () => {
                                            this.$copyText(token).then(() => {
                                                this.$Message.success('复制成功');
                                            }, (e) => {
                                                this.$Message.error('复制失败');
                                                console.log(e);
                                            });
                                        }
                                    }
                                }, '复制')
                            }
                        },
                        width: 64
                    },
                    {title: '创建时间', key: 'created_at', sortable: true},
                    {title: '更新时间', key: 'updated_at', sortable: true},
                    {
                        title: '操作',
                        width: 220,
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
                                                path: `/app-edit`,
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
                                    style: {
                                        marginRight: '5px'
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
                                }, '删除'),
                                h('i-button', {
                                    props: {
                                        size: 'small'
                                    },
                                    on: {
                                        click: () => {
                                            var r = params.row;
                                            this.$router.push({
                                                path: '/app-history',
                                                query: {
                                                    name: r.name,
                                                    profile: r.profile
                                                }
                                            });
                                        }
                                    }
                                }, '历史')
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
