<#assign isContainDictionary = 'false'>
<#list fieldList as obj>
  <#if obj.show_type == '3' || obj.show_type == '4' || obj.show_type == '5'>
    <#if !obj.option_content?contains(";")>
      <#assign isContainDictionary = 'true'>
    </#if>
  </#if>
</#list>
import React, { useState ,useEffect ,useRef } from 'react';
import {
ModalForm, ProFormText, ProFormTextArea,ProFormSelect,ProFormRadio,ProFormCheckbox,ProFormDatePicker } from '@ant-design/pro-form';
import { Editor } from '@tinymce/tinymce-react';
import { UploadOutlined,FileOutlined,DeleteOutlined } from '@ant-design/icons';
import { Form ,Modal,Button,Upload,message} from 'antd';
import { useIntl, FormattedMessage } from 'umi';

import type { TableListItem } from '../data.d';
import { upload } from '../../../common/upload/service';
<#if isContainDictionary == 'true'>
  import { findDictionaryList } from '../../../system/dictionary/service';
</#if>

export type FormValueType = {} & Partial<TableListItem>;

export type UpdateFormProps = {
  onCancel: (flag?: boolean, formVals?: FormValueType) => void;
  onSubmit: (values: FormValueType) => Promise<void>;
  updateModalVisible: boolean;
  values: Partial<TableListItem>;
};

const UpdateForm: React.FC<UpdateFormProps> = (props) => {
  const [form] = Form.useForm();
  <#list fieldList as obj>
    <#if obj.show_type == '6'>
      const ${obj.field_name}ref = useRef();
    </#if>
  <#if obj.show_type == '3' || obj.show_type == '4' || obj.show_type == '5'>
  const [${obj.field_name}Data, set${obj.field_name}Data] = useState<any>([]);
  </#if>
  <#if obj.show_type == '8'>
  const [${obj.field_name}Files, set${obj.field_name}Files] = useState<any>([]);
  </#if>
  </#list>

  useEffect(() => {
    if (form && !props.updateModalVisible) {
      form.resetFields();
    }
<#list fieldList as obj>
  <#if obj.show_type == '8'>
    set${obj.field_name}Files(props.values?.${obj.field_name}FileList||[])
  </#if>
<#if obj.show_type == '3'>
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
  }, [props.updateModalVisible]);

  const intl = useIntl();
  let index = 0;
  const handleOk = () => {
    form.submit();
    if(index>0){
      props.onCancel()
    }
    index++;
  };
  const handleCancel = () => {
    props.onCancel()
  }
  const handleFinish = (values: Record<string, any>) => {
    <#list fieldList as obj>
      <#if obj.show_type == '6'>
        values.${obj.field_name} = ${obj.field_name}ref.current.getContent()
      </#if>
      <#if obj.show_type == '8'>
        values.${obj.field_name} = ${obj.field_name}Files.map((item:any) => item.id).join(',')
      </#if>
    </#list>
      props.onSubmit(values as FormValueType);
  };

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
    <Modal
      width={640}
      title={intl.formatMessage({
        id: '编辑',
        defaultMessage: '编辑',
      })}
      visible={props.updateModalVisible}
      destroyOnClose
      onOk={handleOk} onCancel={handleCancel}
    >
      <Form
        form={form} 
        onFinish={handleFinish}
        initialValues={props.values}
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
          initialValue={props.values.${obj.field_name}}
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
        </Form>
    </Modal>    
  );
};

export default UpdateForm;
