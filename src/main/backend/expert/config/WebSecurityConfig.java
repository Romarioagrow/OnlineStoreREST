package expert.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder(8);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/**")
                    .authorizeRequests()
                    .antMatchers("/", "/login**", "/js/**", "/error**", "/user/registration").permitAll()
                .and()
                    .authorizeRequests()
                    .antMatchers("/admin**").hasAuthority("ADMIN")
                    .mvcMatchers("/api/user/**").authenticated()
                    .anyRequest().permitAll()
                .and()
                    .formLogin()
                    .loginPage("/user/login")
                    .loginProcessingUrl("/user/login")
                    .defaultSuccessUrl("/", true)
                    .permitAll()
                .and()
                    .logout().permitAll()
                    .logoutUrl("/user/logout")
                    .logoutSuccessUrl("/")
                .and().csrf().disable();
    }
}
