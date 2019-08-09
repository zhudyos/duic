<template>
  <q-page>
    <q-table
      flat
      dark
      :data="items"
      :columns="columns"
      row-key="id"
      :pagination.sync="pagination"
      :rows-per-page-options="[10, 30, 100]"
      :loading="loading"
      :filter="filter"
      @request="loadApps"
    >
      <template v-slot:top>
        <div class="col-2 q-table__title">应用列表</div>
        <q-space />
        <q-btn flat>创建</q-btn>
        <q-btn flat>复制</q-btn>
        <q-btn flat>删除</q-btn>
        <div class="col-12 row">
          <div class="offset-9 col-3">
            <q-input dark dense debounce="400" v-model="filter" placeholder="搜索" class="full-width">
              <template v-slot:append>
                <q-icon
                  v-if="filter !== ''"
                  name="mdi-close"
                  @click="filter = ''"
                  class="cursor-pointer"
                />
                <q-icon name="mdi-magnify" />
              </template>
            </q-input>
          </div>
        </div>
      </template>

      <template v-slot:body="props">
        <q-tr :props="props">
          <q-td key="name" :props="props">
            {{props.row.name}}
            <q-btn
              dense
              round
              flat
              :icon="props.expand ? 'mdi-menu-up' : 'mdi-menu-down'"
              @click="props.expand = !props.expand"
            />
          </q-td>
          <q-td key="profile" :props="props">{{props.row.profile}}</q-td>
          <q-td key="description" :props="props">{{props.row.description}}</q-td>
        </q-tr>
        <q-tr v-show="props.expand" :props="props">
          <q-td colspan="100%">
            <div class="text-left">This is expand slot for row above: {{ props.row.name }}.</div>
          </q-td>
        </q-tr>
      </template>
    </q-table>
  </q-page>
</template>
<script>
import axios from "axios";
export default {
  data: () => ({
    pagination: {
      page: 1,
      rowsPerPage: 10,
      rowsNumber: 0
    },
    loading: false,
    filter: "",
    columns: [
      {
        name: "name",
        label: "名称（name）"
      },
      {
        name: "profile",
        label: "环境（profile）"
      },
      {
        name: "description",
        label: "描述"
      }
    ],
    items: []
  }),
  mounted() {
    this.loadApps({ pagination: this.pagination });
  },
  methods: {
    loadApps(props) {
      this.loading = true;

      let { page, rowsPerPage } = props.pagination;
      axios
        .get(`/api/admins/apps/user?page=${page}&size=${rowsPerPage}`)
        .then(response => {
          const data = response.data || {};
          this.items = data.items;

          this.pagination.page = page;
          this.pagination.rowsPerPage = rowsPerPage;
          this.pagination.rowsNumber = data.total_items;
          this.loading = false;
        });
    }
  }
};
</script>