<style lang="less">
    @import "../../styles/common";

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
                <button-group>
                    <i-button type="primary" @click="createApp()">创建</i-button>
                    <i-button type="primary" @click="cloneApp()">克隆</i-button>
                </button-group>

            </i-col>
        </row>

        <modal title="克隆应用" v-model="clone.modal" width="800">
            <p>Content of dialog</p>
            <p>Content of dialog</p>
            <p>Content of dialog</p>
        </modal>

        <row class="margin-bottom-10">
            <i-col span="12">
                <div @keyup.enter="search()">
                    <i-input v-model="q">
                        <i-button slot="append" @click="search()">
                            <i class="fa fa-search" aria-hidden="true"></i>
                        </i-button>
                    </i-input>
                </div>
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
                q: '',
                clone: {
                    modal: false
                },
                total: 0,
                pageSize: 50,
                appData: [],
                appColumns: [
                    {title: '名称 (name)', key: 'name', sortable: true},
                    {
                        title: '配置 (profile)',
                        key: 'profile',
                        sortable: true,
                        render: (h, params) => {
                            return h('a', {
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
                            }, params.row.profile)
                        }
                    },
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
                    {
                        title: 'IP 限制',
                        render: (h, params) => {
                            var ipLimit = params.row.ip_limit;
                            if (ipLimit) {
                                return h('tooltip', [
                                    h('div', '显示'),
                                    h('div', {
                                        slot: 'content',
                                        style: {
                                            whiteSpace: 'normal'
                                        }
                                    }, ipLimit)
                                ])
                            }
                        },
                        width: 80
                    },
                    {title: '创建时间', key: 'created_at', sortable: true},
                    {title: '更新时间', key: 'updated_at', sortable: true},
                    {
                        title: '操作',
                        width: 180,
                        align: 'center',
                        render: (h, params) => {
                            return h('div', [
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
            var q = this.$route.query.q;
            if (q) {
                this.q = q;
            }
            this.loadAppByUser()
        },
        methods: {
            loadAppByUser() {
                var p = this.$refs.page;
                axios.get(`/api/admins/search/apps?page=${p.currentPage}&size=${p.currentPageSize}&q=${this.q}`).then(resp => {
                    this.appData = resp.data.items;
                    this.total = resp.data.total
                })
            },
            search() {
                this.$router.push({path: '/app', query: {q: this.q}});
                this.loadAppByUser();
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
            },
            cloneApp() {
                this.clone.modal = true;
            }
        }
    };
</script>
