<template>
  <div>
    <div v-show="menu_success">
      <page-header-wrapper :title="menu_data.name" content="" :breadcrumb="{}">
        <a-card :bordered="false">
          <div class="table-page-search-wrapper">
            <a-form layout="inline">
              <a-row :gutter="24">
                <template v-for="item in menu_data.fieldPdList">
                  <a-col
                    :md="6"
                    :sm="24"
                    v-if="
                      item.field_query == 'true' &&
                      'select,checkbox,radio'.indexOf(item.field_type) != -1
                    "
                    :key="item.id"
                  >
                    <a-form-item :label="item.field_comment">
                      <a-select
                        v-model="queryParam[item.field_name]"
                        placeholder="请选择"
                        default-value=""
                      >
                        <a-select-option value="">请选择</a-select-option>
                        <a-select-option
                          v-for="(item, index) in item.options"
                          :key="index"
                          :value="item.value"
                          >{{ item.label }}</a-select-option
                        >
                      </a-select>
                    </a-form-item>
                  </a-col>
                  <a-col
                    :md="6"
                    :sm="24"
                    v-if="
                      item.field_query == 'true' &&
                      'input,textarea,number,rate,slider,editor,switch,date,time'.indexOf(
                        item.field_type
                      ) != -1
                    "
                    :key="item.id"
                  >
                    <a-form-item :label="item.field_comment">
                      <a-input
                        v-model="queryParam[item.field_name]"
                        :placeholder="item.field_comment"
                      />
                    </a-form-item>
                  </a-col>
                </template>

                <a-col
                  :md="6"
                  :sm="24"
                  v-if="
                    menu_data.status_type == '1' || menu_data.status_type == '2'
                  "
                >
                  <a-form-item label="使用状态">
                    <a-select
                      v-model="queryParam.status"
                      placeholder="请选择"
                      default-value=""
                    >
                      <a-select-option value="">请选择</a-select-option>
                      <a-select-option value="0">启用</a-select-option>
                      <a-select-option value="1">停用</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :md="9" :sm="24" v-if="menu_data.open_timequery == '0'">
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
              <a-button v-action:vdata:add @click="add()">添加</a-button>
              <a-button v-action:vdata:edit type="primary" @click="edits()"
                >编辑</a-button
              >
              <a-button v-action:vdata:del type="danger" @click="dels()"
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
            <a
              slot="name"
              @click="() => info(record)"
              slot-scope="text, record, index"
              >{{ text }}</a
            >
            <template slot="operation" slot-scope="text, record, index">
              <div class="editable-row-operations">
                <a-space size="small">
                  <a-button
                    size="small"
                    v-action:vdata:info
                    @click="() => info(record)"
                    >详情</a-button
                  >
                  <a-button
                    size="small"
                    type="primary"
                    v-if="
                      record.processStatus == '0' ||
                      record.processStatus == '' ||
                      record.processStatus == undefined
                    "
                    v-action-dauth:[{...record,auth:`vdata:edit`}]
                    @click="() => edit(record)"
                    >编辑</a-button
                  >
                  <a-button
                    size="small"
                    type="danger"
                    v-if="
                      record.processStatus == '0' ||
                      record.processStatus == '' ||
                      record.processStatus == undefined
                    "
                    v-action-dauth:[{...record,auth:`vdata:del`}]
                    @click="() => del(record)"
                    >删除</a-button
                  >
                  <a-button
                    size="small"
                    type="danger"
                    v-if="menu_data.status_type == '2' && record.status == '0'"
                    v-action:vdata:status
                    @click="() => updateStatus(record.id, '1')"
                    >禁用</a-button
                  >
                  <a-button
                    size="small"
                    type="primary"
                    v-if="menu_data.status_type == '2' && record.status == '1'"
                    v-action:vdata:status
                    @click="() => updateStatus(record.id, '0')"
                    >启用</a-button
                  >
                  <a-button
                    size="small"
                    type="danger"
                    v-if="menu_data.status_type == '1' && record.status == '0'"
                    v-action:vdata:status
                    disabled
                    >禁用</a-button
                  >
                  <a-button
                    size="small"
                    type="primary"
                    v-if="menu_data.status_type == '1' && record.status == '1'"
                    v-action:vdata:status
                    @click="() => updateStatus(record.id, '0')"
                    >启用</a-button
                  >
                </a-space>
              </div>
            </template>
          </a-table>
        </a-card>
      </page-header-wrapper>
    </div>
    <div v-show="!menu_success">
      <a-result
        status="404"
        title="参数配置错误，请检查配置"
        sub-title="请检查菜单配置下项，菜单规则【/customize/vdata/自定义菜单ID】"
      >
      </a-result>
    </div>
    <a-modal
      v-model="apply_visible"
      title="发起流程"
      :footer="null"
      width="1000px"
      on-ok="handleOk"
      :destroyOnClose="true"
    >
      <template slot="footer"> </template>
      <p-edit-form
        :record="record"
        @finishResponse="finishResponse"
      ></p-edit-form>
    </a-modal>
  </div>
