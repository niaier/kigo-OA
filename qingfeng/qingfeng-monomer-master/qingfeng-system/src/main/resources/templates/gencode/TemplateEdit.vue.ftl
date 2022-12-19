<template>
<#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
<div>
    <div style="margin-bottom: 10px">
        <a-button @click="save"> 保存草稿 </a-button>
        <a-button type="primary" style="margin-left: 10px" @click="send"
        >流程发送</a-button
        >
    </div>
    <a-card
        style="width: 100%"
        :tab-list="cardList"
        :active-tab-key="activeKey"
        @tabChange="(key) => onTabChange(key, 'activeKey')"
    >
    <p v-if="activeKey === 'form'">
</#if>

  <a-form-model
    ref="ruleForm"
    :model="form"
    :rules="rules"
    :label-col="labelCol"
    :wrapper-col="wrapperCol"
  >
   <#if tablePd.temp_type == '1'>
    <a-form-model-item ref="parent_name" label="父级名称" prop="parent_name">
      <a-input disabled v-model="form.parent_name" />
    </a-form-model-item>
    </#if>
<#list fieldList as obj>
    <#if obj.field_operat == 'Y'>
        <#if obj.show_type == '1'>
	<a-form-model-item ref="${obj.field_name}" label="${obj.field_comment}" prop="${obj.field_name}">
	    <a-input v-model="form.${obj.field_name}"
		    @blur="() => {
		        ${'$'}refs.${obj.field_name}.onFieldBlur();
		    }"
	      />
	</a-form-model-item>
	</#if>
	<#if obj.show_type == '2'>
	<a-form-model-item label="${obj.field_comment}" prop="${obj.field_name}">
		<a-input v-model="form.${obj.field_name}" type="textarea" />
	</a-form-model-item>
	</#if>
	<#if obj.show_type == '3'>
	<a-form-model-item ref="${obj.field_name}" label="${obj.field_comment}" prop="${obj.field_name}">
        <a-select v-model="form.${obj.field_name}" placeholder="请选择${obj.field_comment}">
            <a-select-option v-for="(item, index) in ${obj.field_name}_data" :key = "index" :value="item.id"> {{item.name}} </a-select-option>
        </a-select>
    </a-form-model-item>
	</#if>
	<#if obj.show_type == '4'>
    <a-form-model-item ref="${obj.field_name}" label="${obj.field_comment}" prop="${obj.field_name}">
	    <a-radio-group v-model="form.${obj.field_name}">
	        <a-radio v-for="(item, index) in ${obj.field_name}_data" :value="item.id">{{item.name}} </a-radio>
	    </a-radio-group>
    </a-form-model-item>
	</#if>
	<#if obj.show_type == '5'>
    <a-form-model-item ref="${obj.field_name}" label="${obj.field_comment}" prop="${obj.field_name}">
	    <a-checkbox-group v-model="form.${obj.field_name}">
		    <a-checkbox v-for="(item, index) in ${obj.field_name}_data" :value="item.id">{{item.name}}</a-checkbox>
	    </a-checkbox-group>
    </a-form-model-item>
	</#if>
	<#if obj.show_type == '6'>
    <a-form-model-item ref="${obj.field_name}" label="${obj.field_comment}" prop="${obj.field_name}">
	    <rich-text :text="form.${obj.field_name}" @editorChange="editorChange" />
    </a-form-model-item>
	</#if>
	<#if obj.show_type == '8'>
    <a-form-model-item ref="${obj.field_name}" label="${obj.field_comment}" prop="${obj.field_name}">
        <a-upload
            name="file"
            :multiple="true"
            :show-upload-list="false"
            :customRequest="upload$obj.field_name}File"
            :loading="uploading"
        >
        <a-button><a-icon type="upload" /> Upload </a-button>
        </a-upload>
        <div v-for="(item, index) in ${obj.field_name}FileList" :style="{ lineHeight: '30px' }">
            <a @click="download${obj.field_name}File(item)"><a-icon type="link" />{{ item.name }}</a>
            <a @click="del${obj.field_name}File(item)" :style="{ paddingLeft: '10px', color: 'red' }"><a-icon type="delete"/></a>
        </div>
    </a-form-model-item>
	</#if>
</#if>
</#list>
  </a-form-model>
<#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
        </p>
        <p v-else-if="activeKey === 'flowchart'">
            <img :src="imageUrl" alt="流程图" class="image" />
        </p>
    </a-card>
