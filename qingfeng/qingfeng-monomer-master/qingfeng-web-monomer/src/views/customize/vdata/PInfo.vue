<template>
  <a-card
    style="width: 100%"
    :tab-list="cardList"
    :active-tab-key="activeKey"
    @tabChange="(key) => onTabChange(key, 'activeKey')"
  >
    <div v-if="activeKey === 'form'">
      <div class="spin" v-show="!initStatus"><a-spin size="large" /></div>
      <k-form-build
        ref="kfb"
        :value="jsonData"
        :dynamicData="dynamicData"
        v-show="initStatus"
      />
    </div>
    <div v-else-if="activeKey === 'flowchart'">
      <img :src="imageUrl" alt="流程图" class="image" />
    </div>
    <div v-else-if="activeKey === 'processTrack'">
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
    </div>
  </a-card>
</template>
<script>
import {
  findActivityList,
  findActivityTaskList,
} from "@/api/activiti/processTask";
import { findVFormInfo, findVDataInfo } from "@/api/customize/vdata";
import axios from "axios";
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
      jsonData: {},
      form_data: {},
      dynamicData: this.record.dynamicData,
      form: {},
      initStatus: false,

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
  components: {},
  mounted() {
    if (this.record.id != undefined) {
      this.form = this.record;
      //查询菜单配置信息
      this.jsonData = JSON.parse(this.record.table_content);
      this.initStatus = true;
    } else {
      if (this.record.businessKey.indexOf("customize") != -1) {
        //查询菜单配置信息
        let data = this.record.businessKey.split(":");
        this.initData(data[2]);
      }
    }
    //初始化流程图
    this.flowchart(this.record);
    //查询流程办理节点信息
    this.activityList();
  },
  updated() {
    if (this.record.id != undefined) {
      //查询编辑项内容
      if (this.form.id != "" && this.form.id != undefined) {
        this.$refs.kfb.setData(this.form);
        this.$refs.kfb.disableAll();
      } else {
        this.initStatus = true;
      }
    } else {
      if (this.record.businessKey.indexOf("customize") != -1) {
        //查询菜单配置信息
        let data = this.record.businessKey.split(":");
        this.initFormData(data[2], data[3]);
      }
    }
  },
  methods: {
    initData(menu_id) {
      findVFormInfo({ menu_id: menu_id }).then((response) => {
        this.form_data = response.data.data;
        this.dynamicData = response.data.object;
        this.jsonData = JSON.parse(this.form_data.table_content);
      });
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
<style scoped>
.spin {
  text-align: center;
  border-radius: 4px;
  margin-bottom: 20px;
  padding: 30px 50px;
  margin: 40px 0;
}
</style>
