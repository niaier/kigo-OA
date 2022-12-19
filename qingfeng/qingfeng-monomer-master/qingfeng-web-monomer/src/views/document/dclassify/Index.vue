<template>
  <page-header-wrapper>
    <a-row :gutter="24">
      <a-col :md="24" :lg="6">
        <a-card :bordered="false">
          <dic-tree
            ref="child"
            :type="type"
            @selectTree="selectTree"
          ></dic-tree>
        </a-card>
      </a-col>
      <a-col :md="24" :lg="18">
        <a-card :bordered="false">
          <div class="table-page-search-wrapper">
            <a-form layout="inline">
              <a-row :gutter="24">
                <a-col :md="6" :sm="24">
                  <a-form-item label="分类名称">
                    <a-input v-model="queryParam.name" placeholder="" />
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
              <a-button v-action:dclassify:add @click="add()">添加</a-button>
              <a-button v-action:dclassify:edit type="primary" @click="edits()"
                >编辑</a-button
              >
              <a-button v-action:dclassify:del type="danger" @click="dels()"
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
                    v-action:dclassify:info
                    @click="() => info(record)"
                    >详情</a-button
                  >
                  <a-button
                    size="small"
                    type="primary"
                    v-action-dauth:[{...record,auth:`dclassify:edit`}]
                    @click="() => edit(record)"
                    >编辑</a-button
                  >
                  <a-button
                    size="small"
                    type="danger"
                    v-action-dauth:[{...record,auth:`dclassify:del`}]
                    @click="() => del(record)"
                    >删除</a-button
                  >
                </a-space>
              </div>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>
  </page-header-wrapper>
</template>
<script>
import { mapState } from "vuex";
import { getListPage, delData, updateStatus } from "@/api/document/dclassify";
import { ACCESS_TOKEN } from "@/store/mutation-types";
import storage from "store";
import EditForm from "./Edit";
import InfoForm from "./Info";
import DicTree from "./Tree";
const columns = [
  {
    title: "分类名称",
    dataIndex: "name",
    sorter: true,
    ellipsis: true,
  },

  {
    title: "类型",
    dataIndex: "type",
    customRender: (text, row, index) => {
      if (text == "0") {
        return "系统文档";
      } else if (text == "1") {
        return "共享文档";
      } else if (text == "2") {
        return "个人文档";
      }
    },
  },
  {
    title: "排序",
    dataIndex: "order_by",
  },
  {
    title: "创建时间",
    dataIndex: "create_time",
  },
  {
    title: "操作",
    dataIndex: "operation",
    scopedSlots: { customRender: "operation" },
    width: "240px",
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
        name: "",
      },
      parent_id: "",
      parent_name: "",
      dc_cascade: "",
      level_num: "",
    };
  },
  components: {
    EditForm,
    DicTree,
    InfoForm,
  },
  computed: {
    rowSelection() {
      return {
        onChange: (selectedRowKeys, selectedRows) => {
          this.selectedRows = selectedRows;
        },
      };
    },
  },
  watch: {
    $route(to, from) {
      let path = to.path;
      this.type = path.substring(path.lastIndexOf("/") + 1);
      if (this.type != undefined) {
        this.fetch({ pageSize: 10 });
        this.$refs.child.getTreeList({ type: this.type });
      }
    },
  },
  mounted() {
    let path = this.$route.path;
    this.type = path.substring(path.lastIndexOf("/") + 1);
    if (this.type != undefined) {
      this.fetch({ pageSize: 10 });
      this.$refs.child.getTreeList({ type: this.type });
    }
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
    selectTree(id, name, value) {
      this.parent_id = id;
      this.parent_name = name;
      this.dc_cascade = value.split(":")[0];
      this.level_num = value.split(":")[1];
      this.fetch({ pageSize: 10 });
    },
    fetch(params = {}) {
      this.loading = true;
      getListPage({
        parent_id: this.parent_id,
        ...params,
        type: this.type,
      }).then((response) => {
        const pagination = { ...this.pagination };
        pagination.total = response.data.data.total;
        this.loading = false;
        this.data = response.data.data.rows;
        this.pagination = pagination;
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
    del(record) {
      if (record.child_num > 0) {
        this.$message.warning(
          "删除的数据中存在含有下级菜单的数据，请重新选择。"
        );
        return;
      }
      this.delData(record.id);
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
        let delBol = true;
        for (let i = 0; i < this.selectedRows.length; i++) {
          const record = this.selectedRows[i];
          if (record.child_num > 0) {
            delBol = false;
          }
        }
        if (!delBol) {
          this.$message.warning("批量删除中存在含有下级的数据，请重新选择。");
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
    dialogData(record) {
      const that = this;
      if (this.parent_id == "" || this.parent_id == null) {
        that.$error({
          title: "提示：",
          content: "请选择左侧父级节点",
        });
        return;
      }
      record = {
        parent_id: this.parent_id,
        parent_name: this.parent_name,
        dc_cascade: this.dc_cascade,
        level_num: this.level_num,
        ...record,
        type: this.type,
      };
      this.$dialog(
        EditForm,
        {
          record,
          on: {
            ok() {
              that.fetch({ pageSize: 10 });
              that.getTreeList();
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
          delData(ids).then((response) => {
            if (response.status == 200) {
              that.$success({
                title: "提示：",
                content: "数据删除成功",
              });
              that.fetch({ pageSize: 10 });
              that.getTreeList();
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
    getTreeList() {
      this.$refs.child.getTreeList({ type: this.type });
    },
  },
};
</script>
<style scoped>
</style>