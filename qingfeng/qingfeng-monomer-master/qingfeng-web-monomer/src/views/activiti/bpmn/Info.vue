<template>
<a-card :bordered="false">
      <a-descriptions title="流程设计详情">
        <a-descriptions-item label="流程id">{{form.processId}}</a-descriptions-item>
        <a-descriptions-item label="流程名称">{{form.processName}}</a-descriptions-item>
        <a-descriptions-item label="版本标签">{{form.versionTag}}</a-descriptions-item>
        <a-descriptions-item label="可执行">{{form.isExecutable=='true'?'是':'否'}}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{form.create_time}}</a-descriptions-item>
      </a-descriptions>
      <a-divider style="margin-bottom: 32px"/>
    </a-card>
</template>
<script>
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
        process_id: "",
        process_name: "",
        process_version: "",
        status: "",
        create_time: "",
      },
    };
  },
  mounted() {
    this.form = this.record;
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
