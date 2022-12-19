<#assign isContainStatus = 'false'>
<#assign isContainType = 'false'>
<#list fieldList as obj>
  <#if obj.field_name == 'status' && (tablePd.status_type == '0' || tablePd.status_type == '1')>
    <#assign isContainStatus = 'true'>
  </#if>
  <#if obj.field_name == 'type'>
    <#assign isContainType = 'true'>
  </#if>
</#list>
<template>
  <page-header-wrapper>
<#if tablePd.temp_type == '1'>
    <a-row :gutter="24">
      <a-col :md="24" :lg="6">
        <a-card :bordered="false">
          <tree ref="child" @selectTree="selectTree"></tree>
        </a-card>
      </a-col>
      <a-col :md="24" :lg="18">
</#if>
        <a-card :bordered="false">
          <div class="table-page-search-wrapper">
            <a-form layout="inline">
              <a-row :gutter="24">
<#list fieldList as obj>
    <#if obj.field_query == 'Y'>
	<#if obj.show_type == '1'||obj.show_type == '2'||obj.show_type == '6'>
                <a-col :md="6" :sm="24">
                  <a-form-item label="${obj.field_comment}">
                    <a-input v-model="queryParam.${obj.field_name}" placeholder="${obj.field_comment}" />
                  </a-form-item>
                </a-col>
	</#if>
	<#if obj.show_type == '7'>
                <a-col :md="6" :sm="24">
                  <a-form-item label="${obj.field_comment}">
                    <a-date-picker  v-model="queryParam.${obj.field_name}"  placeholder="${obj.field_comment}" />
                  </a-form-item>
                </a-col>
	</#if>
	<#if obj.show_type == '3'>

	</#if>
	<#if obj.show_type == '4'>

	</#if>
	<#if obj.show_type == '5'>

	</#if>
	</#if>
</#list>
                <a-col :md="6" :sm="24">
                  <a-button
                    type="primary"
                    @click="fetch({ pageSize: 10, ...queryParam })"
                    >查询</a-button
                  >
                  <a-button
                    style="margin-left: 8px"
                    @click="() => (queryParam = {})"
                    >重置</a-button
                  >
                </a-col>
              </a-row>
            </a-form>
          </div>
          <div style="margin: 0 0 10px 0">
            <a-space size="small">
              <a-button
                v-action:${tablePd.bus_name}:add
                @click="add()"
                >添加</a-button
              >
              <#if tablePd.temp_type != '0'||tablePd.open_process != '0'||tablePd.process_key==''>
              <a-button
                v-action:${tablePd.bus_name}:edit
                type="primary"
                @click="edits()"
                >编辑</a-button
              >
              <a-button
                v-action:${tablePd.bus_name}:del
                type="danger"
                @click="dels()"
                >删除</a-button
              >
              </#if>
            </a-space>
          </div>

          <a-table
            :row-selection="rowSelection"
            :columns="columns"
            :data-source="data"
            :pagination="pagination"
            :loading="loading"
            @change="handleTableChange"
            bordered
          >
            <template slot-scope="text, record, index">
              <div>
                {{ text }}
              </div>
            </template>
            <template slot="operation" slot-scope="text, record, index">
              <div class="editable-row-operations">
                <a-space size="small">
                  <a-button
                    size="small"
                    v-action:${tablePd.bus_name}:info
                    @click="() => info(record)"
                    >详情</a-button
                  >
                  <a-button
                    size="small"
                    type="primary"
                    v-action-dauth:[{...record,auth:`${tablePd.bus_name}:edit`}]
                    <#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
                      v-if="record.processStatus == '0'"
                    </#if>
                    @click="() => edit(record)"
                    >编辑</a-button
                  >
                  <a-button
                    size="small"
                    type="danger"
                    v-action-dauth:[{...record,auth:`${tablePd.bus_name}:del`}]
                    <#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
                    v-if="record.processStatus == '0'"
                    </#if>
                    @click="() => del(record)"
                    >删除</a-button
                  >
                  <#if isContainStatus == 'true'>
                  <a-button
                    size="small"
                    type="danger"
                    v-action:${tablePd.bus_name}:status
                    v-show="record.status == '0'"
                    @click="() => updateStatus(record.id, '1')"
                  >禁用</a-button>
                  <a-button
                    size="small"
                    type="primary"
                    v-action:${tablePd.bus_name}:status
                    v-show="record.status == '1'"
                    @click="() => updateStatus(record.id, '0')"
                  >启用</a-button>
                  </#if>
                </a-space>
              </div>
            </template>
          </a-table>
        </a-card>
<#if tablePd.temp_type == '1'>
      </a-col>
    </a-row>
