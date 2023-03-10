package com.qingfeng.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingfeng.common.service.IUploadService;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.common.UploadFile;
import com.qingfeng.entity.system.Role;
import com.qingfeng.entity.system.SystemUser;
import com.qingfeng.entity.system.UserOrganize;
import com.qingfeng.entity.system.UserRole;
import com.qingfeng.system.mapper.UserMapper;
import com.qingfeng.system.service.IUserGroupService;
import com.qingfeng.system.service.IUserOrganizeService;
import com.qingfeng.system.service.IUserRoleService;
import com.qingfeng.system.service.IUserService;
import com.qingfeng.utils.*;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @ProjectName UserServiceImpl
 * @author qingfeng
 * @version 1.0.0
 * @Description TODO
 * @createTime 2021/4/3 0003 21:38
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class UserServiceImpl extends ServiceImpl<UserMapper, SystemUser> implements IUserService {

    @Autowired
    private IUserRoleService userRoleService;
    @Autowired
    private IUserOrganizeService userOrganizeService;
    @Autowired
    private IUploadService uploadService;
    @Autowired
    private IUserGroupService userGroupService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * @title findListPage
     * @description ????????????????????????
     * @author qingfeng
     * @updateTime 2021/4/3 0003 21:38
     */
    @Override
    public IPage<SystemUser> findListPage(SystemUser user, QueryRequest request) {
        Page<SystemUser> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.baseMapper.findListPage(page, user);
    }

    /**
     * @title findList
     * @description ??????????????????
     * @author qingfeng
     * @updateTime 2021/4/3 0003 21:39
     */
    @Override
    public List<SystemUser> findList(SystemUser user){
        List<SystemUser> list = this.baseMapper.findList(user);
        return list;
    }

    /**
     * @title createUser
     * @description ????????????
     * @author qingfeng
     * @updateTime 2021/4/3 0003 21:39
     */
    @Override
    @Transactional
    public void createUser(SystemUser user) {
//        Map<String, Object> columnMap = new HashMap<>();
//        columnMap.put("login_name","admin");
//        Collection list = listByMap(columnMap);
//        System.out.println("################");
//        System.out.println(list.size());
//        QueryWrapper queryWrapper = new QueryWrapper();
//        queryWrapper.eq("login_name","admin");
//        List ls = list(queryWrapper);
//        System.out.println("$$$$$$$$$$$$$$$$$$$$$");
//        System.out.println(ls.size());
//        System.out.println(JsonToMap.list2json(ls));
        // ????????????
        String id = GuidUtil.getUuid();
        user.setId(id);
        String time = DateTimeUtil.getDateTimeStr();
        user.setCreate_time(time);
        user.setStatus("0");
        user.setType("1");
        user.setPwd_error_num("0");
        user.setLogin_password(passwordEncoder.encode(user.getLogin_password()));
        //??????????????????
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        user.setCreate_user(authParams.split(":")[1]);
        user.setCreate_organize(authParams.split(":")[2]);
        save(user);
        //????????????????????????
        UserOrganize userOrganize = new UserOrganize();
        userOrganize.setId(GuidUtil.getUuid());
        userOrganize.setUser_id(id);
        userOrganize.setType("0");
        userOrganize.setUse_status("0");
        userOrganize.setOrganize_id(user.getOrganize_id());
        userOrganize.setOrganize_name(user.getOrganize_name());
        userOrganize.setOrder_by("1");
        userOrganize.setCreate_user(authParams.split(":")[1]);
        userOrganize.setCreate_time(time);
        userOrganizeService.save(userOrganize);
        //????????????
        if(Verify.verifyIsNotNull(user.getFileIds())){
            UploadFile uploadFile = new UploadFile();
            uploadFile.setObj_id(id);
            uploadFile.setUpdate_time(time);
            String fileIds[] = user.getFileIds().split(",");
            for (int j = 0; j < fileIds.length; j++) {
                uploadFile.setId(fileIds[j]);
                uploadService.updateById(uploadFile);
            }
        }

    }

    /**
     * @title updateUser
     * @description ????????????
     * @author qingfeng
     * @updateTime 2021/4/3 0003 21:39
     */
    @Override
    @Transactional
    public void updateUser(SystemUser user) {
        // ????????????
        String time = DateTimeUtil.getDateTimeStr();
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        user.setUpdate_time(time);
        user.setUpdate_user(authParams.split(":")[1]);
        updateById(user);
        //????????????
        if(Verify.verifyIsNotNull(user.getFileIds())){
            UploadFile uploadFile = new UploadFile();
            uploadFile.setObj_id(user.getId());
            uploadFile.setUpdate_time(time);
            String fileIds[] = user.getFileIds().split(",");
            for (int j = 0; j < fileIds.length; j++) {
                uploadFile.setId(fileIds[j]);
                uploadService.updateById(uploadFile);
            }
        }

//        userRoleService.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getUserId()));
//        String[] roles = user.getRoleId().split(StringPool.COMMA);
//        setUserRoles(user, roles);
    }

    /**
     * @title deleteUsers
     * @description ????????????
     * @author qingfeng
     * @updateTime 2021/4/3 0003 21:39
     */
    @Override
    @Transactional
    public void deleteUsers(String[] ids) {
        List<String> list = Arrays.asList(ids);
        removeByIds(list);
        //??????????????????
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.in("user_id",ids);
        userOrganizeService.remove(queryWrapper);
        //????????????????????????
        userGroupService.remove(queryWrapper);
        //????????????????????????
        userRoleService.remove(queryWrapper);
    }

    /**
     * @title updatePwd
     * @description ????????????
     * @author qingfeng
     * @updateTime 2021/4/3 0003 21:39
     */
    @Transactional
    public void updatePwd(SystemUser user) {
        String[] ids = user.getIds().split(",");
        List<SystemUser> list = new ArrayList<SystemUser>();
        for (String id:ids) {
            SystemUser u = new SystemUser();
            u.setId(id);
            u.setLogin_password(passwordEncoder.encode(user.getLogin_password()));
            u.setUpdate_time(DateTimeUtil.getDateTimeStr());
            list.add(u);
            System.out.println("##:"+JsonToMap.bean2json(u));
        }
        updateBatchById(list);
    }



    /**
     * ??????????????????
     * @param pd
     * @return
     */
    public PageData findUserInfo(PageData pd){
        return this.baseMapper.findUserInfo(pd);
    }

    /**
     * ????????????????????????
     * @param pd
     * @return
     */
    public List<Role> findUserRoleList(PageData pd){
        return this.baseMapper.findUserRoleList(pd);
    }

    /**
     * ????????????????????????
     * @param pd
     * @return
     */
    public PageData findUserOrganizeInfo(PageData pd){
        return this.baseMapper.findUserOrganizeInfo(pd);
    }

    /**
     * @title updateAuth
     * @description ????????????
     * @author qingfeng
     * @updateTime 2021/4/3 0003 21:40
     */
    @Transactional
    public void updateAuth(PageData pd){
        String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
        //??????????????????
        String create_user = userParams.split(":")[1];
        String time = DateTimeUtil.getDateTimeStr();
        String[] role_ids = pd.get("role_ids").toString().split(",");
        //????????????????????????
        pd.put("user_id",pd.get("id"));
        pd.put("role_ids", Arrays.asList(role_ids));
        this.baseMapper.delUserRole(pd);
        if(Verify.verifyIsNotNull(pd.get("role_ids"))){
            String user_id = pd.get("id").toString();
            List<UserRole> list = new ArrayList<UserRole>();
            //????????????
            for (int i = 0; i < role_ids.length; i++) {
                UserRole userRole = new UserRole();
                //??????id
                userRole.setId(GuidUtil.getUuid());
                userRole.setRole_id(role_ids[i]);
                userRole.setUser_id(user_id);
                userRole.setCreate_time(time);
                userRole.setCreate_user(create_user);
                list.add(userRole);
            }
            userRoleService.saveBatch(list);
        }

        //??????????????????
        UserOrganize userOrganize = new UserOrganize();
        String[] showAuthData = pd.get("showAuthData").toString().split(",");
        String[] operaAuthData = pd.get("operaAuthData").toString().split(",");
        StringBuilder authOrgIds = new StringBuilder();
        StringBuilder authOrgCascade = new StringBuilder();
        StringBuilder authParams = new StringBuilder();
        if(Verify.verifyIsNotNull(pd.get("showAuthData"))){
            for (int i = 0; i < showAuthData.length; i++) {
                String showAuth[] = showAuthData[i].toString().split(":");
                authOrgIds.append(showAuth[0]).append(",");
                authOrgCascade.append(showAuth[1]).append(",");
                if(ArrayUtils.contains(operaAuthData,showAuthData[i])){
                    authParams.append(showAuth[0]).append(":Y").append(",");
                }else{
                    authParams.append(showAuth[0]).append(":N").append(",");
                }
            }
            if(authOrgIds.length()>0){
                userOrganize.setAuthOrgIds(authOrgIds.substring(0,authOrgIds.length()-1));
                userOrganize.setAuthOrgCascade(authOrgCascade.substring(0,authOrgCascade.length()-1));
                userOrganize.setAuthParams(authParams.substring(0,authParams.length()-1));
            }
        }
        userOrganize.setUpdate_time(time);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id",pd.get("user_id").toString());
        queryWrapper.eq("organize_id",pd.get("organize_id").toString());
        userOrganizeService.update(userOrganize,queryWrapper);
    }

    /**
     * @title updateUserOrgUseStatus
     * @description ????????????????????????
     * @author qingfeng
     * @updateTime 2021/4/3 0003 21:40
     */
    public void updateUserOrgUseStatus(PageData pd){
        this.baseMapper.updateUserOrgUseStatus(pd);
    }


    /**
     * @title findUserList
     * @description ????????????????????????
     * @author Administrator
     * @updateTime 2021/9/11 0011 10:00
     */
    public List<PageData> findUserList(PageData pd){
        return this.baseMapper.findUserList(pd);
    }

}