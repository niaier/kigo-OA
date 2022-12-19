<template>
  <div class="body">
    <a-card
      class="card"
      title="流程节点"
      :bordered="false"
      v-show="data.length > 0"
    >
      <a-radio-group
        v-model="currentNode"
        button-style="solid"
        @change="selectNode"
      >
        <a-radio-button
          :value="item"
          v-for="(item, index) in data"
          :key="index"
        >
          {{ item.node_name }}
        </a-radio-button>
      </a-radio-group>
    </a-card>
    <!-- 0-选择人员(单选人)，1-指定组织选择人员(单选人)，2-用户组人员选择(单选人)，
    3-发起人本组织选择(单选人)，8-指定范围选择(单选人)，9-代理人(单选人)，10-候选人（多选人），11-候选组（多选组） -->
    <!-- 4-部门经理（发起人部门经理），5-上级领导（发起人上级领导），6-分管领导（发起人分管领导），7-流程发起人 -->
    <a-card
      class="card"
      title="选择经办人"
      :bordered="false"
      v-show="data.length > 0"
      v-if="!objectData.node_flag"
    >
      <a-textarea
        placeholder="选择经办人"
        v-model="deal_names"
        allow-clear
        :rows="4"
        readonly
      />
      <el-button
        @click="selectOneUser()"
        v-show="
          '0,1,2,3,8,9'.indexOf(assign_mode) != -1 && multiInstances == ''
        "
        type="primary"
        style="margin-top: 8px; float: right"
        >选择</el-button
      >
      <el-button
        @click="selectMoreUser()"
        v-show="
          (assign_mode == '10' || assign_mode == '11') && multiInstances == ''
        "
        type="primary"
        style="margin-top: 8px; float: right"
        >选择</el-button
      >
      <el-button
        @click="selectMultiMoreUser()"
        v-show="multiInstances != ''"
        type="primary"
        style="margin-top: 8px; float: right"
        >选择</el-button
      >
    </a-card>
    <a-card
      class="card"
      title="选择经办人"
      :bordered="false"
      v-show="data.length > 0"
      v-if="objectData.node_flag"
    >
      <div style="text-align: center; font-size: 16px">无需指定</div>
    </a-card>
    <a-card
      class="card"
      title="办理意见"
      :bordered="false"
      v-show="record.process_status == '1'"
    >
      <a-textarea
        v-model="approve_opinion"
        placeholder="请输入审批意见"
        allow-clear
        :rows="4"
        @change="setApproveOpinion"
      />
    </a-card>
  </div>
