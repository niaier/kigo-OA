<template>
  <div>
    <mt-radio title="radio list" v-model="value" :options="['optionA', 'optionB', 'optionC']"> </mt-radio>
  </div>
</template>

<script>
export default {
  data() {
    return {
      options: [
        {
          label: 'disabled option',
          value: 'valueF',
          disabled: true,
        },
        {
          label: 'optionA',
          value: 'valueA',
        },
        {
          label: 'optionB',
          value: 'valueB',
        },
      ],
    }
  },
  methods: {},
}
</script>
