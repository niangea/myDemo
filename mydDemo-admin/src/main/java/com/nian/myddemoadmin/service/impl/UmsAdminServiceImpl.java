package com.nian.myddemoadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nian.myDemoSecurity.util.JwtTokenUtil;
import com.nian.myddemoadmin.bo.AdminUserDetails;
import com.nian.myddemoadmin.dto.UmsAdminParam;
import com.nian.myddemoadmin.mapper.UmsAdminMapper;
import com.nian.myddemoadmin.mapper.UmsAdminPermissionRelationMapper;
import com.nian.myddemoadmin.model.UmsAdmin;
import com.nian.myddemoadmin.model.UmsPermission;
import com.nian.myddemoadmin.model.UmsRole;
import com.nian.myddemoadmin.service.UmsAdminService;
;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;


@Service
public class UmsAdminServiceImpl implements UmsAdminService {

    @Autowired
    UmsAdminMapper umsAdminMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenUtil jwtTokenUtil;
    private static final Logger LOGGER = LoggerFactory.getLogger(UmsAdminServiceImpl.class);


    @Autowired
    private UmsAdminPermissionRelationMapper adminPermissionRelationMapper;

    @Override
    public UmsAdmin getAdminByUsername(String username) {
        QueryWrapper<UmsAdmin> wrapper = new QueryWrapper();
        wrapper.eq("username",username);
        List<UmsAdmin> adminList = umsAdminMapper.selectList(wrapper);
        if (adminList != null && adminList.size() > 0) {
            return adminList.get(0);
        }
        return null;
    }

    @Override
    public UmsAdmin register(UmsAdminParam umsAdminParam) {

        UmsAdmin umsAdmin = new UmsAdmin();

        BeanUtils.copyProperties(umsAdminParam,umsAdmin);

        umsAdmin.setCreateTime(new Date());

        umsAdmin.setStatus(1);

        QueryWrapper<UmsAdmin> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("username",umsAdmin.getUsername());

        List<UmsAdmin> umsAdminList = umsAdminMapper.selectList(queryWrapper);

        if(umsAdminList.size() > 0){
            return null;
        }

        String encodePassword = passwordEncoder.encode(umsAdmin.getPassword());

        umsAdmin.setPassword(encodePassword);

        umsAdminMapper.insert(umsAdmin);

        return umsAdmin;
    }

    @Override
    public String login(String username, String password) {
        String token = null;
        //密码需要客户端加密后传递
        try {
            UserDetails userDetails = loadUserByUsername(username);
            if(!passwordEncoder.matches(password,userDetails.getPassword())){
                throw new BadCredentialsException("密码不正确");
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            token = jwtTokenUtil.generateToken(userDetails);
//            updateLoginTimeByUsername(username);
//            insertLoginLog(username);
        } catch (AuthenticationException e) {
            LOGGER.warn("登录异常:{}", e.getMessage());
        }
        return token;
    }



    /**
     * 添加登录记录
     * @param
     */
//    private void insertLoginLog(String username) {
//        UmsAdmin admin = getAdminByUsername(username);
//        UmsAdminLoginLog loginLog = new UmsAdminLoginLog();
//        loginLog.setAdminId(admin.getId());
//        loginLog.setCreateTime(new Date());
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = attributes.getRequest();
//        loginLog.setIp(request.getRemoteAddr());
//        loginLogMapper.insert(loginLog);
//    }

    @Override
    public String refreshToken(String oldToken) {
        return null;
    }

    @Override
    public UmsAdmin getItem(Long id) {
        return null;
    }

    @Override
    public List<UmsAdmin> list(String name, Integer pageSize, Integer pageNum) {
        return null;
    }

    @Override
    public int update(Long id, UmsAdmin admin) {
        return 0;
    }

    @Override
    public int delete(Long id) {
        return 0;
    }

    @Override
    public int updateRole(Long adminId, List<Long> roleIds) {
        return 0;
    }

    @Override
    public List<UmsRole> getRoleList(Long adminId) {
        return null;
    }

    @Override
    public int updatePermission(Long adminId, List<Long> permissionIds) {
        return 0;
    }

    @Override
    public List<UmsPermission> getPermissionList(Long adminId) {
        return adminPermissionRelationMapper.getPermissionList(adminId);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {

        //获取用户信息
        UmsAdmin admin = getAdminByUsername(username);
        if (admin != null) {
            List<UmsPermission> permissionList = getPermissionList(admin.getId());
            return new AdminUserDetails(admin,permissionList);
        }
        throw new UsernameNotFoundException("用户名或密码错误");
    }
}