</#if>
    <#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
    <a-modal
      v-model="apply_visible"
      title="发起流程"
      :footer="null"
      width="1000px"
      on-ok="handleOk"
      :destroyOnClose="true"
    >
      <template slot="footer"> </template>
      <edit-form :record="record" @finishResponse="finishResponse"></edit-form>
    </a-modal>
    </#if>
  </page-header-wrapper>
</template>
<script>
import { mapState } from "vuex";
import { getListPage, delData, updateStatus } from "@/api/${tablePd.mod_name}/${tablePd.bus_name}";
import { findDataAuth } from "@/core/baseAction.js";
import storage from "store";
import EditForm from "./Edit";
import InfoForm from "./Info";
import FormInfo from "@/views/activiti/processTask/FormInfo";
<#if tablePd.temp_type == '1'>
import Tree from "./Tree";
</#if>
const columns = [
<#list fieldList as obj>
<#if obj.field_list == 'Y'>
  {
    title: "${obj.field_comment}",
    dataIndex: "${obj.field_name}",
    ellipsis: true,
  },
</#if>
</#list>
  <#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
  {
    title: "当前办理状态",
    dataIndex: "dealStatus",
    ellipsis: true,
  },
  {
    title: "当前办理人",
    dataIndex: "assignee",
    ellipsis: true,
  },
  {
    title: "起办时间",
    dataIndex: "dealTime",
    ellipsis: true,
  },
  </#if>
  <#if tablePd.temp_type != '0'||tablePd.open_process != '0'||tablePd.process_key==''>
  {
    title: "创建时间",
    dataIndex: "create_time",
  },
  </#if>
  {
    title: "操作",
    dataIndex: "operation",
    scopedSlots: { customRender: "operation" },
    <#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
    width: "190px",
    </#if>
    <#if tablePd.temp_type != '0'||tablePd.open_process != '0'||tablePd.process_key==''>
    width: "240px",
    </#if>
  },
];

