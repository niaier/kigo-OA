<template>
  <div>
    <!-- <a-skeleton active v-show="!initStatus" :paragraph="{ rows: 10 }" /> -->
    <!-- <a-spin size="large" v-show="!initStatus" /> -->
    <k-form-build
      ref="kfb"
      :value="jsonData"
      :dynamicData="dynamicData"
      v-show="visable"
    />
  </div>
</template>
<script>
import {
  findVFormInfo,
  saveOrUpdate,
  findVDataInfo,
  findVFormData,
  rejectAnyNod,
  delegateTask,
} from "@/api/customize/vdata";
import { findDictionaryList } from "@/api/system/dictionary";
import { findFormParams } from "@/api/activiti/processTask";
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
      visable: false,
      menu_id: "",
      table_id: "",
      hideFieldKey: "",
    };
  },
  mounted() {
    //查询菜单配置信息
    let data = this.record.businessKey.split(":");
    this.initData(data[2]);
    this.menu_id = data[2];
  },
  updated() {
    //查询编辑项内容
    let data = this.record.businessKey.split(":");
    this.initFormData(data[2], data[3]);
  },
  methods: {
    initData(menu_id) {
      findVFormInfo({ menu_id: menu_id, formKey: this.record.formKey }).then(
        (response) => {
          this.form_data = response.data.data;
          this.table_id = this.form_data.id;
          this.dynamicData = response.data.object;
          this.jsonData = JSON.parse(this.form_data.table_content);
        }
      );
    },
    initFormData(menu_id, id) {
      findVDataInfo({
        table_id: this.record.formKey,
        menu_id: menu_id,
        id: id,
      }).then((response) => {
        this.form = response.data;
        this.$refs.kfb.setData(response.data);
        this.initStatus = true;
        this.$refs.kfb.disableAll();
        findFormParams({
          nodeType: "nodeType",
          taskId: this.record.id,
        }).then((response) => {
          this.visable = true;
          let data = response.data.data;
          if (data.writeFieldKey != undefined) {
            this.$refs.kfb.enable(data.writeFieldKey.split(","));
          }
          if (data.hideFieldKey != undefined) {
            this.$refs.kfb.hide(data.hideFieldKey.split(","));
            this.hideFieldKey = data.hideFieldKey;
          }
        });
      });
    },
    currentUserOrganize(value) {},
    getFormData() {
      return new Promise((resolve) => {
        this.$refs.kfb.getData().then((values) => {
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
                  if (Array.isArray(values[k][j]) && values[k][j].length > 0) {
                    values[k][j] = values[k][j].join(",");
                  }
                }
              }
            }
          }
          resolve(values);
        });
      });
    },
    onOk(data) {
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
              taskId: data.id,
              approve_opinion: data.approve_opinion,
              nodeData: data.nodeData,
              // businessKey: data.businessKey,
              // assignee: data.assignee,
              // assignee_name: data.assignee_name,
              // originalAssignee: data.originalAssignee,
              // executionId: data.executionId,
              // formKey: data.formKey,
              // procdef_name: data.procdef_name,
              // processDefinitionId: data.processDefinitionId,
              // processInstanceId: data.processInstanceId,
              // start_user_name: data.start_user_name,
              // suspensionState: data.suspensionState,
              // taskDefinitionKey: data.taskDefinitionKey,
              // tenantId: data.tenantId,
              submitType: "3",
              ...values,
              id: this.form.id,
              table_id: this.table_id,
              menu_id: this.menu_id,
              hideFieldKey: this.hideFieldKey,
            }).then((response) => {
              this.$emit("finishResponse", response);
              resolve(true);
            });
          })
          .catch(() => {});
      });
    },
    onRejectAnyNode(data) {
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
            rejectAnyNod({
              taskId: data.id,
              flowElementId: data.flowElementId,
              ...values,
              id: this.form.id,
              table_id: this.table_id,
              menu_id: this.menu_id,
              hideFieldKey: this.hideFieldKey,
            }).then((response) => {
              if (response.data.success) {
                this.$emit("finishResponse", response);
                resolve(true);
              } else {
                this.$message.warning(response.data.msg);
              }
            });
          })
          .catch(() => {});
      });
    },

    onDelegateTask(data) {
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
            delegateTask({
              taskId: data.taskId,
              userId: data.userId,
              userName: data.userName,
              ...values,
              id: this.form.id,
              table_id: this.table_id,
              menu_id: this.menu_id,
              hideFieldKey: this.hideFieldKey,
            }).then((response) => {
              this.$emit("finishResponse", response);
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
  },
};
</script>