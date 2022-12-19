<template>
  <a-form-model
    ref="ruleForm"
    :model="form"
    :rules="rules"
    :label-col="labelCol"
    :wrapper-col="wrapperCol"
  >
    <a-form-model-item label="选择分类" prop="share_classify_id">
      <a-tree-select
        v-model="form.share_classify_id"
        style="width: 100%"
        :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
        :tree-data="treeData"
        placeholder="选择分类"
        tree-default-expand-all
      >
      </a-tree-select>
    </a-form-model-item>
    <a-form-model-item label="备注" prop="share_remark">
      <a-input v-model="form.share_remark" type="textarea" />
    </a-form-model-item>
  </a-form-model>
</template>
<script>
import { updateShare } from "@/api/document/dfile";
import { findList } from "@/api/document/dclassify";
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
        order_by: 0,
        remark: "",
      },
      rules: {
        remark: [
          {
            min: 0,
            max: 500,
            message: "长度不得大于500个字符",
            trigger: "blur",
          },
        ],
      },
      treeData: [],
    };
  },
  mounted() {
    this.form = this.record;
    this.getTreeList({type:'1'});
  },
  methods: {
    getTreeList(params) {
      findList(params).then((response) => {
        const treeData = this.fommat({
          arrayList: response.data.data,
          pidStr: "parent_id",
        });
        this.treeData = treeData;
      });
    },
    fommat({
      arrayList,
      pidStr = "parent_id",
      idStr = "id",
      childrenStr = "children",
    }) {
      let listOjb = {}; // 用来储存{key: obj}格式的对象
      let treeList = []; // 用来储存最终树形结构数据的数组
      // 将数据变换成{key: obj}格式，方便下面处理数据
      for (let i = 0; i < arrayList.length; i++) {
        var data = arrayList[i];
        data.key = data.id;
        data.value = data.id;
        if (data.child_num == 0) {
          data.isLeaf = true;
        }
        data.title = data.name;
        data.icon = "";
        listOjb[arrayList[i][idStr]] = data;
      }
      // 根据pid来将数据进行格式化
      for (let j = 0; j < arrayList.length; j++) {
        // 判断父级是否存在
        let haveParent = listOjb[arrayList[j][pidStr]];
        if (haveParent) {
          // 如果有没有父级children字段，就创建一个children字段
          !haveParent[childrenStr] && (haveParent[childrenStr] = []);
          // 在父级里插入子项
          haveParent[childrenStr].push(arrayList[j]);
        } else {
          // 如果没有父级直接插入到最外层
          treeList.push(arrayList[j]);
        }
      }
      return treeList;
    },
    resetForm() {
      this.$refs.ruleForm.resetFields();
    },
    onOk() {
      return new Promise((resolve) => {
        updateShare(this.form).then((response) => {
          resolve(true);
        });
      });
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
