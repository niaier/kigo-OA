<template>
  <div>
    <van-nav-bar title="青锋OA系统" />
    <!-- 新闻信息 -->
    <van-swipe :autoplay="5000" :touchable="true">
      <van-swipe-item v-for="(item, index) in xwxxList" :key="index" @click="findNewInfo(item)">
        <img v-lazy="item.show_tpdz" height="180px" width="100%" />
        <div class="custom-indicator">
          {{item.title}}{{ index + 1 }}/4
        </div>
      </van-swipe-item>
    </van-swipe>
    <!-- 公告信息 -->
    <van-notice-bar
      color="#ed6a0c"
      left-icon="volume-o"
      :scrollable="true"
    >{{ggxxData}}</van-notice-bar>
    <!-- 导航信息 -->
    <div class="line"></div>
    <van-grid clickable :column-num="4">
      <van-grid-item text="发起流程" @click="sendProcess">
        <img width="32px" src="@/assets/img/processAdd.png" />
        <span style="padding-top:4px">发起流程</span>
      </van-grid-item>
      <van-grid-item text="待办事项" @click="process(0)">
        <img width="32px" src="@/assets/img/processDb.png" />
        <span style="padding-top:4px">待办事项</span>
      </van-grid-item>
      <van-grid-item text="已办事项" @click="process(1)">
        <img width="32px" src="@/assets/img/processYb.png" />
        <span style="padding-top:4px">已办事项</span>
      </van-grid-item>
      <van-grid-item icon="home-o" text="我的发起" @click="process(2)">
        <img width="32px" src="@/assets/img/pricessMy.png" />
        <span style="padding-top:4px">我的发起</span>
      </van-grid-item>
    </van-grid>
    <!-- 信息模块 -->
    <van-list
      v-model="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
      :style="{ marginBottom: '40px' }"
    >
      <div class="line"></div>
      <div v-for="item in gkxxList" :key="item.id" @click="findNewInfo(item)">
        <div class="head">
          <div class="title">
            {{item.title}}
          </div>
          <div class="time">{{item.publish_time}}</div>
        </div>
        <div style="clear: both" class="hairline van-hairline--top"></div>
        <div class="middle">
          <div class="content">
            {{item.intro}}
          </div>
          <div class="img">
            <van-image
              width="60"
              height="60"
              :src="item.show_tpdz"
            />
          </div>
        </div>
        <div style="clear: both" class="hairline van-hairline--bottom"></div>
        <div class="bottom bottom-text"><img height="18px" src="@/assets/img/eye.png"></img><span style="padding-left:4px">{{item.read_num}}预览</span></div>
        <div class="line" style="clear: both"></div>
      </div>
    </van-list>

    <tabber></tabber>
  </div>
</template>

<script>
import { getListPage, updateReadNum } from "@/api/common/graphic";
import tabber from "@/components/layout/tabbar";
import store from "@/store";
export default {
  data() {
    return {
      loading: false,
      finished: false,
      xwxxList: [],
      ggxxData: "暂无公告",
      gkxxList: [],
      pageNum: 1,
      gkxxType: "0",
    };
  },
  components: {
    tabber,
  },
  mounted() {
    this.initData();
  },
  methods: {
    initData() {
      //查询新闻信息
      getListPage({
        pageSize: 5,
        pageNum: 1,
        type: "0",
      }).then((response) => {
        this.xwxxList = response.data.data.rows;
      });
      //查询公告信息
      getListPage({
        pageSize: 1,
        pageNum: 1,
        type: "1",
      }).then((response) => {
        if (response.data.data.rows.length > 0) {
          this.ggxxData = response.data.data.rows[0].intro;
        }
      });
      //查询公开信息
      getListPage({
        pageSize: 10,
        pageNum: this.pageNum,
        type: "2",
      }).then((response) => {
        this.gkxxList = response.data.data.rows;
      });
    },
    onLoad() {
      if (this.gkxxType == "1") {
        return;
      }
      // 异步更新数据
      this.pageNum++;
      getListPage({
        pageSize: 10,
        pageNum: this.pageNum,
        type: "2",
      }).then((response) => {
        if (response.data.data.rows.length > 0) {
          this.gkxxList.push(response.data.data.rows);
        } else {
          this.gkxxType = 1;
        }
      });
      // 加载状态结束
      this.loading = false;
    },
    findNewInfo(item) {
      updateReadNum({ id: item.id }).then((response) => {
        item.read_num++;
        this.$router.push({ name: "newInfo", params: item });
      });
    },
    sendProcess() {
      this.$router.push({ name: "send", params: {} });
    },
    process(i) {
      this.$router.push({ name: "process", params: { index: i } });
      store.commit("SET_TABBAR", 1);
    },
  },
};
</script>

<style scoped>
.custom-indicator {
  position: absolute;
  left: 0px;
  right: 0px;
  bottom: 0px;
  padding: 2px 5px;
  font-size: 12px;
  color: #e6e0e0;
  background: rgba(244, 244, 239, 0.5);
}
.head {
  line-height: 32px;
  margin: 2px 10px 0 10px;
  color: #303133;
  white-space: nowrap;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}
.title {
  width: 70%;
  float: left;
  overflow: hidden;
  text-overflow: ellipsis;
}
.time {
  width: 30%;
  float: right;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: center;
}
.middle {
  font-size: 13px;
  color: #333;
  padding: 0px 10px;
}
.content {
  width: 80%;
  float: left;
  -webkit-line-clamp: 3;
  overflow: hidden;
  word-break: break-all;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-box-orient: vertical;
}
.img {
  width: 20%;
  float: right;
  text-align: center;
}
.bottom {
  padding: 0px 10px;
  height: 28px;
  line-height: 28px;
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
.hairline {
  height: 5px;
  margin-top: 0px;
  margin-bottom: 0px;
}
.bottom-text {
  color: rgb(96, 98, 102);
  font-size: 12px;
  margin: 0px 0px 0px 3px;
}
</style>