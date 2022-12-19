<template>
  <a-config-provider :locale="locale">
    <div id="app">
      <router-view v-if="RouterState" />
    </div>
  </a-config-provider>
</template>

<script>
import { domTitle, setDocumentTitle } from "@/utils/domUtil";
import { i18nRender } from "@/locales";

export default {
  name: "App",
  provide() {
    return {
      reload: this.reload,
    };
  },
  data() {
    return {
      RouterState: true,
    };
  },
  methods: {
    goBack() {
      console.log("返回操作");
    },
    reload() {
      this.RouterState = false;
      this.$nextTick(() => {
        this.RouterState = true;
      });
    },
  },
  computed: {
    locale() {
      // sync dom title
      const { title } = this.$route.meta;
      title && setDocumentTitle(`${i18nRender(title)} - ${domTitle}`);

      return this.$i18n.getLocaleMessage(this.$store.getters.lang).antLocale;
    },
  },
};
</script>
