<template>
  <div>
    <!-- <a-skeleton active v-show="!initStatus" :paragraph="{ rows: 10 }" /> -->
    <!-- <a-spin size="large" v-show="!initStatus" /> -->
    <k-form-build
      ref="kfb"
      :value="jsonData"
      :dynamicData="dynamicData"
      @change="handleChange"
    />
  </div>
</template>
<script>
import {
  findVFormInfo,
  saveOrUpdate,
  findVDataInfo,
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
      dynamicData: this.record.dynamicData,
      form: {},
      initStatus: false,
    };
  },
  mounted() {
    this.form = this.record;
    //查询菜单配置信息
    this.jsonData = JSON.parse(this.record.table_content);
  },
  updated() {
    //查询编辑项内容
    if (this.form.id != "" && this.form.id != undefined) {
      this.$refs.kfb.setData(this.form);
    } else {
      this.initStatus = true;
    }
  },
  methods: {
    // initData(menu_id) {
    //   findVFormInfo({ menu_id: menu_id }).then((response) => {
    //     this.form_data = response.data;
    //     this.jsonData = JSON.parse(response.data.table_content);
    //   });
    // },
    // initFormData() {
    //   findVDataInfo({
    //     table_id: this.form.table_id,
    //     menu_id: this.form.menu_id,
    //     id: this.form.id,
    //   }).then((response) => {
    //     this.$refs.kfb.setData(this.form);
    //     // this.$refs.kfb.setData(response.data);
    //     this.initStatus = true;
    //   });
    // },
    currentUserOrganize(value) {},
    onOk() {
      return new Promise((resolve) => {
        // 使用getData函数获取数据
        this.$refs.kfb
          .getData()
          .then((values) => {
            for (let k in values) {
              if (Array.isArray(values[k]) && values[k].length > 0) {
                if (
                  Object.prototype.toString.call(values[k][0]) ==
                  "[object String]"
                ) {
                  values[k] = values[k].join(",");
                } else if (
                  Object.prototype.toString.call(values[k][0]) ==
                  "[object Object]"
                ) {
                  for (let j in values[k]) {
                    if (
                      Array.isArray(values[k][j]) &&
                      values[k][j].length > 0
                    ) {
                      values[k][j] = values[k][j].join(",");
                    }
                  }
                }
              }
            }
            saveOrUpdate({
              ...values,
              id: this.form.id,
              table_id: this.record.table_id,
              menu_id: this.record.menu_id,
            }).then((response) => {
              resolve(true);
            });
          })
          .catch(() => {});
      });
    },
    onCancel() {
      return new Promise((resolve) => {
        resolve(true);
      });
    },
    handleChange(value, key, record) {
      if (record.linkFieldSet != undefined) {
        record.linkFieldSet.forEach((item) => {
          if (item.linkField == value) {
            let hideLinkField = item.hideLinkField;
            let showLinkField = item.showLinkField;
            // console.log('***---')
            // console.log(hideLinkField)
            // console.log(showLinkField) 
            if (hideLinkField != undefined) {
              this.$refs.kfb.hide(hideLinkField.split(","));
            }
            if (showLinkField != undefined) {
              this.$refs.kfb.enable(showLinkField.split(","));
            }
          }
        });
      }
    },
  },
};
</script>