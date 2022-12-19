<template>
  <div>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="24">
            <a-col :md="6" :sm="24">
              <a-form-item label="任务名称">
                <a-input v-model="queryParam.name" placeholder="" />
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="任务定义key">
                <a-input v-model="queryParam.key" placeholder="" />
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="状态">
                <a-select
                  v-model="queryParam.status"
                  placeholder="请选择"
                  default-value=""
                >
                  <a-select-option value="">请选择</a-select-option>
                  <a-select-option value="0">待签收</a-select-option>
                  <a-select-option value="1">待完成</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-button
                type="primary"
                @click="fetch({ pageSize: 10, ...queryParam })"
                >查询</a-button
              >
              <a-button
                style="margin-left: 8px"
                @click="() => (queryParam = {})"
                >重置</a-button
              >
            </a-col>
          </a-row>
        </a-form>
      </div>
      <div style="margin: 0 0 10px 0">
        <a-space size="small"> </a-space>
      </div>

      <a-table
        :row-selection="rowSelection"
        :columns="columns"
        :data-source="data"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
        bordered
      >
        <template
          v-for="col in ['name', 'age', 'address']"
          :slot="col"
          slot-scope="text, record, index"
        >
          <div :key="col">
            {{ text }}
          </div>
        </template>
        <template slot="operation" slot-scope="text, record, index">
          <div class="editable-row-operations">
            <a-space :size="4">
              <div slot="actions">
                <a-space :size="4">
                  <a-button
                    size="small"
                    type="primary"
                    v-action:processTask:claimTask
                    v-show="record.status == '0'"
                    @click="() => claim(record)"
                    >签收任务</a-button
                  >
                  <a-button
                    size="small"
                    type="primary"
                    v-action:processTask:completeTask
                    v-show="record.status == '1'"
                    @click="() => complete(record)"
                    >完成任务</a-button
                  >
                  <a-button
                    size="small"
                    type="primary"
                    v-action:processTask:flowchart
                    @click="() => flowchart(record)"
                    >流程图</a-button
                  >
                  <a-button
                    size="small"
                    type="primary"
                    @click="() => formInfo(record)"
                    >详情</a-button
                  >
                </a-space>
              </div>
            </a-space>
          </div>
        </template>
      </a-table>
    </a-card>
    <a-modal width="800px" v-model="visible" title="流程图" on-ok="handleOk">
      <template slot="footer">
        <a-button key="back" @click="handleCancel"> 关闭 </a-button>
      </template>
      <img :src="imageUrl" alt="流程图" class="image" />
    </a-modal>
    <a-modal
      title="完成任务"
      :visible="complete_visible"
      :confirm-loading="confirmLoading"
      @ok="completeTask"
      @cancel="handleCancel"
      ok-text="确认"
      cancel-text="取消"
      :destroyOnClose="true"
    >
      <a-textarea v-model="check_opinion" placeholder="请输入审批意见" />
    </a-modal>
    <a-modal
      v-model="check_visible"
      title="审核"
      :footer="null"
      width="1000px"
      on-ok="handleOk"
      :destroyOnClose="true"
    >
      <template slot="footer"> </template>
      <form-check
        :record="record"
        @finishResponse="finishResponse"
      ></form-check>
    </a-modal>
  </div>
</template>
<script>
import { mapState } from "vuex";
import {
  findTaskListPage,
  claimTask,
  completeTask,
  shutdownTask,
} from "@/api/activiti/processTask";
import axios from "axios";
import { formatDate } from "@/utils/date.js";
import FormInfo from "../../processTask/FormInfo";
import FormCheck from "../../processTask/FormCheck";
import VDataInfo from "@/views/customize/vdata/PInfo";
const columns = [
  {
    title: "标题",
    dataIndex: "procdef_name",
    sorter: true,
    // ellipsis: true,
    customRender: (text, row, index) => {
      return (
        row.start_user_name +
        "与" +
        formatDate(row.start_time) +
        "发起【" +
        text +
        "】的流程"
      );
    },
  },
  {
    title: "当前任务名称",
    dataIndex: "name",
    sorter: true,
    // ellipsis: true,
  },
  {
    title: "当前办理人",
    dataIndex: "assignee_name",
    customRender: (text, row, index) => {
      if (row.status == "0") {
        return "未签收";
      } else if (row.status == "1") {
        return text;
      }
    },
  },
  {
    title: "业务类型",
    dataIndex: "businessKey",
    customRender: (text, row, index) => {
      if (text.indexOf("gencode") != -1) {
        return "代码生成";
      } else if (text.indexOf("customize") != -1) {
        return "自定义表单";
      } else {
        return "普通表单";
      }
    },
  },
  {
    title: "创建时间",
    dataIndex: "createTime",
    customRender: (text, row, index) => {
      return formatDate(text);
    },
  },
  {
    title: "操作",
    dataIndex: "operation",
    scopedSlots: { customRender: "operation" },
    width: "180px",
  },
];

