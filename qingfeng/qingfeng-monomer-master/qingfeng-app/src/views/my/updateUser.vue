<template>
  <div class="main">
    <van-nav-bar
      title="青锋OA系统"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
    />
    <van-form @submit="onSubmit">
      <van-field
        v-model="form.name"
        name="name"
        placeholder="请输入姓名"
        :rules="[{ required: true, message: '' }]"
      />
      <van-divider :style="{ borderColor: '#cacbcdcc', margin: '0px 10px' }" />
      <van-field name="sex" placeholder="请选择性别">
        <template #input>
          <van-radio-group v-model="form.sex" direction="horizontal">
            <van-radio name="1">男</van-radio>
            <van-radio name="2">女</van-radio>
          </van-radio-group>
        </template>
      </van-field>

      <van-divider :style="{ borderColor: '#cacbcdcc', margin: '0px 10px' }" />
      <van-field v-model="form.phone" name="phone" placeholder="请输入手机号" />
      <van-divider :style="{ borderColor: '#cacbcdcc', margin: '0px 10px' }" />
      <van-field v-model="form.email" name="email" placeholder="请输入邮箱" />
      <van-divider :style="{ borderColor: '#cacbcdcc', margin: '0px 10px' }" />
      <van-field
        readonly
        clickable
        name="birth_date"
        :value="form.birth_date"
        placeholder="请输入出生日期"
        @click="showPicker = true"
      />
      <van-popup v-model="showPicker" position="bottom" v-show="showPicker">
        <van-datetime-picker
          type="date"
          @confirm="onConfirm"
          @cancel="showPicker = false"
        />
      </van-popup>
      <van-divider :style="{ borderColor: '#cacbcdcc', margin: '0px 10px' }" />
      <van-field v-model="form.motto" name="motto" placeholder="请输入座右铭" />
      <van-divider :style="{ borderColor: '#cacbcdcc', margin: '0px 10px' }" />
      <van-field
        v-model="form.live_address"
        name="live_address"
        placeholder="请输入居住地址"
      />
      <van-divider :style="{ borderColor: '#cacbcdcc', margin: '0px 10px' }" />
      <van-field
        v-model="form.birth_address"
        name="birth_address"
        placeholder="请输入出生地址"
      />
      <van-divider :style="{ borderColor: '#cacbcdcc', margin: '0px 10px' }" />
      <van-field name="头像上传">
        <template #input>
          <van-uploader
            v-model="fileList"
            multiple
            :max-count="1"
            :after-read="afterRead"
            :before-delete="delUpload"
          />
        </template>
      </van-field>
      <van-divider :style="{ borderColor: '#cacbcdcc', margin: '0px 10px' }" />
      <van-field v-model="form.remark" name="remark" placeholder="请输入备注" />
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
import { getLoginUser, updateUser } from "@/api/auth/login";
import { upload } from "@/api/common/upload";
import { Notify } from "vant";
export default {
  data() {
    return {
      form: {
        id: "",
        name: "",
        sex: "",
        phone: "",
        email: "",
        birth_date: "",
        motto: "",
        live_address: "",
        birth_address: "",
        remark: "",
        fileIds: "",
      },
      showPicker: false,
      imageUrl: "",
      fileList: [],
    };
  },
  mounted() {
    this.getUserInfo();
  },
  methods: {
    getUserInfo() {
      getLoginUser().then((response) => {
        this.form = response.data.data;
        console.log(response)
        this.imageUrl = response.data.data.imageUrl;
        if (this.imageUrl != "" && this.imageUrl != undefined) {
          this.fileList = [{ url: this.imageUrl }];
        }
      });
    },
    afterRead(file) {
      // 此时可以自行将文件上传至服务器
      this.uploadImage(file);
    },
    // 上传头像
    uploadImage(file) {
      this.loading = true;
      const formData = new FormData();
      formData.append("file", file.file);
      upload(formData).then(
        (res) => {
          if (res.status == 200) {
            this.loading = false;
            this.imageUrl = res.data.data.show_file_path;
            this.form.fileIds = res.data.data.id;
          }
        },
        (err) => {
          this.avatarLoading = false;
        }
      );
    },
    delUpload(file) {
      this.fileList = [];
      this.form.fileIds = "";
    },
    onSubmit(values) {
      updateUser(this.form).then((response) => {
        if (response.status == 200) {
          Notify({ type: "success", message: "信息修改成功。" });
          this.$router.go(-1);
        } else {
          Notify({ type: "danger", message: "信息修改失败。" });
        }
      });
    },
    onClickLeft() {
      this.$router.go(-1);
    },
    onConfirm(time) {
      const d = new Date(time);
      this.form.birth_date =
        d.getFullYear() +
        "-" +
        this.p(d.getMonth() + 1) +
        "-" +
        this.p(d.getDate());
      this.showPicker = false;
    },
    p(s) {
      return s < 10 ? "0" + s : s;
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
</style>