<template>
<a-card :bordered="false">
      <a-descriptions title="详情信息">
        <a-descriptions-item label="标题">{{form.title}}</a-descriptions-item>
        <a-descriptions-item label="发布人">{{form.publish_user}}</a-descriptions-item>
        <a-descriptions-item label="发布时间">{{form.publish_time}}</a-descriptions-item>
        <a-descriptions-item label="图片地址"><img v-if="imageUrl" width="100px" :src="imageUrl" alt="avatar" /></a-descriptions-item>
      
        <a-descriptions-item label="排序">{{form.order_by}}</a-descriptions-item>
        <a-descriptions-item label="备注">{{form.remark}}</a-descriptions-item>
        <a-descriptions-item label="简介">{{form.intro}}</a-descriptions-item>
      
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
        type: "",
        title: "",
        intro: "",
        content: "",
        tpdz: "",
        publish_user: "",
        publish_time: "",
        read_num: "",
        status: "",
        order_by: 0,
        remark: "",
      },
      imageUrl:""
    };
  },
   mounted() {
    this.form = this.record;
    this.imageUrl = this.record.show_tpdz;
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
