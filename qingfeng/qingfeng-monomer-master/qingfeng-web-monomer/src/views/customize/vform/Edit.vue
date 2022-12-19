<template>
  <div class="design_div">
    <div style="color:red">绑定数据表必须含有字段：类型(type)、模块id(menu_id)、主表id(main_id)、状态(status)、创建人(create_user)、创建组织(create_organize)、创建时间(update_time)、修改人(update_user)、修改时间(update_time)</div>
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
import { saveOrUpdate } from "@/api/customize/vform";
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
      jsonData: {},
      data: {},
    };
  },
  mounted() {
    if (this.record.id != "" && this.record.id != undefined) {
      this.jsonData = JSON.parse(this.record.table_content);
      this.data = this.jsonData;
      this.importData();
    }
  },
  methods: {
    handleSave(values) {
      //alert("触发保存方法");
      //console.log(values);
      //this.$refs.qfRef.handleReset();
      let data = this.$refs.qfRef.getValue();
      if (data.config.table_name == "" || data.config.table_name == undefined) {
        this.$message.error("数据表不可为空");
        return;
      }
      data.table_content = values;
      console.log(this.record);
      let myRecord = "";
      if (this.record != "") {
        myRecord = JSON.stringify(this.record);
      }
      saveOrUpdate({
        record: myRecord,
        table_content: values,
        config: JSON.stringify(data.config),
        list: JSON.stringify(data.list),
      }).then((response) => {
        if (response.data.success) {
          this.$success({
            title: "提示：",
            content: "表单创建成功。",
          });
          this.$emit("finishResponse", response);
        } else {
          this.$message.error(response.data.msg);
        }
      });
    },
    handleClose(values) {},
    importData() {
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