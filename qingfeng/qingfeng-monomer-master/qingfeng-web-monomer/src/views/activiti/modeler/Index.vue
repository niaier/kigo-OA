<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="24">
            <a-col :md="6" :sm="24">
              <a-form-item label="模型名称">
                <a-input v-model="queryParam.name" placeholder="" />
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="模型KEY">
                <a-input v-model="queryParam.code" placeholder="" />
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
          <a-button v-action:modeler:add @click="add()">添加</a-button>
          <a-button v-action:modeler:edit type="primary" @click="edits()"
            >编辑</a-button
          >
          <a-button v-action:modeler:del type="danger" @click="dels()"
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
            <a-space :size="4">
              <div slot="actions">
                <a-space :size="4">
                  <a-button
                    size="small"
                    v-action:modeler:info
                    @click="() => info(record)"
                    >详情</a-button
                  >
                  <a-button
                    size="small"
                    type="primary"
                    v-action:modeler:edit
                    @click="() => edit(record)"
                    >编辑</a-button
                  >
                  <a-button
                    size="small"
                    type="danger"
                    v-action:modeler:del
                    @click="() => del(record.id)"
                    >删除</a-button
                  >
                  <a-button
                    size="small"
                    type="primary"
                    v-action:modeler:publish
                    @click="() => publishModeler(record)"
                    >发布</a-button
                  >
                </a-space>
              </div>
              <div slot="actions">
                <a-dropdown>
                  <a-menu slot="overlay">
                    <a-menu-item v-action:modeler:revokePublish
                      ><a @click="() => revokePublishModeler(record)"
                        >撤销</a
                      ></a-menu-item
                    >
                    <a-menu-item v-action:modeler:download
                      ><a @click="() => download(record)">下载</a></a-menu-item
                    >
                    <a-menu-item v-action:modeler:flowchart
                      ><a @click="() => flowchartModeler(record)"
                        >流程图</a
                      ></a-menu-item
                    >
                    <a-menu-item v-action:modeler:nodes
                      ><a @click="() => nodes(record)">节点管理</a></a-menu-item
                    >
                  </a-menu>
                  <a>更多<a-icon type="down" /></a>
                </a-dropdown>
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
  </page-header-wrapper>
</template>
<script>
import { mapState } from "vuex";
import {
  getListPage,
  del,
  updateStatus,
  publish,
  revokePublish,
} from "@/api/activiti/modeler";
import { ACCESS_TOKEN } from "@/store/mutation-types";
import storage from "store";
import EditForm from "./Edit";
import InfoForm from "./Info";
import Nodes from "./Nodes";
import { formatDate } from "@/utils/date.js";
import axios from "axios";
const columns = [
  {
    title: "模型",
    dataIndex: "name",
    sorter: true,
    ellipsis: true,
  },
  {
    title: "模型key",
    dataIndex: "key",
    ellipsis: true,
  },
  {
    title: "部署id",
    dataIndex: "deploymentId",
    customRender: (text, row, index) => {
      if (text == "" || text == null || text == undefined) {
        return "未部署";
      } else {
        return "已部署：" + text;
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
    title: "最后更新时间",
    dataIndex: "lastUpdateTime",
    customRender: (text, row, index) => {
      return formatDate(text);
    },
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
        name: "",
        code: "",
      },
      visible: false,
      imageUrl: "",
    };
  },
  components: {
    EditForm,
    InfoForm,
    Nodes,
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
        // Read total count from server
        // pagination.total = data.totalCount;
        pagination.total = response.data.count;
        this.loading = false;
        this.data = response.data.data;
        this.pagination = pagination;
      });
    },
    handleCancel(e) {
      this.visible = false;
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
          footer: "",
        }
      );
    },
    edit(record) {
      window.open(
        "http://localhost:8301/system/modeler/modeler.html?modelId=" + record.id
      );
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
        window.open(
          "http://localhost:8301/system/modeler/modeler.html?modelId=" +
            record.id
        );
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
    publishModeler(record) {
      const that = this;
      publish({ modelId: record.id }).then((response) => {
        that.fetch({ pageSize: 10 });
      });
    },
    revokePublishModeler(record) {
      const that = this;
      revokePublish({ modelId: record.id }).then((response) => {
        that.fetch({ pageSize: 10 });
      });
    },
    flowchartModeler(record) {
      this.visible = true;
      console.log(
        process.env.VUE_APP_API_BASE_URL +
          `/activiti/modeler/findFlowchart?deploymentId=` +
          record.deploymentId
      );
      if (record.deploymentId == "" || record.deploymentId == null) {
        this.$message.warning(
          "流程模型数据为空，请先设计流程并成功保存，再进行查看。"
        );
      } else {
        axios({
          method: "GET",
          url:
            process.env.VUE_APP_API_BASE_URL +
            `/activiti/modeler/findFlowchart?deploymentId=` +
            record.deploymentId,
          responseType: "arraybuffer",
        })
          .then((res) => {
            console.log(res);
            return (
              "data:image/png;base64," +
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
                description: res.msg,
              });
            } else {
              this.$notification.warning({
                message: "获取流程图失败",
                description: res.msg,
              });
            }
          });
      }
    },
    download(record) {
      window.location.href =
        process.env.VUE_APP_API_URL +
        "/activiti/modeler/downloadModeler?modelId=" +
        record.id;
    },

    updateStatus(id, status) {
      const that = this;
      updateStatus(id, status).then((response) => {
        if (response.status == 200) {
          that.fetch({ pageSize: 10 });
        } else {
          that.$error({
            title: "提示：",
            content: "状态修改异常",
          });
        }
      });
    },
    exportModeler(modelId) {
      window.location.href =
        process.env.VUE_APP_API_URL +
        "/activiti/modeler/exportModeler?modelId=" +
        modelId;
    },
    nodes(record) {
      const that = this;
      this.$dialog(
        Nodes,
        // component props
        {
          record,
          on: {
            ok() {
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
          width: 1100,
          centered: true,
          maskClosable: false,
          okText: "确认",
          cancelText: "取消",
        }
      );
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