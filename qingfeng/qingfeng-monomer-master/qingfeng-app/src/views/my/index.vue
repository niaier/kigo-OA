<template>
  <div>
    <van-nav-bar title="青锋OA系统" />
    <div class="head">
      <div style="float: left">
        <img
          class="touxiang"
          width="80px"
          height="80px"
          src="@/assets/img/touxiang.png"
        />
      </div>
      <div class="uinfo" style="float: left" @click="updateUser">
        <div class="uinfo-name">姓名：{{ username }}</div>
        <div class="uinfo-org">组织：{{ orgname }}</div>
      </div>
      <div class="r" style="float: right" @click="updateUser"><van-icon name="arrow" /></div>
    </div>
    <div class="line" style="clear: both; margin-top: 5px"></div>
    <div>
      <div class="content" @click="updateUser">
        <van-icon name="user-o" />
        个人信息
        <div class="content-r" style="float: right">
          <van-icon name="arrow" />
        </div>
      </div>
      <van-divider :style="{ borderColor: '#cacbcdcc', margin: '0px' }" />
      <div class="content" @click="updatePwd">
        <van-icon name="setting-o" />
        密码重置
        <div class="content-r" style="float: right">
          <van-icon name="arrow" />
        </div>
      </div>
      <div class="line" style="clear: both; margin-top: 5px"></div>
      <div class="content" @click="outLogin">
        <van-icon name="revoke" />
        退出登录
        <div class="content-r" style="float: right">
          <van-icon name="arrow" />
        </div>
      </div>
      <div class="line" style="clear: both; margin-top: 5px"></div>
    </div>
    <tabber></tabber>
  </div>
</template>

<script>
import tabber from "@/components/layout/tabbar";
import store from "@/store";
export default {
  data() {
    return { userInfo: {}, username: "", orgname: "" };
  },
  components: {
    tabber,
  },
  computed: {},
  mounted() {
    this.userInfo = this.$store.getters.userInfo;
    if (this.userInfo.name == undefined) {
      this.username = storage.get("username");
      this.orgname = storage.get("orgname");
    }else{
      this.username = this.userInfo.name;
      this.orgname = this.userInfo.orgPd.organize_name;
    }
  },
  methods: {
    outLogin() {
      store.commit("SET_TABBAR", 0);
      return this.$store.dispatch("Logout").then(() => {
        this.$router.push({ name: "login" });
      });
    },
    updatePwd(){
      this.$router.push("/updatePwd")
    },
    updateUser(){
      this.$router.push("/updateUser")
    }
  },
};
</script>

<style scoped>
.touxiang {
  border-radius: 50%;
  margin: 20px;
}
.head {
  height: 120px;
  width: 100%;
  /* background-color: aquamarine; */
  /* box-shadow: 5px 0px 2px 5px rgba(42, 122, 255, 0.5); */
  margin-top: 10px;
}
.uinfo {
  height: 120px;
  padding-top: 40px;
}
.uinfo-name {
  font-size: 16px;
  color: #303133;
  line-height: 24px;
}
.uinfo-org {
  font-size: 14px;
  color: #909399;
  line-height: 24px;
}
.r {
  line-height: 120px;
  margin-right: 20px;
}

.content {
  height: 32px;
  line-height: 32px;
  margin: 10px 20px;
}
.content-r {
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
</style>