<template>
  <div>
    <van-nav-bar title="青锋系统" />
    <div class="title">欢迎登录青锋系统</div>
    <van-form class="form" @submit="onSubmit">
      <van-field
        v-model="username"
        name="username"
        placeholder="请输入登录名称"
        :rules="[{ required: true, message: '' }]"
      />
      <van-divider :style="{ borderColor: '#cacbcdcc', margin: '0px 10px' }" />
      <van-field
        v-model="password"
        name="password"
        type="password"
        placeholder="请输入登录密码"
        :rules="[{ required: true, message: '' }]"
      />
      <van-divider :style="{ borderColor: '#cacbcdcc', margin: '0px 10px' }" />
      <van-row>
        <van-col span="16"
          ><van-field
            v-model="code"
            name="code"
            placeholder="请输入验证码"
            :rules="[{ required: true, message: '' }]"
        /></van-col>
        <van-col span="8"
          ><img
            :src="imageCode"
            alt="codeImage"
            class="code-image"
            @click="getCodeImage"
        /></van-col>
      </van-row>
      <van-divider :style="{ borderColor: '#cacbcdcc', margin: '0px 10px' }" />
      <div style="margin: 16px">
        <van-button
          round
          block
          type="info"
          native-type="submit"
          :disabled="username == '' || password == '' || code == ''"
          >提交</van-button
        >
      </div>
    </van-form>
    <div class="fotter">登录代表同意 <a>用户协议、隐私政策</a></div>
  </div>
</template>

<script>
import { mapActions } from "vuex";
import { randomNum } from "@/utils";
import axios from "axios";
import { Notify } from "vant";
export default {
  data() {
    return {
      username: "",
      password: "",
      code: "",
      disabled: true,
      imageCode: "",
      codeUrl: process.env.VUE_APP_API_BASE_URL + `/auth/captcha`,
      randomId: randomNum(24, 16),
    };
  },
  mounted() {
    this.getCodeImage();
  },
  methods: {
    ...mapActions(["Login", "GetInfo", "Logout"]),
    onSubmit(values) {
      const { Login } = this;
      Login({ ...values, key: this.randomId })
        .then((res) => this.loginSuccess(res))
        .catch((err) => this.requestFailed(err))
        .finally(() => {});
    },
    loginSuccess(res) {
      if (res.status == 200) {
        const { GetInfo } = this;
        GetInfo({}).then((res) => {
          if (res.status == 200) {
            //执行跳转
            this.$router.push({ path: "/" });
            // 延迟 1 秒显示欢迎信息
            setTimeout(() => {
              Notify({ type: "success", message: "欢迎回来" });
            }, 1000);
          } else {
            // 延迟 1 秒显示欢迎信息
            setTimeout(() => {
              Notify({ type: "danger", message: "登录失败", duration: 2000 });
            }, 1000);
          }
        });
      }
    },
    requestFailed(err) {
      console.log(err);
      setTimeout(() => {
        Notify({
          type: "danger",
          message: "请求出现错误，请稍后再试",
          duration: 1000,
        });
      }, 1000);
    },
    getCodeImage() {
      axios({
        method: "GET",
        url: `${this.codeUrl}?key=${this.randomId}`,
        responseType: "arraybuffer",
      })
        .then((res) => {
          return (
            "data:image/png;base64," +
            btoa(
              new Uint8Array(res.data).reduce(
                (data, byte) => data + String.fromCharCode(byte),
                ""
              )
            )
          );
        })
        .then((res) => {
          this.imageCode = res;
        })
        .catch((e) => {
          if (e.toString().indexOf("429") !== -1) {
            this.$notification.warning({
              message: "请求超时,请稍后重试",
              description: res.msg,
            });
          } else {
            this.$notification.warning({
              message: "获取图片验证码失败",
              description: res.msg,
            });
          }
        });
    },
  },
};
</script>

<style scoped>
.title {
  height: 140px;
  line-height: 140px;
  text-align: center;
  font-size: 30px;
  font-weight: 600;
  background: linear-gradient(to right, rgb(0, 186, 189), rgb(239, 17, 255));
  -webkit-background-clip: text;
  color: transparent;
}
.form {
  margin-top: 20px;
}
.fotter {
  margin: 20px;
  text-align: center;
}
</style>