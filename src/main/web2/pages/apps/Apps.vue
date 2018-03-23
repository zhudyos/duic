<template>
    <v-card>
        <v-card-actions>
            <v-btn flat @click.stop="creationDialog = !creationDialog">创建</v-btn>
            <v-btn flat>克隆</v-btn>
            <v-spacer></v-spacer>
            <v-flex md3>
                <v-text-field
                        append-icon="search"
                        label="搜索"
                        single-line
                        hide-details
                        v-model="q"></v-text-field>
            </v-flex>
        </v-card-actions>

        <v-data-table
                :headers="headers"
                :items="items"
                no-data-text="没有数据"
                hide-actions>
            <template slot="items" slot-scope="props">
                <td>{{props.item.name}}</td>
                <td>
                    <a>{{props.item.profile}}</a>
                </td>
                <td>{{props.item.description}}</td>
                <td>
                    <a v-if="props.item.token" v-clipboard:copy="props.item.token">复制</a>
                </td>
                <td>{{props.item.ip_limit}}</td>
                <td>{{props.item.created_at}}</td>
                <td>{{props.item.updated_at}}</td>
            </template>
        </v-data-table>
        <div class="text-xs-right pt-2">
            <v-pagination v-model="pagination.page" :length="pagination.totalPages" @input="inputPage"></v-pagination>
        </div>

        <div id="test"></div>

        <!-- 组件 -->
        <v-dialog v-if="creationDialog" v-model="creationDialog" max-width="800px">
            <d-creation-app></d-creation-app>
        </v-dialog>
    </v-card>
</template>
<script>

    // creation/update/deletion

    import Vue from 'vue'
    import axios from 'axios'
    import DCreationApp from '../../components/apps/DCreationApp.vue'

    export default {
        components: {DCreationApp: DCreationApp},
        data: () => ({
            headers: [
                {
                    text: '名称(name)',
                    value: 'name'
                },
                {
                    text: '配置(profile)',
                    value: 'profile'
                },
                {
                    text: '描述',
                    sortable: false,
                    value: 'description'
                },
                {
                    text: '令牌',
                    sortable: false,
                    value: 'token'
                },
                {
                    text: 'IP 限制',
                    sortable: false,
                    value: 'ip_limit'
                },
                {
                    text: '创建时间',
                    value: 'created_at'
                },
                {
                    text: '修改时间',
                    value: 'updated_at'
                }
            ],
            items: [],
            pagination: {
                rowsPerPage: 15,
                page: 1,
                totalItems: 0,
                totalPages: 0
            },
            q: '',
            creationDialog: false
        }),
        mounted() {
            this.loadAppByUser()
        },
        methods: {
            loadAppByUser() {
                let p = this.pagination
                axios.get(`/api/admins/search/apps?page=${p.page}&size=${p.rowsPerPage}&q=${this.q}`).then(response => {
                    this.items = response.data.items
                    this.pagination.totalItems = response.data.total_items
                    this.pagination.totalPages = response.data.total_pages
                })
            },
            inputPage(v) {
                this.pagination.page = v
                this.loadAppByUser()
            }
        }
    }
</script>