<template>
  <div style="margin-top: 16px">
    <el-form-item label="分配类型">
      <el-radio-group v-model="userTaskForm.type">
        <el-tooltip
          class="item"
          effect="dark"
          content="流程设计器指定"
          placement="right-start"
        >
          <el-radio label="0">静态分配</el-radio>
        </el-tooltip>
        <el-tooltip
          class="item"
          effect="dark"
          content="程序动态指定"
          placement="right-start"
        >
          <el-radio label="1">动态分配</el-radio>
        </el-tooltip>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="处理用户" v-show="userTaskForm.type == '0'">
      <el-input v-model="userTaskForm.assigneeName" clearable disabled />
      <el-button
        @click="selectAssignee()"
        type="primary"
        style="margin-top: 8px"
        >选择</el-button
      >
      <el-button @click="clearAssignee()" type="primary" style="margin-top: 8px"
        >清空</el-button
      >
    </el-form-item>
    <el-form-item label="候选用户" v-show="userTaskForm.type == '0'">
      <el-input
        type="textarea"
        v-model="userTaskForm.candidateUsersName"
        clearable
        disabled
      />
      <el-button
        @click="selectCandidateUsers()"
        type="primary"
        style="margin-top: 8px"
        >选择</el-button
      >
      <el-button
        @click="clearCandidateUsers()"
        type="primary"
        style="margin-top: 8px"
        >清空</el-button
      >
    </el-form-item>
    <el-form-item label="候选分组" v-show="userTaskForm.type == '0'">
      <el-input
        type="textarea"
        v-model="userTaskForm.candidateGroupsName"
        clearable
        disabled
      />
      <el-button
        @click="selectCandidateGroups()"
        type="primary"
        style="margin-top: 8px"
        >选择</el-button
      >
      <el-button
        @click="selectCandidateGroups()"
        type="primary"
        style="margin-top: 8px"
        >清空</el-button
      >
    </el-form-item>

    <el-form-item label="指定类型" v-show="userTaskForm.type == '1'">
      <el-select
        v-model="userTaskForm.assign_mode"
        placeholder="请选择指定类型"
        @change="selectAssignMode()"
      >
        <el-option label="所有人员中选择（根据组织选择）" value="0"></el-option>
        <el-option label="组织选择（指定组织父节点）" value="1"></el-option>
        <el-option label="用户组选择（选择指定组内成员）" value="2"></el-option>
        <el-option label="发起人本组织选择" value="3"></el-option>
        <el-option label="部门经理" value="4"></el-option>
        <el-option label="上级领导" value="5"></el-option>
        <el-option label="分管领导" value="6"></el-option>
        <el-option label="流程发起人" value="7"></el-option>
        <el-option label="指定范围选择" value="8"></el-option>
        <el-option label="代理人（选择单用户）" value="9"></el-option>
        <el-option label=" 候选人（选择多用户）" value="10"></el-option>
        <el-option label="候选组（选择多组织）" value="11"></el-option>
      </el-select>
    </el-form-item>
    <el-form-item
      label="用户组选择"
      v-show="userTaskForm.type == '1' && userTaskForm.assign_mode == '2'"
    >
      <el-select
        v-model="userTaskForm.assign_content"
        placeholder="请选择用户组"
        @change="selectAssignMode()"
      >
        <el-option
          v-for="item in groupList"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        >
        </el-option>
      </el-select>
    </el-form-item>
    <el-form-item
      label="选择组织信息"
      v-show="userTaskForm.type == '1' && userTaskForm.assign_mode == '1'"
    >
      <el-input
        type="textarea"
        v-model="userTaskForm.organize_name"
        clearable
        disabled
      />
      <el-button
        @click="selectOneOrganize()"
        type="primary"
        style="margin-top: 8px"
        >选择</el-button
      >
    </el-form-item>
    <el-form-item
      label="选择用户信息"
      v-show="
        userTaskForm.type == '1' &&
        (userTaskForm.assign_mode == '8' || userTaskForm.assign_mode == '10')
      "
    >
      <el-input
        type="textarea"
        v-model="userTaskForm.user_names"
        clearable
        disabled
      />
      <el-button
        @click="selectMoreUser()"
        type="primary"
        style="margin-top: 8px"
        >选择</el-button
      >
    </el-form-item>
    <el-form-item
      label="选择用户信息"
      v-show="userTaskForm.type == '1' && userTaskForm.assign_mode == '9'"
    >
      <el-input
        type="textarea"
        v-model="userTaskForm.user_name"
        clearable
        disabled
      />
      <el-button @click="selectOneUser()" type="primary" style="margin-top: 8px"
        >选择</el-button
      >
    </el-form-item>
    <el-form-item
      label="选择组织信息"
      v-show="userTaskForm.type == '1' && userTaskForm.assign_mode == '11'"
    >
      <el-input
        type="textarea"
        v-model="userTaskForm.organize_names"
        clearable
        disabled
      />
      <el-button
        @click="selectMoreOrganize()"
        type="primary"
        style="margin-top: 8px"
        >选择</el-button
      >
    </el-form-item>

    <!-- <el-form-item label="处理用户"> -->
    <!-- <el-select>  -->
    <!-- <el-select v-model="userTaskForm.assignee"> 
          <el-option v-for="ak in mockData" :key="'ass-' + ak" :label="`用户${ak}`" :value="`user${ak}`" /> 
      </el-select>
      <el-input
        v-model="userTaskForm.assignee"
        clearable
        disabled
        @click="updateElementTask('assignee')"
      />
      <el-button @click="updateElementTask('assignee')" type="primary"
        >选择</el-button
      >
      <el-button @click="clearAssignee('assignee')" type="primary">清空</el-button>
    </el-form-item>
    <el-form-item label="候选用户">
      <el-select v-model="userTaskForm.candidateUsers" multiple collapse-tags @change="updateElementTask('candidateUsers')">
        <el-option v-for="uk in mockData" :key="'user-' + uk" :label="`用户${uk}`" :value="`user${uk}`" />
      </el-select>
    </el-form-item>
    <el-form-item label="候选分组">

      <el-select v-model="userTaskForm.candidateGroups" multiple collapse-tags @change="updateElementTask('candidateGroups')">
        <el-option v-for="gk in mockData" :key="'ass-' + gk" :label="`分组${gk}`" :value="`group${gk}`" />
      </el-select>
    </el-form-item>
    <el-form-item label="到期时间">
      <el-input v-model="userTaskForm.dueDate" clearable @change="updateElementTask('dueDate')" />
    </el-form-item>
    <el-form-item label="跟踪时间">
      <el-input v-model="userTaskForm.followUpDate" clearable @change="updateElementTask('followUpDate')" />
    </el-form-item>
    <el-form-item label="优先级">
      <el-input v-model="userTaskForm.priority" clearable @change="updateElementTask('priority')" />
    </el-form-item>  -->
    <!-- 弹出框 -->
    <el-dialog title="提示" :visible.sync="dialogVisible" width="30%">
      <el-table :data="tableData" @row-click="cellclick" style="width: 100%">
        <el-table-column prop="id" label="id" width="180"> </el-table-column>
        <el-table-column prop="user_name" label="姓名" width="180">
        </el-table-column>
      </el-table>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="dialogVisible = false"
          >确 定</el-button
        >
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from "axios";
import SelectOneUser from "@/views/system/User/SelectOneUser";
import SelectMoreUser from "@/views/system/User/SelectMoreUser";
import SelectOneOrganize from "@/views/system/Organize/SelectOneOrganize";
import SelectMoreOrganize from "@/views/system/Organize/SelectMoreOrganize";
import {
  findUserOrOrganizeNames,
  findAssignment,
  saveAssignment,
  findGroupList,
} from "@/api/activiti/modeler";
export default {
  name: "UserTask",
  props: {
    id: String,
    type: String,
  },
  data() {
    return {
      defaultTaskForm: {
        type: "0",
        assign_mode: "",
        assignee: "",
        candidateUsers: [],
        candidateGroups: [],
        dueDate: "",
        followUpDate: "",
        priority: "",
        propertyForm: [],
      },
      checktype: "",
      dialogVisible: false,
      tableData: [],
      groupList: [],
      userTaskForm: {
        type: "0",
      },
      mockData: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
    };
  },
  watch: {
    id: {
      immediate: true,
      handler() {
        this.bpmnElement = window.bpmnInstances.bpmnElement;
        this.bpmnInstances = window.bpmnInstances;
        this.$nextTick(() => this.resetTaskForm());
      },
    },
  },
  mounted() {
    this.initGroup();
  },
  methods: {
    resetTaskForm() {
      this.bpmnELement = window.bpmnInstances.bpmnElement;
      console.log(this.bpmnELement);

      for (let key in this.defaultTaskForm) {
        let value;
        if (key === "candidateUsers" || key === "candidateGroups") {
          value = this.bpmnElement?.businessObject[key]
            ? this.bpmnElement.businessObject[key].split(",")
            : [];
          console.log(value);
          if (value != "") {
            findUserOrOrganizeNames({ type: key, ids: value.join(",") }).then(
              (response) => {
                // console.log(response);
                if (key === "candidateUsers") {
                  this.$set(
                    this.userTaskForm,
                    "candidateUsers",
                    response.data.data.myIds
                  );
                  this.$set(
                    this.userTaskForm,
                    "candidateUsersName",
                    response.data.data.myNames
                  );
                } else if (key === "candidateGroups") {
                  this.$set(
                    this.userTaskForm,
                    "candidateGroups",
                    response.data.data.myIds
                  );
                  this.$set(
                    this.userTaskForm,
                    "candidateGroupsName",
                    response.data.data.myNames
                  );
                }
              }
            );
          } else {
            if (key === "candidateUsers") {
              this.$set(this.userTaskForm, "candidateUsers", "");
              this.$set(this.userTaskForm, "candidateUsersName", "");
            } else if (key === "candidateGroups") {
              this.$set(this.userTaskForm, "candidateGroups", "");
              this.$set(this.userTaskForm, "candidateGroupsName", "");
            }
          }
        } else if (key === "assignee") {
          value =
            this.bpmnElement?.businessObject[key] || this.defaultTaskForm[key];
          if (value != "") {
            findUserOrOrganizeNames({ type: key, ids: value }).then(
              (response) => {
                this.$set(
                  this.userTaskForm,
                  "assignee",
                  response.data.data.myIds
                );
                this.$set(
                  this.userTaskForm,
                  "assigneeName",
                  response.data.data.myNames
                );
              }
            );
          } else {
            this.$set(this.userTaskForm, "assignee", "");
            this.$set(this.userTaskForm, "assigneeName", "");
          }
        } else {
          value =
            this.bpmnElement?.businessObject[key] || this.defaultTaskForm[key];
          this.$set(this.userTaskForm, key, value);
        }
        // console.log(key + "|" + value);
      }

      let type = this.bpmnElement?.businessObject["$attrs"]["activiti:type"];
      console.log(type)
      this.$set(this.userTaskForm, "type", type + "");
      if (type == "1") {
        let assign_mode =
          this.bpmnElement?.businessObject["$attrs"]["activiti:assign_mode"];
        this.$set(this.userTaskForm, "assign_mode", assign_mode);
        let assign_content =
          this.bpmnElement?.businessObject["$attrs"]["activiti:assign_value"];
        if (assign_content != "") {
          this.$set(this.userTaskForm, "assign_content", assign_content);
        }
        if (assign_mode == "1") {
          this.$set(
            this.userTaskForm,
            "organize_id",
            assign_content.split("#")[0]
          );
          this.$set(
            this.userTaskForm,
            "organize_name",
            assign_content.split("#")[1]
          );
        } else if (
          assign_mode == "8" ||
          assign_mode == "10"
        ) {
          this.$set(
            this.userTaskForm,
            "user_ids",
            assign_content.split("#")[0]
          );
          this.$set(
            this.userTaskForm,
            "user_names",
            assign_content.split("#")[1]
          );
        } else if (assign_mode == "9") {
          this.$set(this.userTaskForm, "user_id", assign_content.split("#")[0]);
          this.$set(
            this.userTaskForm,
            "user_name",
            assign_content.split("#")[1]
          );
        } else if (assign_mode == "11") {
          this.$set(
            this.userTaskForm,
            "organize_ids",
            assign_content.split("#")[0]
          );
          this.$set(
            this.userTaskForm,
            "organize_names",
            assign_content.split("#")[1]
          );
        }
      }
    },
    async updateElementTask(key) {
      this.checktype = key;
      this.dialogVisible = true;
      let taskAttr = Object.create(null);
      if (key === "candidateUsers" || key === "candidateGroups") {
        taskAttr[key] =
          this.userTaskForm[key] && this.userTaskForm[key].length
            ? this.userTaskForm[key].join()
            : null;
      } else {
        taskAttr[key] = this.userTaskForm[key] || null;
        console.log(taskAttr[key]);
      }
      window.bpmnInstances.modeling.updateProperties(
        this.bpmnElement,
        taskAttr
      );
    },
    cellclick(row) {
      var key = this.checktype;
      let taskAttr = Object.create(null);
      this.userTaskForm.assignee = row.id;
      this.dialogVisible = false;
      if (key === "candidateUsers" || key === "candidateGroups") {
        taskAttr[key] =
          this.userTaskForm[key] && this.userTaskForm[key].length
            ? this.userTaskForm[key].join()
            : null;
      } else {
        taskAttr[key] = this.userTaskForm[key] || null;
        console.log(taskAttr[key]);
      }
      window.bpmnInstances.modeling.updateProperties(
        this.bpmnElement,
        taskAttr
      );
    },
    clearAssignee(key) {
      let value;
      let taskAttr = Object.create(null);
      if (key === "candidateUsers" || key === "candidateGroups") {
        console.log(key);
      } else {
        taskAttr[key] = ""; //this.bpmnElement?.businessObject[key] || this.defaultTaskForm[key];
      }
      this.$set(this.userTaskForm, key, "");
      window.bpmnInstances.modeling.updateProperties(
        this.bpmnElement,
        taskAttr
      );
    },

    //初始化initGroup
    initGroup() {
      findGroupList({}).then((response) => {
        this.groupList = response.data.data;
      });
    },

    //选择办理人
    selectAssignee() {
      this.dialog(SelectOneUser, "assignee", {
        user_id: this.userTaskForm.assignee,
        user_name: this.userTaskForm.assigneeName,
      });
    },
    //选择候选人
    selectCandidateUsers() {
      this.dialog(SelectMoreUser, "candidateUsers", {
        user_ids: this.userTaskForm.candidateUsers,
        user_names: this.userTaskForm.candidateUsersName,
      });
    },
    //选择候选组
    selectCandidateGroups() {
      this.dialog(SelectMoreOrganize, "candidateGroups", {
        organize_ids: this.userTaskForm.candidateGroups,
        organize_names: this.userTaskForm.candidateGroupsName,
      });
    },

    //选择单组织
    selectOneOrganize() {
      this.dialog(SelectOneOrganize, "oneOrganize", {
        organize_id: this.userTaskForm.organize_id,
        organize_name: this.userTaskForm.organize_name,
      });
    },
    //选择多用户
    selectMoreUser() {
      this.dialog(SelectMoreUser, "moreUser", {
        user_ids: this.userTaskForm.user_ids,
        user_names: this.userTaskForm.user_names,
      });
    },
    //选择单用户
    selectOneUser() {
      this.dialog(SelectOneUser, "oneUser", {
        user_id: this.userTaskForm.user_id,
        user_name: this.userTaskForm.user_name,
      });
    },
    //选择多组织
    selectMoreOrganize() {
      this.dialog(SelectMoreOrganize, "moreOrganize", {
        organize_ids: this.userTaskForm.organize_ids,
        organize_names: this.userTaskForm.organize_names,
      });
    },

    //选择用户组织弹框
    dialog(component, fileType, record) {
      console.log(component, fileType, record);
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
              if (type == "1") {
                if (fileType == "assignee") {
                  that.userTaskForm.assignee = value.split(":")[0];
                  that.userTaskForm.assigneeName = value.split(":")[1];
                  //更新文档参数
                  that.updateActivitiProperties(
                    "assignee",
                    value.split(":")[0]
                  );
                  that.updateActivitiProperties("activiti:type", 0);
                } else if (fileType == "oneUser") {
                  that.userTaskForm.user_id = value.split(":")[0];
                  that.userTaskForm.user_name = value.split(":")[1];

                  that.updateActivitiProperties("activiti:type", 1);
                  that.updateActivitiProperties(
                    "activiti:assign_mode",
                    that.userTaskForm.assign_mode
                  );
                  that.updateActivitiProperties(
                    "activiti:assign_value",
                    value.split(":")[0] + "#" + value.split(":")[1]
                  );

                }
              } else if (type == "2") {
                if (fileType == "candidateUsers") {
                  that.userTaskForm.candidateUsers = value.split(":")[0];
                  that.userTaskForm.candidateUsersName = value.split(":")[1];
                  //更新文档参数
                  that.updateActivitiProperties(
                    "candidateUsers",
                    value.split(":")[0]
                  );
                  that.updateActivitiProperties("activiti:type", 0);
                } else if (fileType == "moreUser") {
                  that.userTaskForm.user_ids = value.split(":")[0];
                  that.userTaskForm.user_names = value.split(":")[1];

                  that.updateActivitiProperties("activiti:type", 1);
                  that.updateActivitiProperties(
                    "activiti:assign_mode",
                    that.userTaskForm.assign_mode
                  );
                  that.updateActivitiProperties(
                    "activiti:assign_value",
                    value.split(":")[0] + "#" + value.split(":")[1]
                  );
                }
              } else if (type == "3") {
                if (fileType == "oneOrganize") {
                  that.userTaskForm.organize_id = value.split(":")[0];
                  that.userTaskForm.organize_name = value.split(":")[1];
                  that.updateActivitiProperties("activiti:type", 1);
                  that.updateActivitiProperties(
                    "activiti:assign_mode",
                    that.userTaskForm.assign_mode
                  );
                  that.updateActivitiProperties(
                    "activiti:assign_value",
                    value.split(":")[0] + "#" + value.split(":")[1]
                  );
                }
              } else if (type == "4") {
                if (fileType == "candidateGroups") {
                  that.userTaskForm.candidateGroups = value.split(":")[0];
                  that.userTaskForm.candidateGroupsName = value.split(":")[1];
                  //更新文档参数
                  that.updateActivitiProperties(
                    "candidateGroups",
                    value.split(":")[0]
                  );
                  that.updateActivitiProperties("activiti:type", 0);
                } else if (fileType == "moreOrganize") {
                  that.userTaskForm.organize_ids = value.split(":")[0];
                  that.userTaskForm.organize_names = value.split(":")[1];
                  that.updateActivitiProperties("activiti:type", 1);
                  that.updateActivitiProperties(
                    "activiti:assign_mode",
                    that.userTaskForm.assign_mode
                  );
                  that.updateActivitiProperties(
                    "activiti:assign_value",
                    value.split(":")[0] + "#" + value.split(":")[1]
                  );
                }
              }
              that.$forceUpdate();
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
    updateActivitiProperties(key, value) {
      let taskAttr = {};
      taskAttr[key] = value;
      window.bpmnInstances.modeling.updateProperties(
        this.bpmnElement,
        taskAttr
      );
    },
    selectAssignMode() {
      let assign_mode = this.userTaskForm.assign_mode;
      let assign_content = this.userTaskForm.assign_content;
      if (
        assign_mode == "0" ||
        assign_mode == "3" ||
        assign_mode == "4" ||
        assign_mode == "5" ||
        assign_mode == "6" ||
        assign_mode == "7"
      ) {
        this.updateActivitiProperties("activiti:type", 1);
        this.updateActivitiProperties("activiti:assign_mode", assign_mode);
        this.updateActivitiProperties("activiti:assign_value", "");
      } else if (
        assign_mode == "2" &&
        assign_content != "" &&
        assign_content != undefined
      ) {
        this.updateActivitiProperties("activiti:type", 1);
        this.updateActivitiProperties("activiti:assign_mode", assign_mode);
        this.updateActivitiProperties("activiti:assign_value", assign_content);
      }
    },
  },

  beforeDestroy() {
    this.bpmnElement = null;
  },
};
</script>
