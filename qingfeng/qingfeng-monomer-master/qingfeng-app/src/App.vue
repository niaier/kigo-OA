<template>
  <a-config-provider :locale="locale">
    <div id="app" class="main">
      <router-view/>
    </div>
  </a-config-provider>
</template>

<script>
import { i18nRender } from '@/locales'

export default {
  data () {
    return {
    }
  },
  computed: {
    locale () {
    }
  }
}
</script>
<style>
.main{
}
</style>