<template>
  <div>
    <div>
      <a-tree-select
        style="width: 100%"
        show-search
        :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
        :tree-data="treeData"
        placeholder="选择组织"
        tree-default-expand-all
        @change="selectTree"
      >
      </a-tree-select>
    </div>
    <div style="padding-top: 10px">
      <template v-for="item in tagList">
        <a-tooltip :key="item.id" :title="item.name">
          <a-tag
            color="blue"
            closable
            @close="delTab(item.id + ':' + item.name)"
          >
            {{ item.name }}
          </a-tag>
        </a-tooltip>
      </template>
    </div>
  </div>
</template>

<script>
import { getTreeList } from "@/api/system/organize";
import { getList } from "@/api/system/user";
export default {
  props: {
    record: {
      type: Object,
      default: null,
    },
  },
  data() {
    return {
      treeData: [],
      list: [],
      tagList: [],
    };
  },
  mounted() {
    let organizeIds = this.record.organize_id.split(",");
    let organizeNames = this.record.organize_name.split(",");
    for (var i = 0; i < organizeIds.length; i++) {
      this.tagList.push({ id: organizeIds[i], name: organizeNames[i] });
    }
    this.treeList({ parent_cascade: this.record.parent_id });
  },
  methods: {
    selectTree(value, label, extra) {
       value.forEach((item) => {
        let val = item.split(":");
        let data = this.tagList.filter((item) => item.id == val[0]);
        if (data.length == 0) {
          this.tagList.push({ id: val[0], name: val[1] });
        }
      });
    },
    treeList(params) {
      console.log(params);
      getTreeList(params).then((response) => {
        console.log(response.data.data);
        const treeData = this.fommat({
          arrayList: response.data.data.list,
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
        data.title = data.name;
        data.key = data.id;
        data.value = data.id;
        console.log(data.child_num);
        if (data.child_num == 0) {
          data.isLeaf = true;
        }
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
    delTab(val) {
      let tagList = this.tagList.filter(
        (item) => item.id + ":" + item.name != val
      );
      this.tagList = tagList;
      console.log(this.tagList);
    },
    filterOption(input, option) {
      return (
        option.componentOptions.children[0].text
          .toLowerCase()
          .indexOf(input.toLowerCase()) >= 0
      );
    },
    onOk() {
      console.log("------------ok");
      console.log(this.tagList);
      let organizeIds = [];
      let organizeNames = [];
      this.tagList.forEach((item) => {
        organizeIds.push(item.id);
        organizeNames.push(item.name);
      });
      this.$emit("initValue", organizeIds + ":" + organizeNames);
      return new Promise((resolve) => {
        resolve(true);
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
