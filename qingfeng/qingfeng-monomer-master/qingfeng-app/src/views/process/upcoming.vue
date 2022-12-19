<template>
  <div>
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div v-for="(item, index) in list" @click="info(item)">
          <div class="statusClass">待办</div>
          <div
            class="main"
            :style="{ borderLeft: colorList[index % 4] + ' solid 4px' }"
          >
            <div class="head">
              <div class="title">{{ item.procdef_name }}</div>
              <div class="uname">{{ item.start_user_name }}</div>
              <div class="time">{{ timeA(item) }}</div>
            </div>
            <div class="content">
              {{ titleA(item) }}
            </div>
            <div style="clear: both"></div>
          </div>
          <div class="line5" style="clear: both"></div>
        </div>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script>
import { findTaskListPage } from "@/api/activiti/index";
import { formatDate } from "@/utils/date.js";
export default {
  data() {
    return {
      colorList: ["#2a7aff", "#7ed321", "#f31876", "#f65a5a"],
      list: [],
      loading: false,
      finished: false,
      refreshing: false,
      pageNum: 0,
    };
  },
  computed: {},
  methods: {
    timeA(item) {
      return formatDate(item.start_time);
    },
    titleA(item) {
      return (
        item.start_user_name +
        "与" +
        formatDate(item.start_time) +
        "发起【" +
        item.procdef_name +
        "】的流程"
      );
    },
    onLoad() {
      let that = this;
      if (this.refreshing) {
        this.list = [];
        this.refreshing = false;
      }
      // 异步更新数据
      this.pageNum++;
      findTaskListPage({
        limit: 10,
        page: this.pageNum,
        type: "userTask",
      }).then((response) => {
        let data = response.data.data;
        that.list.push(...data);
        that.loading = false;
        if (data.length <= 0) {
          that.finished = true;
        }
      });
    },
    onRefresh() {
      // 清空列表数据
      this.finished = false;
      this.pageNum = 0;
      // 重新加载数据
      // 将 loading 设置为 true，表示处于加载状态
      this.loading = true;
      this.onLoad();
    },
    info(item) {
      this.$router.push({ name: "formCheck", params: item });
    },
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
  width: 40%;
  float: left;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
  font-weight: bold;
}
.time {
  width: 40%;
  color: #646566;
  float: right;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: center;
  font-size: 14px;
}
.uname {
  width: 20%;
  float: right;
}
.content {
  margin: 0px 10px 0 0px;
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
  margin-top: 4px;
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