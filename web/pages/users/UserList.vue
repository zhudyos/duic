<template>
  <q-page>
    <q-card>
      <q-table
        flat
        :data="items"
        :columns="columns"
        row-key="email"
        :pagination.sync="pagination"
        :rows-per-page-options="[10, 30, 100]"
        :loading="loading"
        @request="loadUsers"
      >
        <template v-slot:top>
          <div class="col-2 q-table__title">用户列表</div>
          <q-space />
          <q-btn flat @click="addUser">创建</q-btn>
        </template>
        <template v-slot:body="props">
          <q-tr :props="props">
            <q-td key="email" :props="props">{{ props.row.email }}</q-td>
            <q-td key="createdAt" :props="props">{{
              props.row.created_at
            }}</q-td>
            <q-td key="updatedAt" :props="props">{{
              props.row.updated_at
            }}</q-td>
            <q-td key="ops" :props="props">
              <q-btn
                dense
                flat
                size="sm"
                icon="mdi-key-change"
                @click="resetPwd(props.row.email)"
              />
              <q-btn
                dense
                flat
                size="sm"
                icon="mdi-delete"
                @click="deleteUser(props.row.email)"
              />
            </q-td>
          </q-tr>
        </template>
      </q-table>
    </q-card>
  </q-page>
</template>
<script>
import axios from "axios";
import UserResetPwd from "./UserResetPwd";
import UserAdd from "./UserAdd";
export default {
  data: () => ({
    pagination: {
      page: 1,
      rowsPerPage: 10,
      rowsNumber: 0
    },
    columns: [
      {
        name: "email",
        label: "邮箱"
      },
      {
        name: "createdAt",
        label: "创建时间"
      },
      {
        name: "updatedAt",
        label: "更新时间"
      },
      {
        name: "ops",
        label: "操作"
      }
    ],
    loading: false,
    items: []
  }),
  mounted() {
    this.loadUsers();
  },
  methods: {
    loadUsers(props) {
      this.loadUsers0(props || { pagination: this.pagination });
    },
    loadUsers0(props) {
      this.loading = true;

      let { page, rowsPerPage } = props.pagination;
      axios
        .get(`/api/admins/users?page=${page}&size=${rowsPerPage}`)
        .then(response => {
          const data = response.data || {};
          this.items = data.items;

          this.pagination.page = page;
          this.pagination.rowsPerPage = rowsPerPage;
          this.pagination.rowsNumber = data.total_items;
          this.loading = false;
        });
    },
    addUser() {
      this.$q
        .dialog({
          component: UserAdd,
          root: this.$root
        })
        .onOk(this.loadUsers);
    },
    resetPwd(email) {
      this.$q.dialog({
        component: UserResetPwd,
        email: email
      });
    },
    deleteUser(email) {
      this.$q
        .dialog({
          title: "删除用户",
          message: `确认删除用户 ${email}，同时删除用户已关联的配置权限`,
          cancel: true
        })
        .onOk(() => {
          this.loading = true;
          axios
            .delete(`/api/admins/users/${email}`)
            .then(() => {
              this.loadUsers();
            })
            .catch(error => {
              this.loading = false;

              const d = error.response.data || {};
              let text = d.message || "删除失败";
              this.$q.notify({
                color: "negative",
                message: text,
                position: "top"
              });
            });
        });
    }
  }
};
</script>
