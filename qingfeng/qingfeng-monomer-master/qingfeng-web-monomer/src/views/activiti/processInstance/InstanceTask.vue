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
                    type="danger"
                    v-action:processTask:claimTask
                    v-show="record.status == '0'"
                    @click="() => claim(record)"
                    >签收任务</a-button
                  >
                  <a-button
                    size="small"
                    type="danger"
                    v-action:processTask:completeTask
                    v-show="record.status == '1'"
                    @click="() => complete(record)"
                    >完成任务</a-button
                  >
                  <a-button
                    size="small"
                    type="primary"
                    v-action:processTask:shutdownTask
                    @click="() => shutdown(record)"
                    >强制结束</a-button
                  >
                  <a-button
                    size="small"
                    type="primary"
                    v-action:processTask:flowchart
                    @click="() => flowchart(record)"
                    >流程图</a-button
                  >
                </a-space>
              </div>
              <!-- <div slot="actions">
                <a-dropdown>
                  <a-menu slot="overlay">
                    <a-menu-item v-action:processTask:del
                      ><a @click="() => del(record)">删除</a></a-menu-item
                    >
                  </a-menu>
                  <a>更多<a-icon type="down" /></a>
                </a-dropdown>
              </div> -->
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
    >
      <a-textarea v-model="check_opinion" placeholder="请输入审批意见" />
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
import { formatCtsDate } from "@/utils/date.js";
const columns = [
  {
    title: "任务名称",
    dataIndex: "name",
    sorter: true,
    // ellipsis: true,
  },
  {
    title: "任务定义key",
    dataIndex: "taskdefinitionkey",
    ellipsis: true,
  },
  {
    title: "代理人员",
    dataIndex: "assignee_name",
    customRender: (text, row, index) => {
      if (row.status == "0") {
        return "未签收";
      } else if (row.status == "1") {
        return text;
      }
    },
  },
  // {
  //   title: "实例id（执行流）",
  //   dataIndex: "executionid",
  //   // ellipsis: true,
  // },
  // {
  //   title: "流程实例ID",
  //   dataIndex: "processinstanceid",
  //   // ellipsis: true,
  // },
  // {
  //   title: "流程定义ID",
  //   dataIndex: "processdefinitionid",
  //   // ellipsis: true,
  // },
  {
    title: "创建时间",
    dataIndex: "createtime",
    customRender: (text, row, index) => {
      return formatCtsDate(text);
    },
  },
  {
    title: "操作",
    dataIndex: "operation",
    scopedSlots: { customRender: "operation" },
    width: "280px",
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
      data: [],
      pagination: {},
      loading: false,
      columns,
      selectedRows: [],
      queryParam: {
        name: "",
        key: "",
      },
      visible: false,
      imageUrl: "",
      complete_visible: false,
      confirmLoading: false,
      check_opinion: "",
      completeRecord: "",
    };
  },
  components: {},
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
      console.log(this.record)
      this.loading = true;
      findTaskListPage({ ...params, type: "allTask",processInstanceId:this.record.processInstanceId }).then((response) => {
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
      this.check_opinion = "";
      this.completeRecord = record;
      this.complete_visible = true;
    },

    //完成
    completeTask() {
      const record = this.completeRecord;
      this.complete_visible = false;
      const that = this;
      completeTask({
        taskId: record.id,
        processInstanceId: record.processinstanceid,
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

    shutdown(record) {
      const that = this;
      that.$confirm({
        title: "提示",
        content: (h) => <div style="color:red;">确定结束流程任务吗？</div>,
        okText: "确认",
        cancelText: "取消",
        onOk() {
          shutdownTask({ taskId: record.id }).then((response) => {
            if (response.data.success) {
              that.$success({
                title: "提示：",
                content: "流程任务结束成功",
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
    flowchart(record) {
      console.log(record);
      this.visible = true;
      axios({
        method: "GET",
        url:
          process.env.VUE_APP_API_BASE_URL +
          `/activiti/processTask/readResource?processInstanceId=` +
          record.processinstanceid,
        responseType: "arraybuffer",
      })
        .then((res) => {
          console.log(res);
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
          console.log(res);
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

    handleCancel(e) {
      this.visible = false;
      this.complete_visible = false;
    },
  },
};
</script>
<style scoped>
</style>