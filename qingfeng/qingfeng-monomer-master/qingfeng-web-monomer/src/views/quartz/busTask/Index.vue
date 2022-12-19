<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="24">
            <a-col :md="6" :sm="24">
              <a-form-item label="任务名称">
                <a-input v-model="queryParam.job_name" placeholder="" />
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-button
                type="primary"
                @click="fetch({ results: 10, ...queryParam })"
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
        <a-space size="small">
          <a-button v-action:busTask:add @click="add()"
            >添加</a-button
          >
          <a-button
            v-action:busTask:edit
            type="primary"
            @click="edits()"
            >编辑</a-button
          >
          <a-button
            v-action:busTask:del
            type="danger"
            @click="dels()"
            >删除</a-button
          >
        </a-space>
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
        <template slot-scope="text, record, index">
          <div :key="col">
            {{ text }}
          </div>
        </template>
        <template slot="operation" slot-scope="text, record, index">
          <div class="editable-row-operations">
            <a-space size="small">
              <a-button
                size="small"
                v-action:busTask:info
                @click="() => info(record)"
                >详情</a-button
              >
              <a-button
                size="small"
                type="primary"
                v-action-dauth:[{...record,auth:`busTask:edit`}]
                @click="() => edit(record)"
                >编辑</a-button
              >
              <a-button
                size="small"
                type="danger"
                v-action-dauth:[{...record,auth:`busTask:del`}]
                @click="() => del(record)"
                >删除</a-button
              >
              <a-button
                size="small"
                type="primary"
                v-action:busTask:execution
                @click="() => execution(record)"
                >执行</a-button
              >
              <a-button
                size="small"
                type="primary"
                v-action:busTask:stopOrRestore
                v-show="record.trigger_state == 'N'"
                @click="() => stopOrRestore(record, 'Y')"
                >恢复</a-button
              >
              <a-button
                size="small"
                type="danger"
                v-action:busTask:stopOrRestore
                v-show="record.trigger_state == 'Y'"
                @click="() => stopOrRestore(record, 'N')"
                >停止</a-button
              >
            </a-space>
          </div>
        </template>
      </a-table>
    </a-card>
  </page-header-wrapper>
</template>
<script>
import { mapState } from "vuex";
import {
  getListPage,
  del,
  stopOrRestore,
  execution,
} from "@/api/quartz/busTask";
import { findDataAuth } from "@/core/baseAction.js";
import EditForm from "./Edit";
import InfoForm from "./Info";
const columns = [
  {
    title: "任务名称",
    dataIndex: "job_name",
    sorter: true,
    ellipsis: true,
  },
  {
    title: "任务描述",
    dataIndex: "description",
    ellipsis: true,
  },
  {
    title: "执行时间表达式",
    dataIndex: "cron_expression",
    ellipsis: true,
  },
  {
    title: "状态",
    dataIndex: "trigger_state",
    customRender: (text, row, index) => {
      if (text == "Y") {
        return "执行中";
      } else {
        return "已停止";
      }
    },
  },
  {
    title: "排序",
    dataIndex: "order_by",
    ellipsis: true,
  },
  {
    title: "执行开始时间",
    dataIndex: "trigger_time",
    ellipsis: true,
  },
  {
    title: "操作",
    dataIndex: "operation",
    scopedSlots: { customRender: "operation" },
    width: "300px",
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
        id: "",
        job_name: "",
      },
    };
  },
  components: {
    EditForm,
    InfoForm,
  },
  computed: {
    ...mapState({
    }),
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
        getCheckboxProps: (record) => ({
          props: {
            disabled: record.name === "Disabled User", // Column configuration not to be checked
            name: record.name,
          },
        }),
      };
    },
  },
  mounted() {
    this.fetch({ results: 10 });
    console.log(this.$test());
  },
  methods: {
    handleTableChange(pagination, filters, sorter) {
      const pager = { ...this.pagination };
      pager.current = pagination.current;
      this.pagination = pager;
      this.fetch({
        results: pagination.pageSize,
        page: pagination.current,
        ...filters,
      });
    },
    fetch(params = {}) {
      console.log("params:", params);
      this.loading = true;
      getListPage(params).then((response) => {
        const pagination = { ...this.pagination };
        // Read total count from server
        // pagination.total = data.totalCount;
        pagination.total = response.data.data.total;
        this.loading = false;
        this.data = response.data.data.rows;
        this.pagination = pagination;
      });
    },
    info(record) {
      this.$dialog(
        InfoForm,
        // component props
        {
          record,
          on: {
            ok() {
              that.fetch({ results: 10 });
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
          width: 700,
          centered: true,
          maskClosable: false,
          footer: "",
        }
      );
    },
    edit(record) {
      this.dialogData(record);
    },
    del(record) {
      this.delData(record.id);
    },
    add() {
      this.dialogData({});
    },
    edits() {
      if (this.selectedRows.length == 1) {
        const record = this.selectedRows[0];
         if (findDataAuth(record)) {
          this.dialogData(record);
        } else {
          this.$message.warning("没有编辑权限,请选择其他数据进行编辑。");
        }
      } else {
        this.$error({
          title: "提示：",
          content: "请选择一条数据进行编辑。",
          okText: "确认",
        });
      }
    },
    dels() {
      if (this.selectedRows.length > 0) {
        let delBol = true;
        for (let i = 0; i < this.selectedRows.length; i++) {
          const record = this.selectedRows[i];
          if (!findDataAuth(record)) {
            delBol = false;
          }
        }
        if (!delBol) {
          this.$message.warning(
            "批量删除中存在没有删除权限的数据，请重新选择。"
          );
          return;
        }
        const id = this.selectedRows.map((map) => {
          return map.id;
        });
        this.delData(id);
      } else {
        this.$error({
          title: "提示：",
          content: "请选择需要删除的数据。",
          okText: "确认",
        });
      }
    },
    stopOrRestore(record, status) {
      const that = this;
      stopOrRestore({id:record.id,job_name:record.job_name, job_group:record.job_group, trigger_state:status}).then(
        (response) => {
          if (response.status==200) {
            that.fetch({ results: 10 });
          } else {
            that.$error({
              title: "提示：",
              content: "操作失败",
            });
          }
        }
      );
    },
    execution(record) {
      const that = this;
      execution(record.job_name, record.job_group).then((response) => {
        if (response.status==200) {
          that.$success({
            title: "提示：",
            content: "执行成功",
          });
        } else {
          that.$error({
            title: "提示：",
            content: "执行失败",
          });
        }
      });
    },
    dialogData(record) {
      const that = this;
      this.$dialog(
        EditForm,
        // component props
        {
          record,
          on: {
            ok() {
              that.fetch({ results: 10 });
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
          title: "操作",
          width: 700,
          centered: true,
          maskClosable: false,
          okText: "确认",
          cancelText: "取消",
        }
      );
    },
    delData(ids) {
      const that = this;
      that.$confirm({
        title: "确定要删除选择的数据吗?",
        content: (h) => <div style="color:red;">数据删除后不可恢复</div>,
        okText: "确认",
        cancelText: "取消",
        onOk() {
          del(ids).then((response) => {
            if (response.status==200) {
              that.$success({
                title: "提示：",
                content: "数据删除成功",
              });
              that.fetch({ results: 10 });
            } else {
              that.$error({
                title: "提示：",
                content: "数据删除失败",
              });
            }
          });
        },
        onCancel() {
          console.log("Cancel");
        },
      });
    },
  },
};
</script>
<style scoped>
</style>