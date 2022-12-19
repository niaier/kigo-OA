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
                  v-model="queryParam.finished"
                  placeholder="请选择"
                  default-value=""
                >
                  <a-select-option value="">请选择</a-select-option>
                  <a-select-option value="0">已完成</a-select-option>
                  <a-select-option value="1">未完成</a-select-option>
                  <a-select-option value="2">全部</a-select-option>
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
  </div>
</template>
<script>
import { mapState } from "vuex";
import { findTaskListPage, delTask } from "@/api/activiti/processHistory";
import axios from "axios";
import { formatDate } from "@/utils/date.js";
import FormInfo from "../../processTask/FormInfo";
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
    title: "任务名称",
    dataIndex: "name",
    // ellipsis: true,
  },
  {
    title: "办理人员",
    dataIndex: "assignee_name",
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
    title: "任务创建时间",
    dataIndex: "startTime",
    customRender: (text, row, index) => {
      return formatDate(text);
    },
  },
  {
    title: "任务完成时间",
    dataIndex: "endTime",
    customRender: (text, row, index) => {
      if (text == "" || text == null) {
        return "任务未结束";
      } else {
        return formatDate(text);
      }
    },
  },
  {
    title: "操作",
    dataIndex: "operation",
    scopedSlots: { customRender: "operation" },
    width: "140px",
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
        finished: "",
      },
      visible: false,
      imageUrl: "",
    };
  },
  components: { FormInfo },
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
      findTaskListPage({
        ...params,
        type: "userTask",
      }).then((response) => {
        const pagination = { ...this.pagination };
        // Read total count from server
        // pagination.total = data.totalCount;
        pagination.total = response.data.count;
        this.loading = false;
        this.data = response.data.data;
        this.pagination = pagination;
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
      this.$dialog(
        FormInfo,
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