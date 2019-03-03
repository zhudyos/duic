<!--

    Copyright 2017-2019 the original author or authors

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
            <v-flex md3>
                <!--<v-btn color="error" :disabled="refreshBtnDisabled">刷新集群配置</v-btn>-->
                <label class="body-1">数据库最新数据状态：{{lastDataTime}}</label>
            </v-flex>
            <v-spacer></v-spacer>
            <v-flex md3>
                <v-text-field
                        v-model="search"
                        append-icon="search"
                        label="搜索"
                        single-line
                        hide-details
                ></v-text-field>
            </v-flex>
        </v-card-actions>

        <v-data-table
                hide-actions
                no-data-text="没有数据"
                :headers="headers"
                :search="search"
                :items="items"
                :pagination.sync="pagination"
        >
            <template slot="items" slot-scope="props">
                <td>{{props.item.host}}</td>
                <td>{{props.item.port}}</td>
                <td>{{props.item.init_at}}</td>
                <td>{{props.item.active_at}}</td>
                <td>{{props.item.last_data_time}}</td>
            </template>
        </v-data-table>

        <div class="text-xs-right pt-2">
            <v-pagination v-model="pagination.page"
                          :length="pagination.totalPages"
                          :total-visible="pagination.totalItems"
            ></v-pagination>
        </div>

    </v-card>
</template>
<script>
    import axios from 'axios'

    export default {
        data: () => ({
            search: '',
            headers: [
                {
                    text: '主机',
                    value: 'host',
                    sortable: false
                },
                {
                    text: '端口',
                    value: 'port',
                    sortable: false
                },
                {
                    text: '初始时间',
                    value: 'init_at'
                },
                {
                    text: '活跃时间',
                    value: 'active_at'
                },
                {
                    text: '最新数据状态',
                    value: 'last_data_time'
                }
            ],
            items: [],
            pagination: {
                rowsPerPage: 15,
                page: 1,
                totalItems: 0,
                totalPages: 0
            },
            refreshBtnDisabled: true,
            lastDataTime: 0
        }),
        mounted() {
            this.loadServers()
        },
        methods: {
            loadServers() {
                axios.get(`/api/admins/servers`).then(response => {
                    this.items = response.data
                    this.pagination.totalItems = this.items.length
                    this.pagination.totalPages = Math.ceil(this.items.length / this.pagination.rowsPerPage)

                    this.loadLastDataTime()
                })
            },
            loadLastDataTime() {
                axios.get(`/api/admins/apps/last-data-time`).then(response => {
                    this.lastDataTime = response.data.last_data_time
                    // this.items.forEach(item => {
                    //     if (this.refreshBtnDisabled && item.last_data_time !== this.lastDataTime) {
                    //         this.refreshBtnDisabled = false
                    //     }
                    // })
                })
            }
        }
    }
</script>