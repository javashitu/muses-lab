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
@EntityScan(basePackages = "com.muses.manager.persistence.ck.warehouse.entity")
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "entityManagerFactoryClickhouse1",
        transactionManagerRef = "transactionManagerClickhouse1",
        basePackages = "com.muses.recommend.persistence.ck.warehouse.repo"
)
public class WarehouseCKDataSourceConfig {

    @Bean(name = "dataSourceClickhouse1")
    @ConfigurationProperties(prefix = "spring.datasource.clickhouse1")
    public DataSource dataSourceClickhouse() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "jpaPropertiesClickhouse1")
    @ConfigurationProperties(prefix = "spring.jpa.clickhouse1")
    public JpaProperties jpaPropertiesClickhouse() {
        return new JpaProperties();
    }

    @Bean(name = "entityManagerFactoryBeanClickhouse1")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBeanClickhouse(@Qualifier("dataSourceClickhouse1") DataSource dataSourceClickhouse,
                                                                                     @Qualifier("jpaPropertiesClickhouse1") JpaProperties jpaProperties,
                                                                                     EntityManagerFactoryBuilder factoryBuilder) {
        Map<String, String> properties = jpaProperties.getProperties();
        properties.put("hibernate.physical_naming_strategy", CamelCaseToUnderscoresNamingStrategy.class.getName());
        properties.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());

        return factoryBuilder
                .dataSource(dataSourceClickhouse)
                .properties(properties)
                .packages("com.muses.recommend.persistence.ck.warehouse.entity")
                .persistenceUnit("clickhousePersistenceUnit")
                .build();
    }

    @Bean(name = "entityManagerFactoryClickhouse1")
    public EntityManagerFactory entityManagerFactoryClickhouse(@Qualifier("entityManagerFactoryBeanClickhouse1") LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean) {
        return localContainerEntityManagerFactoryBean.getObject();
    }

    @Bean(name = "entityManagerClickhouse1")
    public EntityManager entityManagerClickhouse(@Qualifier("entityManagerFactoryClickhouse1") EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.createEntityManager();
    }

    @Bean(name = "transactionManagerClickhouse1")
    public PlatformTransactionManager transactionManagerClickhouse(@Qualifier("entityManagerFactoryClickhouse1") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
