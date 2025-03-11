package com.in28minutes.api_gateway;

import java.util.function.Function;

import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.Buildable;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.UriSpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGatewayConfiguration {

	// Configuracion de la ruta 
	@Bean
	public RouteLocator gatewayRouter(RouteLocatorBuilder builder) {
	
		// Filtro de ruta --->  
		// Se incorpora un header al request antes de que el gateway haga el enrutado
		// Tambien se añade un parametro
		Function<GatewayFilterSpec, UriSpec> filter1 = f -> f.addRequestHeader("MyHeader", "MyURI").addRequestParameter("Param", "MyValue");
		
		// Funcion Lambda que redirige los paths /get a una url especifica 
		Function<PredicateSpec, Buildable<Route>> routeFunction = p -> p.path("/get").filters(filter1).uri("http://httpbin.org:80");
		
		Function<PredicateSpec, Buildable<Route>> routeFunction2 = p -> p.path("/currency-exchange/**").uri("lb://currency-exchange");
		Function<PredicateSpec, Buildable<Route>> routeFunction3 = p -> p.path("/currency-conversion/**").uri("lb://currency-conversion");
		
		// Rewrite 
		// Cambiamos el path "currency-conversion-new" por el path "currency-conversion-feign" antes de reenrutar al servicio
		Function<GatewayFilterSpec, UriSpec> filter2 = f -> f.rewritePath("/currency-conversion-new/(?<segment>.*)", "/currency-conversion/feign/${segment}");
		Function<PredicateSpec, Buildable<Route>> routeFunction4 = p -> p.path("/currency-conversion-new/**").filters(filter2).uri("lb://currency-conversion");
		
		RouteLocator routeLocator = builder.routes()
				.route(routeFunction)
				.route(routeFunction2)
				.route(routeFunction3)
				.route(routeFunction4)
				.build();
		
		return routeLocator;
		
	}
	
}
