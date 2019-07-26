package lucky.apollo.portal.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import javax.sql.DataSource;

/**
 * @Author luckylau
 * @Date 2019/7/12
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String SECURITY_IGNORE_URLS_SPILT_CHAR = ",";

    private static final String USER_ROLE = "USER";

    private static final String IGNORE_URLS = "openapi/**, /vendor/**, /styles/**, /scripts/**, /views/**, /img/**";

    @Autowired
    private DataSource dataSource;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.
                csrf().
                disable();
        http.
                headers()
                .frameOptions()
                .sameOrigin();
        http.
                authorizeRequests()
                .antMatchers("/**").hasAnyRole(USER_ROLE);
        http.
                formLogin()
                .loginPage("/login").permitAll()
                .failureUrl("/login?#/error")
                .and()
                .httpBasic();

        http.
                logout()
                .logoutUrl("/user/logout").invalidateHttpSession(true).clearAuthentication(true)
                .logoutSuccessHandler(getLogoutSuccessHandler());
        http.
                exceptionHandling()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"));
    }

    @Override
    public void configure(WebSecurity web) {
        for (String ignoreURL : IGNORE_URLS.trim().split(SECURITY_IGNORE_URLS_SPILT_CHAR)) {
            web.ignoring().antMatchers(ignoreURL.trim());
        }
    }

    @Bean(name = "urlLogoutHandler")
    public SimpleUrlLogoutSuccessHandler getLogoutSuccessHandler() {
        SimpleUrlLogoutSuccessHandler urlLogoutHandler = new SimpleUrlLogoutSuccessHandler();
        urlLogoutHandler.setDefaultTargetUrl("/login?#/logout");
        return urlLogoutHandler;
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(jdbcUserDetailsManager(dataSource)).passwordEncoder(new BCryptPasswordEncoder());
    }

    @Bean
    public JdbcUserDetailsManager jdbcUserDetailsManager(DataSource datasource) {
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(datasource);
        jdbcUserDetailsManager.setUsersByUsernameQuery("select Username,Password,Enabled from `Users` where Username = ?");
        jdbcUserDetailsManager.setAuthoritiesByUsernameQuery("select Username,Authority from `Authorities` where Username = ?");
        jdbcUserDetailsManager.setUserExistsSql("select Username from `Users` where Username = ?");
        jdbcUserDetailsManager
                .setCreateUserSql("insert into `Users` (Username, Password, Enabled) values (?,?,?)");
        jdbcUserDetailsManager
                .setUpdateUserSql("update `Users` set Password = ?, Enabled = ? where Username = ?");
        jdbcUserDetailsManager.setDeleteUserSql("delete from `Users` where Username = ?");
        jdbcUserDetailsManager
                .setCreateAuthoritySql("insert into `Authorities` (Username, Authority) values (?,?)");
        jdbcUserDetailsManager
                .setDeleteUserAuthoritiesSql("delete from `Authorities` where Username = ?");
        jdbcUserDetailsManager
                .setChangePasswordSql("update `Users` set Password = ? where Username = ?");

        return jdbcUserDetailsManager;
    }


}