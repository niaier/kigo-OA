<template>
  <div>
    <a-card :bordered="false">
      <a-table
        :row-selection="rowSelection"
        :columns="columns"
        :data-source="data"
        :loading="loading"
        @change="handleTableChange"
        bordered
      >
        <template
          v-for="col in ['name', 'age', 'address']"
          :slot="col"
          slot-scope="text, record, index"
        >
        </template>
        <template slot="operation" slot-scope="text, record, index">
          <div class="editable-row-operations">
            <a-space :size="4">
              <div slot="actions">
                <a-space :size="4">
                  <a-button
                    size="small"
                    type="primary"
                    v-show="record.node_type == 'UserTask'"
                    @click="() => setAssignment(record)"
                    >设置办理人</a-button
                  >
                </a-space>
              </div>
            </a-space>
          </div>
        </template>
      </a-table>
    </a-card>
  </div>
</template>
<script>
import { findNodes } from "@/api/activiti/modeler";
import axios from "axios";
import AssignmentForm from "./Assignment";
import { formatCtsDate } from "@/utils/date.js";
const columns = [
  {
    title: "节点名称",
    dataIndex: "node_name",
    sorter: true,
    // ellipsis: true,
  },
  {
    title: "节点ID",
    dataIndex: "node_key",
    // ellipsis: true,
  },
  {
    title: "节点类型",
    dataIndex: "node_type",
    customRender: (text, row, index) => {
      if (text == "StartEvent") {
        return "启动节点";
      } else if (text == "UserTask") {
        return "用户任务节点";
      }
    },
  },
  {
    title: "办理人信息",
    dataIndex: "executionid",
    // ellipsis: true,
    customRender: (text, row, index) => {
      if (row.node_type == "StartEvent") {
        return "不可设置";
      } else if (row.node_type == "UserTask") {
        if (row.assignmentPd == "" || row.assignmentPd == null) {
          return "未设置";
        } else {
          if (row.assignmentPd.type == "0") {
            return "静态指定";
          } else if (row.assignmentPd.type == "1") {
            if (row.assignmentPd.assign_mode == "0") {
              return "所有人员中选择（根据组织选择）";
            } else if (row.assignmentPd.assign_mode == "1") {
              return "组织选择（指定组织父节点）";
            } else if (row.assignmentPd.assign_mode == "2") {
              return "用户组选择（选择指定组内成员）";
            } else if (row.assignmentPd.assign_mode == "3") {
              return "发起人本组织选择";
            } else if (row.assignmentPd.assign_mode == "4") {
              return "部门经理";
            } else if (row.assignmentPd.assign_mode == "5") {
              return "上级领导";
            } else if (row.assignmentPd.assign_mode == "6") {
              return "分管领导";
            } else if (row.assignmentPd.assign_mode == "7") {
              return "流程发起人";
            } else if (row.assignmentPd.assign_mode == "8") {
              return "指定范围选择";
            } else if (row.assignmentPd.assign_mode == "9") {
              return "代理人（选择单用户）";
            } else if (row.assignmentPd.assign_mode == "10") {
              return "候选人（选择多用户）";
            } else if (row.assignmentPd.assign_mode == "11") {
              return "候选组（选择多组织）";
            }
          }
        }
      }
    },
  },
  {
    title: "操作",
    dataIndex: "operation",
    scopedSlots: { customRender: "operation" },
    width: "160px",
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
    };
  },
  components: {},
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
    handleTableChange(filters, sorter) {
      this.fetch({
        sortField: sorter.field,
        sortOrder: sorter.order,
        ...filters,
      });
    },
    fetch(params = {}) {
      this.loading = true;
      findNodes({
        ...params,
        model_id: this.record.id,
      }).then((response) => {
        // Read total count from server
        // pagination.total = data.totalCount;
        this.loading = false;
        this.data = response.data;
      });
    },
    setAssignment(data) {
      const that = this;
      let record = {model_id:this.record.id,node_key:data.node_key,node_type:data.node_type};
      this.$dialog(
        AssignmentForm,
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
    handleCancel(e) {
      this.visible = false;
      this.complete_visible = false;
    },
  },
};
</script>
<style scoped>
</style>