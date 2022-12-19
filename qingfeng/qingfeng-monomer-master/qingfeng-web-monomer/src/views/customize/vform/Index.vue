<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="24">
            <a-col :md="6" :sm="24">
              <a-form-item label="表单名称">
                <a-input
                  v-model="queryParam.table_comment"
                  placeholder="表单名称"
                />
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="数据表名称">
                <a-input
                  v-model="queryParam.table_name"
                  placeholder="数据表名称"
                />
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
          <a-button v-action:vform:add @click="add()"
            >添加</a-button
          >
          <a-button
            v-action:vform:edit
            type="primary"
            @click="edits()"
            >编辑</a-button
          >
          <a-button
            v-action:vform:del
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
          <div>
            {{ text }}
          </div>
        </template>
        <template slot="operation" slot-scope="text, record, index">
          <div class="editable-row-operations">
            <a-space size="small">
              <a-button
                size="small"
                v-action:vform:info
                @click="() => info(record)"
                >预览</a-button
              >
              <a-button
                size="small"
                type="primary"
                v-action-dauth:[{...record,auth:`vform:edit`}]
                @click="() => edit(record)"
                >编辑</a-button
              >
              <a-button
                size="small"
                type="danger"
                v-action-dauth:[{...record,auth:`vform:del`}]
                @click="() => del(record)"
                >删除</a-button
              >
            </a-space>
          </div>
        </template>
      </a-table>
    </a-card>
    <a-modal
      v-if="edit_visible"
      v-model="edit_visible"
      title="设计表单"
      :footer="null"
      width="90%"
      on-ok="handleOk"
    >
      <template slot="footer"> </template>
      <edit-form :record="record" @finishResponse="finishResponse"></edit-form>
    </a-modal>
  </page-header-wrapper>
</template>
<script>
import { mapState } from "vuex";
import { getListPage, delData, updateStatus } from "@/api/customize/vform";
import { findDataAuth } from "@/core/baseAction.js";
import storage from "store";
import EditForm from "./Edit";
import InfoForm from "./Info";
const columns = [
  {
    title: "表单类型",
    dataIndex: "table_type",
    ellipsis: true,
    customRender: (text, row, index) => {
      if (text == "0") {
        return "创建数据表";
      } else {
        return "绑定数据表";
      }
    },
  },
  {
    title: "表单名称",
    dataIndex: "table_comment",
    ellipsis: true,
  },
  {
    title: "数据表名称",
    dataIndex: "table_name",
    ellipsis: true,
  },
  // {
  //   title: "排序",
  //   dataIndex: "order_by",
  //   ellipsis: true,
  // },
  {
    title: "创建时间",
    dataIndex: "create_time",
  },
  {
    title: "操作",
    dataIndex: "operation",
    scopedSlots: { customRender: "operation" },
    width: "190px",
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
      queryParam: {},
      edit_visible: false,
      record: {},
    };
  },
  components: {
    EditForm,
    InfoForm,
  },
  computed: {
    ...mapState({
      // 动态主路由
    }),
    rowSelection() {
      return {
        onChange: (selectedRowKeys, selectedRows) => {
          this.selectedRows = selectedRows;
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
        ...filters,
      });
    },
    fetch(params = {}) {
      this.loading = true;
      getListPage({ ...params,type:'0' }).then((response) => {
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
          // footer: "",
          okText: "获取数据",
          cancelText: "关闭",
        }
      );
    },
    edit(record) {
      this.record = record;
      this.edit_visible = true;
    },
    del(record) {
      this.delData(record.id);
    },
    add() {
      this.record = "";
      this.edit_visible = true;
    },
    finishResponse(response) {
      this.edit_visible = false;
      this.fetch({ pageSize: 10 });
    },
    edits() {
      if (this.selectedRows.length == 1) {
        const record = this.selectedRows[0];
        if (findDataAuth(record)) {
          this.record = record;
          this.edit_visible = true;
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
    delData(ids) {
      const that = this;
      that.$confirm({
        title: "确定要删除选择的数据吗?",
        content: (h) => <div style="color:red;">数据删除后不可恢复</div>,
        okText: "确认",
        cancelText: "取消",
        onOk() {
          delData(ids).then((response) => {
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
          console.log("OK：" + ids);
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