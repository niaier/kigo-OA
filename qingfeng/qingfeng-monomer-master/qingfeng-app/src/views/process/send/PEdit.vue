<template>
  <div>
    <van-nav-bar
      title="青锋OA系统"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
    />
    <div class="line5"></div>
    <div style="margin: 5px 10px">
      <van-button type="info" size="small" @click="send">流程发送</van-button>
    </div>
    <van-tabs v-model="active">
      <van-tab title="表单">
        <van-divider
          :style="{
            borderColor: '#1989fa',
            margin: '10px 0px 0px 0px',
          }"
        >
        </van-divider>
        <k-form-build
          ref="kfb"
          :value="jsonData"
          :dynamicData="dynamicData"
          @change="handleChange"
        />
      </van-tab>
      <van-tab title="流程图">
        <van-divider
          :style="{
            borderColor: '#1989fa',
            margin: '10px 0px 0px 0px',
          }"
        >
        </van-divider>
        <div class="divImg" @click="showFlowImg">
          <img :src="imageUrl" alt="流程图" class="image" />
        </div>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script>
import axios from "axios";
import { ImagePreview } from "vant";
import { findActivityList, findActivityTaskList } from "@/api/activiti/index";
import { findVFormInfo, saveOrUpdate } from "@/api/customize/vdata";
import { findDictionaryList } from "@/api/system/dictionary";
import { formatDate } from "@/utils/date.js";
import AssignmentForm from "../check/Assignment.vue";
import { Notify } from "vant";
import store from "@/store";
export default {
  data() {
    return {
      active: 0,
      jsonData: {},
      form_data: {},
      dynamicData: {},
      form: { id: "" },
      record: {},
      initStatus: false,
      imageUrl: "",
      hideFieldKey: "",
    };
  },
  mounted() {
    this.record = this.$route.params;
    this.flowchart(this.record);
    this.initData(this.record.menu_id);
  },
  methods: {
    initData(menu_id) {
      findVFormInfo({ menu_id: menu_id, formKey: this.record.formKey }).then(
        (response) => {
          this.form_data = response.data.data;
          this.table_id = this.form_data.id;
          this.dynamicData = response.data.object;
          this.jsonData = JSON.parse(this.form_data.table_content);
          this.jsonData.config.labelWidth = 36;
          this.jsonData.config.customStyle = "margin:20px";
        }
      );
    },
    send() {
      const that = this;
      let record = {
        processDefinitionKey: this.record.process_key,
        process_status: "0",
        taskId: "",
        processInstanceId: "",
      };
      this.$refs.kfb
        .getData()
        .then((values) => {
          for (let k in values) {
            if (Array.isArray(values[k]) && values[k].length > 0) {
              if (
                Object.prototype.toString.call(values[k][0]) ==
                "[object String]"
              ) {
                values[k] = values[k].join(",");
              } else if (
                Object.prototype.toString.call(values[k][0]) ==
                "[object Object]"
              ) {
                for (let j in values[k]) {
                  if (Array.isArray(values[k][j]) && values[k][j].length > 0) {
                    values[k][j] = values[k][j].join(",");
                  }
                }
              }
            }
          }
          this.$dialog(
            AssignmentForm,
            {
              record: { ...values, ...record },
              on: {
                complete(data, approve_opinion) {
                  saveOrUpdate({
                    ...values,
                    id: that.form.id,
                    table_id: that.record.table_id,
                    menu_id: that.record.menu_id,
                    submitType: "2",
                    nodeData: data,
                    approve_opinion: approve_opinion,
                    hideFieldKey: that.hideFieldKey,
                  }).then((response) => {
                    if (response.data.success) {
                      Notify({ type: "success", message: "任务办理成功" });
                      that.$router.push({
                        name: "process",
                        params: { index: 2 },
                      });
                      store.commit("SET_TABBAR", 1);
                    } else {
                      Notify({ type: "danger", message: "response.data.msg" });
                    }
                  });
                },
                ok() {},
                cancel() {},
                close() {},
              },
            },
            // modal props
            {
              title: "任务办理",
              width: 800,
              centered: true,
              maskClosable: false,
              okText: "确定",
              cancelText: "取消",
              bodyStyle: {
                backgroundColor: "#f0f2f5",
                padding: "16px",
                border: "#FFFFFF 4px solid",
              },
            }
          );
          return true;
        })
        .catch(() => {});
    },
    handleChange(value, key, record) {
      if (record.linkFieldSet != undefined) {
        record.linkFieldSet.forEach((item) => {
          if (item.linkField == value) {
            let hideLinkField = item.hideLinkField;
            let showLinkField = item.showLinkField;
            if (hideLinkField != undefined) {
              this.$refs.kfb.hide(hideLinkField.split(","));
            }
            if (showLinkField != undefined) {
              this.$refs.kfb.enable(showLinkField.split(","));
            }
          }
        });
      }
    },
    //查询流程图
    flowchart(record) {
      this.visible = true;
      axios({
        method: "GET",
        url:
          process.env.VUE_APP_API_BASE_URL +
          `/activiti/processDefinition/readResource?procDefinitionKey=` +
          record.process_key,
        responseType: "arraybuffer",
      })
        .then((res) => {
          return (
            "data:image/svg+xml;base64," +
            btoa(
              new Uint8Array(res.data).reduce(
                (data, byte) => data + String.fromCharCode(byte),
                ""
              )
            )
          );
        })
        .then((res) => {
          this.imageUrl = res;
        })
        .catch((e) => {
          if (e.toString().indexOf("429") !== -1) {
            this.$notification.warning({
              message: "请求超时,请稍后重试",
              description: "",
            });
          } else {
            this.$notification.warning({
              message: "获取流程图失败",
              description: "",
            });
          }
        });
    },
    showFlowImg() {
      ImagePreview([this.imageUrl]);
    },
    onClickLeft() {
      this.$router.go(-1);
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
.divImg {
  width: 100%;
  overflow: auto;
}
.image {
  width: 100% !important;
}
</style>