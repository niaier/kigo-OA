<template>
  <div>
    <div style="margin-bottom: 10px">
      <a-button type="primary" @click="check"> 审核 </a-button>
      <a-button style="margin-left: 10px" @click="backDown">驳回</a-button>
      <a-button style="margin-left: 10px" @click="addSign">任务委派</a-button>
    </div>
    <a-card
      style="width: 100%"
      :tab-list="cardList"
      :active-tab-key="activeKey"
      @tabChange="(key) => onTabChange(key, 'activeKey')"
    >
      <p v-show="activeKey === 'form'">
        <leaveInfo
          :businessKey="record.businessKey"
          v-if="record.businessKey.indexOf('gencode:leave') != -1"
        ></leaveInfo>
        <chuchaiInfo
          :businessKey="record.businessKey"
          v-if="record.businessKey.indexOf('gencode:chuchai') != -1"
        ></chuchaiInfo>
        <VDataCheck
          ref="checkRef"
          :record="record"
          v-if="record.businessKey.indexOf('customize') != -1"
          @finishResponse="finishResponse"
        ></VDataCheck>
        <!-- <k-form-build ref="kfb" :value="jsonData" v-if="record.businessKey.indexOf('customize') != -1" :dynamicData="dynamicData" /> -->
      </p>
      <p v-show="activeKey === 'flowchart'">
        <img :src="imageUrl" alt="流程图" class="image" />
      </p>
      <p v-show="activeKey === 'processTrack'">
        <a-card class="card" title="当前节点信息" :bordered="false">
          <a-table
            :columns="actColumns"
            :data-source="actData"
            :pagination="false"
            bordered
          >
          </a-table>
        </a-card>
        <a-card class="card" title="办理过程记录" :bordered="false">
          <a-table
            :columns="hisColumns"
            :data-source="hisData"
            :pagination="false"
            bordered
          >
          </a-table>
        </a-card>
      </p>
    </a-card>
  </div>
</template>
<script>
import {
  findActivityList,
  findActivityTaskList,
  completeCheck,
  rejectAnyNode,
  delegateTask,
} from "@/api/activiti/processTask";
import axios from "axios";
import VDataCheck from "@/views/customize/vdata/Check";
import leaveInfo from "@/views/gencode/leave/Info";
import chuchaiInfo from "@/views/gencode/chuchai/Info";

import AssignmentForm from "@/views/activiti/processTask/Assignment";
import RejectNodeForm from "@/views/activiti/processTask/RejectNode";
import SelectOneUser from "@/views/system/User/SelectOneUser";
import { formatDate } from "@/utils/date.js";
const actColumns = [
  {
    title: "序号",
    dataIndex: "",
    customRender: function (t, r, index) {
      return parseInt(index) + 1;
    },
  },
  {
    title: "环节名称",
    dataIndex: "name",
    customRender: (text, row, index) => {
      if (text != null) {
        return text;
      } else {
        return "";
      }
    },
  },
  {
    title: "当前办理人",
    dataIndex: "assignee_name",
    customRender: (text, row, index) => {
      if (text != null) {
        return text;
      } else {
        return "";
      }
    },
  },
  {
    title: "任务创建时间",
    dataIndex: "createTime",
    customRender: (text, row, index) => {
      if (text != "" && text != null) {
        return formatDate(text);
      }
    },
  },
  {
    title: "办理状态",
    dataIndex: "",
    customRender: (text, row, index) => {
      if (row.assignee_name != null) {
        return "未完成";
      } else {
        if (row.claimTime == null) {
          return "未签收";
        } else {
          return "未完成（签收时间" + formatDate(row.claimTime);
          +"）";
        }
      }
    },
  },
  {
    title: "环节状态",
    dataIndex: "suspensionState",
    ellipsis: true,
    customRender: (text, row, index) => {
      if (text == "1") {
        return "活跃状态";
      } else if (text == "2") {
        return "中断状态";
      }
    },
  },
  {
    title: "备注",
    dataIndex: "owner_name",
    ellipsis: true,
    customRender: (text, row, index) => {
      if (text != "" && text != null) {
        return "所属人：" + text;
      } else {
        return "";
      }
    },
  },
];

