<template>
  <q-page>
    <q-table
      flat
      class="app-list"
      :data.sync="items"
      :columns="columns"
      :pagination.sync="pagination"
      :rows-per-page-options="[0]"
      :loading="loading"
      row-key="index"
    >
      <template v-slot:top>
        <div class="col-2 q-table__title">应用列表</div>
        <q-space />
        <q-btn flat @click="addApp">创建</q-btn>
        <div class="col-12 row">
          <div class="offset-8 col-4">
            <q-input
              dense
              debounce="500"
              v-model="filter"
              placeholder="搜索"
              class="full-width"
              @keyup.enter.native="loadApps()"
            >
              <template v-slot:append>
                <q-icon
                  v-if="filter !== ''"
                  name="mdi-close"
                  @click="
                    filter = '';
                    loadApps();
                  "
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
            {{ props.row.name }}
            <q-btn
              dense
              flat
              size="sm"
              :icon="props.expand ? 'mdi-chevron-up' : 'mdi-chevron-down'"
              @click="props.expand = !props.expand"
            />
          </q-td>
          <q-td key="profile" :props="props">
            <a
              class="text-yellow-8 cursor-pointer"
              @click="openNewTabEdit(props.row.name, props.row.profile)"
              >{{ props.row.profile }}</a
            >
          </q-td>
          <q-td key="description" :props="props">{{
            props.row.description
          }}</q-td>
          <q-td key="ops" :props="props">
            <q-btn
              dense
              flat
              size="sm"
              icon="mdi-pencil"
              @click="editApp(props.row.name, props.row.profile)"
            />
            <q-btn
              dense
              flat
              size="sm"
              icon="mdi-delete"
              @click="deleteApp(props.row.name, props.row.profile)"
            />
          </q-td>
        </q-tr>
        <q-tr v-show="props.expand" :props="props">
          <q-td colspan="100%">
            <q-card>
              <q-list padding class="details">
                <q-item-label header>详细</q-item-label>
                <q-item>
                  <q-item-section side class="caption">
                    <q-item-label caption>名称(name)</q-item-label>
                  </q-item-section>
                  <q-item-section>
                    <q-item-label>{{ props.row.name }}</q-item-label>
                  </q-item-section>
                </q-item>
                <q-item>
                  <q-item-section side class="caption">
                    <q-item-label caption>环境(profile(</q-item-label>
                  </q-item-section>
                  <q-item-section>
                    <q-item-label>{{ props.row.profile }}</q-item-label>
                  </q-item-section>
                </q-item>
                <q-item>
                  <q-item-section side class="caption">
                    <q-item-label caption>描述</q-item-label>
                  </q-item-section>
                  <q-item-section>
                    <q-item-label>{{ props.row.description }}</q-item-label>
                  </q-item-section>
                </q-item>
                <q-item v-if="props.row.token">
                  <q-item-section side class="caption">
                    <q-item-label caption>令牌</q-item-label>
                  </q-item-section>
                  <q-item-section>
                    <q-item-label style="word-break: break-all;">{{
                      props.row.token
                    }}</q-item-label>
                  </q-item-section>
                </q-item>
                <q-item v-if="props.row.ip_limit">
                  <q-item-section side class="caption">
                    <q-item-label caption>IP限制</q-item-label>
                  </q-item-section>
                  <q-item-section>
                    <q-item-label>{{ props.row.ip_limit }}</q-item-label>
                  </q-item-section>
                </q-item>
                <q-item>
                  <q-item-section side class="caption">
                    <q-item-label caption>创建时间</q-item-label>
                  </q-item-section>
                  <q-item-section>
                    <q-item-label>{{ props.row.created_at }}</q-item-label>
                  </q-item-section>
                </q-item>
                <q-item>
                  <q-item-section side class="caption">
                    <q-item-label caption>修改时间</q-item-label>
                  </q-item-section>
                  <q-item-section>
                    <q-item-label>{{ props.row.updated_at }}</q-item-label>
                  </q-item-section>
                </q-item>
              </q-list>
            </q-card>
          </q-td>
        </q-tr>
      </template>
    </q-table>
  </q-page>
</template>
<script>
import axios from "axios";
import AppAdd from "./AppAdd.vue";
import AppEdit from "./AppEdit.vue";

export default {
  data: () => ({
    loading: false,
    filter: "",
    pagination: {
      rowsPerPage: 0
    },
    columns: [
      {
        name: "name",
        label: "名称(name)"
      },
      {
        name: "profile",
        label: "环境(profile)"
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
    loadApps() {
      this.loadApps0();
    },
    loadApps0() {
      this.loading = true;

      axios
        .get(
          `/api/admins/apps/user?q=${this.filter}`
        )
        .then(response => {
          this.items = response.data || [];
          this.loading = false;
        })
        .catch(() => {
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
      this.$q
        .dialog({
          component: AppAdd,
          root: this.$root
        })
        .onOk(() => {
          this.loadApps();
        });
    },
    editApp(name, profile) {
      this.$q.dialog({
        component: AppEdit,
        root: this.$root,
        name: name,
        profile: profile
      });
    },
    deleteApp(name, profile) {
      this.$q
        .dialog({
          title: "删除应用",
          message: `确认删除应用 <label class="text-negative text-weight-bold">${name}/${profile}</label>`,
          html: true,
          cancel: true,
          focus: "cancel"
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
              this.$q.notify({
                color: "negative",
                message: `配置删除失败：${d.message}`,
                position: "top"
              });
            });
        });
    }
  }
};
</script>
<style lang="stylus" scoped>
.app-list {
  height: calc(100vh - 200px);

  th {
    position: sticky;
    z-index: 1;
  }
}
.details {
  width: 400px;
  max-width: 500px;

  .caption {
    width: 110px;
  }
}
</style>
