<template>
  <a-card
    style="width: 100%"
    :tab-list="cardList"
    :active-tab-key="activeKey"
    @tabChange="(key) => onTabChange(key, 'activeKey')"
  >
    <p v-if="activeKey === 'form'">
      <leaveInfo
        :businessKey="record.businessKey"
        v-if="record.businessKey.indexOf('gencode:leave') != -1"
      ></leaveInfo>
      <chuchaiInfo
        :businessKey="record.businessKey"
        v-if="record.businessKey.indexOf('gencode:chuchai') != -1"
      ></chuchaiInfo>
      <VDataInfo
        :record="record"
        v-if="record.businessKey.indexOf('customize') != -1"
      ></VDataInfo>
    </p>
    <p v-else-if="activeKey === 'flowchart'">
      <img :src="imageUrl" alt="流程图" class="image" />
    </p>
    <p v-else-if="activeKey === 'processTrack'">
      <a-card
        class="card"
        title="当前节点信息"
        :bordered="false"
        v-show="actData.length > 0"
      >
        <a-table
          :columns="actColumns"
          :data-source="actData"
          :pagination="false"
          bordered
        >
        </a-table>
      </a-card>
      <a-card
        class="card"
        title="办理过程记录"
        :bordered="false"
        v-show="hisData.length > 0"
      >
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
</template>
<script>
import {
  findActivityList,
  findActivityTaskList,
} from "@/api/activiti/processTask";
import axios from "axios";
import VDataInfo from "@/views/customize/vdata/Info";
import leaveInfo from "@/views/gencode/leave/Info";
import chuchaiInfo from "@/views/gencode/chuchai/Info";

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
    VDataInfo,
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
  },
};
</script>
