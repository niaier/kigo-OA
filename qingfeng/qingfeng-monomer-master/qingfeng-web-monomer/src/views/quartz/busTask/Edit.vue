<template>
  <div>
    <a-form-model
      ref="ruleForm"
      :model="form"
      :rules="rules"
      :label-col="labelCol"
      :wrapper-col="wrapperCol"
    >
      <a-form-model-item ref="job_name" label="任务名称" prop="job_name">
        <a-input
          v-model="form.job_name"
          @blur="
            () => {
              $refs.job_name.onFieldBlur();
            }
          "
        />
      </a-form-model-item>
      <a-form-model-item ref="notice_user" label="接收人" prop="notice_user">
        <a-input
          v-model="form.notice_user"
          @blur="
            () => {
              $refs.notice_user.onFieldBlur();
            }
          "
        />
      </a-form-model-item>
      <a-form-model-item
        ref="cron_expression"
        label="执行时间表达式"
        prop="cron_expression"
      >
        <a-input
          v-model="form.cron_expression"
          @input="handleinput"
          @blur="
            () => {
              $refs.cron_expression.onFieldBlur();
            }
          "
        >
          <a-icon
            slot="prefix"
            type="schedule"
            title="corn控件"
            @click="openModal"
          />
          <a-icon
            v-if="form.cron_expression"
            slot="suffix"
            type="close-circle"
            @click="handleEmpty"
            title="清空"
          />
        </a-input>
      </a-form-model-item>
      <a-form-model-item label="任务描述" prop="description">
        <a-input v-model="form.description" type="textarea" />
      </a-form-model-item>
      <a-form-model-item label="排序" prop="order_by">
        <a-input-number
          id="inputNumber"
          v-model="form.order_by"
          :min="0"
          :max="1000"
          @change=""
        />
      </a-form-model-item>
      <a-form-model-item label="备注" prop="remark">
        <a-input v-model="form.remark" type="textarea" />
      </a-form-model-item>
    </a-form-model>

    <AntCron ref="innerVueCron" :data="afterCron" @ok="handleOK"></AntCron>
  </div>
</template>
<script>
import { saveOrUpdate } from "@/api/quartz/busTask";
import AntCron from "../Cron/antCron/AntCron";
import { replaceWeekName } from "../Cron/antCron/validator";
export default {
  components: {
    AntCron,
  },
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
      form: {
        id: "",
        job_name: "",
        notice_user: "",
        cron_expression: "",
        description: "",
        order_by: "1",
        remark: "",
      },
      afterCron: "",
      rules: {
        job_name: [
          { required: true, message: "必填项不能为空", trigger: "blur" },
          { min: 0, max: 50, message: "长度不得大于50个字符", trigger: "blur" },
        ],
        notice_user: [
          { required: true, message: "必填项不能为空", trigger: "blur" },
          { min: 0, max: 50, message: "长度不得大于50个字符", trigger: "blur" },
        ],
        cron_expression: [
          { required: true, message: "必填项不能为空", trigger: "blur" },
          { min: 0, max: 50, message: "长度不得大于50个字符", trigger: "blur" },
        ],
        description: [
          {
            min: 0,
            max: 500,
            message: "长度不得大于500个字符",
            trigger: "blur",
          },
        ],
      },
    };
  },
  mounted() {
    this.form = this.record;
    this.form.oldJobName = this.record.job_name;
    this.form.oldJobGroup = this.record.job_group;
  },
  methods: {
    resetForm() {
      this.$refs.ruleForm.resetFields();
    },
    onOk() {
      console.log("监听了 modal ok 事件");
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
    openModal() {
      this.$refs.innerVueCron.show();
    },
    handleOK(val) {
      this.form.cron_expression = val;
      console.log(this.form.cron_expression);
      this.$forceUpdate();
    },
    handleinput(evt) {
      this.form.cron_expression = evt.target.value;
    },
    handleEmpty() {
      this.handleOK("");
    },
  },
};
</script>
