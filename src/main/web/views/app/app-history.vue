<style>
</style>
<template>
    <div>
        <breadcrumb class="ctn-breadcrumb-menu">
            <breadcrumb-item to="/">首页</breadcrumb-item>
            <breadcrumb-item to="/app">应用列表</breadcrumb-item>
            <breadcrumb-item>修改历史(name: {{$route.query.name}}, profile: {{$route.query.profile}})</breadcrumb-item>
        </breadcrumb>
        <i-table border :columns="columns" :data="items">
        </i-table>
    </div>
</template>
<script>
    import axios from 'axios';

    export default {
        name: 'app-history',
        data() {
            return {
                items: [],
                columns: [{
                    title: 'HID',
                    key: 'hid'
                }, {
                    title: '修改人',
                    key: 'revised_by'
                }, {
                    title: '修改时间',
                    key: 'updated_at'
                }]
            }
        },
        mounted() {
            this.loadItems()
        },
        methods: {
            loadItems() {
                var query = this.$route.query;
                axios.get(`/api/admins/apps/${query.name}/${query.profile}/histories`).then((resp) => {
                    this.items = resp.data;
                });
            }
        }
    }

</script>