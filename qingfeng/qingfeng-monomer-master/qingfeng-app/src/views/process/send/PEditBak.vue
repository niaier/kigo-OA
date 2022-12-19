<template>
  <div>
    <div style="margin-bottom: 10px">
      <a-button type="primary" style="margin-left: 10px" @click="send"
        >流程发送</a-button
      >
    </div>
    <a-card
      style="width: 100%"
      :tab-list="cardList"
      :active-tab-key="activeKey"
      @tabChange="(key) => onTabChange(key, 'activeKey')"
    >
      <p v-show="activeKey === 'form'">
        <k-form-build
          ref="kfb"
          :value="jsonData"
          :dynamicData="dynamicData"
          @change="handleChange"
        />
      </p>
      <p v-show="activeKey === 'flowchart'">
        <img :src="imageUrl" alt="流程图" class="image" />
      </p>
    </a-card>
  </div>
</template>
<script>
import { saveOrUpdate } from "@/api/customize/vdata";
import axios from "axios";
import AssignmentForm from "@/views/activiti/processTask/Assignment";
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
      jsonData: {},
      form_data: {},
      dynamicData: this.record.dynamicData,
      form: { id: "" },
      initStatus: false,
      cardList: [
        {
          key: "form",
          tab: "表单",
        },
        {
          key: "flowchart",
          tab: "流程图",
        },
      ],
      activeKey: "form",
      imageUrl: "",
      hideFieldKey: "",
    };
  },
  mounted() {
    this.form = this.record;
    //查询菜单配置信息
    this.jsonData = JSON.parse(this.record.table_content);
    //查询流程图
    this.flowchart(this.record);
  },
  updated() {
    //查询编辑项内容
    if (this.form.id != "" && this.form.id != undefined) {
      this.$refs.kfb.setData(this.form);
    } else {
      this.initStatus = true;
    }
  },
  components: {
    AssignmentForm,
  },
  methods: {
    save() {
      // 使用getData函数获取数据
      console.log(this.$refs.kfb.getData());
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
                  if (Array.isArray(values[k][j]) && values[k][j].length > 0) {
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
            submitType: "1",
            hideFieldKey: "",
          }).then((response) => {
            this.$emit("finishResponse", response);
          });
        })
        .catch(() => {});
    },
    send() {
      const that = this;
      let record = {
        processDefinitionKey: this.record.process_key,
        process_status: "0",
        taskId: "",
        processInstanceId: "",
      };
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
                  if (Array.isArray(values[k][j]) && values[k][j].length > 0) {
                    values[k][j] = values[k][j].join(",");
                  }
                }
              }
            }
          }
          this.$dialog(
            AssignmentForm,
            {
              record: { ...values, ...record },
              on: {
                complete(data, approve_opinion) {
                  saveOrUpdate({
                    ...values,
                    id: that.form.id,
                    table_id: that.record.table_id,
                    menu_id: that.record.menu_id,
                    submitType: "2",
                    nodeData: data,
                    approve_opinion: approve_opinion,
                    hideFieldKey: that.hideFieldKey,
                  }).then((response) => {
                    that.$emit("finishResponse", response);
                  });
                },
                ok() {},
                cancel() {},
                close() {},
              },
            },
            // modal props
            {
              title: "任务办理",
              width: 800,
              centered: true,
              maskClosable: false,
              okText: "确定",
              cancelText: "取消",
              bodyStyle: {
                backgroundColor: "#f0f2f5",
                padding: "16px",
                border: "#FFFFFF 4px solid",
              },
            }
          );
          return true;
        })
        .catch(() => {});
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
    //切换tab
    onTabChange(key, activeKey) {
      this.activeKey = key;
    },
    //查询流程图
    flowchart(record) {
      this.visible = true;
      axios({
        method: "GET",
        url:
          process.env.VUE_APP_API_BASE_URL +
          `/activiti/processDefinition/readResource?procDefinitionKey=` +
          record.process_key,
        responseType: "arraybuffer",
      })
        .then((res) => {
          return (
            "data:image/svg+xml;base64," +
            btoa(
              new Uint8Array(res.data).reduce(
                (data, byte) => data + String.fromCharCode(byte),
                ""
              )
            )
          );
        })
        .then((res) => {
          this.imageUrl = res;
        })
        .catch((e) => {
          if (e.toString().indexOf("429") !== -1) {
            this.$notification.warning({
              message: "请求超时,请稍后重试",
              description: "",
            });
          } else {
            this.$notification.warning({
              message: "获取流程图失败",
              description: "",
            });
          }
        });
    },
  },
};
</script>