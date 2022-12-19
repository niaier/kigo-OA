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
                <a-col :md="5" :sm="24">
                  <a-form-item label="文件名称">
                    <a-input v-model="queryParam.name" placeholder="" />
                  </a-form-item>
                </a-col>
                <a-col :md="5" :sm="24">
                  <a-form-item label="是否共享">
                    <a-select
                      v-model="queryParam.is_share"
                      placeholder="请选择"
                      default-value=""
                    >
                      <a-select-option value="">请选择</a-select-option>
                      <a-select-option value="0">未共享</a-select-option>
                      <a-select-option value="1">共享中</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :md="9" :sm="24">
                  <a-form-item label="创建时间">
                    <a-range-picker
                      v-model="queryParam.queryTime"
                      :ranges="{
                        Today: [moment(), moment()],
                        'This Month': [moment(), moment().endOf('month')],
                      }"
                      :placeholder="['开始时间', '结束时间']"
                      okText="确认"
                      show-time
                      format="YYYY-MM-DD HH:mm:ss"
                      @change="queryTimeChange"
                    />
                  </a-form-item>
                </a-col>
                <a-col :md="5" :sm="24">
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
              <a-button v-action:dfile:add v-if="type != '1'" @click="add()"
                >上传</a-button
              >
              <a-button
                v-action:dfile:edit
                type="primary"
                v-if="type != '1'"
                @click="edits()"
                >文档标注</a-button
              >
              <a-button
                v-action:dfile:del
                type="danger"
                v-if="type != '1'"
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
                    v-action:dfile:info
                    @click="() => download(record)"
                    >下载</a-button
                  >
                  <a-button
                    size="small"
                    type="primary"
                    v-if="type != '1'"
                    v-action:dfile:edit
                    @click="() => edit(record)"
                    >标注</a-button
                  >
                  <a-button
                    size="small"
                    type="danger"
                    v-if="type != '1'"
                    v-action:dfile:del
                    @click="() => del(record)"
                    >删除</a-button
                  >
                  <a-button
                    size="small"
                    type="primary"
                    v-if="type != '1'"
                    v-show="record.is_share == '0'"
                    @click="() => updateShare(record, '1')"
                    >共享</a-button
                  >
                  <a-button
                    size="small"
                    v-if="type != '1'"
                    v-show="record.is_share == '1'"
                    @click="() => updateShare(record, '0')"
                    >取消共享</a-button
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
import axios from "axios";
import { mapState } from "vuex";
import { getListPage, delData, updateShare } from "@/api/document/dfile";
import { ACCESS_TOKEN } from "@/store/mutation-types";
import storage from "store";
import UoloaderForm from "./Uoloader";
import EditForm from "./Edit";
import ShareForm from "./Share";
import DicTree from "../dclassify/Tree";
import moment from "moment";
import { saveAs } from "file-saver";
const columns = [
  {
    title: "分类名称",
    dataIndex: "classify_name",
    sorter: true,
    ellipsis: true,
  },
  {
    title: "文件名称",
    dataIndex: "name",
    sorter: true,
    ellipsis: true,
  },
  {
    title: "文件类型",
    dataIndex: "file_type",
    sorter: true,
    ellipsis: true,
  },
  {
    title: "文件大小",
    dataIndex: "file_size",
    customRender: (text, row, index) => {
      let size = text / 1024 / 1024;
      return size.toFixed(2) + "M";
    },
  },
  {
    title: "是否共享",
    dataIndex: "is_share",
    customRender: (text, row, index) => {
      if (text == "0") {
        return "未共享";
      } else if (text == "1") {
        return "共享中";
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
    width: "280px",
  },
];

const shareColumns = [
  {
    title: "分类名称",
    dataIndex: "classify_name",
    sorter: true,
    ellipsis: true,
  },
  {
    title: "文件名称",
    dataIndex: "name",
    sorter: true,
    ellipsis: true,
  },
  {
    title: "文件类型",
    dataIndex: "file_type",
    sorter: true,
    ellipsis: true,
  },
  {
    title: "文件大小",
    dataIndex: "file_size",
    customRender: (text, row, index) => {
      let size = text / 1024 / 1024;
      return size.toFixed(2) + "M";
    },
  },
  {
    title: "分享人",
    dataIndex: "user_name",
  },
  {
    title: "创建时间",
    dataIndex: "create_time",
  },
  {
    title: "操作",
    dataIndex: "operation",
    scopedSlots: { customRender: "operation" },
    width: "100px",
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
        is_share: "",
        start_time: "",
        end_time: "",
      },
      classify_id: "",
      classify_name: "",
    };
  },
  components: {
    UoloaderForm,
    EditForm,
    DicTree,
    ShareForm,
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
      if (this.type == 1) {
        this.columns = shareColumns;
      } else {
        this.columns = columns;
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
    if (this.type == 1) {
      this.columns = shareColumns;
    } else {
      this.columns = columns;
    }
  },
  methods: {
    moment,
    //时间段改变赋值
    queryTimeChange(dates, dateStrings) {
      this.queryParam.start_time = dateStrings[0];
      this.queryParam.end_time = dateStrings[1];
    },
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
      this.classify_id = id;
      this.classify_name = name;
      this.fetch({ pageSize: 10 });
    },
    fetch(params = {}) {
      this.loading = true;
      getListPage({
        classify_id: this.classify_id,
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
    download(record) {
      axios({
        method: "post",
        url: process.env.VUE_APP_API_URL + "/document/dfile/downloadFile",
        data: record,
        responseType: "blob",
      })
        .then((response) => {
          if (!response.data) {
            return;
          }
          saveAs(new Blob([response.data]), record.name);

          // let url = window.URL.createObjectURL(new Blob([response.data]));
          // let link = document.createElement("a");
          // link.style.display = "none";
          // link.href = url;
          // link.setAttribute("download", record.name);

          // document.body.appendChild(link);
          // link.click();
        })
        .catch((error) => {});
    },
    edit(record) {
      this.dialogEditData(record);
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
        this.dialogEditData(record);
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
      if (
        this.classify_id == "" ||
        this.classify_id == null ||
        this.classify_id == "1"
      ) {
        that.$error({
          title: "提示：",
          content: "请先选择左侧文档分类",
        });
        return;
      }
      record = {
        classify_id: this.classify_id,
        classify_name: this.classify_name,
        ...record,
        type: this.type,
      };
      this.$dialog(
        UoloaderForm,
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
          width: 1200,
          centered: true,
          maskClosable: false,
          okText: "确认",
          cancelText: "取消",
        }
      );
    },
    dialogEditData(record) {
      const that = this;
      if (
        this.classify_id == "" ||
        this.classify_id == null ||
        this.classify_id == "1"
      ) {
        that.$error({
          title: "提示：",
          content: "请先选择左侧文档分类",
        });
        return;
      }
      record = {
        classify_id: this.classify_id,
        classify_name: this.classify_name,
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
          width: 600,
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
    updateShare(record, is_share) {
      const that = this;
      if (is_share == "0") {
        that.$confirm({
          title: "提示：",
          content: (h) => <div style="color:red;">确定要取消文档分享吗？</div>,
          okText: "确认",
          cancelText: "取消",
          onOk() {
            updateShare({ ...record, is_share: "0" }).then((response) => {
              that.fetch({ pageSize: 10 });
            });
          },
          onCancel() {
            console.log("Cancel");
          },
        });
      } else {
        record = {
          ...record,
          is_share: is_share,
        };
        this.$dialog(
          ShareForm,
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
            width: 600,
            centered: true,
            maskClosable: false,
            okText: "确认",
            cancelText: "取消",
          }
        );
      }
    },
    getTreeList() {
      this.$refs.child.getTreeList({ type: this.type });
    },
  },
};
</script>
<style scoped>
</style>