const hisColumns = [
  {
    title: "序号",
    dataIndex: "",
    customRender: function (t, r, index) {
      return parseInt(index) + 1;
    },
  },
  {
    title: "环节类型",
    dataIndex: "activityType",
    customRender: (text, row, index) => {
      if (text == "startEvent") {
        return "开始节点";
      } else if (text == "endEvent") {
        return "结束节点";
      } else if (text == "userTask") {
        return "用户节点";
      } else {
        return "其他节点";
      }
    },
  },
  {
    title: "环节名称",
    dataIndex: "activityName",
    customRender: (text, row, index) => {
      if (text != null) {
        return text;
      } else {
        return "";
      }
    },
  },
  {
    title: "办理人",
    dataIndex: "assignee_name",
    customRender: (text, row, index) => {
      if (text != null) {
        return text;
      } else {
        return "";
      }
    },
  },
  {
    title: "办理意见",
    dataIndex: "comment_msg",
    customRender: (text, row, index) => {
      if (text != null) {
        return text;
      } else {
        return "";
      }
    },
  },
  {
    title: "办理开始时间",
    dataIndex: "startTime",
    ellipsis: true,
    customRender: (text, row, index) => {
      if (text != "" && text != null) {
        return formatDate(text);
      }
    },
  },
  {
    title: "办理结束时间",
    dataIndex: "endTime",
    ellipsis: true,
    customRender: (text, row, index) => {
      if (text != "" && text != null) {
        return formatDate(text);
      }
    },
  },
];

export default {
  // 声明当前子组件接收父组件传递的属性
  props: {
    record: {
      type: Object,
      default: null,
    },
  },
  data() {
    return {
      labelCol: { span: 4 },
      wrapperCol: { span: 14 },
      other: "",
      mybol: false,
      headers: {
        authorization: "authorization-text",
      },
      uploading: false,
      form: {},
      cardList: [
        {
          key: "form",
          tab: "表单",
        },
        {
          key: "flowchart",
          tab: "流程图",
        },
        {
          key: "processTrack",
          tab: "流程跟踪单",
        },
      ],
      activeKey: "form",
      imageUrl: "",
      hisData: [],
      hisColumns,
      actData: [],
      actColumns,
    };
  },
  components: {
    AssignmentForm,
    RejectNodeForm,
    VDataCheck,
    leaveInfo,
    chuchaiInfo,
  },
  mounted() {
    if (this.record.id != undefined) {
      this.form = this.record;
    }
    this.flowchart(this.record);
    this.activityList();
  },
  methods: {
    onTabChange(key, activeKey) {
      this.activeKey = key;
    },
    resetForm() {
      this.$refs.ruleForm.resetFields();
    },
    onOk() {
      return new Promise((resolve) => {
        resolve(true);
      });
    },
    onCancel() {
      return new Promise((resolve) => {
        resolve(true);
      });
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
    flowchart(record) {
      this.visible = true;
      axios({
        method: "GET",
        url:
          process.env.VUE_APP_API_BASE_URL +
          `/activiti/processTask/readResource?processInstanceId=` +
          record.processInstanceId,
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
    check() {
      const that = this;
      let record = {
        processDefinitionKey: "",
        process_status: "1",
        taskId: this.record.id,
        processInstanceId: this.record.processInstanceId,
      };
      that.$refs.checkRef.getFormData().then((formatDate) => {
        this.$dialog(
          AssignmentForm,
          {
            record: { ...record, ...formatDate },
            on: {
              complete(data, approve_opinion) {
                if (that.record.businessKey.indexOf("customize") != -1) {
                  that.$refs.checkRef.onOk({
                    ...that.record,
                    nodeData: data,
                    approve_opinion: approve_opinion,
                  });
                } else {
                  completeCheck({
                    ...that.record,
                    nodeData: data,
                    approve_opinion: approve_opinion,
                  }).then((response) => {
                    that.$emit("finishResponse", response);
                  });
                }
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
    finishResponse(response) {
      this.$emit("finishResponse", response);
    },
    backDown() {
      let record = {
        taskId: this.record.id,
      };
      let that = this;
      this.$dialog(
        RejectNodeForm,
        {
          record,
          on: {
            complete(data) {
              if (that.record.businessKey.indexOf("customize") != -1) {
                that.$refs.checkRef.onRejectAnyNode({
                  ...that.record,
                  nodeData: data,
                  flowElementId: data.activityId,
                });
              } else {
                rejectAnyNode({
                  taskId: that.record.id,
                  flowElementId: data.activityId,
                }).then((response) => {
                  that.$emit("finishResponse", response);
                });
              }
            },
          },
        },
        {
          title: "流程驳回",
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
    },
    addSign() {
      const that = this;
      let record = {
        user_id: "",
        user_name: "",
      };
      this.$dialog(
        SelectOneUser,
        {
          record,
          on: {
            ok() {},
            cancel() {},
            close() {},
            initValue(value, type) {
              if (that.record.businessKey.indexOf("customize") != -1) {
                that.$refs.checkRef.onDelegateTask({
                  taskId: that.record.id,
                  userId: value.split(":")[0],
                  userName: value.split(":")[1],
                });
              } else {
                delegateTask({
                  taskId: that.record.id,
                  userId: value.split(":")[0],
                  userName: value.split(":")[1],
                }).then((response) => {
                  that.$emit("finishResponse", response);
                });
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
  },
};
</script>
<style scoped>
.ant-modal-body {
  padding: 10px;
  font-size: 14px;
  line-height: 1.5;
  word-wrap: break-word;
}
</style>