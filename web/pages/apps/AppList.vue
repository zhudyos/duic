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
        <q-btn flat @click="addApp">创建</q-btn>
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
            <q-btn dense flat size="sm" icon="mdi-dots-vertical">
              <q-popup-proxy>
                <q-card dark class="bg-grey-10">
                  <q-list padding dark class="details">
                    <q-item-label header>详细</q-item-label>
                    <q-item>
                      <q-item-section side class="caption">
                        <q-item-label caption>名称（name）</q-item-label>
                      </q-item-section>
                      <q-item-section>
                        <q-item-label>{{props.row.name}}</q-item-label>
                      </q-item-section>
                    </q-item>
                    <q-item>
                      <q-item-section side class="caption">
                        <q-item-label caption>环境（profile）</q-item-label>
                      </q-item-section>
                      <q-item-section>
                        <q-item-label>{{props.row.profile}}</q-item-label>
                      </q-item-section>
                    </q-item>
                    <q-item>
                      <q-item-section side class="caption">
                        <q-item-label caption>描述</q-item-label>
                      </q-item-section>
                      <q-item-section>
                        <q-item-label>{{props.row.description}}</q-item-label>
                      </q-item-section>
                    </q-item>
                    <q-item v-if="props.row.token">
                      <q-item-section side class="caption">
                        <q-item-label caption>令牌</q-item-label>
                      </q-item-section>
                      <q-item-section>
                        <q-item-label style="word-break: break-all;">{{props.row.token}}</q-item-label>
                      </q-item-section>
                    </q-item>
                    <q-item v-if="props.row.ip_limit">
                      <q-item-section side class="caption">
                        <q-item-label caption>IP限制</q-item-label>
                      </q-item-section>
                      <q-item-section>
                        <q-item-label>{{props.row.ip_limit}}</q-item-label>
                      </q-item-section>
                    </q-item>
                    <q-item>
                      <q-item-section side class="caption">
                        <q-item-label caption>创建时间</q-item-label>
                      </q-item-section>
                      <q-item-section>
                        <q-item-label>{{props.row.created_at}}</q-item-label>
                      </q-item-section>
                    </q-item>
                    <q-item>
                      <q-item-section side class="caption">
                        <q-item-label caption>修改时间</q-item-label>
                      </q-item-section>
                      <q-item-section>
                        <q-item-label>{{props.row.updated_at}}</q-item-label>
                      </q-item-section>
                    </q-item>
                  </q-list>
                </q-card>
              </q-popup-proxy>
            </q-btn>
          </q-td>
          <q-td key="profile" :props="props">
            <a
              class="text-yellow-8 cursor-pointer"
              @click="openNewTabEdit(props.row.name, props.row.profile)"
            >{{props.row.profile}}</a>
          </q-td>
          <q-td key="description" :props="props">{{props.row.description}}</q-td>
          <q-td key="ops" :props="props">
            <q-btn dense flat size="sm" icon="mdi-pencil" />
            <q-btn dense flat size="sm" icon="mdi-content-duplicate" />
            <q-btn
              dense
              flat
              size="sm"
              icon="mdi-delete"
              @click="deleteApp(props.row.name, props.row.profile)"
            />
          </q-td>
        </q-tr>
      </template>
    </q-table>
  </q-page>
</template>
<script>
import axios from "axios";
import AppAdd from "./AppAdd.vue";

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
      },
      {
        name: "ops",
        label: "操作"
      }
    ],
    items: []
  }),
  mounted() {
    this.loadApps();
  },
  methods: {
    loadApps(props) {
      this.loadApps0(props || { pagination: this.pagination });
    },
    loadApps0(props) {
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
    },
    openNewTabEdit(name, profile) {
      const route = this.$router.resolve({
        path: "/config-edit",
        query: {
          name: name,
          profile: profile
        }
      });
      window.open(route.href, "_blank");
    },
    addApp() {
      this.$q.dialog({
        component: AppAdd,
        root: this.$root
      });
    },
    deleteApp(name, profile) {
      this.$q
        .dialog({
          dark: true,
          title: "删除应用",
          message: `确认删除应用 ${name}/${profile}`,
          cancel: true
        })
        .onOk(() => {
          this.loading = true;

          axios
            .delete(`/api/admins/apps/${name}/${profile}`)
            .then(() => {
              this.loadApps();
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
<style lang="stylus" scoped>
.details {
  width: 400px;
  max-width: 500px;

  .caption {
    width: 110px;
  }
}
</style>