export default {
  data() {
    return {
      data: [],
      pagination: {},
      loading: false,
      columns,
      selectedRows: [],
      queryParam: {
        name: "",
        key: "",
        status: "",
      },
      visible: false,
      imageUrl: "",
      complete_visible: false,
      confirmLoading: false,
      check_opinion: "",
      completeRecord: "",

      check_visible: false,
      record: {},
      checkType: "gencode",
    };
  },
  components: { FormInfo, FormCheck, VDataInfo },
  computed: {
    rowSelection() {
      return {
        onChange: (selectedRowKeys, selectedRows) => {
          this.selectedRows = selectedRows;
          console.log(
            `selectedRowKeys: ${selectedRowKeys}`,
            "selectedRows: ",
            selectedRows
          );
        },
      };
    },
  },
  mounted() {
    this.fetch({ pageSize: 10 });
  },
  methods: {
    handleTableChange(pagination, filters, sorter) {
      const pager = { ...this.pagination };
      pager.current = pagination.current;
      this.pagination = pager;
      this.fetch({
        pageSize: pagination.pageSize,
        pageNum: pagination.current,
        sortField: sorter.field,
        sortOrder: sorter.order,
        ...filters,
      });
    },
    fetch(params = {}) {
      this.loading = true;
      findTaskListPage({ ...params, type: "userTask" }).then((response) => {
        const pagination = { ...this.pagination };
        // Read total count from server
        // pagination.total = data.totalCount;
        pagination.total = response.data.count;
        this.loading = false;
        this.data = response.data.data;
        this.pagination = pagination;
      });
    },
    //签收
    claim(record) {
      const that = this;
      that.$confirm({
        title: "提示",
        content: (h) => <div style="color:red;">确定签收任务吗？</div>,
        okText: "确认",
        cancelText: "取消",
        onOk() {
          claimTask({ taskId: record.id }).then((response) => {
            if (response.data.success) {
              that.$success({
                title: "提示：",
                content: "任务签收成功",
                okText: "确认",
              });
              that.fetch({ pageSize: 10 });
            } else {
              this.$error({
                title: "提示：",
                content: response.data.msg,
                okText: "确认",
              });
            }
          });
        },
        onCancel() {
          console.log("Cancel");
        },
      });
    },
    //完成
    complete(record) {
      if (record.businessKey.indexOf("gencode") != -1) {
        this.check_visible = true;
        this.record = record;
        this.checkType = "gencode";
        // this.$dialog(
        //   FormCheck,
        //   {
        //     record,
        //     on: {
        //       ok() {},
        //       cancel() {},
        //       close() {},
        //     },
        //   },
        //   {
        //     title: "审核",
        //     width: 1000,
        //     centered: true,
        //     maskClosable: false,
        //     footer: "",
        //   }
        // );
      } else if (record.businessKey.indexOf("customize") != -1) {
        this.check_visible = true;
        this.record = record;
        this.checkType = "customize";
      } else {
        this.check_opinion = "";
        this.completeRecord = record;
        this.complete_visible = true;
      }
    },
    finishResponse(response) {
      this.check_visible = false;
      this.fetch({ pageSize: 10 });
    },

    //完成
    completeTask() {
      const record = this.completeRecord;
      this.complete_visible = false;
      const that = this;
      completeTask({
        taskId: record.id,
        processInstanceId: record.processInstanceId,
        comment: this.check_opinion,
      }).then((response) => {
        if (response.data.success) {
          that.$success({
            title: "提示：",
            content: "任务完成成功",
            okText: "确认",
          });
          that.fetch({ pageSize: 10 });
        } else {
          this.$error({
            title: "提示：",
            content: response.data.msg,
            okText: "确认",
          });
        }
      });
    },

    flowchart(record) {
      this.imageUrl = "";
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
    formInfo(data) {
      var record = {
        processInstanceId: data.processInstanceId,
        businessKey: data.businessKey,
      };
      let component = FormInfo;
      if (data.businessKey.indexOf("customize") != -1) {
        component = VDataInfo;
      }
      this.$dialog(
        component,
        // component props
        {
          record,
          on: {
            ok() {
              that.fetch({ pageSize: 10 });
              console.log("ok 回调");
            },
            cancel() {
              console.log("cancel 回调");
            },
            close() {
              console.log("modal close 回调");
            },
          },
        },
        // modal props
        {
          title: "详情",
          width: 1000,
          centered: true,
          maskClosable: false,
          footer: "",
        }
      );
    },
    handleCancel(e) {
      this.visible = false;
      this.complete_visible = false;
    },
  },
};
</script>
<style scoped>
</style>