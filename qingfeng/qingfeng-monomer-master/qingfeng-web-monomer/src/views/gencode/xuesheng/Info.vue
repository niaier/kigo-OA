<template>
    <a-form-model
        ref="ruleForm"
        :model="form"
        :rules="rules"
        :label-col="labelCol"
        :wrapper-col="wrapperCol"
    >
                        <a-form-model-item ref="name" label="名称" prop="name">
                    <a-input
                        disabled
                        v-model="form.name"
                        @blur="() => {
                            $refs.name.onFieldBlur();
                        }"
                    />
                </a-form-model-item>
                <a-form-model-item ref="sex" label="性别" prop="sex">
                    <a-input
                        disabled
                        v-model="form.sex"
                        @blur="() => {
                            $refs.sex.onFieldBlur();
                        }"
                    />
                </a-form-model-item>
                <a-form-model-item ref="age" label="年龄" prop="age">
                    <a-input
                        disabled
                        v-model="form.age"
                        @blur="() => {
                            $refs.age.onFieldBlur();
                        }"
                    />
                </a-form-model-item>
                <a-form-model-item ref="banji" label="班级" prop="banji">
                    <a-input
                        disabled
                        v-model="form.banji"
                        @blur="() => {
                            $refs.banji.onFieldBlur();
                        }"
                    />
                </a-form-model-item>
                <a-form-model-item ref="order_by" label="排序" prop="order_by">
                    <a-input
                        disabled
                        v-model="form.order_by"
                        @blur="() => {
                            $refs.order_by.onFieldBlur();
                        }"
                    />
                </a-form-model-item>
                <a-form-model-item label="备注" prop="remark">
                    <a-input disabled v-model="form.remark" type="textarea" />
                </a-form-model-item>
    </a-form-model>
</template>
<script>
    import { findDictionaryList } from "@/api/system/dictionary";
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
                labelCol: { span: 4 },
                wrapperCol: { span: 14 },
                other: "",
                mybol: false,
                headers: {
                    authorization: "authorization-text",
                },
                uploading: false,
                form: {
                    id: "",
                    name:"",
                    sex:"",
                    age:"",
                    banji:"",
                    order_by:"",
                    remark:"",
                },
                rules: {
                name: [
                    { required: true, message: "必填项不能为空", trigger: "blur" },
                    { min: 0, max: 120, message: "长度不得大于120个字符", trigger: "blur" },
                ],
            },
        }
    },
    components: {
    },
    mounted() {
        if(this.record.id != undefined) {
            this.form = this.record;
        }
        this.initData();
    },
    methods: {
        resetForm() {
            this.$refs.ruleForm.resetFields();
        },
        initData(){
        },
    },
  };
</script>