</div>
</#if>
</template>
<script>
import { saveOrUpdate } from "@/api/${tablePd.mod_name}/${tablePd.bus_name}";
import { findDictionaryList } from "@/api/system/dictionary";
<#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
import AssignmentForm from "@/views/activiti/processTask/Assignment";
import axios from "axios";
</#if>
<#list fieldList as obj>
<#if obj.field_operat == 'Y'>
<#if obj.show_type == '6'>
import RichText from "@/components/Common/RichText";
</#if>
<#if obj.show_type == '8'>
import { upload } from "@/api/common/upload";
</#if>
</#if>
</#list>
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
<#list fieldList as obj>
    <#if obj.field_operat == 'Y'>
    <#if obj.show_type == '5'>
	    ${obj.field_name}:[],
    </#if>
    <#if obj.show_type != '5'>
        ${obj.field_name}:"",
    </#if>
    </#if>
</#list>
      },
  <#list fieldList as obj>
      <#if obj.show_type == '3' || obj.show_type == '4' || obj.show_type == '5'>
        ${obj.field_name}_data: [],
      </#if>
      <#if obj.show_type == '8'>
          ${obj.field_name}FileList: [],
      </#if>
  </#list>
       <#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
       cardList: [
           {
               key: "form",
               tab: "表单",
           },
          {
              key: "flowchart",
              tab: "流程图",
          },
      ],
      activeKey: "form",
      imageUrl: "",
      leave_type_data: [],
      </#if>
      rules: {
      <#list fieldList as obj>
	    <#if obj.field_operat == 'Y' && obj.verify_rule=='required'>
        ${obj.field_name}: [
            { required: true, message: "必填项不能为空", trigger: "blur" },
            { min: 0, max: 120, message: "长度不得大于120个字符", trigger: "blur" },
        ],
	    </#if>
	</#list>
      },
    }
  },
  components: {
<#list fieldList as obj>
    <#if obj.field_operat == 'Y'>
        <#if obj.show_type == '6'>
    RichText,
        </#if>
    </#if>
</#list>
    <#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
    AssignmentForm,
    </#if>
  },
  mounted() {
    if(this.record.id != undefined) {
      this.form = this.record;
    }
<#if tablePd.temp_type == '1'>
    this.form.parent_id = this.record.parent_id;
    this.form.parent_name = this.record.parent_name;
    this.${'$'}forceUpdate();
</#if>
<#list fieldList as obj>
<#if obj.show_type == '8'>
    if (this.record.introFileList != undefined) {
        this.${obj.field_name}FileList = this.record.${obj.field_name}FileList
    }
</#if>
</#list>
    this.initData();
    <#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
    this.flowchart(this.record);
    </#if>
  },
  methods: {
<#list fieldList as obj>
    <#if obj.show_type == '8'>
    del${obj.field_name}File(record,fileList) {
        let index = this.${obj.field_name}FileList.findIndex((ele) => {
            return ele.id === record.id;
        })
      //假设没有找到
      if (index === -1) {
          return console.log("删除失败");
      }
      //删除元素
      this.${obj.field_name}FileList.splice(index, 1);
      this.form.${obj.field_name} = this.${obj.field_name}FileList.map((map) => {
        return map.id;
      })
    },
    download${obj.field_name}File(record) {
      window.location.href =
          process.env.VUE_APP_API_URL +
          "/system/upload/downloadFile?name=" +
          record.name +
          "&file_path=" +
          record.file_path;
    },
    // 上传文件
    upload${obj.field_name}File(file) {
      this.uploading = true;
      const formData = new FormData();
      formData.append("file", file.file);
      upload(formData).then((res) => {
          if (res.status==200) {
              this.${obj.field_name}FileList.push(res.data.data);
              this.uploading = false;
              this.form.${obj.field_name} = this.${obj.field_name}FileList.map((map) => {
                  return map.id;
              }).join(',')
          }
        },
      (err) => {
          this.uploading = false;
      })
   },
    </#if>
</#list>
  <#list fieldList as obj>
    <#if obj.field_operat == 'Y'>
    <#if obj.show_type == '6'>
    editorChange: function (html) {
      this.content = html;
    },
    </#if>
    </#if>
</#list>
    <#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
    onTabChange(key, activeKey) {
        this.activeKey = key;
    },
    </#if>
    resetForm() {
      this.${'$'}refs.ruleForm.resetFields();
    },
    <#if tablePd.temp_type != '0'||tablePd.open_process != '0'||tablePd.process_key==''>
    onOk() {
        return new Promise((resolve) => {
            this.${'$'}refs.ruleForm.validate((valid) => {
                if (valid) {
                    <#list fieldList as obj>
                    <#if obj.field_operat == 'Y'>
                    <#if obj.show_type == '5'>
                    this.form.${obj.field_name} = this.form.${obj.field_name}.join(',');
                    </#if>
                    </#if>
                    </#list>
                    saveOrUpdate(this.form).then((response) => {
                        resolve(true)
                    })
                    return true;
                }else {
                    console.log("error submit!!");
                    return false;
                }
            })
        })
    },
    </#if>
    <#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
    save() {
      this.$refs.ruleForm.validate((valid) => {
          if (valid) {
              saveOrUpdate({
                  ...this.form,
                  submitType: "1",
              }).then((response) => {
                  this.$emit("finishResponse", response);
              });
          } else {
              console.log("error submit!!");
              return false;
          }
      });
    },
    send() {
      const that = this;
      let record = {
          processDefinitionKey: this.record.process_key,
          process_status: "0",
          taskId: "",
          processInstanceId: "",
      };
      this.$refs.ruleForm.validate((valid) => {
          if (valid) {
              this.$dialog(
                  AssignmentForm,
                  {
                      record,
                      on: {
                          complete(data, approve_opinion) {
                              saveOrUpdate({
                                  ...that.form,
                                  nodeData: data,
                                  approve_opinion: approve_opinion,
                                  submitType: "2",
                              }).then((response) => {
                                  that.$emit("finishResponse", response);
                              });
                          },
                          ok() {},
                          cancel() {},
                          close() {},
                      },
                  },
                  // modal props
                  {
                      title: "任务办理",
                      width: 800,
                      centered: true,
                      maskClosable: false,
                      okText: "确定",
                      cancelText: "取消",
                      bodyStyle: {
                          backgroundColor: "#f0f2f5",
                          padding: "16px",
                          border: "#FFFFFF 4px solid",
                      },
                  }
              );
              return true;
          } else {
              console.log("error submit!!");
              return false;
          }
      });
    },
    </#if>
    onCancel() {
      console.log("监听了 modal cancel 事件");
      return new Promise((resolve) => {
        resolve(true)
      })
    },
    initData(){
<#list fieldList as obj>
    <#if obj.show_type == '3' || obj.show_type == '4' || obj.show_type == '5'>
    <#if obj.option_content?contains(";")>
        const options = [
            <#list obj.option_content?split(";") as name>
                <#assign param = name?split("/")>
                { id: '${param[0]}', name: '${param[1]}' },
            </#list>
        ];
        this.${obj.field_name}_data = options;
    </#if>
    <#if !obj.option_content?contains(";")>
        findDictionaryList({parent_code:'${obj.option_content}'}).then((response) => {
            this.${obj.field_name}_data = response.data;
        })
    </#if>

    </#if>
    <#if obj.show_type != '5'>
        <#if obj.default_value??>
            this.form.${obj.field_name} = '${obj.default_value!''}';
        </#if>
    </#if>
    <#if obj.show_type == '5'>
        <#if obj.default_value??>
            this.form.${obj.field_name} = ['${obj.default_value!''}'];
        </#if>
    </#if>
</#list>
    },
    <#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
    flowchart(record) {
      this.visible = true;
      axios({
          method: "GET",
          url:
              process.env.VUE_APP_API_BASE_URL +
              `/system/activiti/processDefinition/readResource?procDefinitionKey=` +
              record.process_key,
          responseType: "arraybuffer",
      })
      .then((res) => {
          return (
              "data:image/svg+xml;base64," +
              btoa(
                  new Uint8Array(res.data).reduce(
                      (data, byte) => data + String.fromCharCode(byte),
                      ""
                  )
              )
          );
      })
      .then((res) => {
          this.imageUrl = res;
      })
      .catch((e) => {
          if (e.toString().indexOf("429") !== -1) {
              this.$notification.warning({
                  message: "请求超时,请稍后重试",
                  description: "",
              });
          } else {
              this.$notification.warning({
                  message: "获取流程图失败",
                  description: "",
              });
          }
      });
    },
    </#if>
  },
};
</script>
