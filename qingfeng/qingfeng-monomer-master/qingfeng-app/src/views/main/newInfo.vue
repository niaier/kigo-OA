<template>
  <div>
    <van-nav-bar
      title="青锋OA系统"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
    />

    <div>
      <div class="title">
        {{data.title}}
      </div>
      <div style="clear: both" class="hairline van-hairline--bottom"></div>
      <div class="time">
        <div class="time-left">{{data.publish_time}}</div>
        <div class="time-right"><img height="20px" src="@/assets/img/eye.png"></img><span style="padding-left:4px">{{data.read_num}}阅读</span></div>
      </div>
      <div style="clear: both" class="hairline"></div>
      <div class="img"><img width="95%" :src="data.show_tpdz"/></div>
      <div style="clear: both" class="hairline"></div>
      <div class="content">
        <div v-html="data.content"></div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      data: {},
    };
  },
  components: {
  },
  mounted() {
    this.data = this.$route.params;
  },
  methods: {
    onClickLeft() {
      this.$router.go(-1);
    },
  },
};
</script>

<style scoped>
.title {
  font-size: 19px;
  font-weight: 700;
  padding-top: 10px;
  margin: 2px 10px 0 10px;
  color: #303133;
  overflow: hidden;
  display: -webkit-box;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
.hairline {
  height: 5px;
  margin-top: 0px;
  margin-bottom: 0px;
}
.time {
  margin: 5px 20px 0 20px;
}
.time-left {
  width: 50%;
  float: left;
}
.time-right {
  width: 50%;
  float: right;
  text-align: right;
}
.content {
  margin: 0px 15px 60px 15px;
  font-size: 14px;
}
.img {
  text-align: center;
}
</style>