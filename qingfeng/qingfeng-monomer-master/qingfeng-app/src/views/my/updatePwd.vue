<template>
  <div class="main">
    <van-nav-bar
      title="青锋OA系统"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
    />
    <div class="title">密码重置</div>
    <van-form @submit="onSubmit">
      <van-field
        v-model="old_password"
        type="password"
        name="old_password"
        placeholder="请输入旧密码"
        :rules="[{ required: true, message: '' }]"
      />
      <van-divider :style="{ borderColor: '#cacbcdcc', margin: '0px 10px' }" />
      <van-field
        v-model="login_password"
        type="password"
        name="login_password"
        placeholder="请输入登录密码"
        :rules="[{ required: true, message: '' }]"
      />
      <van-divider :style="{ borderColor: '#cacbcdcc', margin: '0px 10px' }" />
      <van-field
        v-model="confirm_password"
        type="password"
        name="confirm_password"
        placeholder="请输入确认密码"
        :rules="[{ required: true, message: '' }]"
      />
      <van-divider :style="{ borderColor: '#cacbcdcc', margin: '0px 10px' }" />
      <div style="margin: 16px">
        <van-button round block type="info" native-type="submit"
          >提交</van-button
        >
      </div>
    </van-form>
    <div class="line" style="clear: both"></div>
  </div>
</template>

<script>
import { updatePwd } from "@/api/auth/login";
import { Notify } from "vant";
export default {
  data() {
    return {
      old_password: "",
      login_password: "",
      confirm_password: "",
    };
  },
  methods: {
    onSubmit(values) {
      if (this.login_password == this.confirm_password) {
        updatePwd(values).then((response) => {
          if (response.status == 200) {
            Notify({ type: "success", message: response.data.data.msg });
            this.$router.go(-1);
          } else {
            Notify({ type: "danger", message: response.data.data.msg });
          }
        });
      } else {
        Notify({
          message: "两次密码输入不一致",
          color: "#ad0000",
          background: "#ffe1e1",
        });
      }
    },
    onClickLeft() {
      this.$router.go(-1);
    },
  },
};
</script>

<style scoped>
.main {
  background-color: rgb(237, 237, 237);
  height: calc(100vh);
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
.title {
  height: 80px;
  line-height: 80px;
  text-align: center;
  font-size: 30px;
  font-weight: 600;
  background: linear-gradient(to right, rgb(0, 186, 189), rgb(239, 17, 255));
  -webkit-background-clip: text;
  color: transparent;
}
</style>