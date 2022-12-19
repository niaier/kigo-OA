<template>
  <div>
    <van-nav-bar
      title="青锋OA系统"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
    />
    <div class="line5"></div>
    <div v-for="(name, index) in names" :key="index">
      <van-divider content-position="left">{{ name }}</van-divider>
      <div>
        <van-button
          v-for="item in menuList"
          v-show="item.parent_name == name"
          @click="clickMenu(item)"
          class="btn"
          color="#1989fa"
          plain
          size="mini"
          type="primary"
          >{{ item.name }}</van-button
        >
      </div>
    </div>
  </div>
</template>

<script>
import { findMenuList } from "@/api/system/menu";
import { findMenuForm } from "@/api/customize/vmenu";
export default {
  data() {
    return {
      menuList: [],
      names: [],
    };
  },
  components: {},
  created() {
    this.initData();
  },
  mounted() {},
  methods: {
    initData() {
      let that = this;
      findMenuList({}).then((response) => {
        let mType = [];
        response.data.data.forEach((item) => {
          if (!mType.includes(item.parent_name)) {
            mType.push(item.parent_name);
          }
        });
        that.names = mType;
        that.menuList = response.data.data;
      });
    },
    onClickLeft() {
      this.$router.go(-1);
    },
    clickMenu(item) {
      //查询
      let menu_id = item.path.substring(item.path.lastIndexOf("/") + 1);
      console.log(menu_id);
      findMenuForm({ id: menu_id }).then((response) => {
        let data = response.data.data;
        let record = {
          process_key: data.vmenu.process_id,
          menu_id: data.vmenu.id,
          table_id: data.vmenu.table_id,
        };
        this.$router.push({ name: "PEdit", params: record });
      });
    },
  },
};
</script>

<style scoped>
.line {
  background-color: rgb(237, 237, 237);
  height: 8px;
  margin-top: 0px;
  margin-bottom: 0px;
}
.line5 {
  background-color: rgb(237, 237, 237);
  height: 5px;
  margin-top: 0px;
  margin-bottom: 0px;
}
.btn {
  width: 46%;
  float: left;
  margin: 1% 2%;
}
.van-divider {
  margin: 8px 0;
}
</style>