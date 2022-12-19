<template>
  <div class="design_div">
    <k-form-design
      showToolbarsText
      toolbarsTop
      title="青锋表单设计器"
      :showHead="false"
      :hideModel="false"
      :formData="data"
      @save="handleSave"
      @close="handleClose"
      ref="qfRef"
    />
  </div>
</template>

<script>
export default {
  data() {
    return {
      jsonData: {
        list: [
          {
            type: "input",
            label: "输入框",
            options: {
              type: "text",
              width: "100%",
              defaultValue: "",
              placeholder: "请输入",
              clearable: false,
              maxLength: null,
              addonBefore: "",
              addonAfter: "",
              hidden: false,
              disabled: false,
            },
            model: "input_1652375101596",
            key: "input_1652375101596",
            help: "",
            rules: [
              {
                required: false,
                message: "必填项",
              },
            ],
          },
          {
            type: "input",
            label: "输入框",
            options: {
              type: "text",
              width: "100%",
              defaultValue: "",
              placeholder: "请输入",
              clearable: false,
              maxLength: null,
              addonBefore: "",
              addonAfter: "",
              hidden: false,
              disabled: false,
            },
            model: "input_1652375102748",
            key: "input_1652375102748",
            help: "",
            rules: [
              {
                required: false,
                message: "必填项",
              },
            ],
          },
          {
            type: "textarea",
            label: "文本框",
            options: {
              width: "100%",
              minRows: 4,
              maxRows: 6,
              maxLength: null,
              defaultValue: "",
              clearable: false,
              hidden: false,
              disabled: false,
              placeholder: "请输入",
            },
            model: "textarea_1652375103834",
            key: "textarea_1652375103834",
            help: "",
            rules: [
              {
                required: false,
                message: "必填项",
              },
            ],
          },
        ],
        config: {
          layout: "horizontal",
          labelCol: {
            xs: 4,
            sm: 4,
            md: 4,
            lg: 4,
            xl: 4,
            xxl: 4,
          },
          labelWidth: 100,
          labelLayout: "flex",
          wrapperCol: {
            xs: 18,
            sm: 18,
            md: 18,
            lg: 18,
            xl: 18,
            xxl: 18,
          },
          hideRequiredMark: false,
          customStyle: "",
          table_name: "table_1652375100204",
          table_comment: "table_1652375100204",
          table_type: "0",
        },
      },
    };
  },
  mounted() {
    this.importData();
  },
  methods: {
    handleSave(values) {
      //alert("触发保存方法");
      //console.log(values);
      //this.$refs.qfRef.handleReset();
      console.log(this.$refs.qfRef.getValue());
    },
    handleClose(values) {},
    importData() {
      console.log("*************");
      console.log(this.jsonData);
      this.$refs.qfRef.handleSetData(this.jsonData);
    },
  },
};
</script>

<style>
.design_div {
  margin-top: -20px;
}
.operating-area {
  background-color: #f6f6f6;
}
.k-form-design {
  margin-top: -20px;
}
</style>