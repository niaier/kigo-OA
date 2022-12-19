<template>
  <div>
    <van-nav-bar title="青锋OA系统" right-text="发起流程" @click-right="sendProcess"/>
    <van-tabs
      v-model:active="active"
      sticky
      @click="onClick"
      :style="{ marginBottom: '50px' }"
    >
      <van-tab title="待办事项">
        <template #title
          ><img width="18px" src="@/assets/img/processDb.png" />&nbsp;待办事项
        </template>
        <div class="line5" style="clear: both"></div>
        <upcoming></upcoming>
      </van-tab>
      <van-tab title="已办事项">
        <template #title
          ><img width="18px" src="@/assets/img/processYb.png" />&nbsp;已办事项
        </template>
        <div class="line5" style="clear: both"></div>
        <done></done>
      </van-tab>
      <van-tab title="我的发起">
        <template #title
          ><img width="18px" src="@/assets/img/pricessMy.png" />&nbsp;我的发起
        </template>
        <div class="line5" style="clear: both"></div>
        <my></my>
      </van-tab>
    </van-tabs>
    <tabber></tabber>
  </div>
</template>

<script>
import tabber from "@/components/layout/tabbar";

import done from "./done.vue";
import my from "./my.vue";
import upcoming from "./upcoming.vue";
export default {
  data() {
    return {
      active: 0,
      colorList: ["#2a7aff", "#7ed321", "#f31876", "#f65a5a"],
      num: 10,
    };
  },
  components: {
    tabber,
    done,
    my,
    upcoming,
  },
  mounted() {
    this.params = this.$route.params;
    if(this.params.index!=undefined){
      this.active = this.params.index;
    }
  },
  methods: {
    onClick(name, title) {
      console.log(name + ":" + title);
    },
    sendProcess(){
       this.$router.push({ name: "send", params: {} });
    }
  },
};
</script>

<style scoped>
.main {
}
.head {
  line-height: 28px;
  margin: 2px 15px 0 15px;
  color: #303133;
  white-space: nowrap;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}
.title {
  width: 50%;
  float: left;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
  font-weight: bold;
}
.time {
  width: 25%;
  color: #646566;
  float: right;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: center;
  font-size: 14px;
}
.content {
  margin: 0px 10px 0 10px;
  font-size: 12px;
  color: #646566;
  width: 92%;
  text-align: center;
  float: left;
  -webkit-line-clamp: 2;
  overflow: hidden;
  word-break: break-all;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  line-height: 24px;
}
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
.statusClass {
  margin-top: 8px;
  position: absolute;
  right: 4px;
  line-height: 60px;
  width: 60px;
  height: 60px;
  background-color: #95a1a13d;
  border-radius: 50%;
  text-align: center;
  font-size: 22px;
  transform: rotate(30deg);
}
.statusActiviClass {
  margin-top: 8px;
  position: absolute;
  right: 4px;
  line-height: 42px;
  width: 42px;
  height: 42px;
  background-color: #28f8663d;
  border-radius: 50%;
  text-align: center;
  font-size: 12px;
  transform: rotate(30deg);
}
</style>