</template>
<script>
import { findNextAssignment } from "@/api/activiti/processTask";
import { findGroupUser } from "@/api/system/group";
import SelectOneUser from "@/views/system/User/SelectOneUser";
import SelectMoreUser from "@/views/system/User/SelectMoreUser";
import SelectUser from "@/views/activiti/processTask/SelectUser";
import SelectMUser from "@/views/activiti/processTask/SelectMUser";

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
      data: [],
      currentNode: "",
      assign_mode: "",
      approve_opinion: "",
      objectData: {},
      multiInstances: "",
      //当前节点办理人和办理人名称
      deal_ids: "",
      deal_names: "",
    };
  },
  mounted() {
    this.initData();
  },
  methods: {
    initData() {
      findNextAssignment(this.record).then((response) => {
        this.data = response.data.data;
        this.objectData = response.data.object;
        console.log(response);
        console.log(this.objectData);
        if (this.data.length > 0) {
          this.currentNode = this.data[0];
          this.assign_mode = this.data[0].assign_mode;
          this.multiInstances = this.data[0].multiInstances;
          this.selectNode();
        }
      });
    },
    // 0-选择人员(单选人)，1-指定组织选择人员(单选人)，2-用户组人员选择(单选人)，
    // 3-发起人本组织选择(单选人)，8-指定范围选择(单选人)，9-代理人(单选人)，10-候选人（多选人），11-候选组（多选组）
    //  4-部门经理（发起人部门经理），5-上级领导（发起人上级领导），6-分管领导（发起人分管领导），7-流程发起人
    // 0,1,2,3,8,[9,10,11]
    // 4,5,6,7
    selectNode() {
      let node = this.currentNode;
      this.objectData.node_flag = false;
      this.assign_mode = node.assign_mode;
      this.multiInstances = node.multiInstances;
      if (node.assign_mode == undefined) {
        this.objectData.node_flag = true;
      } else if (node.assign_mode == "4") {
        //部门经理
        this.data.forEach((item) => {
          if (item.id == node.id) {
            item.deal_ids = this.objectData.depart_leader;
            item.deal_names = this.objectData.depart_leader_name;
            this.deal_ids = this.objectData.depart_leader;
            this.deal_names = this.objectData.depart_leader_name;
          }
        });
      } else if (node.assign_mode == "5") {
        //上级领导
        this.data.forEach((item) => {
          if (item.id == node.id) {
            item.deal_ids = this.objectData.direct_leader;
            item.deal_names = this.objectData.direct_leader_name;

            this.deal_ids = this.objectData.direct_leader;
            this.deal_names = this.objectData.direct_leader_name;
          }
        });
      } else if (node.assign_mode == "6") {
        //分管领导
        this.data.forEach((item) => {
          if (item.id == node.id) {
            item.deal_ids = this.objectData.branch_leader;
            item.deal_names = this.objectData.branch_leader_name;

            this.deal_ids = this.objectData.branch_leader;
            this.deal_names = this.objectData.branch_leader_name;
          }
        });
      } else if (node.assign_mode == "7") {
        //流程发起人
        this.data.forEach((item) => {
          if (item.id == node.id) {
            item.deal_ids = this.objectData.start_user_id;
            item.deal_names = this.objectData.start_user_name;

            this.deal_ids = this.objectData.start_user_id;
            this.deal_names = this.objectData.start_user_name;
          }
        });
      } else if (
        node.assign_mode == "9" ||
        node.assign_mode == "10" ||
        node.assign_mode == "11"
      ) {
        //代理人（选择单用户）、候选人（选择多用户）、候选组（选择多组织）
        this.data.forEach((item) => {
          if (item.id == node.id) {
            if (item.assign_content.indexOf("#") != -1) {
              item.deal_ids = item.assign_content.split("#")[0];
              item.deal_names = item.assign_content.split("#")[1];

              this.deal_ids = item.assign_content.split("#")[0];
              this.deal_names = item.assign_content.split("#")[1];
            }
          }
        });
      } else {
        this.data.forEach((item) => {
          if (item.id == node.id) {
            this.deal_ids = item.deal_ids;
            this.deal_names = item.deal_names;
          }
        });
      }
    },
    selectOneUser() {
      if (this.currentNode.assign_mode == "2") {
        //查询用户组下所有人员信息
        findGroupUser({ group_id: this.currentNode.assign_content }).then(
          (response) => {
            this.dialog(SelectUser, "selectUser", {
              user_id: this.deal_ids,
              user_name: this.deal_names,
              data: response.data.data,
            });
          }
        );
      } else if (this.currentNode.assign_mode == "8") {
        let arr = [];
        if (this.currentNode.assign_content.indexOf("#") != -1) {
          let user_ids = this.currentNode.assign_content
            .split("#")[0]
            .split(",");
          let user_names = this.currentNode.assign_content
            .split("#")[1]
            .split(",");
          for (let i = 0; i < user_ids.length; i++) {
            arr.push({ user_id: user_ids[i], user_name: user_names[i] });
          }
        }
        this.dialog(SelectUser, "selectUser", {
          user_id: this.deal_ids,
          user_name: this.deal_names,
          data: arr,
        });
      } else {
        let parent_organize = "";
        if (this.currentNode.assign_mode == "1") {
          if (this.currentNode.assign_content.indexOf("#") != -1) {
            parent_organize = this.currentNode.assign_content.split("#")[0];
          }
        } else if (this.currentNode.assign_mode == "3") {
          parent_organize = this.objectData.start_organize_id;
        }
        this.dialog(SelectOneUser, "oneUser", {
          user_id: this.deal_ids,
          user_name: this.deal_names,
          parent_id: parent_organize,
        });
      }
    },
    selectMoreUser() {
      this.dialog(SelectMoreUser, "moreUser", {
        user_id: this.deal_ids,
        user_name: this.deal_names,
      });
    },
    selectMultiMoreUser() {
      if (this.currentNode.assign_mode == "2") {
        //查询用户组下所有人员信息
        findGroupUser({ group_id: this.currentNode.assign_content }).then(
          (response) => {
            this.dialog(SelectMUser, "selectMUser", {
              user_id: this.deal_ids,
              user_name: this.deal_names,
              data: response.data.data,
            });
          }
        );
      } else if (this.currentNode.assign_mode == "8") {
        let arr = [];
        if (this.currentNode.assign_content.indexOf("#") != -1) {
          let user_ids = this.currentNode.assign_content
            .split("#")[0]
            .split(",");
          let user_names = this.currentNode.assign_content
            .split("#")[1]
            .split(",");
          for (let i = 0; i < user_ids.length; i++) {
            arr.push({ user_id: user_ids[i], user_name: user_names[i] });
          }
        }
        this.dialog(SelectMUser, "selectMUser", {
          user_id: this.deal_ids,
          user_name: this.deal_names,
          data: arr,
        });
      } else {
        let parent_organize = "";
        if (this.currentNode.assign_mode == "1") {
          if (this.currentNode.assign_content.indexOf("#") != -1) {
            parent_organize = this.currentNode.assign_content.split("#")[0];
          }
        } else if (this.currentNode.assign_mode == "3") {
          parent_organize = this.objectData.start_organize_id;
        }
        this.dialog(SelectMoreUser, "moreUser", {
          user_id: this.deal_ids,
          user_name: this.deal_names,
          parent_id: parent_organize,
        });
      }
    },
    //选择用户组织弹框
    dialog(component, fileType, record) {
      const that = this;
      this.$dialog(
        component,
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
              if (fileType == "oneUser") {
                that.deal_ids = value.split(":")[0];
                that.deal_names = value.split(":")[1];
                //更新节点数据
                that.data.forEach((item) => {
                  if (item.id == that.currentNode.id) {
                    item.deal_ids = value.split(":")[0];
                    item.deal_names = value.split(":")[1];
                  }
                });
              } else if (fileType == "moreUser") {
                that.deal_ids = value.split(":")[0];
                that.deal_names = value.split(":")[1];
                //更新节点数据
                that.data.forEach((item) => {
                  if (item.id == that.currentNode.id) {
                    item.deal_ids = value.split(":")[0];
                    item.deal_names = value.split(":")[1];
                  }
                });
              } else if (fileType == "selectUser") {
                that.deal_ids = value.split(":")[0];
                that.deal_names = value.split(":")[1];
                //更新节点数据
                that.data.forEach((item) => {
                  if (item.id == that.currentNode.id) {
                    item.deal_ids = value.split(":")[0];
                    item.deal_names = value.split(":")[1];
                  }
                });
              } else if (fileType == "selectMUser") {
                let deal_ids = [];
                let deal_names = [];
                value.forEach((item) => {
                  deal_ids.push(item.split(":")[0]);
                  deal_names.push(item.split(":")[1]);
                });
                that.deal_ids = deal_ids;
                that.deal_names = deal_names;
                //更新节点数据
                that.data.forEach((item) => {
                  if (item.id == that.currentNode.id) {
                    item.deal_ids = deal_ids;
                    item.deal_names = deal_names;
                  }
                });
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
    setApproveOpinion(e) {
      this.data.forEach((item, index) => {
        if (item.id == this.currentNode) {
          item.approve_opinion = e.target.value;
        }
      });
    },
    onOk() {
      let checkAssignee = true;
      this.data.forEach((item) => {
        console.log(item)
        if (this.currentNode.assign_mode != undefined) {
          if (item.deal_ids == undefined) {
            this.$message.warning(
              "节点【" + item.node_name + "】未设置审批人"
            );
            checkAssignee = false;
          }
        }
      });
      if (checkAssignee) {
        this.$emit("complete", this.data, this.approve_opinion);
        return new Promise((resolve) => {
          resolve(true);
        });
      }
    },
    onCancel() {
      console.log("监听了 modal cancel 事件");
      return new Promise((resolve) => {
        resolve(true);
      });
    },
  },
};
</script>
<style scoped>
.card {
  margin-bottom: 12px;
}
.ant-radio-button-wrapper {
  margin: 4px;
}
</style>
