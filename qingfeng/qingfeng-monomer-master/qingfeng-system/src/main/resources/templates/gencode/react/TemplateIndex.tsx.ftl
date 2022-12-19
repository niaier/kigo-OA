<#assign isContainDictionary = 'false'>
<#list fieldList as obj>
  <#if obj.show_type == '3' || obj.show_type == '4' || obj.show_type == '5'>
    <#if !obj.option_content?contains(";")>
      <#assign isContainDictionary = 'true'>
    </#if>
  </#if>
</#list>
import { PlusOutlined,FileOutlined,DeleteOutlined } from '@ant-design/icons';
import { Button, message, Drawer, Modal, FormInstance,Upload,Form } from 'antd';
import React, { useState, useRef, useEffect } from 'react';
import { UploadOutlined } from '@ant-design/icons';
import { Editor } from '@tinymce/tinymce-react';
import { useIntl, FormattedMessage, connect, ConnectProps } from 'umi';
import type { ConnectState } from '@/models/connect';
import type { CurrentUser } from '@/models/user';
import { PageContainer, FooterToolbar } from '@ant-design/pro-layout';
import type { ProColumns, ActionType } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { ModalForm, ProFormText, ProFormTextArea,ProFormSelect,ProFormRadio,ProFormCheckbox,ProFormDatePicker } from '@ant-design/pro-form';
import type { ProDescriptionsItemProps } from '@ant-design/pro-descriptions';
import ProDescriptions from '@ant-design/pro-descriptions';
import type { FormValueType } from './components/UpdateForm';
import UpdateForm from './components/UpdateForm';
<#if tablePd.temp_type == '1'>
import Tree from './components/Tree';
</#if>
import { upload } from '../../common/upload/service';
<#if isContainDictionary == 'true'>
  import { findDictionaryList } from '../../system/dictionary/service';
</#if>
import type { TableListItem } from './data.d';
import { queryData, updateData, addData, removeData, updateStatus } from './service';

/**
 * 添加节点
 *
 * @param fields
 */
const handleAdd = async (fields: TableListItem) => {
  const hide = message.loading('正在添加');
  try {
  <#list fieldList as obj>
  <#if obj.show_type == '5'>
    if (fields?.${obj.field_name} instanceof Array) {
      fields.${obj.field_name} = fields.${obj.field_name}.join(',');
    }
  </#if>
  </#list>
    await addData({ ...fields });
    hide();
    message.success('添加成功');
    return true;
  } catch (error) {
    hide();
    message.error('添加失败请重试！');
    return false;
  }
};

/**
 * 更新节点
 *
 * @param fields
 */
const handleUpdate = async (fields: FormValueType) => {
  const hide = message.loading('正在配置');
  try {
<#list fieldList as obj>
  <#if obj.show_type == '5'>
    if (fields?.${obj.field_name} instanceof Array) {
      fields.${obj.field_name} = fields.${obj.field_name}.join(',');
    }
  </#if>
</#list>
    await updateData(fields);
    hide();
    message.success('配置成功');
    return true;
  } catch (error) {
    hide();
    message.error('配置失败请重试！');
    return false;
  }
};

/**
 * 删除节点
 *
 * @param selectedRows
 */
const handleRemove = async (selectedRows: TableListItem[]) => {
  const hide = message.loading('正在删除');
  if (!selectedRows) return true;
  try {
    await removeData({
      ids: selectedRows.map((row) => row.id),
    });
    hide();
    message.success('删除成功，即将刷新');
    return true;
  } catch (error) {
    hide();
    message.error('删除失败，请重试');
    return false;
  }
};


const handleRemoveOne = async (selectedRow: TableListItem) => {
  const hide = message.loading('正在删除');
  if (!selectedRow) return true;
  try {
    let params = [selectedRow.id]
    await removeData({
      ids: params,
    });
    hide();

    message.success('删除成功，即将刷新');
    return true;
  } catch (error) {
    hide();
    message.error('删除失败，请重试');
    return false;
  }
};


const handleUpdateStatus = async (id: string, status: string) => {
  const hide = message.loading('正在更新状态');
  try {
    await updateStatus(id, status);
    message.success('状态更新成功，即将刷新');
    return true;
  } catch (error) {
    hide();
    message.error('状态更新失败，请重试');
    return false;
  }
};

export type GlobalTableProps = {
  currentUser?: CurrentUser;
} & Partial<ConnectProps>;

