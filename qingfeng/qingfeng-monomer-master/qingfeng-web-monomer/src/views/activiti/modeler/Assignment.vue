<template>
  <a-form-model
    ref="ruleForm"
    :model="form"
    :rules="rules"
    :label-col="labelCol"
    :wrapper-col="wrapperCol"
  >
    <a-form-model-item ref="assign_mode" label="名称" prop="assign_mode">
      <a-select
        v-model="form.assign_mode"
        placeholder="请选择名称"
        @change="selectChange"
      >
        <a-select-option value="0">
          所有人员中选择（根据组织选择）
        </a-select-option>
        <a-select-option value="1">
          组织选择（指定组织父节点）
        </a-select-option>
        <a-select-option value="2">
          用户组选择（选择指定组内成员）
        </a-select-option>
        <a-select-option value="3"> 发起人本组织选择 </a-select-option>
        <a-select-option value="4"> 部门经理 </a-select-option>
        <a-select-option value="5"> 上级领导 </a-select-option>
        <a-select-option value="6"> 分管领导 </a-select-option>
        <a-select-option value="7"> 流程发起人 </a-select-option>
        <a-select-option value="8"> 指定范围选择 </a-select-option>
        <a-select-option value="9"> 代理人（选择单用户） </a-select-option>
        <a-select-option value="10"> 候选人（选择多用户） </a-select-option>
        <a-select-option value="11"> 候选组（选择多组织） </a-select-option>
      </a-select>
    </a-form-model-item>
    <a-form-model-item v-show="form.assign_mode == 1" label="请选择组织">
      <a-input v-model="form.organize_id" placeholder="组织id" />
      <a-input v-model="form.organize_name" placeholder="组织名称" />
      <a-button @click="selectOneOrganize" type="primary"> 选择 </a-button>
    </a-form-model-item>

    <a-form-model-item v-show="form.assign_mode == 2" label="请选择">
      <a-select v-model="form.group_id" placeholder="请选择用户组">
        <a-select-option
          v-for="(data, index) in groupList"
          :key="index"
          :value="data.id"
        >
          {{ data.name }}
        </a-select-option>
      </a-select>
    </a-form-model-item>

    <a-form-model-item
      v-show="form.assign_mode == 8 || form.assign_mode == 10"
      label="请选择用户"
    >
      <a-input v-model="form.user_ids" placeholder="用户ids" />
      <a-textarea v-model="form.user_names" placeholder="用户名称" />
      <a-button size="small" type="primary" @click="selectMoreUser">
        选择
      </a-button>
    </a-form-model-item>

    <a-form-model-item v-show="form.assign_mode == 9" label="请选择用户">
      <a-input v-model="form.user_id" placeholder="用户id" />
      <a-textarea v-model="form.user_name" placeholder="用户名称" />
      <a-button size="small" type="primary" @click="selectOneUser">
        选择
      </a-button>
    </a-form-model-item>

    <a-form-model-item v-show="form.assign_mode == 11" label="请选择组织">
      <a-input v-model="form.organize_ids" placeholder="组织ids" />
      <a-textarea v-model="form.organize_names" placeholder="组织名称" />
      <a-button size="small" type="primary" @click="selectMoreOrganize">
        选择
      </a-button>
    </a-form-model-item>
  </a-form-model>
