package crac;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "barEntityManagerFactory", transactionManagerRef = "barTransactionManager", basePackages = {
		"crac.models.komet.daos" })
public class KometConfig {
	
	@Value("${komet.jpa.properties.hibernate.ddl-auto}")
    private String dll;
	
	@Value("${komet.jpa.properties.hibernate.dialect}")
    private String dialect;


	@Bean(name = "barDataSource")
	@ConfigurationProperties(prefix = "komet.datasource")
	public DataSource barDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "barEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean barEntityManagerFactory(EntityManagerFactoryBuilder builder,
			@Qualifier("barDataSource") DataSource barDataSource) {
		Map<String, Object> properties = new HashMap<String, Object>();
	    properties.put("hibernate.hbm2ddl.auto", dll);
	    properties.put("hibernate.dialect", dialect);
	    return builder.dataSource(barDataSource)
				.packages("crac.models.komet.entities")
				.persistenceUnit("komet")
				.properties(properties)
				.build();
	}

	@Bean(name = "barTransactionManager")
	public PlatformTransactionManager barTransactionManager(
			@Qualifier("barEntityManagerFactory") EntityManagerFactory barEntityManagerFactory) {
		return new JpaTransactionManager(barEntityManagerFactory);
	}

}