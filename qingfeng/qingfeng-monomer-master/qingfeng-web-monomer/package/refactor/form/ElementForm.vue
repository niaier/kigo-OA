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

      <el-form-item label="编辑字段" v-if="elementType == 'UserTask'">
        <el-select
          multiple
          placeholder="请选择"
          v-model="writeFieldKey"
          @change="updateElementwriteFieldKey"
        >
          <el-option-group
            v-for="group in fieldList"
            :key="group.id"
            :label="group.table_comment+'('+group.flag+')'"
          >
            <el-option
              v-for="item in group.vfields"
              :key="item.id"
              :label="item.field_comment"
              :value="item.field_name"
            >
            </el-option>
          </el-option-group>
        </el-select>
      </el-form-item>
      <el-form-item label="隐藏字段" v-if="elementType == 'UserTask'">
        <el-select
          multiple
          placeholder="请选择"
          v-model="hideFieldKey"
          @change="updateElementHideFieldKey"
        >
          <el-option-group
            v-for="group in fieldList"
            :key="group.id"
            :label="group.table_comment"
          >
            <el-option
              v-for="item in group.vfields"
              :key="item.id"
              :label="item.field_comment"
              :value="item.field_name"
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
      writeFieldKey: [],
      hideFieldKey: [],
      otherExtensions: [],
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
      console.log(this.bpmnELement)
      this.elementType = this.bpmnELement.type.split(":")[1];
      if (this.elementType == "UserTask") {
        let formKey =
          this.bpmnELement.parent.businessObject.flowElements[0].formKey;
        if (formKey == undefined || formKey == null) {
          this.formList = [];
          this.fieldList = [];
        } else {
          this.initData({ table_id: formKey });
        }
      } else if (this.elementType == "StartEvent") {
        this.initData({});
      }

      this.formKey = this.bpmnELement.businessObject.formKey;
      if (this.formKey != undefined) {
        this.getFieldList({ table_id: this.formKey });
      }

      // 获取元素扩展属性 或者 创建扩展属性
      this.elExtensionElements =
        this.bpmnELement.businessObject.get("extensionElements");

      this.writeFieldKey = [];
      this.hideFieldKey = [];
      if (this.elExtensionElements.values.length > 0) {
        let writeFieldKey =
          this.elExtensionElements.values[0].$attrs.writeFieldKey;
        let hideFieldKey =
          this.elExtensionElements.values[0].$attrs.hideFieldKey;
        if (writeFieldKey != undefined && writeFieldKey != "") {
          this.writeFieldKey = writeFieldKey.toString().split(",");
        }
        if (hideFieldKey != undefined && hideFieldKey != "") {
          this.hideFieldKey = hideFieldKey.toString().split(",");
        }
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
        this.fieldList = [];
        let object = response.data.object;
        object.flag = "主表";
        let data = response.data.data;
        this.fieldList.push(object);
        data.forEach((element) => {
          element.flag = "从表";
          this.fieldList.push(element);
        });
      });
    },
    updateElementFormKey() {
      window.bpmnInstances.modeling.updateProperties(this.bpmnELement, {
        formKey: this.formKey,
      });
      //查询字段信息
      this.getFieldList({ table_id: this.formKey });
    },
    updateElementwriteFieldKey() {
      // window.bpmnInstances.modeling.updateProperties(this.bpmnELement, {
      //   writeFieldKey: this.writeFieldKey,
      // });
      this.otherExtensions = [];
      const Field = window.bpmnInstances.moddle.create(
        `${this.prefix}:Field`,
        { writeFieldKey: this.writeFieldKey, hideFieldKey: this.hideFieldKey }
      );
      this.otherExtensions.push(Field);
      this.updateElementExtensions();
    },
    updateElementHideFieldKey() {
      // window.bpmnInstances.modeling.updateProperties(this.bpmnELement, {
      //   hideFieldKey: this.hideFieldKey,
      // });
      this.otherExtensions = [];
      const Field = window.bpmnInstances.moddle.create(
        `${this.prefix}:Field`,
        { writeFieldKey: this.writeFieldKey, hideFieldKey: this.hideFieldKey }
      );
      this.otherExtensions.push(Field);
      this.updateElementExtensions();
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
          values: this.otherExtensions,
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
