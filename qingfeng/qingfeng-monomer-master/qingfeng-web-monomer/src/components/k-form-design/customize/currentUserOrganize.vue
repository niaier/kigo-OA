<template>
  <div>
     <a-input
      hidden
      v-model="value"
      @input="handleChange"
    />
      <a-input v-model="value.split(':')[1]"  v-if="disabled" disabled />
      <a-input v-model="name"  v-if="!disabled" readOnly placeholder="请输入" @input="handleChange" />
  </div>
</template>
<script>
import { mapGetters } from 'vuex'
export default {
  name: 'cc',
  props: ['record', 'value','disabled'],
  // props: {
  //   record: {
  //     type: Object,
  //     require: true
  //   },
  //   value: {
  //     type: String,
  //     default: ''
  //   }
  // },
  data() {
    return {
      name:''
    };
  },
  methods: {
    handleChange (e) {
      // 使用 onChange 事件修改值
      this.$emit('change', e.target.value)
    }
  },
  computed: {
    ...mapGetters(['currentUser']),
  },
  mounted () {
    // let disabled = this.record.options.disabled;
    // let options = this.record.options;
    // 打印接收的options
    if(this.value==''){
      if(this.record.options.option_type=='0'){
        this.value=this.currentUser.id+":"+this.currentUser.name;
        this.name = this.currentUser.name;
        this.$emit('change', this.value)
      }else if(this.record.options.option_type=='1'){
        this.value=this.currentUser.orgPd.organize_id+":"+this.currentUser.orgPd.organize_name
        this.name = this.currentUser.orgPd.organize_name;
        this.$emit('change', this.value)
      }
    }else{
        this.name = this.value.split(":")[1];
        this.$emit('change', this.value)
    }
  }
}
</script>