</template>
<script>
import { findAssignment, saveAssignment } from "@/api/activiti/modeler";
import SelectOneUser from "@/views/system/User/SelectOneUser";
import SelectMoreUser from "@/views/system/User/SelectMoreUser";
import SelectOneOrganize from "@/views/system/Organize/SelectOneOrganize";
import SelectMoreOrganize from "@/views/system/Organize/SelectMoreOrganize";
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
      form: {
        id: "",
        type: "1",
        assign_mode: "",
        assign_content: "",
        model_id: "",
        node_key: "",
        remark: "",

        group_id: "",
        user_id: "",
        user_name: "",
        user_ids: "",
        user_names: "",
        organize_id: "",
        organize_name: "",
        organize_ids: "",
        organize_names: "",
      },
      groupList: [],
      rules: {
        assign_mode: [
          { required: true, message: "必填项不能为空", trigger: "blur" },
          { min: 0, max: 50, message: "长度不得大于50个字符", trigger: "blur" },
        ],
        key: [
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
    // this.form = this.record;
    this.initData();
  },
  methods: {
    initData() {
      let that = this;
      findAssignment(this.record).then((response) => {
        if (response.data.success) {
          if (response.data.data != null) {
            that.form = response.data.data;
            let assign_content = that.form.assign_content;
            if (that.form.assign_mode == "2") {
              that.form.group_id = assign_content;
            } else if (that.form.assign_mode == "1") {
              that.form.organize_id = assign_content.split("#")[0];
              that.form.organize_name = assign_content.split("#")[1];
            } else if (
              that.form.assign_mode == "8" ||
              that.form.assign_mode == "10"
            ) {
              that.form.user_ids = assign_content.split("#")[0];
              that.form.user_names = assign_content.split("#")[1];
            } else if (that.form.assign_mode == "9") {
              that.form.user_id = assign_content.split("#")[0];
              that.form.user_name = assign_content.split("#")[1];
            } else if (that.form.assign_mode == "11") {
              that.form.organize_ids = assign_content.split("#")[0];
              that.form.organize_names = assign_content.split("#")[1];
            }
          }
          that.groupList = response.data.object;
        } else {
          this.$error({
            title: "提示：",
            content: response.data.msg,
            okText: "确认",
          });
        }
      });
      this.form.model_id = this.record.model_id;
      this.form.node_key = this.record.node_key;
    },
    selectChange(value) {},
    resetForm() {
      this.$refs.ruleForm.resetFields();
    },

    onOk() {
      return new Promise((resolve) => {
        this.$refs.ruleForm.validate((valid) => {
          if (valid) {
            if (this.form.assign_mode == "1") {
              this.form.assign_content =
                this.form.organize_id + "#" + this.form.organize_name;
            } else if (this.form.assign_mode == "2") {
              this.form.assign_content = this.form.group_id;
            } else if (
              this.form.assign_mode == "8" ||
              this.form.assign_mode == "10"
            ) {
              this.form.assign_content =
                this.form.user_ids + "#" + this.form.user_names;
            } else if (this.form.assign_mode == "9") {
              this.form.assign_content =
                this.form.user_id + "#" + this.form.user_name;
            } else if (this.form.assign_mode == "11") {
              this.form.assign_content =
                this.form.organize_ids + "#" + this.form.organize_names;
            }
            saveAssignment(this.form).then((response) => {
              resolve(true);
            });
            return true;
          } else {
            return false;
          }
        });
      });
    },
    onCancel() {
      return new Promise((resolve) => {
        resolve(true);
      });
    },

    selectOneUser() {
      if (this.form.user_id == undefined) {
        this.form.user_id = "";
        this.form.user_name = "";
      }
      const user = {
        user_id: this.form.user_id,
        user_name: this.form.user_name,
      };
      this.dialog(SelectOneUser, user);
    },
    selectMoreUser() {
      if (this.form.user_ids == undefined) {
        this.form.user_ids = "";
        this.form.user_names = "";
      }
      const users = {
        user_ids: this.form.user_ids,
        user_names: this.form.user_names,
      };
      this.dialog(SelectMoreUser, users);
    },
    selectOneOrganize() {
      if (this.form.organize_id == undefined) {
        this.form.organize_id = "";
        this.form.organize_name = "";
      }
      const organize = {
        organize_id: this.form.organize_id,
        organize_name: this.form.organize_name,
      };
      this.dialog(SelectOneOrganize, organize);
    },
    selectMoreOrganize() {
      if (this.form.organize_ids == undefined) {
        this.form.organize_ids = "";
        this.form.organize_names = "";
      }
      const organizes = {
        organize_ids: this.form.organize_ids,
        organize_names: this.form.organize_names,
      };
      this.dialog(SelectMoreOrganize, organizes);
    },
    dialog(component, record) {
      const that = this;
      this.$dialog(
        component,
        // component props
        {
          record,
          on: {
            ok() {},
            cancel() {},
            close() {},
            initValue(value, type) {
              if (type == "1") {
                that.form.user_id = value.split(":")[0];
                that.form.user_name = value.split(":")[1];
              } else if (type == "2") {
                that.form.user_ids = value.split(":")[0];
                that.form.user_names = value.split(":")[1];
              } else if (type == "3") {
                that.form.organize_id = value.split(":")[0];
                that.form.organize_name = value.split(":")[1];
              } else if (type == "4") {
                that.form.organize_ids = value.split(":")[0];
                that.form.organize_names = value.split(":")[1];
              }
              let assign_mode = that.form.assign_mode;
              that.form.assign_mode = "";
              that.form.assign_mode = assign_mode;
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
};
</script>
