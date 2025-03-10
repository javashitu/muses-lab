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
 * @ClassName LabCKDataSourceConfig
 * @Description:
 * @Author: java使徒
 * @CreateDate: 2025/3/9 18:42
 */

@Configuration
@EntityScan(basePackages = "com.muses.manager.persistence.ck.lab.entity")
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "entityManagerFactoryClickhouse2",
        transactionManagerRef = "transactionManagerClickhouse2",
        basePackages = "com.muses.recommend.persistence.ck.lab.repo"
)
public class LabCKDataSourceConfig {

    @Bean(name = "dataSourceClickhouse2")
    @ConfigurationProperties(prefix = "spring.datasource.clickhouse2")
    public DataSource dataSourceClickhouse() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "jpaPropertiesClickhouse2")
    @ConfigurationProperties(prefix = "spring.jpa.clickhouse2")
    public JpaProperties jpaPropertiesClickhouse() {
        return new JpaProperties();
    }

    @Bean(name = "entityManagerFactoryBeanClickhouse2")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBeanClickhouse(@Qualifier("dataSourceClickhouse2") DataSource dataSourceClickhouse,
                                                                                     @Qualifier("jpaPropertiesClickhouse2") JpaProperties jpaProperties,
                                                                                     EntityManagerFactoryBuilder factoryBuilder) {
        Map<String, String> properties = jpaProperties.getProperties();
        properties.put("hibernate.physical_naming_strategy", CamelCaseToUnderscoresNamingStrategy.class.getName());
        properties.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());

        return factoryBuilder
                .dataSource(dataSourceClickhouse)
                .properties(properties)
                .packages("com.muses.recommend.persistence.ck.lab.entity")
                .persistenceUnit("clickhousePersistenceUnit")
                .build();
    }

    @Bean(name = "entityManagerFactoryClickhouse2")
    public EntityManagerFactory entityManagerFactoryClickhouse(@Qualifier("entityManagerFactoryBeanClickhouse2") LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean) {
        return localContainerEntityManagerFactoryBean.getObject();
    }

    @Bean(name = "entityManagerClickhouse2")
    public EntityManager entityManagerClickhouse(@Qualifier("entityManagerFactoryClickhouse2") EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.createEntityManager();
    }

    @Bean(name = "transactionManagerClickhouse2")
    public PlatformTransactionManager transactionManagerClickhouse(@Qualifier("entityManagerFactoryClickhouse2") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
