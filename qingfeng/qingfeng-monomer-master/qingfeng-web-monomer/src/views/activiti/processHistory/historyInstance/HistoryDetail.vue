<template>
  <div>
    <a-card :bordered="false">
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
      </a-table>
    </a-card>
  </div>
</template>
<script>
import { mapState } from "vuex";
import { findDetailListPage } from "@/api/activiti/processHistory";
import { formatDate, formatCtsDate } from "@/utils/date.js";
const columns = [
  {
    title: "流程实例ID",
    dataIndex: "processInstanceId",
    // ellipsis: true,
  },
  {
    title: "执行流ID",
    dataIndex: "executionId",
    // ellipsis: true,
  },
  // {
  //   title: "任务ID",
  //   dataIndex: "taskId",
  //   // ellipsis: true,
  // },
  {
    title: "时间",
    dataIndex: "time",
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
      queryParam: {},
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
      this.loading = true;
      findDetailListPage({
        ...params,
        processInstanceId: this.record.processInstanceId,
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
  },
};
</script>
<style scoped>
</style>