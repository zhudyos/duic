<!--

    Copyright 2017-2018 the original author or authors

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<template>
    <v-card>
        <v-card-actions>
            <v-btn flat @click.stop="creationDialog = !creationDialog">创建</v-btn>
            <v-btn flat :disabled="selectedRowIndex === -1" @click.stop="openDuplicateDialog()">克隆</v-btn>
            <v-spacer></v-spacer>
            <v-flex md3 @keydown.enter="search">
                <v-text-field
                        append-icon="search"
                        :append-icon-cb="search"
                        label="搜索"
                        single-line
                        clearable
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
                <tr @click="selectRow(props.index, props.item)"
                    :class="selectedRowIndex === props.index ?'grey lighten-2':''">
                    <td>{{props.item.name}}</td>
                    <td>
                        <a @click="updateContentApp(props.item.name, props.item.profile)">{{props.item.profile}}</a>
                    </td>
                    <td>{{props.item.description}}</td>
                    <td>
                        <a v-if="props.item.token" v-clipboard:copy="props.item.token">复制</a>
                    </td>
                    <td>{{props.item.ip_limit}}</td>
                    <td>{{props.item.created_at}}</td>
                    <td>{{props.item.updated_at}}</td>
                    <td align="center">
                        <a @click="updateApp(props.item.name, props.item.profile)">
                            <v-icon color="blue">fas fa-edit</v-icon>
                        </a>
                        <a @click="deleteApp(props.item.name, props.item.profile)">
                            <v-icon color="red">fas fa-trash-alt</v-icon>
                        </a>
                        <a @click="$router.push(`/app-histories?name=${props.item.name}&profile=${props.item.profile}`)">
                            <v-icon>fas fa-history</v-icon>
                        </a>
                    </td>
                </tr>
            </template>
        </v-data-table>
        <div class="text-xs-right pt-2">
            <v-pagination v-model="pagination.page" :length="pagination.totalPages" @input="inputPage"></v-pagination>
        </div>

        <div id="test"></div>

        <!-- 组件 -->
        <v-dialog v-if="creationDialog" v-model="creationDialog" max-width="800px">
            <d-creation-app @created="creationDialog = false; loadAppByUser()"></d-creation-app>
        </v-dialog>

        <v-dialog v-if="updateDialog" v-model="updateDialog" max-width="800px">
            <d-update-app @updated="updateDialog = false; loadAppByUser()" v-bind="standbyApp"></d-update-app>
        </v-dialog>

        <v-dialog v-if="duplicateDialog" v-model="duplicateDialog" max-width="800px">
            <d-duplicate-app :name="selectedRow.name" :profile="selectedRow.profile"
                             @input="duplicateDialog = false; loadAppByUser()"></d-duplicate-app>
        </v-dialog>

        <v-dialog v-if="updateContentDialog" v-model="updateContentDialog" fullscreen>
            <d-update-content-app v-bind="standbyApp" @input="updateContentDialog = false"></d-update-content-app>
        </v-dialog>

    </v-card>
</template>
<script>

    // creation/update/deletion

    import Vue from 'vue'
    import axios from 'axios'
    import DCreationApp from '../../components/apps/DCreationApp.vue'
    import DUpdateApp from "../../components/apps/DUpdateApp.vue";
    import DUpdateContentApp from "../../components/apps/DUpdateContentApp.vue";
    import DDuplicateApp from "../../components/apps/DDuplicateApp.vue";

    export default {
        components: {
            DDuplicateApp,
            DUpdateContentApp,
            DUpdateApp,
            DCreationApp: DCreationApp
        },
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
                    value: 'token',
                    width: 80
                },
                {
                    text: 'IP 限制',
                    sortable: false,
                    value: 'ip_limit'
                },
                {
                    text: '创建时间',
                    value: 'created_at',
                    width: 180
                },
                {
                    text: '修改时间',
                    value: 'updated_at',
                    width: 180
                },
                {
                    text: '操作',
                    sortable: false,
                    value: 'ops',
                    align: 'center',
                    width: 130
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
            creationDialog: false,
            updateDialog: false,
            duplicateDialog: false,
            updateContentDialog: false,
            standbyApp: {
                name: '',
                profile: ''
            },
            selectedRowIndex: -1,
            selectedRow: {}
        }),
        mounted() {
            this.$_loadAppByUser()
        },
        watch: {
            q() {
                if (!this.q) {
                    this.search()
                }
            }
        },
        methods: {
            $_loadAppByUser() {
                let p = this.pagination
                let query = this.$route.query

                // 初始化 query 参数
                if (query.page) {
                    p.page = parseInt(query.page)
                }
                if (query.size) {
                    p.rowsPerPage = parseInt(query.size)
                }
                if (query.q) {
                    this.q = query.q
                }

                axios.get(`/api/admins/search/apps?page=${p.page}&size=${p.rowsPerPage}&q=${this.q || ''}`).then(response => {
                    this.items = response.data.items
                    p.totalItems = response.data.total_items
                    p.totalPages = response.data.total_pages
                })
            },
            search() {
                let p = this.pagination
                this.$router.push({
                    path: "/apps",
                    query: {page: 1, size: p.rowsPerPage, q: this.q || ''}
                })
                this.$_loadAppByUser()
            },
            loadAppByUser() {
                let p = this.pagination
                this.$router.push({
                    path: "/apps",
                    query: {page: p.page, size: p.rowsPerPage, q: this.q}
                })
                this.$_loadAppByUser()
            },
            inputPage(v) {
                this.pagination.page = v
                this.loadAppByUser()
            },
            updateApp(name, profile) {
                this.standbyApp = {name: name, profile: profile}
                this.updateDialog = true
            },
            updateContentApp(name, profile) {
                this.standbyApp = {name: name, profile: profile}
                this.updateContentDialog = true
            },
            deleteApp(name, profile) {
                this.$confirm(`确认删除应用 <span class="red--text">${name}/${profile}</span>`, () => {
                    axios.delete(`/api/admins/apps/${name}/${profile}`).then(() => {
                        this.$notice('删除成功', {top: true, color: 'success'})
                        this.loadAppByUser()
                    }).catch(() => {
                        this.$notice('删除失败')
                    })
                })
            },
            openDuplicateDialog() {
                this.duplicateDialog = !this.duplicateDialog
            },
            selectRow(index, item) {
                if (this.selectedRowIndex === index) {
                    this.selectedRowIndex = -1
                    this.selectedRow = {}
                } else {
                    this.selectedRowIndex = index
                    this.selectedRow = item
                }
            }
        }
    }
</script>