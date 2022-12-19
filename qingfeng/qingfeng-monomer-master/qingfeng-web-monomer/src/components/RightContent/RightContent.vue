<template>
  <div :class="wrpCls">
    <a-button @click="uploadFile"> 资源上传 </a-button>
    <avatar-dropdown
      :menu="showMenu"
      :current-user="currentUser"
      :class="prefixCls"
    />
    <select-lang :class="prefixCls" />

    <a-drawer
      title="资源上传"
      placement="right"
      width="640"
      :closable="false"
      :visible="visible"
      @close="onClose"
    >
      <upload></upload>
    </a-drawer>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import AvatarDropdown from './AvatarDropdown'
import SelectLang from '@/components/SelectLang'
import Upload from "@/components/Upload/Index";

export default {
  name: 'RightContent',
  components: {
    AvatarDropdown,
    SelectLang,
    Upload,
  },
  props: {
    prefixCls: {
      type: String,
      default: 'ant-pro-global-header-index-action',
    },
    isMobile: {
      type: Boolean,
      default: () => false,
    },
    topMenu: {
      type: Boolean,
      required: true,
    },
    theme: {
      type: String,
      required: true,
    },
  },
  data () {
    return {
      showMenu: true,
      visible: false
    }
  },
  computed: {
    ...mapGetters(['currentUser', 'avatar']),
    wrpCls () {
      return {
        'ant-pro-global-header-index-right': true,
        [`ant-pro-global-header-index-${(this.isMobile || !this.topMenu) ? 'light' : this.theme}`]: true,
      }
    },
  },
   methods: {
    uploadFile() {
      this.visible = true;
    },
    onClose() {
      this.visible = false;
    },
  },
}
</script>
