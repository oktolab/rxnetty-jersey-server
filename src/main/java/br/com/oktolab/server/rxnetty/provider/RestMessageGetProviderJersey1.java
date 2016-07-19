package br.com.oktolab.server.rxnetty.provider;

//import java.util.ArrayList;

//@Provider JERSEY 1
public class RestMessageGetProviderJersey1 {}/*implements InjectableProvider<BeanQueryParam, Parameter> {
	
	private static final String MSG_ERROR_QUERY_PARAMS = "Erro ao tentar converter QueryParameters na classe '%s'. Params: '%s'.";

	private static final Logger LOG = LoggerFactory.getLogger(RestMessageGetProviderJersey1.class);

	@javax.ws.rs.core.Context
    private UriInfo uriInfo;
	
	@Override
	public ComponentScope getScope() {
		return ComponentScope.PerRequest;
	}
	
	@Override
	public Injectable<Object> getInjectable(ComponentContext ic, BeanQueryParam annotation, Parameter param) {
        return new QueryParamsInjectable(uriInfo, param);
	}
	
	static class QueryParamsInjectable implements Injectable<Object> {
		
		private static Gson gson = new GsonBuilder().setDateFormat("dd/MM/yyyy").create();
		
		private UriInfo uriInfo;
		private Parameter param;
		
		public QueryParamsInjectable(UriInfo uriInfo, Parameter param) {
			this.uriInfo = uriInfo;
			this.param = param;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Object getValue() {
        	Object instance = null;
        	Class<?> parameterClass = param.getRawType();//ParameterClass();
        	MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
        	Map<String, Object> mapToJson = new HashMap<String, Object>();
        	try {
        		for (Entry<String, List<String>> param : params.entrySet()) {
        			String key = param.getKey();
        			Object value = param.getValue().iterator().next();
        			if (mapToJson.containsKey(key)) {
        				Object valueAtMap = mapToJson.get(key);
        				if (valueAtMap instanceof List) {
        					((List)valueAtMap).add(value);
        				} else {
        					ArrayList<Object> listToJson = new ArrayList<Object>();
        					listToJson.add(mapToJson.get(key));
        					listToJson.add(value);
        					mapToJson.put(key, listToJson);
        				}
        			} else {
        				mapToJson.put(key, value);
        			}
        		}
        		String json = gson.toJson(mapToJson);
        		instance = gson.fromJson(json, parameterClass);
        	} catch (Exception e) {
        		LOG.error(String.format(MSG_ERROR_QUERY_PARAMS, parameterClass, params));
        	}
            return instance;
        }
	}

}*/
