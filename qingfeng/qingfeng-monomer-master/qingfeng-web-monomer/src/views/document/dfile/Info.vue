<template>
<a-card :bordered="false">
      <a-descriptions title="详情">
        <a-descriptions-item label="分类名称">{{form.name}}</a-descriptions-item>
        <a-descriptions-item label="排序">{{form.order_by}}</a-descriptions-item>
        <a-descriptions-item label="备注">{{form.remark}}</a-descriptions-item>
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
        name: "",
        order_by: 0,
        remark: "",
      },
    };
  },
  mounted() {
    this.form = this.record;
  },
  methods: {
    onCancel() {
      return new Promise((resolve) => {
        resolve(true);
      });
    },
  },
};
</script>
