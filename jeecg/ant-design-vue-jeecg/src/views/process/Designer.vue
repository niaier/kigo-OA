<!--
 * @Author: niaier-pc niaier@outlook.com
 * @Date: 2022-12-15 12:47:10
 * @LastEditors: niaier-pc niaier@outlook.com
 * @LastEditTime: 2022-12-15 13:33:08
 * @FilePath: \ant-design-vue-jeecg\src\views\process\Designer.vue
 * @Description: 
 * 
 * Copyright (c) 2022 by niaier-pc niaier@outlook.com, All Rights Reserved. 
-->
<!--
 * @Author: niaier-pc niaier@outlook.com
 * @Date: 2022-12-15 12:47:10
 * @LastEditors: niaier-pc niaier@outlook.com
 * @LastEditTime: 2022-12-15 12:50:20
 * @FilePath: \ant-design-vue-jeecg\src\views\process\desinger.vue
 * @Description: 
 * 
 * Copyright (c) 2022 by niaier-pc niaier@outlook.com, All Rights Reserved. 
-->
<!-- 组件说明 -->
<template>
  <div class=''>
    流程设计
    <a-button @click="handleAdd">新增</a-button>
    <DesignerModal ref="modalForm"></DesignerModal>
  </div>
</template>

<script>
  import DesignerModal from './modules/DesignerModal.vue'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  export default {
    name: 'Designer',
    mixins: [JeecgListMixin],
    components: {
      DesignerModal
    },
    data () {
      return {
        disableMixinCreated:true

      };
    },
    computed: {

    },
    methods: {

    },
  }
</script>

<style lang="scss" scoped>

</style>