</template>

<script>
import { mapState } from "vuex";
import {
  findVMenuInfo,
  getListPage,
  findLinkFormList,
  delData,
  updateStatus,
} from "@/api/customize/vdata";
import { findDictionaryList } from "@/api/system/dictionary";
import { findDataAuth } from "@/core/baseAction.js";
import storage from "store";
import EditForm from "./Edit";
import InfoForm from "./Info";
import PEditForm from "./PEdit";
import PInfoForm from "./PInfo";
import { forEach } from "min-dash";
import moment from "moment";
import { isNumber } from "@/utils/index.js";

export default {
  data() {
    return {
      menu_success: true,
      menu_id: "",
      menu_data: {},
      dynamicData: {},

      data: [],
      pagination: {},
      loading: false,
      columns: [],
      selectedRows: [],
      queryParam: {},

      apply_visible: false,
      record: {},
      process_key: "",
    };
  },
  components: {
    EditForm,
    InfoForm,
    PEditForm,
    PInfoForm,
  },
  computed: {
    ...mapState({
      authName: (state) => state.user.authName,
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
  watch: {
    $route: "fetchData",
  },
  created() {
    var self = this;
    self.fetchData();
  },
  mounted() {
    // this.fetchData();
  },
  inject: ["reload"],
  methods: {
    moment,
    //时间段改变赋值
    queryTimeChange(dates, dateStrings) {
      this.queryParam.start_time = dateStrings[0];
      this.queryParam.end_time = dateStrings[1];
    },
    //查询数据
    fetchData() {
      let path = this.$route.path;
      let menu_id = path.substring(path.indexOf("/vdata/") + 7);
      menu_id = menu_id.replaceAll("/", "");
      if (menu_id.length != 32) {
        this.menu_success = false;
        return;
      }
      this.menu_id = menu_id;
      //查询菜单配置信息
      this.findVMenuData(menu_id);
    },
    //查询菜单功能信息，根据菜单功能信息初始化页面信息（查询字段、展示字段等）
    findVMenuData(menu_id) {
      const columns = [];
      findVMenuInfo({ menu_id: menu_id }).then((response) => {
        this.menu_data = response.data;
        if (this.menu_data.open_process == "0") {
          this.process_key = this.menu_data.process_id;
        }
        this.menu_data.fieldPdList.forEach((item) => {
          if (item.dynamic == "false") {
            let options = JSON.parse(item.options);
            item.options = options;
          } else if (item.dynamic == "true") {
            findDictionaryList({ parent_code: item.dynamicKey }).then(
              (response) => {
                item.options = response.data.data;
                item.options.forEach((op) => {
                  op.label = op.name;
                  op.value = op.id;
                });
                this.dynamicData[item.dynamicKey] = item.options;
              }
            );
          }

          if (item.field_list == "true") {
            if (
              "input,textarea,number,rate,slider,editor,switch,date,time".indexOf(
                item.field_type
              ) != -1
            ) {
              let data = {
                title: item.field_comment,
                dataIndex: item.field_name,
                ellipsis: true,
              };
              if (
                item.field_width != "" &&
                item.field_width != undefined &&
                isNumber(item.field_width)
              ) {
                data.width = item.field_width + "px";
              }
              if (item.field_link == "true") {
                data.scopedSlots = { customRender: "name" };
              }
              columns.push(data);
            } else if ("select,checkbox,radio".indexOf(item.field_type) != -1) {
              let data = {
                title: item.field_comment,
                dataIndex: item.field_name,
                ellipsis: true,
                customRender: (text, row, index) => {
                  let label = "";
                  if (Array.isArray(item.options)) {
                    item.options.forEach((op) => {
                      if (op.value == text) {
                        label = op.label;
                      }
                    });
                  }
                  return label;
                },
              };
              if (
                item.field_width != "" &&
                item.field_width != undefined &&
                isNumber(item.field_width)
              ) {
                data.width = item.field_width + "px";
              }
              if (item.field_link == "true") {
                data.scopedSlots = { customRender: "name" };
              }
              columns.push(data);
            }
          }
        });
        let dealStatus = {
          title: "当前办理状态",
          dataIndex: "dealStatus",
          ellipsis: true,
        };
        let assignee = {
          title: "当前办理人",
          dataIndex: "assignee",
          ellipsis: true,
        };
        let dealTime = {
          title: "起办时间",
          dataIndex: "dealTime",
          ellipsis: true,
        };
        if (
          this.menu_data.status_type == "1" ||
          this.menu_data.status_type == "2"
        ) {
          let status = {
            title: "状态",
            dataIndex: "status",
            customRender: (text, row, index) => {
              if (text == "0") {
                return "启用";
              } else {
                return "停用";
              }
            },
          };
          columns.push(status);
        }

        let time = {
          title: "创建时间",
          dataIndex: "create_time",
        };
        let width = 200;
        if (
          this.menu_data.status_type == "1" ||
          this.menu_data.status_type == "2"
        ) {
          width = 250;
        }
        let operate = {
          title: "操作",
          dataIndex: "operation",
          scopedSlots: { customRender: "operation" },
          width: width + "px",
        };
        if (this.menu_data.open_process == "0") {
          columns.push(dealStatus);
          columns.push(assignee);
          columns.push(dealTime);
        } else {
          columns.push(time);
        }
        columns.push(operate);
        //查询初始化关联表信息
        this.initLinkFormList(this.menu_data.table_id);
        this.columns = columns;
        this.fetch({ pageSize: 10 });
      });
    },
    initLinkFormList(table_id) {
      //查询关联表-处理关联表的动态数据信息
      findLinkFormList({ table_id: table_id }).then((response) => {
        let linkFormData = response.data;
        linkFormData.forEach((form_data) => {
          form_data.vfields.forEach((item) => {
            if ("select,checkbox,radio".indexOf(item.field_type) != -1) {
              if (item.dynamic == "true") {
                findDictionaryList({ parent_code: item.dynamicKey }).then(
                  (response) => {
                    item.options = response.data.data;
                    item.options.forEach((op) => {
                      op.label = op.name;
                      op.value = op.id;
                    });
                    this.dynamicData[item.dynamicKey] = item.options;
                    this.$forceUpdate();
                  }
                );
              }
            }
          });
        });
      });
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
    fetch(params = {}) {
      this.loading = true;
      getListPage({ ...params, menu_id: this.menu_id }).then((response) => {
        const pagination = { ...this.pagination };
        // Read total count from server
        // pagination.total = data.totalCount;
        pagination.total = response.data.data.total;
        this.loading = false;
        this.data = response.data.data.rows;
        this.pagination = pagination;
      });
    },
    finishResponse(response) {
      this.apply_visible = false;
      this.fetch({ pageSize: 10 });
    },
    info(record) {
      record = {
        ...record,
        menu_id: this.menu_id,
        table_id: this.menu_data.table_id,
        dynamicData: this.dynamicData,
        table_content: this.menu_data.vform.table_content,
      };
      let component = InfoForm;
      if (this.menu_data.open_process == "0") {
        component = PInfoForm;
      }
      this.$dialog(
        component,
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
          width: 1000,
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
      this.record = {
        ...record,
        process_key: this.process_key,
        menu_id: this.menu_id,
        table_id: this.menu_data.table_id,
        dynamicData: this.dynamicData,
        table_content: this.menu_data.vform.table_content,
      };
      if (this.menu_data.open_process == "0") {
        this.apply_visible = true;
      } else {
        let record = this.record;
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
            title: this.menu_data.name,
            width: 1000,
            centered: true,
            maskClosable: false,
            okText: "确认",
            cancelText: "取消",
          }
        );
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
          delData({ ids: ids, table_id: that.menu_data.table_id }).then(
            (response) => {
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
            }
          );
        },
        onCancel() {},
      });
    },
    updateStatus(id, status) {
      const that = this;
      updateStatus(
        id,
        status,
        that.menu_data.status_type,
        that.menu_data.vform.table_name
      ).then((response) => {
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
  },
};
</script>

