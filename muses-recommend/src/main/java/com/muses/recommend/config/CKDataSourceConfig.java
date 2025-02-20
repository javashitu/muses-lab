package com.muses.recommend.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @ClassName CKDataSourceConfig
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2024/9/29 9:56
 */

@Configuration
@EntityScan(basePackages = "com.muses.manager.persistence.ck.entity")
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "entityManagerFactoryClickhouse",
        transactionManagerRef = "transactionManagerClickhouse",
        basePackages = "com.muses.recommend.persistence.ck.repo"
)
public class CKDataSourceConfig {

    @Bean(name = "dataSourceClickhouse")
    @ConfigurationProperties(prefix = "spring.datasource.clickhouse")
    public DataSource dataSourceClickhouse() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "jpaPropertiesClickhouse")
    @ConfigurationProperties(prefix = "spring.jpa.clickhouse")
    public JpaProperties jpaPropertiesClickhouse() {
        return new JpaProperties();
    }

    @Bean(name = "entityManagerFactoryBeanClickhouse")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBeanClickhouse(@Qualifier("dataSourceClickhouse") DataSource dataSourceClickhouse,
                                                                                     @Qualifier("jpaPropertiesClickhouse") JpaProperties jpaProperties,
                                                                                     EntityManagerFactoryBuilder factoryBuilder) {
        Map<String, String> properties = jpaProperties.getProperties();
        properties.put("hibernate.physical_naming_strategy", CamelCaseToUnderscoresNamingStrategy.class.getName());
        properties.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());

        return factoryBuilder
                .dataSource(dataSourceClickhouse)
                .properties(properties)
                .packages("com.muses.recommend.persistence.ck.entity")
                .persistenceUnit("clickhousePersistenceUnit")
                .build();
    }

    @Bean(name = "entityManagerFactoryClickhouse")
    public EntityManagerFactory entityManagerFactoryClickhouse(@Qualifier("entityManagerFactoryBeanClickhouse") LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean) {
        return localContainerEntityManagerFactoryBean.getObject();
    }

    @Bean(name = "entityManagerClickhouse")
    public EntityManager entityManagerClickhouse(@Qualifier("entityManagerFactoryClickhouse") EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.createEntityManager();
    }

    @Bean(name = "transactionManagerClickhouse")
    public PlatformTransactionManager transactionManagerClickhouse(@Qualifier("entityManagerFactoryClickhouse") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
