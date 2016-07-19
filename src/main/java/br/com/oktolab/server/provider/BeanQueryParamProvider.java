package br.com.oktolab.server.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.model.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.oktolab.gson.GSON;
import br.com.oktolab.server.provider.annotation.BeanQueryParam;

@Produces(MediaType.APPLICATION_JSON)
@Provider
public class BeanQueryParamProvider extends AbstractValueFactoryProvider {

	private static final String MSG_ERROR_QUERY_PARAMS = "Erro ao tentar converter QueryParameters na classe '%s'. Params: '%s'.";

	private static final Logger LOG = LoggerFactory.getLogger(BeanQueryParamProvider.class);

	@javax.ws.rs.core.Context
    private UriInfo uriInfo;
	
    @Inject
    private ServiceLocator locator;

    /**
     * {@link InjectionResolver Injection resolver} for {@link BeanParam bean parameters}.
     */
    @Singleton
    static final class InjectionResolver extends ParamInjectionResolver<BeanQueryParam> {

        /**
         * Creates new resolver.
         */
        public InjectionResolver() {
            super(BeanQueryParamProvider.class);
        }
    }

    /**
     * Creates new instance initialized from parameters injected by HK2.
     * @param mpep Multivalued parameter extractor provider.
     * @param injector HK2 Service locator.
     */
    @Inject
    public BeanQueryParamProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator injector) {
        super(mpep, injector, Parameter.Source.UNKNOWN);
    }

    @Override
    public AbstractContainerRequestValueFactory<?> createValueFactory(Parameter parameter) {
    	if (parameter.isAnnotationPresent(BeanQueryParam.class)) {
    		return new BeanQueryParamValueFactory(locator, uriInfo, parameter);
    	}
    	return null;
    }
    
    
    private static final class BeanQueryParamValueFactory extends AbstractContainerRequestValueFactory<Object> {
    	
        private final Parameter parameter;
        private final ServiceLocator locator;
        private final UriInfo uriInfo;

        private BeanQueryParamValueFactory(ServiceLocator locator, UriInfo uriInfo, Parameter parameter) {
            this.locator = locator;
            this.parameter = parameter;
            this.uriInfo = uriInfo;
        }

        @Override
        public Object provide() {

            final Class<?> rawType = parameter.getRawType();

            final Object fromHk2 = locator.getService(rawType);
            if (fromHk2 != null) { // the bean parameter type is already bound in HK2, let's just take it from there
                return fromHk2;
            }
            return getValue();
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
		public Object getValue() {
        	Object instance = null;
        	Class<?> parameterClass = parameter.getRawType();//ParameterClass();
        	MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
        	Map<String, Object> mapToJson = new HashMap<String, Object>();
        	try {
        		for (Entry<String, List<String>> param : params.entrySet()) {
        			String key = param.getKey();
        			try {
        				parameterClass.getDeclaredField(key);
        			} catch (NoSuchFieldException e) {
        				continue ;
        			}
        			Object value = param.getValue().iterator().next();
        			if (mapToJson.containsKey(key)) {
        				Object valueAtMap = mapToJson.get(key);
        				if (valueAtMap instanceof Collection) {
        					((Collection)valueAtMap).add(value);
        				} else {
        					ArrayList<Object> listToJson = new ArrayList<Object>();
        					listToJson.add(mapToJson.get(key));
        					listToJson.add(value);
        					mapToJson.put(key, listToJson);
        				}
        			} else {
        				if (Collection.class.isAssignableFrom(parameterClass.getDeclaredField(key).getType())) {
        					if (value instanceof String) {
        						String valueStr = (String) value;
        						if (valueStr.startsWith("[") && valueStr.endsWith("]")) {
        							Class<?> type = parameterClass.getDeclaredField(key).getType();
        							mapToJson.put(key, GSON.getGson().fromJson(valueStr, type));
        						} else {
        							Collection fieldInstance = getFieldInstance(parameterClass, key);
        							fieldInstance.add(value);
        							mapToJson.put(key, fieldInstance);
        						}
        						
        					} else {
        						mapToJson.put(key, value);
        					}
        				} else {
        					mapToJson.put(key, value);
        				}
        			}
        		}
        		String json = GSON.getGson().toJson(mapToJson);
        		instance = GSON.getGson().fromJson(json, parameterClass);
        	} catch (Exception e) {
        		LOG.error(String.format(MSG_ERROR_QUERY_PARAMS, parameterClass, params), e);
        	}
            return instance;
        }

		private Collection<?> getFieldInstance(Class<?> parameterClass, String key)
				throws NoSuchFieldException {
			Class<?> type = parameterClass.getDeclaredField(key).getType();
			if (List.class.isAssignableFrom(type)) {
				return new ArrayList<Object>();
			} else if (Set.class.isAssignableFrom(type)) {
				return new LinkedHashSet<Object>();
			}
			return new ArrayList<Object>();
		}
    }
    
}
