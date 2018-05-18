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
        </v-card-actions>

        <v-data-table
                hide-actions
                no-data-text="没有数据"
                :headers="headers"
                :items="items">
            <template slot="items" slot-scope="props">
                <td>{{props.item.email}}</td>
                <td>{{props.item.created_at}}</td>
                <td>{{props.item.updated_at}}</td>
                <td align="center" v-if="$store.state.rootEmail !== props.item.email">
                    <a @click="resetPwdDialog = true; standbyEmail = props.item.email">
                        <v-icon color="warning">fas fa-redo-alt</v-icon>
                    </a>
                    <a @click="deleteUser(props.item.email)">
                        <v-icon color="red">fas fa-trash-alt</v-icon>
                    </a>
                </td>
                <td v-else></td>
            </template>
        </v-data-table>

        <div class="text-xs-right pt-2">
            <v-pagination v-model="pagination.page" :length="pagination.totalPages" @input="inputPage"></v-pagination>
        </div>

        <!-- dialog -->
        <v-dialog v-if="creationDialog" v-model="creationDialog" max-width="800px">
            <d-creation-user @finish="creationDialog = false; loadServers()"></d-creation-user>
        </v-dialog>

        <v-dialog v-if="resetPwdDialog" v-model="resetPwdDialog" max-width="800px">
            <d-reset-user-pwd :email="standbyEmail" @finish="resetPwdDialog = false"></d-reset-user-pwd>
        </v-dialog>

    </v-card>
</template>
<script>
    import axios from 'axios'
    import DCreationUser from "../../components/users/DCreationUser.vue"
    import DResetUserPwd from "../../components/users/DResetUserPwd.vue"

    export default {
        components: {
            DResetUserPwd,
            DCreationUser},
        data: () => ({
            headers: [
                {
                    text: 'e-mail',
                    value: 'email',
                    sortable: false
                },
                {
                    text: '创建时间',
                    value: 'created_at',
                    sortable: false
                },
                {
                    text: '修改时间',
                    value: 'updated_at',
                    sortable: false
                },
                {
                    text: '操作',
                    value: 'ops',
                    align: 'center',
                    sortable: false,
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
            creationDialog: false,
            resetPwdDialog: false,
            standbyEmail: ''
        }),
        mounted() {
            this.loadServers()
        },
        methods: {
            loadServers() {
                let p = this.pagination
                axios.get(`/api/admins/users?page=${p.page}&size=${p.rowsPerPage}`).then(response => {
                    this.items = response.data.items
                    p.totalItems = response.data.total_items
                    p.totalPages = response.data.total_pages
                })
            },
            inputPage(v) {
                this.pagination.page = v
                this.loadServers()
            },
            deleteUser(email) {
                this.$confirm(`确认删除用户 <span class="red--text">${email}</span>`, () => {
                    axios.delete(`/api/admins/users/${email}`).then(() => {
                        this.$notice('删除成功', {top: true, color: 'success'})
                        this.loadServers()
                    })
                })
            }
        }
    }
</script>