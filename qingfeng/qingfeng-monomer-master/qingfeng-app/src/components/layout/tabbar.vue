<template>
  <van-tabbar v-model="active" @change="onChange">
    <van-tabbar-item badge="">
      <span>首页</span>
      <template #icon="props">
        <img :src="props.active ? icon.index_active : icon.index_inactive" />
      </template>
    </van-tabbar-item>
    <van-tabbar-item>
      <span>流程办理</span>
      <template #icon="props">
        <img
          :src="props.active ? icon.process_active : icon.process_inactive"
        />
      </template>
    </van-tabbar-item>
    <van-tabbar-item>
      <span>我的</span>
      <template #icon="props">
        <img :src="props.active ? icon.my_active : icon.my_inactive" />
      </template>
    </van-tabbar-item>
  </van-tabbar>
</template>

<script>
import index_active from "../../assets/img/index_active.png";
import index_inactive from "../../assets/img/index_inactive.png";
import process_active from "../../assets/img/process_active.png";
import process_inactive from "../../assets/img/process_inactive.png";
import my_active from "../../assets/img/my_active.png";
import my_inactive from "../../assets/img/my_inactive.png";
import store from "@/store";
export default {
  data() {
    return {
    //   active: 0,
      icon: {
        index_active: index_active,
        index_inactive: index_inactive,
        process_active: process_active,
        process_inactive: process_inactive,
        my_active: my_active,
        my_inactive: my_inactive,
      },
    };
  },
  computed: {
    active: function () {
      return this.$store.getters.tabbar;
    },
  },
  mounted(){
    this.onChange(this.active);
  },
  methods: {
    onChange(index) {
      if (index == "0") {
        this.$router.push({ name: "index", params: {} });
      } else if (index == "1") {
        this.$router.push({ name: "process", params: {} });
      } else if (index == "2") {
        this.$router.push({ name: "my", params: {} });
      }
      store.commit("SET_TABBAR", index);
    },
  },
};
</script>
<style>
</style>