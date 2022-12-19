<template>
  <div>
    <a-card :bordered="false">
      <a-table
        :row-selection="rowSelection"
        :columns="columns"
        :data-source="data"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
        bordered
      >
      </a-table>
    </a-card>
  </div>
</template>
<script>
import { findExecutionListPage } from "@/api/activiti/processInstance";
import axios from "axios";
import { formatDate } from "@/utils/date.js";
const columns = [
  {
    title: "执行流ID",
    dataIndex: "id",
    sorter: true,
    ellipsis: true,
  },
  // {
  //   title: "执行流父节点ID",
  //   dataIndex: "parentId",
  //   // ellipsis: true,
  // },
  {
    title: "业务表单",
    dataIndex: "businessKey",
    ellipsis: true,
  },
  // {
  //   title: "流程实例ID",
  //   dataIndex: "processInstanceId",
  //   // ellipsis: true,
  // },
  // {
  //   title: "流程定义ID",
  //   dataIndex: "processDefinitionId",
  //   // ellipsis: true,
  // },
  {
    title: "流程定义Key",
    dataIndex: "processDefinitionKey",
    // ellipsis: true,
  },
  {
    title: "节点id",
    dataIndex: "activityId",
    // ellipsis: true,
  },
  {
    title: "状态",
    dataIndex: "suspensionState",
    customRender: (text, row, index) => {
      if (text == "1") {
        return "活跃状态";
      } else if (text == "2") {
        return "中断状态";
      }
    },
  },
  {
    title: "开始时间",
    dataIndex: "startTime",
    customRender: (text, row, index) => {
      return formatDate(text);
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
      data: [],
      pagination: {},
      loading: false,
      columns,
      selectedRows: [],
      queryParam: {
      },
      hisImageUrl: "",
      hisVisible: false,
    };
  },
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
        ...filters,
      });
    },
    fetch(params = {}) {
      this.loading = true;
      findExecutionListPage({
        ...params,
        processInstanceId: this.record.id,
        type: "allTask",
      }).then((response) => {
        const pagination = { ...this.pagination };
        pagination.total = response.data.count;
        this.loading = false;
        this.data = response.data.data;
        this.pagination = pagination;
      });
    },

    onOk() {
      return new Promise((resolve) => {
        resolve(true);
      });
    },
    onCancel() {
      console.log("监听了 modal cancel 事件");
      return new Promise((resolve) => {
        resolve(true);
      });
    },
  },
};
</script>
