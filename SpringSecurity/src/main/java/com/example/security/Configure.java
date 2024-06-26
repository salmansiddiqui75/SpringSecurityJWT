package com.example.security;

import com.example.jwt.AuthEntryPointJwt;
import com.example.jwt.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.sql.DataSource;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class Configure
{
    @Autowired
    DataSource dataSource;
    @Autowired
    private AuthEntryPointJwt authEntryPointJwt;

    @Bean
    public AuthTokenFilter authTokenFilter()
    {
        return new AuthTokenFilter();
    }
    //Below commented one bean is for basic authentication
//    @Bean
//    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
//        http.authorizeHttpRequests((requests) -> requests.anyRequest().authenticated());
//        //http.formLogin(withDefaults());
//        http.httpBasic(withDefaults());
//        return http.build();
//    }

    //In memory authentication

//    @Bean
//    public UserDetailsService userDetailsService()
//    {
//        UserDetails user1= User.withUsername("user1").password(passwordEncoder().encode("password1")).roles("USER").build();
//        UserDetails admin1=User.withUsername("admin1").password(passwordEncoder().encode("admin1")).roles("ADMIN").build();
//
//        //Below code are used for H2-DB store credential
//
//        JdbcUserDetailsManager jdbcUserDetailsManager=new JdbcUserDetailsManager(dataSource);
//        jdbcUserDetailsManager.createUser(user1);
//        jdbcUserDetailsManager.createUser(admin1);
//        return jdbcUserDetailsManager;
//
//        //return new InMemoryUserDetailsManager(user1,admin1);
//    }
    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource)
    {
        return new JdbcUserDetailsManager(dataSource);

    }

    @Bean
    public CommandLineRunner initData(UserDetailsService userDetailsService)
    {
        return args -> {
            JdbcUserDetailsManager manager= (JdbcUserDetailsManager) userDetailsService;
            UserDetails user1=User.withUsername("user1").password(passwordEncoder().encode("password1")).roles("USER").build();
            UserDetails admin1=User.withUsername("admin1").password(passwordEncoder().encode("admin1")).roles("ADMIN").build();
            //Below code are used for H2-DB store credential

            JdbcUserDetailsManager jdbcUserDetailsManager=new JdbcUserDetailsManager(dataSource);
            jdbcUserDetailsManager.createUser(user1);
            jdbcUserDetailsManager.createUser(admin1);
        };
    }

   //Below code for H2 database authentication
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception
    {
        http.authorizeHttpRequests((requests) -> requests.requestMatchers("/h2-console/**").permitAll().
                requestMatchers("/signin").permitAll().anyRequest().authenticated());

        http.sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.exceptionHandling(exception->exception.authenticationEntryPoint(authEntryPointJwt));
        //http.formLogin(withDefaults());
        //http.httpBasic(withDefaults());
        http.headers(headers->headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
        http.csrf(AbstractHttpConfigurer::disable);
        http.addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    //Below code are for password encription

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception
    {
        return configuration.getAuthenticationManager();
    }
}
