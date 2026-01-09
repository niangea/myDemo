package com.nian.myddemoadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nian.myDemoSecurity.util.JwtTokenUtil;
import com.nian.myddemoadmin.bo.AdminUserDetails;
import com.nian.myddemoadmin.constant.MDA;
import com.nian.myddemoadmin.dto.UmsAdminParam;
import com.nian.myddemoadmin.mapper.UmsAdminMapper;
import com.nian.myddemoadmin.mapper.UmsAdminPermissionRelationMapper;
import com.nian.myddemoadmin.mapper.UmsMemberMapper;
import com.nian.myddemoadmin.model.*;
import com.nian.myddemoadmin.service.UmsAdminService;
;
import com.nian.mydemocommon.common.api.TokenInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;


@Service
public class UmsAdminServiceImpl implements UmsAdminService {

    @Autowired
    UmsAdminMapper umsAdminMapper;

    @Autowired
    private UmsMemberMapper memberMapper;

    @Autowired
    private RestTemplate restTemplate;

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


        // -----------------------
        //查询是否已有该用户
        UmsMemberExample example = new UmsMemberExample();
        example.createCriteria().andUsernameEqualTo(umsAdmin.getUsername());


        //没有该用户进行添加操作
        UmsMember umsMember = new UmsMember();
        umsMember.setUsername(umsAdmin.getUsername());
        umsMember.setPassword(passwordEncoder.encode(umsAdmin.getPassword()));
        umsMember.setCreateTime(new Date());
        umsMember.setStatus(1);
        umsMember.setNickname(umsAdmin.getNickName());
        //获取默认会员等级并设置
        UmsMemberLevelExample levelExample = new UmsMemberLevelExample();
        levelExample.createCriteria().andDefaultStatusEqualTo(1);

        memberMapper.insert(umsMember);


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

    @Override
    public TokenInfo ssoLogin(String username, String password) {
        ResponseEntity<TokenInfo> response;

        try{

            //远程调用认证服务器 进行用户登陆

            response = restTemplate.exchange(MDA.OAUTH_LOGIN_URL, HttpMethod.POST, wrapOauthTokenRequest(username,password), TokenInfo.class);

            TokenInfo tokenInfo = response.getBody();

            LOGGER.info("根据用户名:{}登陆成功:TokenInfo:{}",username,tokenInfo);

            return tokenInfo;

        }catch (Exception e) {
            LOGGER.error("根据用户名:{}登陆异常:{}",username,e.getMessage());
            e.printStackTrace();

            return null;
        }
    }

    /**
     * 方法实现说明:封装用户到认证中心的请求头 和请求参数
     * @author:smlz
     * @param userName 用户名
     * @param password 密码
     * @return:
     * @exception:
     * @date:2020/1/22 15:32
     */
    private HttpEntity<MultiValueMap<String, String>> wrapOauthTokenRequest(String userName, String password) {


        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        //封装oauth2 请求头 clientId clientSecret
        HttpHeaders httpHeaders = wrapHttpHeaders();

        //封装请求参数     oauth2 密码模式
        MultiValueMap<String, String> reqParams = new LinkedMultiValueMap<>();
        reqParams.add(MDA.USER_NAME,userName);
//        reqParams.add(MDA.PASS,encoder.encode(password));
        reqParams.add(MDA.PASS,password);
        reqParams.add(MDA.GRANT_TYPE,MDA.PASS);
        reqParams.add(MDA.SCOPE,MDA.SCOPE_AUTH);

        //封装请求参数
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(reqParams, httpHeaders);

        return entity;
    }

    /**
     * 方法实现说明:封装刷新token的请求
     * @author:smlz
     * @param refreshToken:刷新token
     * @return: HttpEntity
     * @exception:
     * @date:2020/1/22 16:14
     */
    private HttpEntity<MultiValueMap<String, String>> wrapRefreshTokenRequest(String refreshToken) {

        HttpHeaders httpHeaders = wrapHttpHeaders();

        MultiValueMap<String,String> param = new LinkedMultiValueMap<>();
        param.add("grant_type","refresh_token");
        param.add("refresh_token",refreshToken);

        HttpEntity<MultiValueMap<String,String>> httpEntity = new HttpEntity<>(param,httpHeaders);

        return httpEntity;
    }

    /**
     * 方法实现说明:封装请求头
     * @author:smlz
     * @return:HttpHeaders
     * @exception:
     * @date:2020/1/22 16:10
     */
    private HttpHeaders wrapHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.setBasicAuth(MDA.CLIENT_ID,MDA.CLIENT_SECRET);
        return httpHeaders;
    }


}
