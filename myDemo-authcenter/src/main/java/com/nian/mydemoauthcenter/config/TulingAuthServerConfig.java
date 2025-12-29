package com.nian.mydemoauthcenter.config;

import com.nian.mydemoauthcenter.component.TulingTokenEnhancer;
import com.nian.mydemoauthcenter.properties.JwtCAProperties;

import com.nian.mydemoauthcenter.tulingmall.service.TulingUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;

import javax.sql.DataSource;
import java.security.KeyPair;
import java.util.Arrays;

/**
用于授权给其他微服务的，

 比如商场使用qq或者微信二维码登录，就是通过此配置发放token（授权码模式）

 直接使用账户密码登录，也是此发放token（密码模式）

 需要第三方（即自己的商场微服务往此应用上面进行注册） ---- 将client_id以及client_secert放到oauth_client_details表里面




*/
@Configuration
@EnableAuthorizationServer
@EnableConfigurationProperties(value = JwtCAProperties.class)
public class TulingAuthServerConfig extends AuthorizationServerConfigurerAdapter {


    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TulingUserDetailService tulingUserDetailService;

    @Autowired
    private JwtCAProperties jwtCAProperties;


    /**
     * 方法实现说明:我们颁发的token通过jwt存储
     * @author:smlz
     * @return:
     * @exception:
     * @date:2020/1/21 21:49
     */
    @Bean
    public TokenStore tokenStore(){
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        //jwt的密钥
        converter.setKeyPair(keyPair());
        return converter;
    }

    @Bean
    public KeyPair keyPair() {
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource(jwtCAProperties.getKeyPairName()), jwtCAProperties.getKeyPairSecret().toCharArray());
        return keyStoreKeyFactory.getKeyPair(jwtCAProperties.getKeyPairAlias(), jwtCAProperties.getKeyPairStoreSecret().toCharArray());
    }


    @Bean
    public TulingTokenEnhancer tulingTokenEnhancer() {
        return new TulingTokenEnhancer();
    }



    /**
从数据库的oauth_client_detail表里面取数据来进行验证
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetails());
    }

    /**
     * 方法实现说明:用于查找我们第三方客户端的组件 主要用于查找 数据库表 oauth_client_details
     * @author:smlz
     * @return:
     * @exception:
     * @date:2020/1/15 20:19
     */
    @Bean
    public ClientDetailsService clientDetails() {
        return new JdbcClientDetailsService(dataSource);
    }

    /**
     TokenEnhancerChain（令牌增强链）
     这是配置中的核心。它允许你将多个 TokenEnhancer按顺序组合起来，对令牌进行分步增强。代码中链入了两个增强器：
     •
     tulingTokenEnhancer()：这是一个自定义增强器。它的主要目的是在标准的令牌信息（如访问范围、过期时间等）之外，额外添加自定义信息。例如，可以将用户的ID、部门等业务数据加入到令牌中。
     6
     7
     •
     jwtAccessTokenConverter()：这是一个核心增强器，负责将原始的OAuth2令牌转换为JWT（JSON Web Token）格式。JWT是一种紧凑且自包含的令牌格式，包含签名，可以防止篡改。它通常会扮演两个角色：一是作为 TokenEnhancer来格式化令牌；二是作为 AccessTokenConverter来在令牌和认证信息之间转换。
     2
     5
     增强顺序：流程通常是 tulingTokenEnhancer先向令牌中添加自定义信息，然后 jwtAccessTokenConverter将这些信息连同标准信息一起打包成JWT格式并进行签名。
     7
     2.
     tokenStore(tokenStore())（令牌存储）
     这个配置指定了授权服务器生成的令牌的存储方式。虽然这里的方法名叫 tokenStore()，但它很可能返回的是一个与JWT相关的存储实现，例如 JwtTokenStore。
     2
     5
     •
     JwtTokenStore的一个关键特点是它实际上不会持久化存储令牌内容。因为它只是将认证信息（OAuth2Authentication）编码到JWT中，资源服务器可以通过验证签名和解析JWT内容来直接获取认证信息，无需每次都与授权服务器的数据库交互。
     2
     •
     这种无状态特性非常适合分布式系统。
     3.
     userDetailsService(tulingUserDetailService)（用户详情服务）
     这个配置至关重要，它用于支持 password（密码模式）授权类型。当客户端使用密码模式请求令牌时，需要提供用户的用户名和密码。授权服务器就会使用这个 UserDetailsService来根据用户名加载用户详情，并验证密码是否正确。
     2
     5
     •
     tulingUserDetailService应该是你自定义的实现，用于从你的用户数据库（如MySQL、LDAP等）中查询用户信息。
     4.
     authenticationManager(authenticationManager)（认证管理器）
     这个配置与 userDetailsService紧密相关。AuthenticationManager是Spring Security的核心接口，负责协调认证过程。在密码模式下，AuthenticationManager会利用配置的 UserDetailsService来执行实际的用户认证逻辑。
     2
     5
     💎 总结与关联
     总而言之，这段代码配置了授权服务器在颁发令牌时的核心行为链条：验证用户凭证​ -> 生成原始令牌​ -> 使用增强链加工令牌（添加自定义信息并转换为JWT格式）-> （以JWT形式）返回令牌。
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {

        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tulingTokenEnhancer(),jwtAccessTokenConverter()));

        endpoints.tokenStore(tokenStore()) //授权服务器颁发的token 怎么存储的
                .tokenEnhancer(tokenEnhancerChain)
                .userDetailsService(tulingUserDetailService) //用户来获取token的时候需要 进行账号密码
                .authenticationManager(authenticationManager);
    }


    /**
     * 方法实现说明:授权服务器安全配置
     * @author:smlz
     * @return:
     * @exception:
     * @date:2020/1/15 20:23
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security
                .checkTokenAccess("isAuthenticated()") // 校验token端点需要认证
                .tokenKeyAccess("permitAll()") // 【关键修改】允许所有人访问token_key端点
                .allowFormAuthenticationForClients(); // 允许客户端使用表单认证
    }

}
