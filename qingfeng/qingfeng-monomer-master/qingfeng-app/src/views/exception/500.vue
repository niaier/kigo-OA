<template>
  <div>
    <k-form-design @save="handleSave" />
  </div>
</template>
<script>
export default {
  methods: {
    handleSave(values) {
      alert("触发保存方法");
      console.log(values);
    }
  }
};
</script>