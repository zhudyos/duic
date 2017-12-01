<style lang="less">
</style>
<template>
    <div>
        <i-table border :columns="appColumns" :data="appData">
        </i-table>

        <row>
            <i-col offset="14">
                <page ref="page" :total="total" show-sizer show-total
                      @on-change="loadApps()"
                      @on-page-size-change="loadApps()"></page>
            </i-col>
        </row>
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
                                                path: `/apps/${r.name}/${r.profile}`
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
            this.loadApps()
        },
        methods: {
            loadApps() {
                var p = this.$refs.page
                axios.get(`/api/admin/apps?page=${p.currentPage}&size=${p.currentPageSize}`).then(resp => {
                    this.appData = resp.data.items;
                    this.total = resp.data.total
                }).catch(resp => {

                })
            }
        }
    };
</script>
