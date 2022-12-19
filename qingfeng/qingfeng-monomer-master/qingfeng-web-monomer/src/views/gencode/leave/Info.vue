<template>
  <a-form-model
    ref="ruleForm"
    :model="form"
    :rules="rules"
    :label-col="labelCol"
    :wrapper-col="wrapperCol"
  >
    <a-form-model-item ref="title" label="标题" prop="title">
      <a-input
        disabled
        v-model="form.title"
        @blur="
          () => {
            $refs.title.onFieldBlur();
          }
        "
      />
    </a-form-model-item>
    <a-form-model-item ref="leave_type" label="请假类型" prop="leave_type">
      <a-select
        disabled
        v-model="form.leave_type"
        placeholder="请选择leave_type"
      >
        <a-select-option
          v-for="(item, index) in leave_type_data"
          :key="index"
          :value="item.id"
        >
          {{ item.name }}
        </a-select-option>
      </a-select>
    </a-form-model-item>
    <a-form-model-item label="请假原因" prop="leave_cause">
      <a-input disabled v-model="form.leave_cause" type="textarea" />
    </a-form-model-item>
    <a-form-model-item ref="order_by" label="排序" prop="order_by">
      <a-input
        disabled
        v-model="form.order_by"
        @blur="
          () => {
            $refs.order_by.onFieldBlur();
          }
        "
      />
    </a-form-model-item>
    <a-form-model-item label="备注" prop="remark">
      <a-input disabled v-model="form.remark" type="textarea" />
    </a-form-model-item>
  </a-form-model>
</template>
<script>
import { findInfo } from "@/api/gencode/leave";
import { findDictionaryList } from "@/api/system/dictionary";
export default {
  // 声明当前子组件接收父组件传递的属性
  props: {
    record: {
      type: Object,
      default: null,
    },
    businessKey: {
      type: String,
      default: null,
    },
  },
  data() {
    return {
      labelCol: { span: 4 },
      wrapperCol: { span: 14 },
      other: "",
      mybol: false,
      headers: {
        authorization: "authorization-text",
      },
      uploading: false,
      form: {
        id: "",
        title: "",
        leave_type: "",
        leave_cause: "",
        order_by: "",
        remark: "",
      },
      leave_type_data: [],
      rules: {
        title: [
          { required: true, message: "必填项不能为空", trigger: "blur" },
          {
            min: 0,
            max: 120,
            message: "长度不得大于120个字符",
            trigger: "blur",
          },
        ],
        leave_type: [
          { required: true, message: "必填项不能为空", trigger: "blur" },
          {
            min: 0,
            max: 120,
            message: "长度不得大于120个字符",
            trigger: "blur",
          },
        ],
        leave_cause: [
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
    if (this.record != null) {
      this.form = this.record;
    } else {
      this.findInfo(this.businessKey);
    }
    this.initData();
  },
  methods: {
    resetForm() {
      this.$refs.ruleForm.resetFields();
    },
    initData() {
      const options = [
        { id: "0", name: "事假" },
        { id: "1", name: "病假" },
        { id: "2", name: "婚假" },
      ];
      this.leave_type_data = options;
    },
    findInfo(businessKey) {
      if (businessKey != "" && businessKey != null) {
        let bKey = businessKey.split(":")[3];
        findInfo({
          id:bKey,
        }).then((response) => {
          this.form = response.data.data;
        });
      }
    },
  },
};
</script>
