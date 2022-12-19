<template>
  <div>
    <a-card :bordered="false">
      <a-descriptions title="模块菜单信息">
        <a-descriptions-item label="模块名称">{{
          form.name
        }}</a-descriptions-item>
        <a-descriptions-item label="表单名称">{{
          form.table_comment
        }}</a-descriptions-item>
        <a-descriptions-item label="状态类型">
          <span v-if="form.status_type == '0'">未开启</span>
          <span v-if="form.status_type == '1'">单启用</span>
          <span v-if="form.status_type == '2'">多启用</span>
        </a-descriptions-item>
        <a-descriptions-item label="开启流程">
          <span v-if="form.open_process == '0'">开启</span>
          <span v-if="form.open_process == '1'">未开启</span>
        </a-descriptions-item>
        <a-descriptions-item v-show="form.open_process == '0'" label="流程定义">{{
          form.process_name
        }}</a-descriptions-item>
        <a-descriptions-item label="条件约束">{{
          form.query_cond
        }}</a-descriptions-item>
        <a-descriptions-item label="表排序">{{
          form.main_order
        }}</a-descriptions-item>
      </a-descriptions>
      <a-divider style="margin-bottom: 32px" />
    </a-card>

    <a-table :data-source="form.fieldList" :pagination="false">
      <a-table-column
        key="field_name"
        title="字段名称"
        data-index="field_name"
      />
      <a-table-column
        key="field_comment"
        title="字段描述"
        data-index="field_comment"
      >
      </a-table-column>
      <a-table-column key="field_list" title="列表显示" data-index="field_list">
      </a-table-column>
      <a-table-column
        key="field_query"
        title="查询展示"
        data-index="field_query"
      >
      </a-table-column>
      <a-table-column key="query_type" title="查询方式" data-index="query_type">
      </a-table-column>
      <a-table-column key="field_link" title="链接字段" data-index="field_link">
      </a-table-column>
      <a-table-column
        key="field_width"
        title="列表宽度"
        data-index="field_width"
      >
      </a-table-column>
      <a-table-column key="order_by" title="排序" data-index="order_by">
      </a-table-column>
    </a-table>

    <a-form-model-item ref="order_by" label="排序" prop="order_by">
      {{ form.order_by }}
    </a-form-model-item>
    <a-form-model-item label="备注" prop="remark">
      {{ form.remark }}
    </a-form-model-item>
  </div>
</template>
<script>
import { getFieldList, getDefinitionList } from "@/api/customize/vmenu";
import { getList } from "@/api/customize/vform";
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
      labelCol: { span: 4 },
      wrapperCol: { span: 18 },
      other: "",
      mybol: false,
      headers: {
        authorization: "authorization-text",
      },
      uploading: false,
      form: {
        id: "",
        name: "",
        table_id: "",
        query_cond: "",
        main_order: "",
        process_id: "",
        status_type: "",
        order_by: "",
        remark: "",
        fieldList: [],
      },
      formData: [],
      fieldData: [],
      processDefinitionList: [],
    };
  },
  components: {},
  mounted() {
    if (this.record.id != undefined) {
      this.form = this.record;
    }
    this.initData();
  },
  methods: {
    onOk() {
      return new Promise((resolve) => {
        resolve(true);
      });
    },
    onCancel() {
      return new Promise((resolve) => {
        resolve(true);
      });
    },
    initData() {
      //查询字段表
      this.findField();
    },
    findField() {
      getFieldList({
        type: "0",
        table_id: this.form.table_id,
        menu_id: this.form.id,
      }).then((response) => {
        let data = response.data.data;
        data.forEach(function (item) {
          if (item.field_list == "" || item.field_list == "false") {
            item.field_list = "否";
          } else if (item.field_list == "true") {
            item.field_list = "是";
          }
          if (item.field_query == "" || item.field_query == "false") {
            item.field_query = "否";
          } else if (item.field_query == "true") {
            item.field_query = "是";
          }
          if (item.field_link == "" || item.field_link == "false") {
            item.field_link = "否";
          } else if (item.field_link == "true") {
            item.field_link = "是";
          }
        });
        this.form.fieldList = data;
        this.$forceUpdate();
      });
    },
    selectTable() {
      this.findField();
    },
    handleChange() {
      this.$forceUpdate();
    },
  },
};
</script>
<style scoped>
.ant-row {
  padding-left: 10px;
}
</style>