<template>
  <div class="properties-centent kk-checkbox">
    <div class="properties-body">
      <a-form>
        <a-form-item label="表单类型" style="border-bottom: 0px solid #ccc">
          <a-select
            v-model="config.table_type"
            placeholder="请选择表单类型"
            @change="selectTypeChange"
          >
            <a-select-option value="0"> 生成数据表 </a-select-option>
            <a-select-option value="1"> 绑定数据表 </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item
          label="数据表名称"
          v-if="config.table_type == '0'"
          style="border-bottom: 0px solid #ccc"
        >
          <a-input
            placeholder="数据表名称"
            @change="changeData"
            v-model="config.table_name"
          />
        </a-form-item>
        <a-form-item
          label="选择数据表"
          v-if="config.table_type == '1'"
          style="border-bottom: 0px solid #ccc"
        >
          <a-select
            v-model="config.table_name"
            placeholder="请选择数据表"
            @change="selectTableChange"
          >
            <a-select-option
              v-for="(item, index) in formData"
              :key="index"
              :value="item.table_name"
            >
              {{ item.table_name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="表单名称">
          <a-input
            placeholder="表单名称"
            @change="changeData"
            v-model="config.table_comment"
          />
        </a-form-item>
        <a-form-item label="表单布局">
          <a-radio-group buttonStyle="solid" v-model="config.layout">
            <a-radio-button value="horizontal">水平</a-radio-button>
            <a-radio-button value="vertical">垂直</a-radio-button>
            <a-radio-button value="inline">行内</a-radio-button>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="标签布局（水平布局生效）">
          <a-radio-group buttonStyle="solid" v-model="config.labelLayout">
            <a-radio-button value="flex">固定</a-radio-button>
            <a-radio-button value="Grid">栅格</a-radio-button>
          </a-radio-group>
        </a-form-item>
        <a-form-item
          v-show="config.labelLayout === 'flex'"
          label="标签宽度（px）"
        >
          <a-input-number v-model="config.labelWidth" />
        </a-form-item>
        <a-form-item label="labelCol" v-show="config.labelLayout !== 'flex'">
          <div class="change-col-box">
            <a-slider
              id="test"
              :max="24"
              :min="0"
              v-model="config.labelCol.xs"
              @change="handleChangeCol"
            />
            <div>
              <label>xs:</label>
              <a-input-number v-model="config.labelCol.xs" />
            </div>
            <div>
              <label>sm:</label>
              <a-input-number v-model="config.labelCol.sm" />
            </div>
            <div>
              <label>md:</label>
              <a-input-number v-model="config.labelCol.md" />
            </div>
            <div>
              <label>lg:</label>
              <a-input-number v-model="config.labelCol.lg" />
            </div>
            <div>
              <label>xl:</label>
              <a-input-number v-model="config.labelCol.xl" />
            </div>
            <div>
              <label>xxl:</label>
              <a-input-number v-model="config.labelCol.xxl" />
            </div>
          </div>
        </a-form-item>
        <a-form-item label="wrapperCol" v-show="config.labelLayout !== 'flex'">
          <div class="change-col-box">
            <div>
              <label>xs:</label>
              <a-input-number v-model="config.wrapperCol.xs" />
            </div>
            <div>
              <label>sm:</label>
              <a-input-number v-model="config.wrapperCol.sm" />
            </div>
            <div>
              <label>md:</label>
              <a-input-number v-model="config.wrapperCol.md" />
            </div>
            <div>
              <label>lg:</label>
              <a-input-number v-model="config.wrapperCol.lg" />
            </div>
            <div>
              <label>xl:</label>
              <a-input-number v-model="config.wrapperCol.xl" />
            </div>
            <div>
              <label>xxl:</label>
              <a-input-number v-model="config.wrapperCol.xxl" />
            </div>
          </div>
        </a-form-item>
        <a-form-item label="预览模态框宽度">
          <a-input-number style="width: 100%" v-model="previewOptions.width" />
        </a-form-item>
        <a-form-item label="表单CSS">
          <a-textarea v-model="config.customStyle" />
        </a-form-item>
        <a-form-item label="表单属性">
          <kCheckbox v-model="config.hideRequiredMark" label="隐藏必选标记" />
        </a-form-item>
        <a-form-item label="提示"> 实际预览效果请点击预览查看 </a-form-item>
      </a-form>
    </div>
  </div>
</template>
<script>
/*
 * author kcz
 * date 2019-11-20
 * description 表单属性设置面板组件
 */
import { getTableList } from "@/api/customize/vform";
import kCheckbox from "../../KCheckbox/index.vue";
import store from "@/store";
export default {
  name: "formProperties",
  components: {
    kCheckbox,
  },
  data() {
    return {
      table_name: "",
      table_comment: "",
      formData: [],
    };
  },
  props: {
    config: {
      type: Object,
      required: true,
    },
    previewOptions: {
      type: Object,
      required: true,
    },
  },
  mounted() {
    if (this.config.table_type == "" || this.config.table_type == undefined) {
      this.config.table_type = "0";
      this.selectTypeChange("0");
    }else{
      this.selectTypeChange(this.config.table_type);
    }
    if (this.config.table_name == "" || this.config.table_name == undefined) {
      const key = "table_" + new Date().getTime();
      this.config.table_name = key;
      this.config.table_comment = key;
    }else{
      this.selectTableChange(this.config.table_name);
    }
    //查询表单列表
    getTableList({}).then((response) => {
      this.formData = response.data.data;
    });
    this.table_name = this.config.table_name;
  },
  methods: {
    selectTypeChange(value) {
      let tableList = this.formData.filter(item => item.table_name==this.config.table_name);
      if(tableList.length==0){
        if(value=='0'){
          this.config.table_name = this.table_name;
        }else{
          this.config.table_name="";
        }
      }
      store.commit("SET_TABLE_TYPE", value);
      // console.log(store.getters.tableType);
    },
    selectTableChange(value) {
      store.commit("SET_TABLE_NAME", value);
      // console.log(store.getters.tableName);
    },
    changeData() {},
    handleChangeCol(e) {
      this.config.labelCol.xs =
        this.config.labelCol.sm =
        this.config.labelCol.md =
        this.config.labelCol.lg =
        this.config.labelCol.xl =
        this.config.labelCol.xxl =
          e;

      this.config.wrapperCol.xs =
        this.config.wrapperCol.sm =
        this.config.wrapperCol.md =
        this.config.wrapperCol.lg =
        this.config.wrapperCol.xl =
        this.config.wrapperCol.xxl =
          24 - e;
    },
  },
};
</script>
<style lang="less" scoped>
.change-col-box {
  > div {
    padding: 5px;
    display: flex;
    > label {
      text-align: right;
      padding-right: 8px;
      display: block;
      font-size: 16px;
      width: 45px;
    }
  }
}
</style>
