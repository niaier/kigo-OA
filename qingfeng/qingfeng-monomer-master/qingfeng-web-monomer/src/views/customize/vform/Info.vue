<template>
  <div>
    <k-form-build ref="qfRef" :value="jsonData" />
    <a-modal v-model="visible" title="表单数据" on-ok="handleOk">
      <template slot="footer">
        <a-button
          key="submit"
          type="primary"
          :loading="loading"
          @click="handleOk"
        >
          关闭
        </a-button>
      </template>
      <pre>{{data}}</pre>
    </a-modal>
  </div>
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
      jsonData: {},
      visible: false,
      data:{}
    };
  },
  mounted() {
    this.jsonData = JSON.parse(this.record.table_content);
  },
  methods: {
    onOk() {
      // 使用getData函数获取数据
      this.$refs.qfRef
        .getData()
        .then((values) => {
          this.visible = true;
          this.data = values;
          console.log("验证通过", values);
        })
        .catch(() => {
          console.log("验证未通过，获取失败");
        });
    },
    onCancel() {
      return new Promise((resolve) => {
        resolve(true);
      });
    },
    handleOk() {
      this.visible = false;
    },
  },
};
</script>