<template>
  <a-form-model
    ref="ruleForm"
    :model="form"
    :rules="rules"
    :label-col="labelCol"
    :wrapper-col="wrapperCol"
  >
    <a-row class="form-row" :gutter="16">
      <a-col :md="12" :lg="24">
        <a-form-model-item
          ref="name"
          label="模块名称"
          prop="name"
          :label-col="{ span: 3 }"
          :wrapper-col="{ span: 20 }"
        >
          <a-input
            v-model="form.name"
            @blur="
              () => {
                $refs.name.onFieldBlur();
              }
            "
          />
        </a-form-model-item>
      </a-col>
    </a-row>
    <a-row class="form-row" :gutter="16">
      <a-col :lg="12" :md="12" :sm="24">
        <a-form-model-item
          ref="table_id"
          label="表单"
          prop="table_id"
          @blur="
            () => {
              $refs.table_id.onFieldBlur();
            }
          "
        >
          <a-select
            v-model="form.table_id"
            placeholder="请选择表单"
            @change="selectTable"
          >
            <a-select-option
              v-for="(item, index) in formData"
              :key="index"
              :value="item.id"
            >
              {{ item.table_comment }}
            </a-select-option>
          </a-select>
        </a-form-model-item>
      </a-col>
      <a-col :lg="12" :md="12" :sm="24">
        <a-form-model-item
          ref="open_timequery"
          label="开启时间查询"
          prop="open_timequery"
        >
          <a-select v-model="form.open_timequery" placeholder="请选择">
            <a-select-option value="0"> 开启 </a-select-option>
            <a-select-option value="1"> 不开启 </a-select-option>
          </a-select>
        </a-form-model-item>
      </a-col>
    </a-row>
    <a-row class="form-row" :gutter="16">
      <a-col :lg="12" :md="12" :sm="24">
        <a-form-model-item
          ref="open_process"
          label="开启流程"
          prop="open_process"
        >
          <a-select v-model="form.open_process" placeholder="请选择" @change="openProcessChange">
            <a-select-option value="0"> 开启 </a-select-option>
            <a-select-option value="1"> 不开启 </a-select-option>
          </a-select>
        </a-form-model-item>
      </a-col>
      <a-col :lg="12" :md="12" :sm="24">
        <a-form-model-item
          v-show="form.open_process == '0'"
          ref="process_id"
          label="流程定义"
          prop="process_id"
        >
          <a-select v-model="form.process_id" placeholder="请选择">
            <a-select-option
              v-for="(item, index) in processDefinitionList"
              :key="index"
              :value="item.key"
            >
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          ref="status_type"
          label="状态类型"
          prop="status_type"
          v-show="form.open_process == '1'"
        >
          <a-select v-model="form.status_type" placeholder="请选择">
            <a-select-option value="0"> 不开启 </a-select-option>
            <a-select-option value="1"> 单启用 </a-select-option>
            <a-select-option value="2"> 多启用 </a-select-option>
          </a-select>
        </a-form-model-item>
      </a-col>
    </a-row>
    <a-row class="form-row" :gutter="16">
      <a-col :lg="12" :md="12" :sm="24">
        <a-form-model-item label="条件约束" prop="query_cond">
          <a-input v-model="form.query_cond" type="textarea" placeholder=" and name='张三' " />
        </a-form-model-item>
      </a-col>
      <a-col :lg="12" :md="12" :sm="24">
        <a-form-model-item label="表排序" prop="main_order">
          <a-input v-model="form.main_order" type="textarea" placeholder=" order by create_time desc " />
        </a-form-model-item>
      </a-col>
    </a-row>
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
        <template slot-scope="text, record, index">
          <span>
            <a-input
              :style="{ width: '80px' }"
              v-model="record.field_comment"
              placeholder="请输入字段描述"
            />
          </span>
        </template>
      </a-table-column>
      <a-table-column key="field_list" title="列表显示" data-index="field_list">
        <template slot-scope="text, record, index">
          <span>
            <a-checkbox
              v-model="record.field_list"
              @change="handleChange"
            ></a-checkbox>
          </span>
        </template>
      </a-table-column>
      <a-table-column
        key="field_query"
        title="查询展示"
        data-index="field_query"
      >
        <template slot-scope="text, record, index">
          <span>
            <a-checkbox
              v-model="record.field_query"
              @change="handleChange"
            ></a-checkbox>
          </span>
        </template>
      </a-table-column>
      <a-table-column key="query_type" title="查询方式" data-index="query_type">
        <template slot-scope="text, record, index">
          <span>
            <a-select
              :style="{ width: '80px' }"
              v-model="record.query_type"
              placeholder="查询方式"
              @change="handleChange"
            >
              <a-select-option value="="> = </a-select-option>
              <a-select-option value="!="> != </a-select-option>
              <a-select-option value=">"> > </a-select-option>
              <a-select-option value=">="> >= </a-select-option>
              <a-select-option value="<"> < </a-select-option>
              <a-select-option value="<="> <= </a-select-option>
              <a-select-option value="like"> like </a-select-option>
              <a-select-option value="is null"> is null </a-select-option>
              <a-select-option value="is not null">
                is not null
              </a-select-option>
            </a-select>
          </span>
        </template>
      </a-table-column>
      <a-table-column key="field_link" title="链接字段" data-index="field_link">
        <template slot-scope="text, record, index">
          <span>
            <a-checkbox
              v-model="record.field_link"
              @change="handleChange"
            ></a-checkbox>
          </span>
        </template>
      </a-table-column>
      <a-table-column
        key="field_width"
        title="列表宽度"
        data-index="field_width"
      >
        <template slot-scope="text, record, index">
          <span>
            <a-input
              :style="{ width: '80px' }"
              v-model="record.field_width"
              placeholder="列表宽度(px)"
            />
          </span>
        </template>
      </a-table-column>
      <a-table-column key="order_by" title="排序" data-index="order_by">
        <template slot-scope="text, record, index">
          <span>
            <a-input
              :style="{ width: '60px' }"
              v-model="record.order_by"
              placeholder="排序"
            />
          </span>
        </template>
      </a-table-column>
    </a-table>
    <div style="margin: 20px 0"></div>

    <a-form-model-item ref="order_by" label="排序" prop="order_by">
      <a-input
        v-model="form.order_by"
        @blur="
          () => {
            $refs.order_by.onFieldBlur();
          }
        "
      />
    </a-form-model-item>
    <a-form-model-item label="备注" prop="remark">
      <a-input v-model="form.remark" type="textarea" />
    </a-form-model-item>
  </a-form-model>
