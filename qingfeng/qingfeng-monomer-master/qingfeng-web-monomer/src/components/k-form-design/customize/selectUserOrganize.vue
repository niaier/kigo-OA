<template>
  <div>
    <a-input
      hidden
      v-model="value"
      @input="handleChange"
    />
    <a-input
      v-model="value.split(':')[1]"
      placeholder="请输入"
      v-if="disabled"
      disabled
    />
    <a-input
      v-model="name"
      readOnly
      placeholder="请输入"
      v-if="!disabled"
      @click="selectData"
      @input="handleChange"
    />
  </div>
</template>
<script>
import SelectOneUser from "@/views/system/User/SelectOneUser";
import SelectMoreUser from "@/views/system/User/SelectMoreUser";
import SelectOneOrganize from "@/views/system/Organize/SelectOneOrganize";
import SelectMoreOrganize from "@/views/system/Organize/SelectMoreOrganize";
export default {
  name: "cc",
  props: ["record", "value","disabled"],
  // props: {
  //   record: {
  //     type: Object,
  //     require: true
  //   },
  //   value: {
  //     type: String,
  //     default: ''
  //   }
  // },
  data() {
    return {
      name:''
    };
  },
  methods: {
    handleChange(e) {
      console.log(e.target.value);
      // 使用 onChange 事件修改值
      this.$emit("change", e.target.value);
    },
    selectData() {
      if (this.record.options.option_type == "0") {
        const user = {
          user_id: this.value.split(":")[0],
          user_name: this.value.split(":")[1]
        };
        this.dialog(SelectOneUser, user);
      } else if (this.record.options.option_type == "1") {
        const organize = {
          organize_id: this.value.split(":")[0],
          organize_name: this.value.split(":")[1]
        };
        this.dialog(SelectOneOrganize, organize);
      } else if (this.record.options.option_type == "2") {
        const users = {
          user_ids: this.value.split(":")[0],
          user_names: this.value.split(":")[1]
        };
        this.dialog(SelectMoreUser, users);
      } else if (this.record.options.option_type == "3") {
        const organizes = {
          organize_ids: this.value.split(":")[0],
          organize_names: this.value.split(":")[1]
        };
        console.log(organizes)
        this.dialog(SelectMoreOrganize, organizes);
      }
    },
    dialog(component, record) {
      const that = this;
      this.$dialog(
        component,
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
            initValue(value, type) {
              that.name=value.split(":")[1];
              that.value=value;
              that.$emit("change", value);
            },
          },
        },
        // modal props
        {
          title: "操作",
          width: 800,
          height: 500,
          centered: true,
          maskClosable: false,
          okText: "确认",
          cancelText: "取消",
        }
      );
    },
  },
  mounted() {
    // 打印接收的options
    console.log(this.value);
    this.disabled = this.record.options.disabled;
    // console.log(this.value)
    this.name = this.value.split(":")[1];
  },
  updated(){
     this.name = this.value.split(":")[1];
  }
};
</script>