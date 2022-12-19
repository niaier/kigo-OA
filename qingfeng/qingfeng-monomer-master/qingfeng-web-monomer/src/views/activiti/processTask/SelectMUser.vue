<template>
  <div>
    <div class="line-boder">
      <a-checkbox-group
        v-model="value"
        :style="{ padding: '10px' }"
        @change="onChange"
      >
        <a-checkbox
          v-for="(item, i) in list"
          :key="i"
          :value="item.user_id + ':' + item.user_name"
          >{{ item.user_name }}</a-checkbox
        >
      </a-checkbox-group>
    </div>
    <div id="selectValue" class="line-boder1">
      <a-tag
        v-for="(item, i) in selectList"
        :key="i"
        color="#2db7f5"
        :style="{ float: 'left' }"
      >
        {{ item.user_name }}
        <a @click="delUser(item.user_id, item.user_name)">x</a>
      </a-tag>
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
      value: [],
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
    console.log(this.record)
    console.log()
    if (this.record.user_id != undefined) {
      this.selectList = [];
      this.value = [];
      for(var i=0;i<this.record.user_id.length;i++){
        let user_id = this.record.user_id[i];
        let user_name = this.record.user_name[i];
        this.selectList.push({ user_id, user_name });
        this.value.push(user_id+":"+user_name);
      }
    }
  },
  methods: {
    onChange(e) {
      console.log(e)
      this.selectList = [];
      e.forEach((item) => {
        const value = item.split(":");
        const user_id = value[0];
        const user_name = value[1];
        this.selectList.push({ user_id, user_name });
      });
    },
    delUser(user_id, user_name) {
      this.selectList = this.selectList.filter(
        (item) => item.user_id != user_id
      );
        this.value = this.value.filter(
        (item) => item != user_id+":"+user_name
      );
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