export default {
  data() {
    return {
      data: [],
      pagination: {},
      loading: false,
      columns,
      selectedRows: [],
      queryParam: {},
      <#if tablePd.temp_type == '1'>
      parent_id: "",
      parent_name: "",
      </#if>
      <#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
      process_key: "",
      apply_visible: false,
      record: {},
      </#if>
    };
  },
  components: {
    EditForm,
    InfoForm,
    <#if tablePd.temp_type == '1'>
    Tree,
    </#if>
    <#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
    FormInfo,
    </#if>
  },
  computed: {
    ...mapState({
      // 动态主路由
    }),
  rowSelection(){
      return {
        onChange: (selectedRowKeys, selectedRows) => {
            this.selectedRows = selectedRows;
        },
        getCheckboxProps: (record) => ({
          props: {
            disabled: record.name === "Disabled User", // Column configuration not to be checked
            name: record.name,
          },
        }),
      }
    },
  },
  mounted() {
    this.fetch({ pageSize: 10 });
  },
  methods: {
      handleTableChange(pagination, filters, sorter){
        console.log(pagination);
        const pager = {...this.pagination }
        pager.current = pagination.current;
        this.pagination = pager;
        this.fetch({
          pageSize: pagination.pageSize,
          pageNum: pagination.current,
          ...filters,
        })
      },
  <#if tablePd.temp_type == '1'>
      selectTree(id, name){
        this.parent_id = id;
        this.parent_name = name;
        this.fetch({pageSize: 10});
      },
  </#if>
      fetch(params = {}){
        this.loading = true;
        <#if tablePd.temp_type == '1'>
        getListPage({parent_id: this.parent_id,...params }).then((response) => {
        </#if>
      <#if tablePd.temp_type != '1'>
        getListPage({...params }).then((response) => {
      </#if>
          const pagination = {...this.pagination }
          // Read total count from server
          // pagination.total = data.totalCount;
          pagination.total = response.data.data.total;
          this.loading = false;
          this.data = response.data.data.rows;
          this.pagination = pagination;
          <#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
          this.process_key = response.data.process_key;
          </#if>
        })
      },
      info(record) {
        let component = InfoForm;
        <#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
        if (record.processStatus != "0") {
          record = {
            processInstanceId: record.processInstanceId,
            businessKey: record.businessKey,
          };
          component = FormInfo;
        }
        </#if>
        this.${'$'}dialog(
          component,
          // component props
          {
            record,
            on: {
              ok() {
                that.fetch({pageSize: 10});
                console.log("ok 回调");
              },
              cancel() {
                console.log("cancel 回调");
              },
              close() {
                console.log("modal close 回调");
              },
            },
          },
          // modal props
          {
            title: "详情",
            width: 1000,
            centered: true,
            maskClosable: false,
            footer: "",
          }
        );
      },
      edit(record){
        this.dialogData(record);
      },
      del(record){
        <#if tablePd.temp_type == '1'>
        if (record.child_num > 0) {
        this.${'$'}message.warning("删除的节点存在下级节点，不可删除。");
        } else {
        this.delData(record.id);
        }
        </#if>
        <#if tablePd.temp_type != '1'>
        this.delData(record.id);
        </#if>
      },
      add(){
        <#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
          this.dialogData({ process_key: this.process_key });
        </#if>
        <#if tablePd.temp_type != '0'||tablePd.open_process != '0'||tablePd.process_key==''>
          this.dialogData({});
        </#if>
      },
      edits(){
        if (this.selectedRows.length == 1) {
          const record = this.selectedRows[0];
          if (findDataAuth(record)) {
            this.dialogData(record);
          } else {
            this.${'$'}message.warning("没有编辑权限,请选择其他数据进行编辑。");
          }
        } else {
         this.${'$'}error({
            title: "提示：",
            content: "请选择一条数据进行编辑。",
            okText: "确认",
          });
        }
      },
      dels(){
        if (this.selectedRows.length > 0) {
          let delBol = true;
          for (let i = 0; i < this.selectedRows.length; i++) {
            const record = this.selectedRows[i];
            if (!findDataAuth(record)) {
                delBol = false;
            }
          }
          if (!delBol) {
            this.${'$'}message.warning("批量删除中存在没有删除权限的数据，请重新选择。");
            return;
          }
        <#if tablePd.temp_type == '1'>
          let names = [];
          for (let i = 0; i < this.selectedRows.length; i++) {
            if (this.selectedRows[i].child_num > 0) {
                names.push(this.selectedRows[i].name)
            }
          }
          if (names.length > 0) {
            this.${'$'}message.warning("节点" + names.join(',') + "存在下级节点，不可删除。");
            return;
          }
        </#if>
          const ids = this.selectedRows.map((map) => {
            return map.id;
          })
          this.delData(ids);
        } else {
          this.${'$'}error({
            title: "提示：",
            content: "请选择需要删除的数据。",
            okText: "确认",
          });
        }
      },
      <#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
      finishResponse(response) {
        this.apply_visible = false;
        this.fetch({ pageSize: 10 });
      },
      </#if>
      dialogData(record){
        const that = this;
        <#if tablePd.temp_type == '1'>
        if (this.parent_id == "" || this.parent_id == null) {
          that.${'$'}error({
            title: "提示：",
            content: "请选择左侧父级节点",
          });
          return;
        }
        </#if>
        record = {
          <#if tablePd.temp_type == '1'>
          parent_id:this.parent_id,
          parent_name:this.parent_name,
          </#if>
          ...record,
        }
        <#if tablePd.temp_type == '0'&&tablePd.open_process == '0'&&tablePd.process_key!=''>
          this.record = {
            ...record,
          };
          this.apply_visible = true;
        </#if>
        <#if tablePd.temp_type != '0'||tablePd.open_process != '0'||tablePd.process_key==''>
        this.${'$'}dialog(
          EditForm,
          // component props
          {
            record,
            on: {
              ok() {
                that.fetch({pageSize: 10});
                <#if tablePd.temp_type == '1'>
                that.getTreeList();
                </#if>
                console.log("ok 回调");
              },
              cancel() {
                  console.log("cancel 回调");
              },
              close() {
                  console.log("modal close 回调");
              },
            },
          },
          // modal props
          {
            title: "操作",
            width: 700,
            centered: true,
            maskClosable: false,
            okText: "确认",
            cancelText: "取消",
          }
        );
        </#if>
      },
      <#if isContainStatus == 'true'>
        updateStatus(id, status) {
          const that = this;
          updateStatus(id, status).then((response) => {
            if (response.status==200) {
              that.fetch({ pageSize: 10 });
            } else {
              that.$error({
                title: "提示：",
                content: "状态修改失败",
              });
            }
          });
        },
      </#if>
      delData(ids){
        const that = this;
        that.${'$'}confirm({
          title: "确定要删除选择的数据吗?",
          content: (h) => <div style="color:red;">数据删除后不可恢复</div>,
          okText:"确认",
          cancelText:"取消",
          onOk(){
            delData(ids).then((response) => {
              if (response.status==200) {
                that.$success({
                  title: "提示：",
                  content: "数据删除成功",
                });
                that.fetch({pageSize: 10});
                <#if tablePd.temp_type == '1'>
                that.getTreeList();
                </#if>
              }else{
                that.$error({
                  title: "提示：",
                  content: "数据删除失败",
                });
              }
            })
            console.log("OK：" + ids);
          },
          onCancel(){
            console.log("Cancel");
          },
        })
      },
      <#if tablePd.temp_type == '1'>
      getTreeList(){
        this.${'$'}refs.child.getTreeList({});
      }
      </#if>
    }
  }
</script>
<style scoped>
</style>