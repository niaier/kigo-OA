<template>
  <div>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="24">
            <a-col :md="6" :sm="24">
              <a-form-item label="流程定义名称">
                <a-input
                  v-model="queryParam.processDefinitionName"
                  placeholder=""
                />
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="流程定义KEY">
                <a-input
                  v-model="queryParam.processDefinitionKey"
                  placeholder=""
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
      <a-table
        :row-selection="rowSelection"
        :columns="columns"
        :data-source="data"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
        bordered
      >
        <template slot="operation" slot-scope="text, record, index">
          <div class="editable-row-operations">
            <a-space :size="4">
              <div slot="actions">
                <a-space :size="4">
                  <a-button
                    size="small"
                    type="primary"
                    v-action:processDefinition:flowchart
                    @click="() => historyFlowchart(record)"
                    >流程图</a-button
                  >
                  <a-button
                    size="small"
                    type="danger"
                    v-action:processDefinition:del
                    @click="() => hisDel(record)"
                    >删除</a-button
                  >
                </a-space>
              </div>
            </a-space>
          </div>
        </template>
      </a-table>
    </a-card>
    <a-modal width="800px" v-model="hisVisible" title="流程图" on-ok="handleOk">
      <template slot="footer">
        <a-button key="back" @click="hisHandleCancel"> 关闭 </a-button>
      </template>
      <img :src="hisImageUrl" alt="流程图" class="image" />
    </a-modal>
  </div>
</template>
<script>
import {
  del,
  findHistoryProcessDefinitionPage,
} from "@/api/activiti/processDefinition";
import axios from "axios";
const columns = [
  {
    title: "ID",
    dataIndex: "id",
    sorter: true,
    ellipsis: true,
  },
  {
    title: "流程定义名称",
    dataIndex: "name",
    ellipsis: true,
  },
  {
    title: "流程定义key",
    dataIndex: "key",
    // ellipsis: true,
  },
  {
    title: "版本",
    dataIndex: "version",
    ellipsis: true,
  },
  // {
  //   title: "部署id",
  //   dataIndex: "deploymentid",
  //   // ellipsis: true,
  // },
  {
    title: "描述",
    dataIndex: "description",
    customRender: (text, row, index) => {
      if (text == "" || text == null || text == undefined) {
        return "";
      } else {
        return text;
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
        processDefinitionName: "",
        processDefinitionKey: "",
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
        sortField: sorter.field,
        sortOrder: sorter.order,
        ...filters,
      });
    },
    fetch(params = {}) {
      this.loading = true;
      findHistoryProcessDefinitionPage({
        ...params,
        key: this.record.key,
      }).then((response) => {
        const pagination = { ...this.pagination };
        pagination.total = response.data.count;
        this.loading = false;
        this.data = response.data.data;
        this.pagination = pagination;
      });
    },

    historyFlowchart(record) {
      this.hisVisible = true;
      axios({
        method: "GET",
        url:
          process.env.VUE_APP_API_BASE_URL +
          `/activiti/modeler/findFlowchart?deploymentId=` +
          record.deploymentid,
        responseType: "arraybuffer",
      })
        .then((res) => {
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
          this.hisImageUrl = res;
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
    },
    hisDel(record) {
      console.log(record);
      const that = this;
      that.$confirm({
        title: "确定要删除选择的数据吗?",
        content: (h) => <div style="color:red;">数据删除后不可恢复</div>,
        okText: "确认",
        cancelText: "取消",
        onOk() {
          del(record.deploymentid).then((response) => {
            if (response.data.success) {
              that.$success({
                title: "提示：",
                content: "数据删除成功",
              });
              that.fetch({ pageSize: 10 });
            } else {
              that.$error({
                title: "提示：",
                content: "数据删除失败:" + response.data.msg,
              });
            }
          });
        },
        onCancel() {
          console.log("Cancel");
        },
      });
    },
    hisHandleCancel(e) {
      this.hisVisible = false;
    },

    onOk() {},
    onCancel() {
      console.log("监听了 modal cancel 事件");
      return new Promise((resolve) => {
        resolve(true);
      });
    },
  },
};
</script>
