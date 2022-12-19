<template>
  <div>
    <div class="line-boder">
      <a-radio-group
        v-model="value"
        :style="{ padding: '10px' }"
        @change="onChange"
      >
        <a-radio
          v-for="(item, i) in list"
          :key="i"
          :value="item.user_id + ':' + item.user_name"
        >
          {{ item.user_name }}
        </a-radio>
      </a-radio-group>
    </div>
    <div id="selectValue" class="line-boder1">
      <blockquote v-for="(item, i) in selectList" :key="i">
        <a-tag color="#2db7f5">
          {{ item.user_name }}
          <a @click="delUser(item.user_id, item.user_name)">x</a>
        </a-tag>
      </blockquote>
    </div>
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
      value: "",
      list: [],
      selectList: [],
    };
  },
  components: {},
  //生命周期 - 创建完成（访问当前this实例）
  created() {},
  //生命周期 - 挂载完成（访问DOM元素）
  mounted() {
    this.list = this.record.data;
    if (this.record.user_id != undefined) {
      const user_id = this.record.user_id;
      const user_name = this.record.user_name;
      this.value = this.record.user_id + ":" + this.record.user_name;
      this.selectList = [];
      if (user_id != "") {
        this.selectList.push({ user_id, user_name });
      }
    }
  },
  methods: {
    onChange(e) {
      const value = e.target.value.split(":");
      const user_id = value[0];
      const user_name = value[1];
      this.selectList = [];
      this.selectList.push({ user_id, user_name });
    },
    delUser(user_id, nauser_nameme) {
      this.selectList = [];
      this.value = "";
    },
    onOk() {
      this.$emit("initValue", this.value, "1");
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
/* @import url(); 引入css类 */
.ant-divider-horizontal {
  margin: 0 0 24px 0;
}

.line-boder {
  height: 180px;
  border: 1px solid #5fb878;
  margin-top: 2px;
  border-radius: 4px;
  overflow-y: scroll;
}

.line-boder1 {
  height: 88px;
  border: 1px solid #5fb878;
  margin-top: 2px;
  border-radius: 4px;
  overflow-y: scroll;
  padding: 4px;
}
</style>