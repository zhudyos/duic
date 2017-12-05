<style type="less">
    .breadcrumb-menu {
        padding-top: 10px;
        padding-bottom: 10px;
    }
</style>
<template>
    <div>
        <breadcrumb class="breadcrumb-menu">
            <breadcrumb-item to="/apps">应用列表</breadcrumb-item>
            <breadcrumb-item>创建应用</breadcrumb-item>
        </breadcrumb>
        <card>
            <i-form :model="app" :rules="validation" label-position="right" :label-width="80">
                <form-item label="应用名称" prop="name">
                    <i-input v-model="app.name" placeholder="应用名称 Name"></i-input>
                </form-item>
                <form-item label="应用环境" prop="profile">
                    <i-input v-model="app.profile" placeholder="应用环境 Profile"></i-input>
                </form-item>
                <form-item label="所属用户">
                    <i-select v-model="app.users" multiple filterable>
                        <i-option v-for="item in users" :value="item" :key="item">{{item}}</i-option>
                    </i-select>
                </form-item>
                <form-item>
                    <i-button type="primary">提交</i-button>
                </form-item>
            </i-form>
        </card>
    </div>
</template>
<script>
    import axios from 'axios';

    export default {
        name: 'app-create',
        data() {
            return {
                validation: {
                    name: [
                        {required: true, message: '应用名称不能为空', trigger: 'blur'},
                        {pattern: /^[\w-]+$/, message: '名称只能由数字、字母、下划线、中划线组成', trigger: 'blur'}
                    ],
                    profile: [
                        {required: true, message: '应用环境不能为空', trigger: 'blur'},
                        {pattern: /^[\w-]+$/, message: '环境只能由数字、字母、下划线、中划线组成', trigger: 'blur'}
                    ]
                },
                app: {
                    name: '',
                    profile: '',
                    users: []
                },
                users: ["kevin", "kevin2"]
            }
        },
        methods: {
            commit() {
            }
        }
    }
</script>