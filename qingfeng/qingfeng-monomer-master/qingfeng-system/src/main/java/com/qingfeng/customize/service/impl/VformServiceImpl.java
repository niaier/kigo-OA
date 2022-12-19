package com.qingfeng.customize.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingfeng.customize.mapper.VfieldMapper;
import com.qingfeng.customize.service.IVfieldService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.customize.Vfield;
import com.qingfeng.entity.customize.Vform;
import com.qingfeng.customize.mapper.VformMapper;
import com.qingfeng.customize.service.IVformService;
import com.qingfeng.utils.DateTimeUtil;
import com.qingfeng.utils.GuidUtil;
import com.qingfeng.utils.PageData;
import com.qingfeng.utils.Verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author qingfeng
 * @title: VformServiceImpl
 * @projectName qingfeng-cloud
 * @description: TODO
 * @date 2021/3/8 000821:13
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class VformServiceImpl extends ServiceImpl<VformMapper, Vform> implements IVformService {

    @Autowired
    public IVfieldService vfieldService;

    String noField = "button、alert、text、html、divider";
    String bjField = "card、tabs、grid、table";
    String dynamicField = "select、checkbox、radio、treeSelect、cascader";

    /**
    * @title findListPage
    * @description 查询数据分页列表
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:57
    */
    public IPage<Vform> findListPage(Vform vform, QueryRequest request){
        Page<Vform> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.baseMapper.findListPage(page, vform);
    }

    /**
    * @title findListPage
    * @description 查询数据列表
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:58
    */
    public List<Vform> findList(Vform vform){
        return this.baseMapper.findList(vform);
    }

    /**
    * @title saveMycontent
    * @description 保存数据
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:58
    */
    @Transactional
    public void saveVform(PageData pd) throws Exception{
        //更新操作:如果表名称发生改变，则修改表名称；如果字段发生变更，则变更数据表结构。
        Map maps = (Map) JSON.parse(pd.get("config").toString());
        Vform vform = new Vform();
        String id = GuidUtil.getUuid();
        vform.setId(id);
        String time = DateTimeUtil.getDateTimeStr();
        vform.setCreate_time(time);
        String type = "0";
        //处理数据权限
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        vform.setCreate_user(authParams.split(":")[1]);
        vform.setCreate_organize(authParams.split(":")[2]);
        vform.setType(type);
        vform.setTable_type("0");
        vform.setTable_name(maps.get("table_name").toString());
        vform.setTable_comment(maps.get("table_comment").toString());
        vform.setTable_content(pd.get("table_content").toString());
        this.save(vform);
        //数据表字段
        List<Vfield> vfields = new ArrayList<Vfield>();
        List<PageData> list = JSON.parseArray(pd.get("list").toString(),PageData.class);
        //获取需要创建的主表字段
        vfields = createField(id,list,vfields,"save",new ArrayList<Vform>(),maps);
        vfieldService.saveBatch(vfields);

        //创建数据表
        pd.put("table_name",maps.get("table_name").toString());
        pd.put("table_comment",maps.get("table_comment").toString());
        this.baseMapper.createTable(pd);
        this.baseMapper.updateTableComment(pd);

        //创建数据字段
        for (Vfield vfield:vfields) {
            pd.put("field_name",vfield.getField_name());
            if(vfield.getField_name().contains("textarea")){
                pd.put("field_type","varchar(500)");
            }else if(vfield.getField_name().contains("editor")||vfield.getField_name().contains("uploadFile")||vfield.getField_name().contains("uploadImg")){
                pd.put("field_type","text");
            }else{
                if(Verify.verifyIsNotNull(vfield.getMaxLength())){
                    if(Integer.parseInt(vfield.getMaxLength())<=120){
                        pd.put("field_type","varchar(120)");
                    }else{
                        pd.put("field_type","varchar("+vfield.getMaxLength()+")");
                    }
                }else{
                    pd.put("field_type","varchar(120)");
                }
            }
            pd.put("field_null","null");
            pd.put("field_comment",vfield.getField_comment());
            this.vfieldService.addField(pd);
        }
    }

    /**
     * @title createField
     * @description 创建字段
     * @author Administrator
     * @updateTime 2021/9/29 0029 0:19
     */
    public List<Vfield> createField(String table_id,List<PageData> list,List<Vfield> vfields,String type,List<Vform> vforms,Map maps){
        String time = DateTimeUtil.getDateTimeStr();
        //当前登录用户
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        for (PageData p:list) {
            if(noField.contains(p.get("type").toString())){
                continue;
            }
            if(bjField.contains(p.get("type").toString())){
                vfields = createLayoutField(table_id,vfields,p,type,vforms,maps);
            }else{
                //model为空则跳过
                if(Verify.verifyIsNull(p.get("model"))){
                    continue;
                }
                if(maps.get("table_type").equals("1")&&p.get("model").toString().contains(p.get("type")+"_")){
                    continue;
                }
                Vfield vfield = new Vfield();
                vfield.setId(GuidUtil.getUuid());
                vfield.setTable_id(table_id);
                vfield.setType("0");
                vfield.setField_name(p.get("model").toString());
                vfield.setField_comment(p.get("label").toString());
                vfield.setField_key(p.get("key").toString());
                vfield.setField_type(p.get("type").toString());
                vfield.setCreate_time(time);
                vfield.setCreate_user(authParams.split(":")[1]);
                vfield.setCreate_organize(authParams.split(":")[2]);
                Map pp = (Map) JSON.parse(p.get("options").toString());
                vfield.setMaxLength(pp.get("maxLength")+"");
                //处理包含动态数据源的选项标签
                if(dynamicField.contains(p.get("type").toString())){
                    vfield.setDynamic(pp.get("dynamic").toString());
                    vfield.setDynamicKey(pp.get("dynamicKey").toString());
                    vfield.setOptions(pp.get("options").toString());
                }
                //自定义字段的拓展类型
                if(vfield.getField_type().equals("loginUserOrganize")||vfield.getField_type().equals("loginUserOrganize")){
                    String option_type = pp.get("option_type").toString();
                    vfield.setOption_type(option_type);
                }
                vfields.add(vfield);
                if(p.get("type").equals("batch")){
                    if(type.equals("save")){
                        createLinkTable(table_id,p,maps);
                    }else if(type.equals("update")){
                        updateLinkTable(table_id,vforms,p,maps);
                    }
                }
            }
        }
        return vfields;
    }

    //递归遍历-创建字段 tabs、grid、table
    public List<Vfield> createLayoutField(String table_id,List<Vfield> vfields,PageData fieldPd,String type,List<Vform> vforms,Map maps){
        if(fieldPd.get("type").equals("card")){
            List<PageData> list = JSON.parseArray(fieldPd.get("list").toString(),PageData.class);
            vfields = createField(table_id,list,vfields,type,vforms,maps);
        }else if(fieldPd.get("type").equals("tabs")||fieldPd.get("type").equals("grid")){
            List<PageData> columns = JSON.parseArray(fieldPd.get("columns").toString(),PageData.class);
            for(PageData cp:columns){
                List<PageData> list = JSON.parseArray(cp.get("list").toString(),PageData.class);
                vfields = createField(table_id,list,vfields,type,vforms,maps);
            }
        }else if(fieldPd.get("type").equals("table")){
            List<PageData> trs = JSON.parseArray(fieldPd.get("trs").toString(),PageData.class);
            for (PageData trPd:trs){
                List<PageData> tds = JSON.parseArray(trPd.get("tds").toString(),PageData.class);
                for (PageData tdPd:tds) {
                    List<PageData> list = JSON.parseArray(tdPd.get("list").toString(),PageData.class);
                    vfields = createField(table_id,list,vfields,type,vforms,maps);
                }
            }
        }
        return vfields;
    }


    public StringBuilder getLinkTableNames(String table_id,List<PageData> list,StringBuilder newTablesSB){
        String time = DateTimeUtil.getDateTimeStr();
        //当前登录用户
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        for (PageData p:list) {
            if(noField.contains(p.get("type").toString())){
                continue;
            }
            if(bjField.contains(p.get("type").toString())){
                newTablesSB = getLayoutLinkTableNames(table_id,newTablesSB,p);
            }else if(p.get("type").equals("batch")){
                newTablesSB.append(p.get("model"));
            }
        }
        return newTablesSB;
    }
    //递归遍历-创建字段 tabs、grid、table
    public StringBuilder getLayoutLinkTableNames(String table_id,StringBuilder newTablesSB,PageData fieldPd){
        if(fieldPd.get("type").equals("card")){
            List<PageData> list = JSON.parseArray(fieldPd.get("list").toString(),PageData.class);
            newTablesSB = getLinkTableNames(table_id,list,newTablesSB);
        }else if(fieldPd.get("type").equals("tabs")||fieldPd.get("type").equals("grid")){
            List<PageData> columns = JSON.parseArray(fieldPd.get("columns").toString(),PageData.class);
            for(PageData cp:columns){
                List<PageData> list = JSON.parseArray(cp.get("list").toString(),PageData.class);
                newTablesSB = getLinkTableNames(table_id,list,newTablesSB);
            }
        }else if(fieldPd.get("type").equals("table")){
            List<PageData> trs = JSON.parseArray(fieldPd.get("trs").toString(),PageData.class);
            for (PageData trPd:trs){
                List<PageData> tds = JSON.parseArray(trPd.get("tds").toString(),PageData.class);
                for (PageData tdPd:tds) {
                    List<PageData> list = JSON.parseArray(tdPd.get("list").toString(),PageData.class);
                    newTablesSB = getLinkTableNames(table_id,list,newTablesSB);

                }
            }
        }
        return newTablesSB;
    }



    //处理关联表数据
    public void createLinkTable(String main_id,PageData pd,Map maps){
        if(Verify.verifyIsNull(pd.get("model"))){
            return;
        }
        Vform vform = new Vform();
        String id = GuidUtil.getUuid();
        vform.setId(id);
        String time = DateTimeUtil.getDateTimeStr();
        vform.setCreate_time(time);
        String type = "1";
        vform.setMain_id(main_id);
        //处理数据权限
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        vform.setCreate_user(authParams.split(":")[1]);
        vform.setCreate_organize(authParams.split(":")[2]);
        vform.setType(type);
        vform.setTable_name(pd.get("model").toString());
        vform.setTable_comment(pd.get("label").toString());
        vform.setTable_content(pd.get("key").toString());
        this.save(vform);
        //数据表字段
        List<Vfield> vfields = new ArrayList<Vfield>();
        List<PageData> list = JSON.parseArray(pd.get("list").toString(),PageData.class);
        for (PageData p:list) {
            if(noField.contains(p.get("type").toString())){
                continue;
            }
            Vfield vfield = new Vfield();
            vfield.setId(GuidUtil.getUuid());
            vfield.setTable_id(id);
            vfield.setType(type);
            vfield.setField_name(p.get("model").toString());
            vfield.setField_comment(p.get("label").toString());
            vfield.setField_key(p.get("key").toString());
            vfield.setField_type(p.get("type").toString());
            vfield.setCreate_time(time);
            vfield.setCreate_user(authParams.split(":")[1]);
            vfield.setCreate_organize(authParams.split(":")[2]);
            Map pp = (Map) JSON.parse(p.get("options").toString());
            vfield.setMaxLength(pp.get("maxLength")+"");
            //处理包含动态数据源的选项标签
            if(dynamicField.contains(p.get("type").toString())){
                vfield.setDynamic(pp.get("dynamic").toString());
                vfield.setDynamicKey(pp.get("dynamicKey").toString());
                vfield.setOptions(pp.get("options").toString());
            }
            vfields.add(vfield);
        }
        vfieldService.saveBatch(vfields);

        if(maps.get("table_type").equals("0")){
            //创建数据表
            pd.put("table_name",pd.get("model").toString());
            pd.put("table_comment",pd.get("label").toString());
            this.baseMapper.dropTable(pd);
            this.baseMapper.createTable(pd);
            this.baseMapper.updateTableComment(pd);

            //创建数据字段
            for (Vfield vfield:vfields) {
                pd.put("field_name",vfield.getField_name());
                if(vfield.getField_name().contains("textarea")){
                    pd.put("field_type","varchar(500)");
                }else if(vfield.getField_name().contains("editor")){
                    pd.put("field_type","text");
                }else{
                    if(Verify.verifyIsNotNull(vfield.getMaxLength())){
                        if(Integer.parseInt(vfield.getMaxLength())<=120){
                            pd.put("field_type","varchar(120)");
                        }else{
                            pd.put("field_type","varchar("+vfield.getMaxLength()+")");
                        }
                    }else{
                        pd.put("field_type","varchar(120)");
                    }
                }
                pd.put("field_null","null");
                pd.put("field_comment",vfield.getField_comment());
                this.vfieldService.addField(pd);
            }
        }
    }

    /**
    * @title updateMycontent
    * @description 更新数据
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:58
    */
    @Transactional
    public void updateVform(PageData pd){
        Vform vform = new Vform();
        Map maps = (Map) JSON.parse(pd.get("config").toString());
        Map record = (Map) JSON.parse(pd.get("record").toString());
        // 更新信息
        String type = "0";
        String id = record.get("id").toString();
        vform.setId(id);
        //查询原数据
        Vform myForm = this.getById(id);
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("table_id",id);
        List<Vfield> fieldList = this.vfieldService.list(wrapper);
        myForm.setVfields(fieldList);
        //查询关联表
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("main_id",id);
        List<Vform> myFormList = this.list(queryWrapper);
        for (Vform form:myFormList) {
            QueryWrapper wp = new QueryWrapper();
            wp.eq("table_id",form.getId());
            List<Vfield> fieldLs = this.vfieldService.list(wp);
            form.setVfields(fieldLs);
        }
        //更新操作:如果表名称发生改变，则修改表名称；如果字段发生变更，则变更数据表结构。
        String time = DateTimeUtil.getDateTimeStr();
        vform.setUpdate_time(time);
        vform.setType("0");
        vform.setTable_type("0");
        //处理数据权限
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        vform.setUpdate_user(authParams.split(":")[1]);
        vform.setTable_name(maps.get("table_name").toString());
        vform.setTable_comment(maps.get("table_comment").toString());
        vform.setTable_content(pd.get("table_content").toString());
        this.updateById(vform);
        //数据表字段
        QueryWrapper removeWrapper = new QueryWrapper();
        removeWrapper.eq("table_id",id);
        this.vfieldService.remove(removeWrapper);
        //组织新-子表表明
        StringBuilder newTablesSB = new StringBuilder();
        List<Vfield> vfields = new ArrayList<Vfield>();
        List<PageData> list = JSON.parseArray(pd.get("list").toString(),PageData.class);
        //获取需要创建的主表字段
        vfields = createField(id,list,vfields,"update",myFormList,maps);
        newTablesSB = getLinkTableNames(id,list,newTablesSB);
        vfieldService.saveBatch(vfields);

        //处理主表
        pd.put("table_name",maps.get("table_name").toString());
        pd.put("table_comment",maps.get("table_comment").toString());
        if(!record.get("table_name").equals(maps.get("table_name"))){
            pd.put("old_table_name",record.get("table_name").toString());
            this.baseMapper.updateTable(pd);
            this.baseMapper.updateTableComment(pd);
        }
        //如果是新增字段-则创建
        StringBuilder fieldNames = new StringBuilder();
        StringBuilder fieldKeys = new StringBuilder();
        fieldList.forEach((item)-> {
            fieldNames.append(item.getField_name()).append(",");
            fieldKeys.append(item.getField_key()).append(",");
        });
        for (Vfield vfield:vfields) {
            pd.put("field_name",vfield.getField_name());
            if(vfield.getField_name().contains("textarea")){
                pd.put("field_type","varchar(500)");
            }else if(vfield.getField_name().contains("editor")||vfield.getField_name().contains("uploadFile")||vfield.getField_name().contains("uploadImg")){
                pd.put("field_type","text");
            }else{
                if(Verify.verifyIsNotNull(vfield.getMaxLength())){
                    if(Integer.parseInt(vfield.getMaxLength())<=120){
                        pd.put("field_type","varchar(120)");
                    }else{
                        pd.put("field_type","varchar("+vfield.getMaxLength()+")");
                    }
                }else{
                    pd.put("field_type","varchar(120)");
                }
            }
            pd.put("field_null","null");
            pd.put("field_comment",vfield.getField_comment());
            if(!fieldKeys.toString().contains(vfield.getField_key())){
                this.vfieldService.addField(pd);
            }else{
                Vfield vf = new Vfield();
                for (Vfield item:fieldList) {
                    if(item.getField_key().equals(vfield.getField_key())){
                        vf = item;
                    }
                }
                pd.put("old_field_name",vf.getField_name());
                this.vfieldService.updateField(pd);
            }
        }
        //如果旧的字段不在新创建的表字段则删除字段
        StringBuilder newFieldKeys = new StringBuilder();
        vfields.forEach((item)->newFieldKeys.append(item.getField_key()).append(","));
        fieldList.forEach((item)-> {
            if(!newFieldKeys.toString().contains(item.getField_key())){
                pd.put("field_name",item.getField_name());
                this.vfieldService.delField(pd);
            }
        });
        //删除多余的表
        String finalNewTablesSB = newTablesSB.toString();
        myFormList.forEach((item)->{
            if(!finalNewTablesSB.contains(item.getTable_name())){
                pd.put("table_name",item.getTable_name());
                this.baseMapper.dropTable(pd);
                //删除关联数据
                QueryWrapper delWrapper = new QueryWrapper();
                delWrapper.eq("table_id",item.getId());
                vfieldService.remove(delWrapper);
                this.removeById(item.getId());
            }
        });
    }

    //更新表子表字段
    public void updateLinkTable(String main_id,List<Vform> vforms,PageData pd,Map maps){
        if(Verify.verifyIsNull(pd.get("model"))){
            return;
        }
        StringBuilder formNameSB = new StringBuilder();
        StringBuilder formKeySB = new StringBuilder();
        vforms.forEach((item)->{
            formNameSB.append(item.getTable_name());
            formKeySB.append(item.getTable_content());
        });
        String type = "1";
        //判断表是否修改
        //包含tableKey,说明数据集没有改变
        if(formKeySB.toString().contains(pd.get("key").toString())){
            Vform vform = new Vform();
            for (Vform item:vforms) {
                if(item.getTable_content().equals(pd.get("key").toString())){
                    vform = item;
                }
            }
            //修改表名称
            pd.put("table_name", pd.get("model").toString());
            pd.put("table_comment", pd.get("label").toString());
            if(maps.get("table_type").toString().equals("0")) {
                if (!formNameSB.toString().contains(pd.get("model").toString())) {
                    pd.put("old_table_name", vform.getTable_name());
                    this.baseMapper.updateTable(pd);
                    this.baseMapper.updateTableComment(pd);
                }
            }
            String time = DateTimeUtil.getDateTimeStr();
            vform.setUpdate_time(time);
            String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
            vform.setUpdate_user(authParams.split(":")[1]);
            vform.setTable_name(pd.get("model").toString());
            vform.setTable_comment(pd.get("label").toString());
            vform.setTable_content(pd.get("key").toString());
            this.updateById(vform);
            //数据表字段
            QueryWrapper removeWrapper = new QueryWrapper();
            removeWrapper.eq("table_id",vform.getId());
            this.vfieldService.remove(removeWrapper);
            List<Vfield> vfields = new ArrayList<Vfield>();
            List<PageData> list = JSON.parseArray(pd.get("list").toString(),PageData.class);
            for (PageData p:list) {
                if(noField.contains(p.get("type").toString())){
                    continue;
                }
                Vfield vfield = new Vfield();
                vfield.setId(GuidUtil.getUuid());
                vfield.setTable_id(vform.getId());
                vfield.setType(type);
                vfield.setField_name(p.get("model").toString());
                vfield.setField_comment(p.get("label").toString());
                vfield.setField_key(p.get("key").toString());
                vfield.setField_type(p.get("type").toString());
                vfield.setCreate_time(time);
                vfield.setCreate_user(authParams.split(":")[1]);
                vfield.setCreate_organize(authParams.split(":")[2]);
                Map pp = (Map) JSON.parse(p.get("options").toString());
                vfield.setMaxLength(pp.get("maxLength")+"");
                //处理包含动态数据源的选项标签
                if(dynamicField.contains(p.get("type").toString())){
                    vfield.setDynamic(pp.get("dynamic").toString());
                    vfield.setDynamicKey(pp.get("dynamicKey").toString());
                    vfield.setOptions(pp.get("options").toString());
                }
                vfields.add(vfield);
            }
            vfieldService.saveBatch(vfields);

            if(maps.get("table_type").toString().equals("0")) {
                //如果是新增字段-则创建
                StringBuilder fieldNames = new StringBuilder();
                StringBuilder fieldKeys = new StringBuilder();
                vform.getVfields().forEach((item)-> {
                    fieldNames.append(item.getField_name()).append(",");
                    fieldKeys.append(item.getField_key()).append(",");
                });
                for (Vfield vfield:vfields) {
                    pd.put("field_name",vfield.getField_name());
                    if(vfield.getField_name().contains("textarea")){
                        pd.put("field_type","varchar(500)");
                    }else if(vfield.getField_name().contains("editor")){
                        pd.put("field_type","text");
                    }else{
                        if(Verify.verifyIsNotNull(vfield.getMaxLength())){
                            if(Integer.parseInt(vfield.getMaxLength())<=120){
                                pd.put("field_type","varchar(120)");
                            }else{
                                pd.put("field_type","varchar("+vfield.getMaxLength()+")");
                            }
                        }else{
                            pd.put("field_type","varchar(120)");
                        }
                    }
                    pd.put("field_null","null");
                    pd.put("field_comment",vfield.getField_comment());
                    if(!fieldKeys.toString().contains(vfield.getField_key())){
                        this.vfieldService.addField(pd);
                    }else{
                        Vfield vf = new Vfield();
                        for (Vfield item:vform.getVfields()) {
                            if(item.getField_key().equals(vfield.getField_key())){
                                vf = item;
                            }
                        }
                        pd.put("old_field_name",vf.getField_name());
                        this.vfieldService.updateField(pd);
                    }
                }
                //如果旧的字段不在新创建的表字段则删除字段
                StringBuilder newFieldKeys = new StringBuilder();
                vfields.forEach((item)->newFieldKeys.append(item.getField_key()).append(","));
                vform.getVfields().forEach((item)-> {
                    if(!newFieldKeys.toString().contains(item.getField_key())){
                        pd.put("field_name",item.getField_name());
                        this.vfieldService.delField(pd);
                    }
                });
            }
        }else{
            createLinkTable(main_id,pd,maps);
        }
    }

    /**
     * @title del
     * @description 数据删除
     * @author Administrator
     * @updateTime 2021/9/28 0028 22:26
     */
    @Transactional
    public void del(String[] ids){
        PageData pd = new PageData();
        for (String id:ids) {
            //查询关联表
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("main_id",id);
            List<Vform> myFormList = this.list(queryWrapper);
            myFormList.forEach((item)->{
                //删除关联子表
                QueryWrapper fieldLinkWrapper = new QueryWrapper();
                fieldLinkWrapper.eq("table_id",item.getId());
                this.vfieldService.remove(fieldLinkWrapper);
                //删除关联表
                pd.put("table_name",item.getTable_name());
                this.baseMapper.dropTable(pd);
            });
            //删除关联主表
            QueryWrapper formLinkWrapper = new QueryWrapper();
            formLinkWrapper.eq("main_id",id);
            this.remove(formLinkWrapper);

            //删除子表
            QueryWrapper fieldWrapper = new QueryWrapper();
            fieldWrapper.eq("table_id",id);
            this.vfieldService.remove(fieldWrapper);
        }
        Collection<Vform> vforms = this.listByIds(Arrays.asList(ids));
        vforms.forEach((item)->{
            //删除表
            pd.put("table_name",item.getTable_name());
            this.baseMapper.dropTable(pd);
        });
        //删除主表
        this.removeByIds(Arrays.asList(ids));
    }

    /**
     * @title findTableList
     * @description 查询数据表信息
     * @author Administrator
     * @updateTime 2021/9/27 0027 22:38
     */
    public List<PageData> findTableList(PageData pd){
        return this.baseMapper.findTableList(pd);
    }

    /**
     * @title findColumndList
     * @description 查询字段列表
     * @author Administrator
     * @updateTime 2022/5/2 0002 23:33
     */
    public List<PageData> findColumndList(PageData pd){
        return this.baseMapper.findColumndList(pd);
    }



    /**
     * @title saveMycontent
     * @description 保存数据
     * @author qingfeng
     * @updateTime 2021/4/3 0003 20:58
     */
    @Transactional
    public void saveform(PageData pd) throws Exception{
        //更新操作:如果表名称发生改变，则修改表名称；如果字段发生变更，则变更数据表结构。
        Map maps = (Map) JSON.parse(pd.get("config").toString());
        Vform vform = new Vform();
        String id = GuidUtil.getUuid();
        vform.setId(id);
        String time = DateTimeUtil.getDateTimeStr();
        vform.setCreate_time(time);
        String type = "0";
        //处理数据权限
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        vform.setCreate_user(authParams.split(":")[1]);
        vform.setCreate_organize(authParams.split(":")[2]);
        vform.setType(type);
        vform.setTable_type("1");
        vform.setTable_name(maps.get("table_name").toString());
        vform.setTable_comment(maps.get("table_comment").toString());
        vform.setTable_content(pd.get("table_content").toString());
        this.save(vform);
        //数据表字段
        List<Vfield> vfields = new ArrayList<Vfield>();
        List<PageData> list = JSON.parseArray(pd.get("list").toString(),PageData.class);
        //获取需要创建的主表字段
        vfields = createField(id,list,vfields,"save",new ArrayList<Vform>(),maps);
        vfieldService.saveBatch(vfields);
    }


    @Transactional
    public void updateform(PageData pd){
        Vform vform = new Vform();
        Map record = (Map) JSON.parse(pd.get("record").toString());
        // 更新信息
        String type = "0";
        String id = record.get("id").toString();
        vform.setId(id);
        //查询原数据
        Vform myForm = this.getById(id);
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("table_id",id);
        List<Vfield> fieldList = this.vfieldService.list(wrapper);
        myForm.setVfields(fieldList);
        //查询关联表
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("main_id",id);
        List<Vform> myFormList = this.list(queryWrapper);
        for (Vform form:myFormList) {
            QueryWrapper wp = new QueryWrapper();
            wp.eq("table_id",form.getId());
            List<Vfield> fieldLs = this.vfieldService.list(wp);
            form.setVfields(fieldLs);
        }
        //更新操作:如果表名称发生改变，则修改表名称；如果字段发生变更，则变更数据表结构。
        Map maps = (Map) JSON.parse(pd.get("config").toString());
        String time = DateTimeUtil.getDateTimeStr();
        vform.setUpdate_time(time);
        //处理数据权限
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        vform.setUpdate_user(authParams.split(":")[1]);
        vform.setTable_name(maps.get("table_name").toString());
        vform.setTable_comment(maps.get("table_comment").toString());
        vform.setTable_content(pd.get("table_content").toString());
        vform.setType("0");
        vform.setTable_type("1");
        this.updateById(vform);
        //数据表字段
        QueryWrapper removeWrapper = new QueryWrapper();
        removeWrapper.eq("table_id",id);
        this.vfieldService.remove(removeWrapper);
        //组织新-子表表明
        StringBuilder newTablesSB = new StringBuilder();
        List<Vfield> vfields = new ArrayList<Vfield>();
        List<PageData> list = JSON.parseArray(pd.get("list").toString(),PageData.class);
        //获取需要创建的主表字段
        vfields = createField(id,list,vfields,"update",myFormList,maps);
        newTablesSB = getLinkTableNames(id,list,newTablesSB);
        vfieldService.saveBatch(vfields);
    }



}