<template>
  <div>
    <div style="margin-bottom: 10px">
      <a-button @click="save"> 保存草稿 </a-button>
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
      <p v-if="activeKey === 'form'">
        <a-form-model
          ref="ruleForm"
          :model="form"
          :rules="rules"
          :label-col="labelCol"
          :wrapper-col="wrapperCol"
        >
          <a-form-model-item ref="title" label="标题" prop="title">
            <a-input
              v-model="form.title"
              @blur="
                () => {
                  $refs.title.onFieldBlur();
                }
              "
            />
          </a-form-model-item>
          <a-form-model-item
            ref="leave_type"
            label="请假类型"
            prop="leave_type"
          >
            <a-select v-model="form.leave_type" placeholder="请选择leave_type">
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
            <a-input v-model="form.leave_cause" type="textarea" />
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
      </p>
      <p v-else-if="activeKey === 'flowchart'">
        <img :src="imageUrl" alt="流程图" class="image" />
      </p>
    </a-card>
  </div>
</template>
<script>
import { saveOrUpdate } from "@/api/gencode/leave";
import { findDictionaryList } from "@/api/system/dictionary";
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
  components: {
    AssignmentForm,
  },
  mounted() {
    if (this.record.id != undefined) {
      this.form = this.record;
    }
    this.initData();
    this.flowchart(this.record);
  },
  methods: {
    onTabChange(key, activeKey) {
      this.activeKey = key;
    },
    resetForm() {
      this.$refs.ruleForm.resetFields();
    },
    save() {
      this.$refs.ruleForm.validate((valid) => {
        if (valid) {
          saveOrUpdate({
            ...this.form,
            submitType: "1",
          }).then((response) => {
            this.$emit("finishResponse", response);
          });
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },
    send() {
      const that = this;
      let record = {
        processDefinitionKey: this.record.process_key,
        process_status: "0",
        taskId: "",
        processInstanceId: "",
      };
      this.$refs.ruleForm.validate((valid) => {
        if (valid) {
          this.$dialog(
            AssignmentForm,
            {
              record,
              on: {
                complete(data, approve_opinion) {
                  saveOrUpdate({
                    ...that.form,
                    nodeData: data,
                    approve_opinion: approve_opinion,
                    submitType: "2",
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
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },
    onCancel() {
      return new Promise((resolve) => {
        resolve(true);
      });
    },
    initData() {
      const options = [
        { id: "0", name: "事假" },
        { id: "1", name: "病假" },
        { id: "2", name: "婚假" },
      ];
      this.leave_type_data = options;
    },
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
<style scoped>
.ant-modal-body {
  padding: 10px;
  font-size: 14px;
  line-height: 1.5;
  word-wrap: break-word;
}
</style>