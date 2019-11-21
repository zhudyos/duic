<template>
  <q-select
    v-model="value"
    :multiple="multiple"
    :use-input="useInput"
    :use-chips="useChips"
  ></q-select>
</template>
<script>
import axios from "axios";
export default {
  name: "DUserEmailSelect",
  props: {
    value: {
      required: true
    },
    multiple: Boolean,
    useInput: Boolean,
    useChips: Boolean
  },
  data: () => ({
    users: [],
    filteredUsers: []
  }),
  mounted() {
    axios.get(`/api/admins/users/emails`).then(response => {
      this.users = response.data;
      this.filteredUsers = response.data;
    });
  },
  methods: {
    filterUser(val, update) {
      if (val === "") {
        update(() => {
          this.filteredUsers = this.users;
        });
        return;
      }

      update(() => {
        const needle = val.toLowerCase();
        this.filteredUsers = this.users.filter(
          v => v.toLowerCase().indexOf(needle) > -1
        );
      });
    }
  }
};
</script>
