<template>
  <a-form-model
    ref="ruleForm"
    :model="form"
    :rules="rules"
    :label-col="labelCol"
    :wrapper-col="wrapperCol"
  >
    <a-form-model-item ref="classify" label="所属分类" prop="classify">
      <a-select v-model="form.classify" placeholder="请选择所属分类">
        <a-select-option
          v-for="(item, index) in classify_data"
          :key="index"
          :value="item.id"
        >
          {{ item.name }}
        </a-select-option>
      </a-select>
    </a-form-model-item>
    <a-form-model-item
      ref="table_comment"
      label="表单名称"
      prop="table_comment"
    >
      <a-input
        v-model="form.table_comment"
        @blur="
          () => {
            $refs.table_comment.onFieldBlur();
          }
        "
      />
    </a-form-model-item>
    <a-form-model-item ref="table_name" label="数据表名称" prop="table_name">
      <a-input
        v-model="form.table_name"
        @blur="
          () => {
            $refs.table_name.onFieldBlur();
          }
        "
      />
    </a-form-model-item>
    <a-form-model-item ref="table_content" label="表内容" prop="table_content">
      <a-input
        v-model="form.table_content"
        @blur="
          () => {
            $refs.table_content.onFieldBlur();
          }
        "
      />
    </a-form-model-item>
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
import { saveOrUpdate } from "@/api/customize/vform";
import { findDictionaryList } from "@/api/system/dictionary";
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
      wrapperCol: { span: 14 },
      other: "",
      mybol: false,
      headers: {
        authorization: "authorization-text",
      },
      uploading: false,
      form: {
        id: "",
        classify: "",
        table_comment: "",
        table_name: "",
        table_content: "",
        order_by: "",
        remark: "",
      },
      classify_data: [],
      rules: {
        classify: [
          { required: true, message: "必填项不能为空", trigger: "blur" },
          {
            min: 0,
            max: 120,
            message: "长度不得大于120个字符",
            trigger: "blur",
          },
        ],
        table_comment: [
          { required: true, message: "必填项不能为空", trigger: "blur" },
          {
            min: 0,
            max: 120,
            message: "长度不得大于120个字符",
            trigger: "blur",
          },
        ],
        table_name: [
          { required: true, message: "必填项不能为空", trigger: "blur" },
          {
            min: 0,
            max: 120,
            message: "长度不得大于120个字符",
            trigger: "blur",
          },
        ],
        table_content: [
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
    initData() {
      findDictionaryList({ parent_code: "customize_table" }).then(
        (response) => {
          this.classify_data = response.data.data;
        }
      );
    },
  },
};
</script>
