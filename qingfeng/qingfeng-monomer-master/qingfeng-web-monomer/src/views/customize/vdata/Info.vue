<template>
  <div>
    <div class="spin" v-show="!initStatus"><a-spin size="large" /></div>
    <k-form-build
      ref="kfb"
      :value="jsonData"
      :dynamicData="dynamicData"
      :disabled="true"
      v-show="initStatus"
    />
  </div>
</template>
<script>
import {
  findVFormInfo,
  findVDataInfo,
  findVFormData,
} from "@/api/customize/vdata";
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
      jsonData: {},
      form_data: {},
      dynamicData: {},
      form: {},
      initStatus: false,
    };
  },
  mounted() {
    if (this.record.businessKey != undefined) {
      if (this.record.businessKey.indexOf("customize") != -1) {
        //查询菜单配置信息
        let data = this.record.businessKey.split(":");
        this.initData(data[2]);
      }
    } else {
      this.form = this.record;
      //查询菜单配置信息
      this.dynamicData = this.record.dynamicData;
      this.jsonData = JSON.parse(this.record.table_content);
      this.initStatus = true;
    }
  },
  updated() {
    if (this.record.businessKey != undefined) {
      if (this.record.businessKey.indexOf("customize") != -1) {
        //查询菜单配置信息
        let data = this.record.businessKey.split(":");
        this.initFormData(data[2], data[3]);
      }
    } else {
      //查询编辑项内容
      if (this.form.id != "" && this.form.id != undefined) {
        this.$refs.kfb.setData(this.form);
      } else {
        this.initStatus = true;
      }
    }
  },
  methods: {
    initData(menu_id) {
      findVFormInfo({ menu_id: menu_id }).then((response) => {
        this.form_data = response.data.data;
        this.dynamicData = response.data.object;
        this.jsonData = JSON.parse(this.form_data.table_content);
      });
    },
    initFormData(menu_id, id) {
      findVDataInfo({
        menu_id: menu_id,
        id: id,
      }).then((response) => {
        this.$refs.kfb.setData(response.data);
        this.initStatus = true;
      });
    },
  },
};
</script>
<style scoped>
.spin {
  text-align: center;
  border-radius: 4px;
  margin-bottom: 20px;
  padding: 30px 50px;
  margin: 40px 0;
}
</style>