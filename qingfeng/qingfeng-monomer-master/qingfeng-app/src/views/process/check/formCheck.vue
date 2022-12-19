<template>
  <div>
    <van-nav-bar
      title="青锋OA系统"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
    />
    <!-- <div class="line"></div> -->
    <div class="divBtn">
      <van-button
        class="button"
        plain
        size="small"
        type="info"
        @click="claim()"
        v-show="record.status == '0'"
        >签&nbsp;&nbsp;收</van-button
      >
      <van-button
        class="button"
        plain
        size="small"
        type="info"
        @click="check()"
        v-show="record.status == '1'"
        >审&nbsp;&nbsp;核</van-button
      >
      <van-button
        class="button"
        plain
        size="small"
        type="info"
        v-show="record.status == '1'"
        @click="reject()"
        >驳&nbsp;&nbsp;回</van-button
      >
      <van-button
        class="button"
        plain
        size="small"
        type="info"
        v-show="record.status == '1'"
        @click="addSign()"
        >委&nbsp;&nbsp;派</van-button
      >
    </div>
    <div class="line"></div>
    <van-tabs v-model="active">
      <van-tab title="表单"
        ><div class="line" style="margin-top: 10px"></div>
        <k-form-build
          ref="kfb"
          :value="jsonData"
          :dynamicData="dynamicData"
          v-show="visable"
      /></van-tab>
      <van-tab title="流程图"
        ><div class="line" style="margin-top: 10px"></div>
        <div class="divImg" @click="showFlowImg">
          <img :src="imageUrl" alt="流程图" class="image" /></div
      ></van-tab>
      <van-tab title="流程跟踪单"
        ><div class="line" style="margin-top: 10px"></div>
        <div v-show="actData.length > 0">
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
import {
  findActivityList,
  findActivityTaskList,
  findFormParams,
  claimTask,
} from "@/api/activiti/index";
import {
  findVFormInfo,
  saveOrUpdate,
  findVDataInfo,
  findVFormData,
  rejectAnyNod,
  delegateTask,
} from "@/api/customize/vdata";
import { findDictionaryList } from "@/api/system/dictionary";
import { formatDate } from "@/utils/date.js";
import { Notify } from "vant";
import rejectNode from "./rejectNode.vue";
import SOneUser from "../../system/User/SOneUser.vue";
import AssignmentForm from "./Assignment.vue";
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
      menu_id: "",
      table_id: "",
      hideFieldKey: "",
      visable: false,
      record: {},
      rejectData: {},
      signUser: "",
    };
  },
  components: {
    rejectNode,
    SOneUser,
  },
  mounted() {
    this.record = this.$route.params;
    console.log("----------------");
    console.log(this.record);
    let data = this.record.businessKey.split(":");
    this.initData(data[2]);
    this.menu_id = data[2];
    this.flowchart(this.record);
    this.activityList();
  },
  updated() {
    this.record = this.$route.params;
    //查询编辑项内容
    let data = this.record.businessKey.split(":");
    this.initFormData(data[2], data[3]);
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
    initFormData(menu_id, id) {
      findVDataInfo({
        table_id: this.record.formKey,
        menu_id: menu_id,
        id: id,
      }).then((response) => {
        this.form = response.data;
        this.$refs.kfb.setData(response.data);
        this.initStatus = true;
        this.$refs.kfb.disableAll();
        findFormParams({
          nodeType: "nodeType",
          taskId: this.record.id,
        }).then((response) => {
          this.visable = true;
          let data = response.data.data;
          if (data.writeFieldKey != undefined) {
            this.$refs.kfb.enable(data.writeFieldKey.split(","));
          }
          if (data.hideFieldKey != undefined) {
            this.$refs.kfb.hide(data.hideFieldKey.split(","));
            this.hideFieldKey = data.hideFieldKey;
          }
        });
      });
    },
    onOk(data) {
      return new Promise((resolve) => {
        // 使用getData函数获取数据
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
                    if (
                      Array.isArray(values[k][j]) &&
                      values[k][j].length > 0
                    ) {
                      values[k][j] = values[k][j].join(",");
                    }
                  }
                }
              }
            }
            saveOrUpdate({
              taskId: this.record.id,
              approve_opinion: data.approve_opinion,
              nodeData: data.nodeData,
              submitType: "3",
              ...values,
              id: this.form.id,
              table_id: this.table_id,
              menu_id: this.menu_id,
              hideFieldKey: this.hideFieldKey,
            }).then((response) => {
              if (response.data.success) {
                Notify({ type: "success", message: "任务办理成功" });
                this.$router.push({ name: "process" });
              } else {
                Notify({ type: "danger", message: "response.data.msg" });
              }
              resolve(true);
            });
          })
          .catch(() => {});
      });
    },

    check() {
      const that = this;
      let record = {
        processDefinitionKey: "",
        process_status: "1",
        taskId: this.record.id,
        processInstanceId: this.record.processInstanceId,
      };
      this.$refs.kfb.getData().then((values) => {
        for (let k in values) {
          if (Array.isArray(values[k]) && values[k].length > 0) {
            if (
              Object.prototype.toString.call(values[k][0]) == "[object String]"
            ) {
              values[k] = values[k].join(",");
            } else if (
              Object.prototype.toString.call(values[k][0]) == "[object Object]"
            ) {
              for (let j in values[k]) {
                if (Array.isArray(values[k][j]) && values[k][j].length > 0) {
                  values[k][j] = values[k][j].join(",");
                }
              }
            }
          }
        }
        that.$dialog(
          AssignmentForm,
          {
            record: { ...record, ...values },
            on: {
              complete(data, approve_opinion) {
                that.onOk({ nodeData: data, approve_opinion: approve_opinion });
              },
            },
          },
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
      });
    },

    reject() {
      const that = this;
      let record = {
        taskId: this.record.id,
      };
      that.$dialog(
        rejectNode,
        {
          record,
          on: {
            ok() {},
            cancel() {},
            close() {},
            initValue(node) {
              that.rejectData = node;
              that.onRejectAnyNode();
            },
          },
        },
        {
          title: "选择退回节点",
          width: 800,
          height: 500,
          centered: true,
          maskClosable: false,
          okText: "确认",
          cancelText: "取消",
        }
      );
    },
    onRejectAnyNode() {
      return new Promise((resolve) => {
        // 使用getData函数获取数据
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
                    if (
                      Array.isArray(values[k][j]) &&
                      values[k][j].length > 0
                    ) {
                      values[k][j] = values[k][j].join(",");
                    }
                  }
                }
              }
            }
            rejectAnyNod({
              taskId: this.record.id,
              flowElementId: this.rejectData.activityId,
              ...values,
              id: this.form.id,
              table_id: this.table_id,
              menu_id: this.menu_id,
              hideFieldKey: this.hideFieldKey,
            }).then((response) => {
              if (response.data.success) {
                this.$router.push({ name: "formCheck", params: this.record });
                resolve(true);
              } else {
                this.$message.warning(response.data.msg);
              }
            });
          })
          .catch(() => {});
      });
    },

    //任务委派
    addSign() {
      const that = this;
      let record = {
        user_id: "",
        user_name: "",
      };
      that.$dialog(
        SOneUser,
        {
          record,
          on: {
            ok() {},
            cancel() {},
            close() {},
            initValue(user_id) {
              let signUser = user_id;
              console.log(signUser);
              if (signUser != undefined && signUser.indexOf(":") != -1) {
                let data = signUser.split(":");
                that.onDelegateTask({ userId: data[0], userName: data[1] });
              }
            },
          },
        },
        {
          title: "操作",
          width: 800,
          height: 500,
          centered: true,
          maskClosable: false,
          okText: "确认",
          cancelText: "取消",
        }
      );
    },
    onDelegateTask(data) {
      console.log(data);
      return new Promise((resolve) => {
        // 使用getData函数获取数据
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
                    if (
                      Array.isArray(values[k][j]) &&
                      values[k][j].length > 0
                    ) {
                      values[k][j] = values[k][j].join(",");
                    }
                  }
                }
              }
            }
            delegateTask({
              taskId: this.record.id,
              userId: data.userId,
              userName: data.userName,
              ...values,
              id: this.form.id,
              table_id: this.table_id,
              menu_id: this.menu_id,
              hideFieldKey: this.hideFieldKey,
            }).then((response) => {
              if (response.data.success) {
                Notify({ type: "success", message: "任务委派成功" });
                this.$router.push({ name: "process" });
              } else {
                Notify({ type: "danger", message: "response.data.msg" });
              }
              resolve(true);
            });
          })
          .catch(() => {});
      });
    },
    //签收
    claim() {
      const that = this;
      that.$confirm({
        title: "提示",
        content: (h) => <div style="color:red;">确定要签收数据吗</div>,
        okText: "确认",
        cancelText: "取消",
        onOk() {
          claimTask({ taskId: this.record.id }).then((response) => {
            if (response.data.success) {
              Notify({ type: "success", message: "任务签收成功" });
              that.record.status = "1";
            } else {
              Notify({ type: "danger", message: "response.data.msg" });
            }
          });
        },
        onCancel() {
          console.log("Cancel");
        },
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
        processInstanceId: this.record.processInstanceId,
      }).then((response) => {
        this.hisData = response.data.data.filter(
          (item) => item.endTime != null
        );
      });

      findActivityTaskList({
        processInstanceId: this.record.processInstanceId,
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
  height: 4px;
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
.divBtn {
  margin: 5px 10px;
}
.button {
  margin: 0 2px;
}
</style>