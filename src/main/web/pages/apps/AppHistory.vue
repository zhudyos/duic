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
        <v-card-text>
            <div>应用名称：<span class="red--text">{{$route.query.name}}</span></div>
            <div>应用配置：<span class="red--text">{{$route.query.profile}}</span></div>
        </v-card-text>
        <v-data-table
                :headers="headers"
                :items="items"
                no-data-text="没有数据"
                hide-actions>
            <template slot="items" slot-scope="props">
                <td>
                    <a @click="standbyHid = props.item.hid; diffDialog = true">{{props.item.hid}}</a>
                </td>
                <td>{{props.item.updated_by}}</td>
                <td>{{props.item.updated_at}}</td>
            </template>
        </v-data-table>

        <!-- 组件 -->
        <v-dialog v-if="diffDialog" v-model="diffDialog" fullscreen max-width="800px">
            <d-app-history-diff @input="diffDialog = false"
                                :name="$route.query.name"
                                :profile="$route.query.profile"
                                :hid="standbyHid"
            ></d-app-history-diff>
        </v-dialog>

    </v-card>
</template>
<script>
    import axios from 'axios'
    import DAppHistoryDiff from "../../components/apps/DAppHistoryDiff.vue";

    export default {
        components: {DAppHistoryDiff},
        data: () => ({
            headers: [
                {
                    text: 'HID',
                    value: 'hid',
                    sortable: false
                },
                {
                    text: '修改人',
                    value: 'updated_by',
                    sortable: false
                },
                {
                    text: '修改时间',
                    value: 'updated_at',
                    sortable: false
                }
            ],
            items: [],
            diffDialog: false,
            standbyHid: ''
        }),
        mounted() {
            this.loadItems()
        },
        methods: {
            loadItems() {
                var query = this.$route.query
                axios.get(`/api/admins/apps/${query.name}/${query.profile}/histories`).then((response) => {
                    this.items = response.data
                })
            }
        }
    }
</script>