<template>
<a-card :bordered="false">
      <a-descriptions title="流程模型信息">
        <a-descriptions-item label="模型名称">{{form.name}}</a-descriptions-item>
        <a-descriptions-item label="模型key">{{form.key}}</a-descriptions-item>
        <a-descriptions-item label="部署id">{{form.deploymentId}}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{form.createTime}}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{form.lastUpdateTime}}</a-descriptions-item>
      </a-descriptions>
      <a-divider style="margin-bottom: 32px"/>
    </a-card>
</template>
<script>
import { formatDate } from "@/utils/date.js";
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
      mybol: false,
      form: {
        id: "",
        name: "",
        key: "",
        deploymentId: "",
        createTime: "",
        lastUpdateTime: "",
      },
    };
  },
  mounted() {
    this.form = this.record;
    if(this.form.deploymentId==''||this.form.deploymentId==null||this.form.deploymentId==undefined){
      this.form.deploymentId='未部署';
    }else{
      this.form.deploymentId="已部署："+this.form.deploymentId;
    }
    this.form.createTime = formatDate(this.form.createTime);
    this.form.lastUpdateTime = formatDate(this.form.lastUpdateTime);
  },
  methods: {
    onCancel() {
      console.log("监听了 modal cancel 事件");
      return new Promise((resolve) => {
        resolve(true);
      });
    },
  },
};
</script>