const TableList: React.FC<GlobalTableProps> = (props) => {
  const formTableRef = useRef<FormInstance>();
  const formRef = useRef<FormInstance>();
  const childref = useRef();
  <#list fieldList as obj>
  <#if obj.show_type == '6'>
  const ${obj.field_name}ref = useRef();
  </#if>
  </#list>

  /** 新建窗口的弹窗 */
  const [createModalVisible, handleModalVisible] = useState<boolean>(false);
  /** 分布更新窗口的弹窗 */
  const [updateModalVisible, handleUpdateModalVisible] = useState<boolean>(false);

  const [showDetail, setShowDetail] = useState<boolean>(false);

  const actionRef = useRef<ActionType>();
  const [currentRow, setCurrentRow] = useState<TableListItem>();
  const [selectedRowsState, setSelectedRows] = useState<TableListItem[]>([]);
<#if tablePd.temp_type == '1'>
  //选中树形
  const [selectTree, setSelectTree] = useState<any>();
</#if>

  /** 国际化配置 */
  const intl = useIntl();
  const { currentUser } = props;
  <#list fieldList as obj>
  <#if obj.show_type == '3' || obj.show_type == '4' || obj.show_type == '5'>
  const [${obj.field_name}Data, set${obj.field_name}Data] = useState<any>([]);
  </#if>
  <#if obj.show_type == '8'>
  const [${obj.field_name}Files, set${obj.field_name}Files] = useState<any>([]);
  </#if>
  </#list>

  useEffect(() => {
    <#list fieldList as obj>
    <#if obj.show_type == '3' >
    <#if obj.option_content?contains(";")>
    const options = [
    <#list obj.option_content?split(";") as name>
    <#assign param = name?split("/")>
    { value: '${param[0]}', label: '${param[1]}' },
    </#list>
    ];
    set${obj.field_name}Data(options);
    </#if>
    <#if !obj.option_content?contains(";")>
    findDictionaryList({parent_code:'${obj.option_content}'}).then((response) => {
      let data = [];
      for(var i=0;i<response.data?.length||0;i++){
        data.push({label:response.data[i].name,value:response.data[i].id});
      }
      set${obj.field_name}Data(data);
    })
    </#if>
    </#if>
      <#if obj.show_type == '5' || obj.show_type == '4'>
        <#if obj.option_content?contains(";")>
          const options = [
          <#list obj.option_content?split(";") as name>
            <#assign param = name?split("/")>
            '${param[1]}',
          </#list>
          ];
          set${obj.field_name}Data(options);
        </#if>
        <#if !obj.option_content?contains(";")>
          findDictionaryList({parent_code:'${obj.option_content}'}).then((response) => {
          let data = [];
          for(var i=0;i<response.data?.length||0;i++){
          data.push(response.data[i].name);
          }
          set${obj.field_name}Data(data);
          })
        </#if>
      </#if>
    </#list>
  }, []);

  const columns: ProColumns<TableListItem>[] = [
  <#assign isFirstList = 'true'>
  <#list fieldList as obj>
    <#if obj.field_list == 'Y'>
      <#if isFirstList == 'true'>
        {
          title: '${obj.field_comment}',
          dataIndex: '${obj.field_name}',
          tip: '${obj.field_comment}',
          render: (dom, entity) => {
            return (
              <a onClick={() => {
                setCurrentRow(entity);
                setShowDetail(true);
                }}
                >
                {dom}
              </a>
            );
          },
        },
        <#assign isFirstList = 'false'>
      </#if>
      <#if isFirstList == 'false'>
        {
          title: '${obj.field_comment}',
          dataIndex: '${obj.field_name}',
          valueType: 'textarea',
        <#if obj.field_query != 'Y'>
          hideInSearch: true,
        </#if>
        },
      </#if>
    </#if>
  </#list>
    {
      title: <FormattedMessage id="创建时间" defaultMessage="创建时间" />,
      dataIndex: 'create_time',
      valueType: 'textarea',
      hideInSearch: true,
    },
    {
      title: <FormattedMessage id="pages.searchTable.titleOption" defaultMessage="操作" />,
      dataIndex: 'option',
      width: '220px',
      valueType: 'option',
      render: (_, record) => [
        <Button type="primary" size='small' key="config"
          style={{ display: currentUser?.authButton.includes('${tablePd.bus_name}:edit') ? 'block' : 'none' }}
          onClick={() => {
            <#if tablePd.temp_type == '1'>
            record.parent_name = selectTree.name;
            record.parent_id = selectTree.id;
            </#if>
            handleUpdateModalVisible(true);
            setCurrentRow(record);
          }}>
          编辑
       </Button>,
        <Button type="primary" size='small' danger
          style={{ display: currentUser?.authButton.includes('${tablePd.bus_name}:del') ? 'block' : 'none' }}
          onClick={async () => {
            Modal.confirm({
              title: '删除任务',
              content: '确定删除该任务吗？',
              okText: '确认',
              cancelText: '取消',
              onOk: () => {
                const success = handleRemoveOne(record);
                if (success) {
                  if (actionRef.current) {
                    actionRef.current.reload();
                  }
                  <#if tablePd.temp_type == '1'>
                  childref.current._childFn();
                  </#if>
                }
              },
            });
          }}>
          删除
      </Button>,
        <Button type="primary" size='small' style={{ display: (record.status == '0' && currentUser?.authButton.includes('${tablePd.bus_name}:status')) ? 'block' : 'none' }} danger onClick={() => {
          const success = handleUpdateStatus(record.id, '1');
          if (success) {
            if (actionRef.current) {
              actionRef.current.reload();
            }
          }
        }}>
          禁用
    </Button>,
        <Button type="primary" size='small' style={{ display: (record.status == '1' && currentUser?.authButton.includes('${tablePd.bus_name}:status')) ? 'block' : 'none' }} onClick={() => {
          const success = handleUpdateStatus(record.id, '0');
          if (success) {
            if (actionRef.current) {
              actionRef.current.reload();
            }
          }
        }}>
          启用
  </Button>,
      ],
    },
  ];

  <#list fieldList as obj>
  <#if obj.field_operat == 'Y'>
  <#if obj.show_type == '8'>
    const upload${obj.field_name}File = async (file: any) => {
      const formData = new FormData();
      formData.append("file", file.file);
      await upload(formData).then(res => {
        console.log('上传完毕11---');
        console.log(res);
        console.log(${obj.field_name}Files.concat(res.data))
        set${obj.field_name}Files(${obj.field_name}Files.concat(res.data));
      })
    }
    const handle${obj.field_name}UploadChange = async (info: any) => {
      if (info.file.status === 'uploading') {
        return;
      }
      if (info.file.status === 'done') {
        console.log('上传完毕---');
        console.log(info);
      }
    }

    const remove${obj.field_name}File = async (item: any) => {
      let data = ${obj.field_name}Files.filter((ele:any) => ele.id !== item.id)
      set${obj.field_name}Files(data);
      message.success('删除成功');
      console.log(data)
    }
  </#if>
  </#if>
  </#list>

  return (
    <PageContainer>
      <#if tablePd.temp_type == '1'>
      <div style={{ width: '20%', float: 'left' }}>
        <Tree myFef={childref} onSelect={async (value: any) => {
          setSelectTree(value);
          //查询列表数据
          if (actionRef.current) {
            formTableRef.current.setFieldsValue({
              parent_id: value.id
            });
            formTableRef.current.submit();
          }
        }}></Tree>
      </div>
      <div style={{ width: '79%', float: 'right' }}>
      </#if>
        <ProTable<TableListItem>
          headerTitle={intl.formatMessage({
            id: 'pages.searchTable.title',
            defaultMessage: '${tablePd.table_comment}',
          })}
          actionRef={actionRef}
          formRef={formTableRef}
          rowKey="id"
          search={{
            labelWidth: 120,
          }}
          toolBarRender={() => [
            <Button
              type="primary"
              key="primary"
              style={{ display: currentUser?.authButton.includes('${tablePd.bus_name}:add') ? 'block' : 'none' }}
              onClick={() => {
                <#if tablePd.temp_type == '1'>
                if (selectTree.id == '' || selectTree.id == null) {
                  message.warning('请选择左侧父级节点');
                } else {
                  formRef.current.setFieldsValue({
                    parent_name: selectTree.name,
                  });
                </#if>
                  handleModalVisible(true);
              <#list fieldList as obj>
              <#if obj.field_operat == 'Y'>
              <#if obj.show_type == '8'>
                 set${obj.field_name}Files([]);
              </#if>
              </#if>
              </#list>
                <#if tablePd.temp_type == '1'>
                }
                </#if>
              }}
            >
              <PlusOutlined /> <FormattedMessage id="pages.searchTable.new" defaultMessage="新建" />
            </Button>,
          ]}
          request={
        <#if tablePd.temp_type == '0'>
            (params, sorter, filter) => queryData({ ...params }).then(res => {
        </#if>
        <#if tablePd.temp_type == '1'>
          (params, sorter, filter) => queryData({ ...params, parent_id: selectTree.id }).then(res => {
        </#if>
              const result = {
                data: res.data.rows,
                total: res.data.total,
                success: true
              }
              return result
            })
          }
          columns={columns}
          rowSelection={{
            onChange: (_, selectedRows) => {
              setSelectedRows(selectedRows);
            },
          }}
        />
        <#if tablePd.temp_type == '1'>
      </div>
        </#if>
      {selectedRowsState?.length > 0 && (
        <FooterToolbar
          extra={
            <div>
              <FormattedMessage id="pages.searchTable.chosen" defaultMessage="已选择" />{' '}
              <a style={{ fontWeight: 600 }}>{selectedRowsState.length}</a>{' '}
              <FormattedMessage id="pages.searchTable.item" defaultMessage="项" />
            </div>
          }
        >
          <Button
            style={{ display: currentUser?.authButton.includes('${tablePd.bus_name}:del') ? 'block' : 'none' }}
            onClick={async () => {
              Modal.confirm({
                title: '删除任务',
                content: '确定删除该任务吗？',
                okText: '确认',
                cancelText: '取消',
                onOk: () => {
                  const success = handleRemove(selectedRowsState);
                  if (success) {
                    setSelectedRows([]);
                    actionRef.current?.reloadAndRest?.();
                  <#if tablePd.temp_type == '1'>
                    childref.current._childFn();
                  </#if>
                  }
                },
              });
            }}
          >
            <FormattedMessage id="pages.searchTable.batchDeletion" defaultMessage="批量删除" />
          </Button>
          {/* <Button type="primary">
            <FormattedMessage id="pages.searchTable.batchApproval" defaultMessage="批量审批" />
          </Button> */}
        </FooterToolbar>
      )}
      <ModalForm
        title={intl.formatMessage({
          id: '新建',
          defaultMessage: '新建',
        })}
        width="600px"
        formRef={formRef}
        visible={createModalVisible}
        onVisibleChange={handleModalVisible}
        onFinish={async (value) => {
        <#list fieldList as obj>
        <#if obj.show_type == '6'>
          value.${obj.field_name} = ${obj.field_name}ref.current.getContent()
        </#if>
        <#if obj.show_type == '8'>
          value.${obj.field_name} = ${obj.field_name}Files.map((item:any) => item.id).join(',')
        </#if>
        </#list>
        <#if tablePd.temp_type == '0'>
          const success = await handleAdd({ ...value } as TableListItem);
        </#if>
        <#if tablePd.temp_type == '1'>
          const success = await handleAdd({ ...value, parent_id: selectTree.id } as TableListItem);
        </#if>
          if (success) {
            handleModalVisible(false);
            if (actionRef.current) {
              actionRef.current.reload();
            }
            formRef.current?.resetFields();
          <#if tablePd.temp_type == '1'>
            childref.current._childFn();
          </#if>
          }
        }}
      >
        <#if tablePd.temp_type == '1'>
        <ProFormText
          label={intl.formatMessage({
            id: '父节点名称',
            defaultMessage: '父节点名称',
          })}
          disabled
          width="xl"
          name="parent_name"
          placeholder="请输入父节点名称"
        />
        </#if>
        <#list fieldList as obj>
        <#if obj.field_operat == 'Y'>
          <#if obj.show_type == '1'>
            <ProFormText
              label='${obj.field_comment}'
              <#if obj.verify_rule=='required'>
              rules={[
                {
                  required: true,
                  message: '${obj.field_comment}',
                },
              ]}
              </#if>
              width="xl"
              name="${obj.field_name}"
              placeholder="请输入${obj.field_comment}"
            />
          </#if>
          <#if obj.show_type == '2'>
          <ProFormTextArea width="xl"
          <#if obj.verify_rule=='required'>
            rules={[
              {
                required: true,
                message: '${obj.field_comment}',
              },
            ]}
            </#if>
            name="${obj.field_name}"
            label='${obj.field_comment}'
            placeholder="请输入${obj.field_comment}" />
          </#if>

          <#if obj.show_type == '3'>
          <ProFormSelect width="xl"
          <#if obj.verify_rule=='required'>
            rules={[
              {
                required: true,
                message: '${obj.field_comment}',
              },
            ]}
            </#if>
            options={${obj.field_name}Data}
            name="${obj.field_name}"
            label='${obj.field_comment}'
            placeholder="请选择${obj.field_comment}" />
          </#if>
          <#if obj.show_type == '4'>
          <ProFormRadio.Group width="xl"
          <#if obj.verify_rule=='required'>
            rules={[
              {
                required: true,
                message: '${obj.field_comment}',
              },
            ]}
            </#if>
            options={${obj.field_name}Data}
            name="${obj.field_name}"
            label='${obj.field_comment}'
            placeholder="请选择${obj.field_comment}" />
          </#if>
          <#if obj.show_type == '5'>
          <ProFormCheckbox.Group width="xl"
          <#if obj.verify_rule=='required'>
            rules={[
              {
                required: true,
                message: '${obj.field_comment}',
              },
            ]}
            </#if>
            options={${obj.field_name}Data}
            name="${obj.field_name}"
            label='${obj.field_comment}'
            placeholder="请选择${obj.field_comment}" />
          </#if>
          <#if obj.show_type == '6'>
          <Editor onInit={(evt, editor) => ${obj.field_name}ref.current = editor}
            initialValue=""
<#--            onEditorChange={handleEditorChange${obj.field_name}}-->
            apiKey='ivw6y17i9tle7joe78yt330tbpe7uygbffddq5jo578r40a0'
            init={{
            height: 500,
            menubar: false,
            toolbar: 'undo redo | formatselect | ' +
            'bold italic backcolor | alignleft aligncenter ' +
            'alignright alignjustify | bullist numlist outdent indent | ' +
            'removeformat | help',
            }}
          />
          </#if>
          <#if obj.show_type == '7'>
          <ProFormDatePicker width="xl"
          <#if obj.verify_rule=='required'>
            rules={[
              {
                required: true,
                message: '${obj.field_comment}',
              },
            ]}
            </#if>
            name="${obj.field_name}"
            label='${obj.field_comment}'
            placeholder="请输入${obj.field_comment}" />
          </#if>
          <#if obj.show_type == '8'>
            <Form.Item label="附件上传" name="requiredMarkValue">
              <Upload
                name="file"
                showUploadList={false}
                customRequest={upload${obj.field_name}File}
                onChange={handle${obj.field_name}UploadChange}
              >
                <Button><UploadOutlined /> 上传 </Button>
              </Upload>
              <div>
                {
                ${obj.field_name}Files.map((item: any) => {
                  return <div id={'div_'+item.id} style={{ lineHeight: '30px' }}>
                    <a onClick={() => {
                    window.location.href =
                    "../api/system/upload/downloadFile?name=" +
                    item.name +
                    "&file_path=" +
                    item.file_path;
                    }}><FileOutlined />{item.name}</a>
                    <a onClick={() => {
                      remove${obj.field_name}File(item)
                    }} style={{ paddingLeft: '10px', color: 'red' }}><DeleteOutlined /></a>
                  </div>
                })
              }
              </div>
            </Form.Item>
          </#if>
        </#if>
        </#list>
      </ModalForm>
      <UpdateForm
        onSubmit={async (value) => {
          const success = await handleUpdate({ ...value, id: currentRow?.id });
          // const success = await handleUpdate({...value,id:currentRow?.id});
          if (success) {
            handleUpdateModalVisible(false);
            setCurrentRow(undefined);
            if (actionRef.current) {
              actionRef.current.reload();
            }
          <#if tablePd.temp_type == '1'>
            childref.current._childFn();
          </#if>
          }
        }}
        onCancel={() => {
          handleUpdateModalVisible(false);
          setCurrentRow(undefined);
        }}
        updateModalVisible={updateModalVisible}
        values={currentRow || {}}
      />
      <Drawer
        width={600}
        visible={showDetail}
        onClose={() => {
          setCurrentRow(undefined);
          setShowDetail(false);
        }}
        closable={false}
      >
        {currentRow?.id && (
          <ProDescriptions<TableListItem>
            column={2}
            title='详情'
            request={async () => ({
              data: currentRow || {},
            })}
            params={{
              id: currentRow?.id,
            }}
            columns={columns as ProDescriptionsItemProps<TableListItem>[]}
          />
        )}
      </Drawer>
    </PageContainer>
  );
};

// export default TableList;
export default connect(({ user, loading }: ConnectState) => ({
  currentUser: user.currentUser,
}))(TableList);