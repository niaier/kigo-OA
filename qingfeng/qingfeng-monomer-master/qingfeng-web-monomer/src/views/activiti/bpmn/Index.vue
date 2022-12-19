<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="24">
            <a-col :md="6" :sm="24">
              <a-form-item label="流程id">
                <a-input v-model="queryParam.processId" placeholder="" />
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="流程名称">
                <a-input v-model="queryParam.processName" placeholder="" />
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
        <a-space size="small">
          <a-button v-action:bpmn:add @click="add()">添加</a-button>
          <a-button v-action:bpmn:edit type="primary" @click="edits()"
            >编辑</a-button
          >
          <a-button v-action:bpmn:del type="danger" @click="dels()"
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
            <a-space size="small">
              <a-button
                size="small"
                v-action:bpmn:info
                @click="() => info(record)"
                >详情</a-button
              >
              <a-button
                size="small"
                type="primary"
                v-action:bpmn:edit
                @click="() => edit(record)"
                >编辑</a-button
              >
              <a-button
                size="small"
                type="danger"
                v-action:bpmn:del
                @click="() => del(record.id)"
                >删除</a-button
              >
              <a-button
                size="small"
                type="primary"
                v-action:bpmn:publish
                @click="() => publishBpmn(record)"
                >发布</a-button
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
import { getListPage, del, publish } from "@/api/activiti/bpmn";
import EditForm from "./Edit";
import InfoForm from "./Info";
const columns = [
  {
    title: "流程id",
    dataIndex: "processId",
    sorter: true,
    ellipsis: true,
  },
  {
    title: "流程名称",
    dataIndex: "processName",
    ellipsis: true,
  },
  {
    title: "版本标签",
    dataIndex: "versionTag",
  },
  {
    title: "可执行",
    dataIndex: "isExecutable",
    customRender: (text, row, index) => {
      if (text == "true") {
        return "是";
      } else {
        return "否";
      }
    },
  },
  {
    title: "创建时间",
    dataIndex: "create_time",
  },
  {
    title: "操作",
    dataIndex: "operation",
    scopedSlots: { customRender: "operation" },
    width: "250px",
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
        processId: "",
        processName: "",
      },
    };
  },
  components: {
    EditForm,
    InfoForm,
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
    this.fetch({ pageSize: 10 });
  },
  methods: {
    handleTableChange(pagination, filters, sorter) {
      console.log(pagination);
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
      console.log("params:", params);
      this.loading = true;
      getListPage(params).then((response) => {
        const pagination = { ...this.pagination };
        pagination.total = response.data.data.total;
        this.loading = false;
        this.data = response.data.data.rows;
        this.pagination = pagination;
      });
    },
    publishBpmn(record) {
      const that = this;
      that.$confirm({
        title: "提示",
        content: (h) => <div style="color:red;">确定要部署流程吗？</div>,
        okText: "确认",
        cancelText: "取消",
        onOk() {
          publish({ id: record.id }).then((response) => {
            // that.fetch({ pageSize: 10 });
            that.$success({
              title: "提示：",
              content: "部署成功。",
            });
          });
          return new Promise((resolve) => {
            resolve(true);
          });
        },
        onCancel() {
        },
      });
    },
    info(record) {
      this.$dialog(
        InfoForm,
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
    del(key) {
      this.delData(key);
    },
    add() {
      this.dialogData({});
    },
    edits() {
      if (this.selectedRows.length == 1) {
        const record = this.selectedRows[0];
        this.dialogData(record);
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
        const ids = this.selectedRows.map((map) => {
          return map.id;
        });
        this.delData(ids);
      } else {
        this.$error({
          title: "提示：",
          content: "请选择需要删除的数据。",
          okText: "确认",
        });
      }
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
              that.fetch({ pageSize: 10 });
              console.log("ok 回调");
            },
            cancel() {
              that.fetch({ pageSize: 10 });
              console.log("cancel 回调");
            },
            close() {
              that.fetch({ pageSize: 10 });
              console.log("modal close 回调");
            },
          },
        },
        // modal props
        {
          title: "操作",
          width: 1200,
          centered: true,
          maskClosable: false,
          okText: "确认",
          cancelText: "关闭",
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
            if (response.status == 200) {
              that.$success({
                title: "提示：",
                content: "数据删除成功",
              });
              that.fetch({ pageSize: 10 });
            } else {
              that.$error({
                title: "提示：",
                content: "数据删除失败",
              });
            }
          });
          console.log("OK:" + ids);
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