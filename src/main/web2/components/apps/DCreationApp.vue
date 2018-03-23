<template>
    <v-card>
        <v-card-title>创建应用</v-card-title>
        <v-card-text>
            <v-container grid-list-md>
                <v-layout row>
                    <v-flex xs4>
                        <v-subheader class="text-xs-right">应用名称</v-subheader>
                    </v-flex>
                    <v-flex xs8>
                        <v-text-field label="name" required></v-text-field>
                    </v-flex>
                </v-layout>
                <v-layout row>
                    <v-flex xs4>
                        <v-subheader>配置名称</v-subheader>
                    </v-flex>
                    <v-flex xs8>
                        <v-text-field label="profile" required></v-text-field>
                    </v-flex>
                </v-layout>
                <v-layout row>
                    <v-flex xs4>
                        <v-subheader>应用描述</v-subheader>
                    </v-flex>
                    <v-flex xs8>
                        <v-text-field label="description" required></v-text-field>
                    </v-flex>
                </v-layout>
                <v-layout row>
                    <v-flex xs4>
                        <v-subheader>访问令牌</v-subheader>
                    </v-flex>
                    <v-flex xs8>
                        <v-text-field label="token" required></v-text-field>
                    </v-flex>
                </v-layout>
                <v-layout row>
                    <v-flex xs4>
                        <v-subheader>IP 限制</v-subheader>
                    </v-flex>
                    <v-flex xs8>
                        <v-text-field label="token" required></v-text-field>
                    </v-flex>
                </v-layout>
                <v-layout row>
                    <v-flex xs4>
                        <v-subheader>所属用户</v-subheader>
                    </v-flex>
                    <v-flex xs8>
                        <v-text-field label="users" required></v-text-field>
                    </v-flex>
                </v-layout>
            </v-container>
        </v-card-text>
        <v-card-actions>
            <v-btn color="blue darken-1" flat @click="show = false">保存</v-btn>
        </v-card-actions>
    </v-card>
</template>
<script>
    export default {
        name: 'DCreationApp',
        props: {
            show: Boolean,
            default: true
        }
    }
</script>