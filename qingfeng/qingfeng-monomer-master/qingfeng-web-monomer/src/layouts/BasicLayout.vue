<template>
  <pro-layout
    :title="siteTitle"
    :menus="menus"
    :collapsed="collapsed"
    :media-query="query"
    :handle-media-query="handleMediaQuery"
    :handle-collapse="handleCollapse"
    :i18n-render="i18nRender"
    v-bind="{
      theme,
      layout,
      isMobile,
      contentWidth,
      fixedHeader,
      fixSiderbar: fixedSidebar,
    }"
  >
    <a-layout-content
      :style="{
        height: '100%',
        width: mainWidth,
        margin: '15px 0px',
        paddingTop: fixedHeader ? '64px' : '0',
      }"
    >
      <multi-tab v-if="multiTabData"></multi-tab>
      <transition name="page-transition"> </transition>
    </a-layout-content>

    <template v-slot:menuHeaderRender>
      <div>
        <img src="~@/assets/logo.svg" :alt="siteTitle" />
        <h1>{{ siteTitle }}</h1>
      </div>
    </template>
    <template v-slot:rightContentRender>
      <right-content
        :top-menu="layout === 'topmenu'"
        :is-mobile="isMobile"
        :theme="theme"
      />
    </template>
    <template v-slot:footerRender>
      <global-footer />
    </template>
    <setting-drawer
      :settings="{
        layout,
        theme,
        contentWidth,
        primaryColor,
        colorWeak,
        fixedHeader,
        fixSiderbar: fixedSidebar,
        hideHintAlert: false,
        hideCopyButton: false,
      }"
      @change="handleSettingChange"
    />
    <router-view />
  </pro-layout>
</template>

<script>
import { mapState } from "vuex";
import { RightContent, GlobalFooter } from "@/components";
import {
  SettingDrawer,
  updateTheme,
  updateColorWeak,
} from "@ant-design-vue/pro-layout";
import { i18nRender } from "@/locales";
import { baseMixin } from "@/store/app-mixin";
import {
  CONTENT_WIDTH_TYPE,
  SIDEBAR_TYPE,
  TOGGLE_COLOR,
  TOGGLE_CONTENT_WIDTH,
  TOGGLE_FIXED_HEADER,
  TOGGLE_FIXED_SIDEBAR,
  TOGGLE_LAYOUT,
  TOGGLE_MOBILE_TYPE,
  TOGGLE_NAV_THEME,
  TOGGLE_WEAK,
} from "@/store/mutation-types";
import defaultSettings from "@/config/defaultSettings";
import MultiTab from "@/components/MultiTab";

export default {
  name: "BasicLayout",
  mixins: [baseMixin], // this[xxx] store value mixin, see this file
  data() {
    this.siteTitle = defaultSettings.title;
    return {
      // base
      menus: [],
      // 侧栏收起状态
      collapsed: false,
      // 媒体查询
      query: {},
      multiTabData: defaultSettings.multiTab,
      windowWidth: document.documentElement.clientWidth, //实时屏幕宽度
      windowHeight: document.documentElement.clientHeight, //实时屏幕高度
    };
  },
  computed: {
    ...mapState({
      // 动态主路由
      mainMenu: (state) => state.permission.addRouters,
    }),
    mainWidth: function() {
      if(this.collapsed){
        return (this.windowWidth-80-40)+'px';
      }else{
        return (this.windowWidth-256-40)+'px';
      }
    }
  },
  created() {
    // bind router (绑定路由)
    const routes = this.mainMenu.find((item) => item.path === "/");
    let menus = (routes && routes.children) || [];
    this.menus = menus;

    // 处理侧栏收起状态
    this.$watch("collapsed", () => {
      this.$store.commit(SIDEBAR_TYPE, this.collapsed);
    });
    this.$watch("isMobile", () => {
      this.$store.commit(TOGGLE_MOBILE_TYPE, this.isMobile);
    });
  },
  // <!--在watch中监听实时宽高-->
  watch: {
    windowHeight(val) {
      let that = this;
      // console.log("实时屏幕高度：", val, that.windowHeight);
    },
    windowWidth(val) {
      let that = this;
      // console.log("实时屏幕宽度：", val, that.windowHeight);
    },
  },
  mounted() {
    var that = this;
    // <!--把window.onresize事件挂在到mounted函数上-->
    window.onresize = () => {
      return (() => {
        window.fullHeight = document.documentElement.clientHeight;
        window.fullWidth = document.documentElement.clientWidth;
        that.windowHeight = window.fullHeight; // 高
        that.windowWidth = window.fullWidth; // 宽
      })();
    };

    const userAgent = navigator.userAgent;
    if (userAgent.indexOf("Edge") > -1) {
      this.$nextTick(() => {
        this.collapsed = !this.collapsed;
        setTimeout(() => {
          this.collapsed = !this.collapsed;
        }, 16);
      });
    }

    // first update color
    // THEME COLOR HANDLER!! PLEASE CHECK THAT!!
    if (
      process.env.NODE_ENV !== "production" ||
      process.env.VUE_APP_PREVIEW === "true"
    ) {
      updateTheme(this.primaryColor);
    }
    //  first update color weak
    if (this.colorWeak) {
      updateColorWeak(this.colorWeak);
    }
  },
  methods: {
    i18nRender,
    handleCollapse(val) {
      this.collapsed = val;
    },
    handleMediaQuery(val) {
      this.query = val;
      if (this.isMobile && !val["screen-xs"]) {
        this.$store.commit(TOGGLE_MOBILE_TYPE, false);
        return;
      }
      if (!this.isMobile && val["screen-xs"]) {
        this.$store.commit(TOGGLE_MOBILE_TYPE, true);
        this.collapsed = false;
        this.$store.commit(TOGGLE_CONTENT_WIDTH, CONTENT_WIDTH_TYPE.Fluid);
      }
    },
    /**
     * 同步和保存设置栏配置
     * */
    handleSettingChange({ type, value }) {
      console.log("type", type, value);
      switch (type) {
        case "contentWidth":
          this.$store.commit(TOGGLE_CONTENT_WIDTH, value);
          break;
        case "primaryColor":
          this.$store.commit(TOGGLE_COLOR, value);
          break;
        case "layout":
          this.$store.commit(TOGGLE_LAYOUT, value);
          if (value === "sidemenu") {
            this.$store.commit(TOGGLE_CONTENT_WIDTH, CONTENT_WIDTH_TYPE.Fluid);
          } else {
            this.$store.commit(TOGGLE_CONTENT_WIDTH, CONTENT_WIDTH_TYPE.Fixed);
            this.$store.commit(TOGGLE_FIXED_SIDEBAR, false);
          }
          break;
        case "theme":
          this.$store.commit(TOGGLE_NAV_THEME, value);
          break;
        case "fixedHeader":
          this.$store.commit(TOGGLE_FIXED_HEADER, value);
          break;
        case "fixSiderbar":
          this.$store.commit(TOGGLE_FIXED_SIDEBAR, value);
          break;
        case "colorWeak":
          this.$store.commit(TOGGLE_WEAK, value);
          break;
      }
    },
    initMenuData(menus){
      menus.forEach(item => {
        if(item.param!=""&&item.param!=undefined){
          item.path = item.path+item.param;
        }
        if(item.children){
          item.children = this.initMenuData(item.children);
        }
      });
      return menus;
    }
  },
  components: {
    RightContent,
    GlobalFooter,
    SettingDrawer,
    MultiTab,
  },
};
</script>

<style lang="less">
@import "./BasicLayout.less";
</style>
