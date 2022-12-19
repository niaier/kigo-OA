<template>
  <div class="panel-tab__content">
    <el-form size="mini" label-width="80px" @submit.native.prevent>
      <el-form-item label="关联表单">
        <el-select v-model="formKey" @change="updateElementFormKey">
          <el-option
            v-for="item in formList"
            :key="item.id"
            :value="item.id"
            :label="item.table_comment"
          />
          <!-- <el-option label="无" value="" /> -->
        </el-select>
      </el-form-item>

      <el-form-item label="只读字段">
        <el-select multiple placeholder="请选择" v-model="readFieldKey" @change="updateElementReadFieldKey">
          <el-option-group
            v-for="group in fieldList"
            :key="group.id"
            :label="group.table_comment"
          >
            <el-option
              v-for="item in group.vfields"
              :key="item.id"
              :label="item.field_comment"
              :value="item.id"
            >
            </el-option>
          </el-option-group>
        </el-select>
      </el-form-item>
      <el-form-item label="隐藏字段">
        <el-select multiple placeholder="请选择" v-model="hideFieldKey" @change="updateElementHideFieldKey">
          <el-option-group
            v-for="group in fieldList"
            :key="group.id"
            :label="group.table_comment"
          >
            <el-option
              v-for="item in group.vfields"
              :key="item.id"
              :label="item.field_comment"
              :value="item.id"
            >
            </el-option>
          </el-option-group>
        </el-select>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { findFormList, findFieldList } from "@/api/activiti/modeler";
export default {
  name: "ElementForm",
  props: {
    id: String,
    type: String,
  },
  inject: {
    prefix: "prefix",
    width: "width",
  },
  data() {
    return {
      formKey: "",
      businessKey: "",
      elementType: "",
      bpmnELement: "",
      formList: [],
      fieldList: [],
      readFieldKey:[],
      hideFieldKey:[]
    };
  },
  watch: {
    id: {
      immediate: true,
      handler(val) {
        val && val.length && this.$nextTick(() => this.resetFormList());
      },
    },
  },
  methods: {
    resetFormList() {
      this.bpmnELement = window.bpmnInstances.bpmnElement;
      console.log(this.bpmnELement);
      this.elementType = this.bpmnELement.type.split(":")[1];
      if (this.elementType == "UserTask") {
        let formKey = this.bpmnELement.parent.businessObject.flowElements[0].formKey;
        console.log("##:"+formKey)
        if(formKey==undefined||formKey==null){
          this.formList = [];
          this.fieldList = [];
        }else{
          console.log('0---')
          this.initData({table_id:formKey});
        }
      } else if (this.elementType == "StartEvent") {
        this.initData({});
      }

      this.formKey = this.bpmnELement.businessObject.formKey;
      if(this.formKey!=undefined){
        this.getFieldList({ table_id: this.formKey });
      }
      let readFieldKey = this.bpmnELement.businessObject.$attrs.readFieldKey;
      if(readFieldKey!=undefined){
        this.readFieldKey = readFieldKey.split(',');
      }
      let hideFieldKey = this.bpmnELement.businessObject.$attrs.hideFieldKey;
      if(hideFieldKey!=undefined){
        this.hideFieldKey = hideFieldKey.split(',');
      }

      // 获取元素扩展属性 或者 创建扩展属性
      this.elExtensionElements =
        this.bpmnELement.businessObject.get("extensionElements") ||
        window.bpmnInstances.moddle.create("bpmn:ExtensionElements", {
          values: [],
        });
      // 获取元素表单配置 或者 创建新的表单配置
      //formProperty
      if (this.prefix == "activiti") {
        console.log("comming");
        this.formData =
          this.elExtensionElements.values.filter(
            (ex) => ex.$type === `${this.prefix}:FormProperty`
          )?.[0] ||
          window.bpmnInstances.moddle.create(`${this.prefix}:FormProperty`, {
            fields: [],
          });
        // 业务标识 businessKey， 绑定在 formData 中
        this.businessKey = this.formData.businessKey;

        // 保留剩余扩展元素，便于后面更新该元素对应属性
        this.otherExtensions = this.elExtensionElements.values.filter(
          (ex) => ex.$type !== `${this.prefix}:FormProperty`
        );
      } else {
        this.formData =
          this.elExtensionElements.values.filter(
            (ex) => ex.$type === `${this.prefix}:FormData`
          )?.[0] ||
          window.bpmnInstances.moddle.create(`${this.prefix}:FormData`, {
            fields: [],
          });
        // 业务标识 businessKey， 绑定在 formData 中
        this.businessKey = this.formData.businessKey;

        // 保留剩余扩展元素，便于后面更新该元素对应属性
        this.otherExtensions = this.elExtensionElements.values.filter(
          (ex) => ex.$type !== `${this.prefix}:FormData`
        );
      }

      // 复制原始值，填充表格
      this.fieldList = JSON.parse(JSON.stringify(this.formData.fields || []));

      // 更新元素扩展属性，避免后续报错
      this.updateElementExtensions();
    },
    initData(params) {
      findFormList(params).then((response) => {
        this.formList = response.data.data;
      });
    },
    getFieldList(params) {
      findFieldList(params).then((response) => {
        let object = response.data.object;
        let data = response.data.data;
        this.fieldList.push(object);
        data.forEach((element) => {
          this.fieldList.push(element);
        });
        console.log(this.fieldList);
      });
    },
    updateElementFormKey() {
      console.log("******************");
      console.log(this.bpmnELement);
      console.log(window.bpmnInstances);
      window.bpmnInstances.modeling.updateProperties(this.bpmnELement, {
        formKey: this.formKey,
      });
      //查询字段信息
      this.getFieldList({ table_id: this.formKey });
    },
    updateElementReadFieldKey(){
       window.bpmnInstances.modeling.updateProperties(this.bpmnELement, {
        readFieldKey: this.readFieldKey,
      });
    },
     updateElementHideFieldKey(){
       window.bpmnInstances.modeling.updateProperties(this.bpmnELement, {
        hideFieldKey: this.hideFieldKey,
      });
    },
    updateElementBusinessKey() {
      window.bpmnInstances.modeling.updateModdleProperties(
        this.bpmnELement,
        this.formData,
        { businessKey: this.businessKey }
      );
    },
    updateElementExtensions() {
      // 更新回扩展元素,生成ExtensionElements的代码
      const newElExtensionElements = window.bpmnInstances.moddle.create(
        `bpmn:ExtensionElements`,
        {
          values: this.otherExtensions.concat(this.formData.fields),
        }
      );
      // 更新到元素上
      //此处直接放到扩展节点的根节点是，配合后台查询，可以自定义，但后台代码需要自己编写
       window.bpmnInstances.modeling.updateProperties(this.bpmnELement, {
        extensionElements: newElExtensionElements,
      }); 
    },
  },
};
</script>
