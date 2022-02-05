/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright 2019-2022 the original author or authors.
 *  *  *  *
 *  *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  *  * you may not use this file except in compliance with the License.
 *  *  *  * You may obtain a copy of the License at
 *  *  *  *
 *  *  *  *      https://www.apache.org/licenses/LICENSE-2.0
 *  *  *  *
 *  *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  *  * See the License for the specific language governing permissions and
 *  *  *  * limitations under the License.
 *  *  *
 *  *
 *
 */

package org.springdoc.core.configuration;

import java.util.Optional;

import org.springdoc.core.converters.PageableOpenAPIConverter;
import org.springdoc.core.customizers.DataRestDelegatingMethodParameterCustomizer;
import org.springdoc.core.customizers.DelegatingMethodParameterCustomizer;
import org.springdoc.core.providers.RepositoryRestConfigurationProvider;
import org.springdoc.core.providers.SpringDataWebPropertiesProvider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;

import static org.springdoc.core.utils.Constants.SPRINGDOC_ENABLED;
import static org.springdoc.core.utils.Constants.SPRINGDOC_PAGEABLE_CONVERTER_ENABLED;
import static org.springdoc.core.utils.SpringDocUtils.getConfig;

/**
 * The type Spring doc pageable configuration.
 * @author bnasslahsen
 */
@Lazy(false)
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = SPRINGDOC_ENABLED, matchIfMissing = true)
@ConditionalOnClass(Pageable.class)
@ConditionalOnWebApplication
public class SpringDocPageableConfiguration {

	/**
	 * Pageable open api converter pageable open api converter.
	 *
	 * @return the pageable open api converter
	 */
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = SPRINGDOC_PAGEABLE_CONVERTER_ENABLED, matchIfMissing = true)
	@Lazy(false)
	PageableOpenAPIConverter pageableOpenAPIConverter() {
		getConfig().replaceParameterObjectWithClass(org.springframework.data.domain.Pageable.class, org.springdoc.core.converters.models.Pageable.class)
				.replaceParameterObjectWithClass(org.springframework.data.domain.PageRequest.class, org.springdoc.core.converters.models.Pageable.class);
		return new PageableOpenAPIConverter();
	}

	/**
	 * Delegating method parameter customizer delegating method parameter customizer.
	 *
	 * @param optionalSpringDataWebPropertiesProvider the optional spring data web properties
	 * @param optionalRepositoryRestConfiguration the optional repository rest configuration
	 * @return the delegating method parameter customizer
	 */
	@Bean
	@ConditionalOnMissingBean
	@Lazy(false)
	DelegatingMethodParameterCustomizer delegatingMethodParameterCustomizer(Optional<SpringDataWebPropertiesProvider> optionalSpringDataWebPropertiesProvider, Optional<RepositoryRestConfigurationProvider> optionalRepositoryRestConfiguration) {
		return new DataRestDelegatingMethodParameterCustomizer(optionalSpringDataWebPropertiesProvider, optionalRepositoryRestConfiguration);
	}
}