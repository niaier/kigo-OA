<template>
  <div class="body">
    <a-radio-group
      v-model="currentNode"
      button-style="solid"
      v-show="data.length > 0"
      style="margin: 0 10px 20px 10px"
    >
      <a-radio-button :value="item" v-for="(item, index) in data" :key="index">
        {{ item.activityName }}
      </a-radio-button>
    </a-radio-group>
    <van-empty description="没有可退回节点" v-show="showReject" />
  </div>
</template>
<script>
import { getRunNodes } from "@/api/activiti/index";
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
      showReject: false,
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
        } else {
          this.showReject = true;
        }
      });
    },
    onOk() {
      this.$emit("initValue", this.currentNode);
      console.log("监听了 modal ok 事件");
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
<style scoped>
.card {
  margin-bottom: 12px;
}
.ant-radio-button-wrapper {
  margin: 4px;
}
</style>
