<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="24">
            <a-col :md="6" :sm="24">
              <a-form-item label="流程实例ID">
                <a-input
                  v-model="queryParam.processInstanceId"
                  placeholder=""
                />
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="流程定义名称">
                <a-input
                  v-model="queryParam.processDefinitionName"
                  placeholder=""
                />
              </a-form-item>
            </a-col>
            <!-- <a-col :md="6" :sm="24">
              <a-form-item label="流程定义ID">
                <a-input
                  v-model="queryParam.processDefinitionId"
                  placeholder=""
                />
              </a-form-item>
            </a-col> -->
            <a-col :md="6" :sm="24">
              <a-form-item label="流程定义key">
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
      <div style="margin: 0 0 10px 0">
        <a-space size="small">
          <a-button v-action:processInstance:del type="danger" @click="dels()"
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
                    type="danger"
                    v-action:processInstance:suspensionState
                    v-show="record.suspensionState == '1'"
                    @click="() => suspensionState(record.id, '1')"
                    >中断流程</a-button
                  >
                  <a-button
                    size="small"
                    type="primary"
                    v-action:processInstance:suspensionState
                    v-show="record.suspensionState == '2'"
                    @click="() => suspensionState(record.id, '2')"
                    >激活流程</a-button
                  >
                  <a-button
                    size="small"
                    type="primary"
                    v-action:processInstance:flowchart
                    @click="() => flowchart(record)"
                    >流程图</a-button
                  >
                  <a-button
                    size="small"
                    type="primary"
                    v-action:processInstance:task
                    @click="() => task(record)"
                    >实例任务</a-button
                  >
                </a-space>
              </div>
              <div slot="actions">
                <a-dropdown>
                  <a-menu slot="overlay">
                    <a-menu-item v-action:processInstance:execution
                      ><a @click="() => execution(record)"
                        >执行流</a
                      ></a-menu-item
                    >
                    <a-menu-item v-action:processInstance:del
                      ><a @click="() => del(record)">删除</a></a-menu-item
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
  suspensionStateUpdate,
  findExecutionListPage,
} from "@/api/activiti/processInstance";
import ExecutionForm from "./Execution";
import InstanceTaskForm from "./InstanceTask";
import axios from "axios";
import { formatDate } from "@/utils/date.js";
const columns = [
  {
    title: "流程实例ID",
    dataIndex: "id",
    sorter: true,
    ellipsis: true,
  },
  {
    title: "业务表单",
    dataIndex: "businessKey",
    ellipsis: true,
    width: "150px",
  },
  {
    title: "流程定义名称",
    dataIndex: "processDefinitionName",
    // ellipsis: true,
  },
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
  // {
  //   title: "部署id",
  //   dataIndex: "deploymentId",
  //   // ellipsis: true,
  // },
  {
    title: "流程启动人",
    dataIndex: "startUserName",
    // ellipsis: true,
  },
  {
    title: "状态",
    dataIndex: "suspensionState",
    customRender: (text, row, index) => {
      if (text == "1") {
        return "活跃状态";
      } else {
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
        processInstanceId: "",
        processDefinitionName: "",
        processDefinitionId: "",
        processDefinitionKey: "",
      },
      visible: false,
      imageUrl: "",
    };
  },
  components: {
    ExecutionForm,
    InstanceTaskForm,
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

    suspensionState(processInstanceId, suspensionState) {
      let msg = "";
      let resultMsg = "";
      if (suspensionState == "1") {
        msg = <div style="color:red;">确认要中断流程实例吗？</div>;
        resultMsg = "中断流程实例成功。";
      } else if (suspensionState == "2") {
        msg = <div style="color:red;">确认要激活流程实例吗？</div>;
        resultMsg = "激活流程实例成功。";
      }
      const that = this;
      that.$confirm({
        title: "提示",
        content: (h) => msg,
        okText: "确认",
        cancelText: "取消",
        onOk() {
          suspensionStateUpdate({ processInstanceId, suspensionState }).then(
            (response) => {
              console.log(response);
              if (response.data.success) {
                that.$success({
                  title: "提示：",
                  content: resultMsg,
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
            }
          );
        },
        onCancel() {
          console.log("Cancel");
        },
      });
    },
    flowchart(record) {
      this.visible = true;
      axios({
        method: "GET",
        url:
          process.env.VUE_APP_API_BASE_URL +
          `/activiti/processTask/readResource?processInstanceId=` +
          record.id,
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
    execution(record) {
      const that = this;
      this.$dialog(
        ExecutionForm,
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
    task(record) {
      const that = this;
      this.$dialog(
        InstanceTaskForm,
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
    del(record) {
      console.log(record);
      const that = this;
      that.$confirm({
        title: "确定要删除选择的数据吗?",
        content: (h) => <div style="color:red;">数据删除后不可恢复</div>,
        okText: "确认",
        cancelText: "取消",
        onOk() {
          del(record.deploymentid).then((response) => {
            console.log(response);
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

    handleCancel(e) {
      this.visible = false;
    },
  },
};
</script>
<style scoped>
</style>