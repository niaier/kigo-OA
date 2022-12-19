<template>
  <page-header-wrapper>
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
      <div style="margin: 0 0 10px 0">
        <a-space size="small">
          <a-upload
            name="file"
            :multiple="false"
            :customRequest="uploadFile"
            :headers="headers"
            accept=".zip,.bar,.bpmn"
            :fileList="fileList"
          >
            <a-button
              v-action:processDefinition:import
              >导入流程定义</a-button
            >
          </a-upload>
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
                    v-action:processDefinition:startProcessInstanceByKey
                    @click="() => startProcessInstanceByKey(record)"
                    >启动流程示例</a-button
                  >

                  <a-button
                    size="small"
                    type="primary"
                    v-action:processDefinition:flowchart
                    @click="() => flowchart(record)"
                    >流程图</a-button
                  >
                </a-space>
              </div>
              <div slot="actions">
                <a-dropdown>
                  <a-menu slot="overlay">
                    <a-menu-item v-action:processDefinition:convertToModel
                      ><a @click="() => toModeler(record)"
                        >转换模型</a
                      ></a-menu-item
                    >
                    <a-menu-item v-action:processDefinition:history
                      ><a @click="() => history(record)"
                        >历史版本</a
                      ></a-menu-item
                    >
                    <a-menu-item v-action:processDefinition:del
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
  convertToModel,
  findProcessDefinition,
  findHistoryProcessDefinitionPage,
  startProcessInstanceByKey,
  uploadFile
} from "@/api/activiti/processDefinition";
import { ACCESS_TOKEN } from "@/store/mutation-types";
import storage from "store";
import HistoryForm from "./History";
import InfoForm from "./Info";
import { formatDate } from "@/utils/date.js";
import axios from "axios";
const columns = [
  {
    title: "ID",
    dataIndex: "id",
    sorter: true,
    ellipsis: true,
    width: "240px",
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
        processDefinitionName: "",
        processDefinitionKey: "",
      },
      visible: false,
      imageUrl: "",
      headers:{},
      fileList:[]
    };
  },
  components: {
    HistoryForm,
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

    startProcessInstanceByKey(record) {
      const that = this;
      that.$confirm({
        title: "提示",
        content: (h) => <div style="color:red;">确认要启动流程示例吗？</div>,
        okText: "确认",
        cancelText: "取消",
        onOk() {
          startProcessInstanceByKey({ processDefinitionKey: record.key }).then(
            (response) => {
              console.log(response);
              if (response.data.success) {
                that.$success({
                  title: "提示：",
                  content: "流程实例启动成功。",
                  okText: "确认",
                });
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
          `/activiti/modeler/findFlowchart?deploymentId=` +
          record.deploymentid,
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
    },
    toModeler(record) {
      const that = this;
      that.$confirm({
        title: "提示",
        content: (h) => (
          <div style="color:red;">确认要将流程定义转换成流程模型吗？</div>
        ),
        okText: "确认",
        cancelText: "取消",
        onOk() {
          convertToModel({ processDefinitionId: record.id }).then(
            (response) => {
              console.log(response);
              if (response.data.success) {
                that.$success({
                  title: "提示：",
                  content: "流程模型转换成功。",
                  okText: "确认",
                });
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
    history(record) {
      const that = this;
      this.$dialog(
        HistoryForm,
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

    // 上传流程定义
    uploadFile(file) {
      this.loading = true;
      const formData = new FormData();
      formData.append("file", file.file);
      uploadFile(formData).then((res) => {
          if (res.data.success) {
            this.loading = false;
             this.$success({
                title: "提示：",
                content: "流程定义部署成功。",
              });
              this.fetch({ pageSize: 10 });
          }
        },
        (err) => {
          this.avatarLoading = false;
        }
      );
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
  },
};
</script>
<style scoped>
</style>