</template>
<script>
import {
  saveOrUpdate,
  getFieldList,
  getDefinitionList,
} from "@/api/customize/vmenu";
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
      labelCol: { span: 5 },
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
        open_timequery: "1",
        open_process: "1",
        process_id: "",
        status_type: "0",
        order_by: "",
        remark: "",
        fieldList: [],
      },
      formData: [],
      fieldData: [],
      processDefinitionList: [],
      rules: {
        table_id: [
          { required: true, message: "必填项不能为空", trigger: "blur" },
          {
            min: 0,
            max: 50,
            message: "长度不得大于50个字符",
            trigger: "blur",
          },
        ],
        name: [
          { required: true, message: "必填项不能为空", trigger: "blur" },
          {
            min: 0,
            max: 120,
            message: "长度不得大于120个字符",
            trigger: "blur",
          },
        ],
      },
    };
  },
  components: {},
  mounted() {
    if (this.record.id != undefined) {
      this.form = this.record;
      console.log(this.form);
    }
    this.initData();
  },
  methods: {
    resetForm() {
      this.$refs.ruleForm.resetFields();
    },
    onOk() {
      return new Promise((resolve) => {
        this.$refs.ruleForm.validate((valid) => {
          if (valid) {
            saveOrUpdate(this.form).then((response) => {
              resolve(true);
            });
            return true;
          } else {
            console.log("error submit!!");
            return false;
          }
        });
      });
    },
    onCancel() {
      console.log("监听了 modal cancel 事件");
      return new Promise((resolve) => {
        resolve(true);
      });
    },
    openProcessChange(value){
      if(value=='0'){
        this.form.status_type='0';
      }
    },
    initData() {
      //查询表单列表
      getList({ type: "0" }).then((response) => {
        this.formData = response.data.data;
        if (this.form.table_id == "" || this.form.table_id == undefined) {
          this.form.table_id = this.formData[0].id;
        }
        //查询字段表
        this.findField();
      });
      //查询流程定义列表
      getDefinitionList({}).then((response) => {
        this.processDefinitionList = response.data.data;
      });
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
            item.field_list = false;
          } else if (item.field_list == "true") {
            item.field_list = true;
          }
          if (item.field_query == "" || item.field_query == "false") {
            item.field_query = false;
          } else if (item.field_query == "true") {
            item.field_query = true;
          }
          if (item.field_link == "" || item.field_link == "false") {
            item.field_link = false;
          } else if (item.field_link == "true") {
            item.field_link = true;
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