<template>
  <div>
    <van-nav-bar
      title="青锋OA系统"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
    />
    <van-tabs v-model="active">
      <van-tab title="表单"
        ><k-form-build
          ref="kfb"
          :value="jsonData"
          :dynamicData="dynamicData"
          :disabled="true"
          v-show="initStatus"
      /></van-tab>
      <van-tab title="流程图"
        ><div class="divImg" @click="showFlowImg">
          <img :src="imageUrl" alt="流程图" class="image" /></div
      ></van-tab>
      <van-tab title="流程跟踪单"
        ><div v-show="actData.length > 0">
          <van-divider content-position="left">当前节点信息</van-divider>
          <div v-for="(item, index) in actData" @click="info(item)">
            <div class="main">
              <div class="head">
                <div class="title">节点：{{ item.name }}</div>
                <div class="time">{{ timeA(item.createTime) }}</div>
              </div>
              <div class="content">
                办理人：{{ item.assignee_name }}，办理状态：{{
                  statusA(item)
                }}，环节状态：{{ stateA(item) }}
              </div>
              <div style="clear: both"></div>
            </div>
            <div class="line5" style="clear: both"></div>
          </div>
        </div>
        <div v-show="hisData.length > 0">
          <van-divider content-position="left">办理过程记录</van-divider>
          <div v-for="(item, index) in hisData" @click="info(item)">
            <div class="main">
              <div class="head">
                <div class="title">节点：{{ item.activityName }}</div>
                <div class="time">环节类型：{{ typeA(item) }}</div>
              </div>
              <div class="content">
                <span v-if="item.assignee_name != undefined"
                  >办理人：{{ item.assignee_name }}&nbsp;&nbsp;</span
                >
                <span v-if="item.comment_msg != undefined"
                  >办理意见：{{ item.comment_msg }}&nbsp;&nbsp;</span
                >
                <span v-if="item.startTime != undefined"
                  >办理时间{{ item.startTime }}至{{
                    item.endTime
                  }}&nbsp;&nbsp;</span
                >
              </div>
              <div style="clear: both"></div>
            </div>
            <div class="line5" style="clear: both"></div>
          </div></div
      ></van-tab>
    </van-tabs>
  </div>
</template>

<script>
import axios from "axios";
import { ImagePreview } from "vant";
import { findActivityList, findActivityTaskList } from "@/api/activiti/index";
import {
  findVFormInfo,
  findVDataInfo,
  findVFormData,
} from "@/api/customize/vdata";
import { findDictionaryList } from "@/api/system/dictionary";
import { formatDate } from "@/utils/date.js";

export default {
  data() {
    return {
      active: 0,
      form: {},
      imageUrl: "",
      hisData: [],
      actData: [],
      jsonData: {},
      form_data: {},
      dynamicData: {},
      initStatus: false,
    };
  },
  mounted() {
    this.form = this.$route.params;
    this.flowchart(this.form);
    this.activityList();

    //查询菜单配置信息
    let data = this.form.businessKey.split(":");
    this.initData(data[2]);
  },
  updated() {
    this.form = this.$route.params;
    if (this.form.businessKey.indexOf("customize") != -1) {
      //查询菜单配置信息
      let data = this.form.businessKey.split(":");
      this.initFormData(data[2], data[3]);
    }
  },
  methods: {
    initData(menu_id) {
      findVFormInfo({ menu_id: menu_id }).then((response) => {
        this.form_data = response.data.data;
        this.dynamicData = response.data.object;
        this.jsonData = JSON.parse(this.form_data.table_content);
        this.jsonData.config.labelWidth = 36;
        this.jsonData.config.customStyle = "margin:20px";
        console.log(this.jsonData);
      });
    },
    initFormData(menu_id, id) {
      findVDataInfo({
        menu_id: menu_id,
        id: id,
      }).then((response) => {
        this.$refs.kfb.setData(response.data);
        this.initStatus = true;
      });
    },
    timeA(item) {
      return formatDate(item);
    },
    statusA(item) {
      if (item.assignee_name != null) {
        return "未完成";
      } else {
        if (item.claimTime == null) {
          return "未签收";
        } else {
          return "未完成（签收时间" + formatDate(item.claimTime);
          +"）";
        }
      }
    },
    stateA(item) {
      if (item.suspensionState == "1") {
        return "活跃状态";
      } else if (item.suspensionState == "2") {
        return "中断状态";
      }
    },
    typeA(item) {
      if (item.activityType == "startEvent") {
        return "开始节点";
      } else if (item.activityType == "endEvent") {
        return "结束节点";
      } else if (item.activityType == "userTask") {
        return "用户节点";
      } else {
        return "其他节点";
      }
    },
    activityList() {
      findActivityList({
        processInstanceId: this.form.processInstanceId,
      }).then((response) => {
        this.hisData = response.data.data.filter(
          (item) => item.endTime != null
        );
      });

      findActivityTaskList({
        processInstanceId: this.form.processInstanceId,
      }).then((response) => {
        this.actData = response.data.data;
      });
    },
    flowchart(item) {
      this.visible = true;
      axios({
        method: "GET",
        url:
          process.env.VUE_APP_API_BASE_URL +
          `/activiti/processTask/readResource?processInstanceId=` +
          item.processInstanceId,
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
.divImg {
  width: 100%;
  overflow: auto;
}
.image {
  width: 100% !important;
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
  margin: 0px 10px 0 15px;
  font-size: 12px;
  color: #969799;
  width: 92%;
  text-align: left;
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
.van-divider {
  margin: 8px 0;
}
.ant-form {
  width: 90%;
  margin: 10px 5%;
}
.ant-form-item-label {
  width: 36px !important;
  text-align: right !important;
}
.ant-form-item {
  width: 100px !important;
}
</style>