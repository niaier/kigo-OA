<template>
  <div class="body">
    <a-card class="card" title="选择退回节点" :bordered="false" v-show="data.length>0">
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
          {{ item.activityName }}
        </a-radio-button>
      </a-radio-group>
    </a-card>
  </div>
</template>
<script>
import { getRunNodes } from "@/api/activiti/processTask";
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
    };
  },
  mounted() {
    this.initData();
  },
  methods: {
    initData() {
      getRunNodes(this.record).then((response) => {
        this.data = response.data.data;
        if (this.data.length > 0) {
          this.currentNode = this.data[0];
        }
      });
    },
    selectNode() {
      console.log(this.currentNode)
    },
    onOk() {
      this.$emit('complete', this.currentNode);
      return new Promise((resolve) => {
        resolve(true);
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
<style scoped>
.card {
  margin-bottom: 12px;
}
.ant-radio-button-wrapper {
  margin: 4px;
}
</style>
