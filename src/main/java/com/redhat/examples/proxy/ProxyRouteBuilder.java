package com.redhat.examples.proxy;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpOperationFailedException;

/**
 * @author Shaishav Parekh
 * @version 0.0.1-SNAPSHOT
 */
public class ProxyRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        onException(HttpOperationFailedException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400"))
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
                .removeHeaders("PROXY_*")
                .transform().constant("Bad Request.");

        from("servlet:///api?matchOnUriPrefix=true")
                // preserve incoming parameters
                .setHeader("PROXY_PATH", simple("${headers.CamelHttpPath}"))
                .setHeader("PROXY_QUERY", simple("${headers.CamelHttpQuery}"))
                .setHeader("PROXY_METHOD", simple("${headers.CamelHttpMethod}"))
                .convertBodyTo(String.class, "UTF-8")
                .setHeader("PROXY_BODY", simple("${body}"))
                .setHeader("PROXY_URI").constant("http://restservice-apachecamel.rhcloud.com/providerservice/rest")
                .to("direct:getPermissions")
                .end();

        from("direct:getPermissions")
                .log("${headers.Auth-Key}")
                .choice()
                // Check for a non null auth-key header
                .when(simple("${headers.Auth-Key} == 'RedHat'"))
                    .to("direct:proxyRequest")
                .endChoice()
                .otherwise()
                    .to("direct:notAuthorized")
                .end();

        from("direct:proxyRequest")
                .setHeader(Exchange.HTTP_URI, simple("${headers.PROXY_URI}"))
                .setHeader(Exchange.HTTP_PATH, simple("${headers.PROXY_PATH}"))
                .setHeader(Exchange.HTTP_QUERY, simple("${headers.PROXY_QUERY}"))
                .setHeader(Exchange.HTTP_METHOD, simple("${headers.PROXY_METHOD}"))
                .setHeader(Exchange.CONTENT_TYPE, simple("application/json"))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        if (exchange.getIn().getHeader("PROXY_BODY") != null)
                            exchange.getOut().setBody(exchange.getIn().getHeader("PROXY_BODY").toString());
                        else
                            exchange.getOut().setBody("");
                        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                    }
                })
                .log("${headers}")
                .log("${body}")
                .removeHeaders("PROXY_*")
                .removeHeader("zip")
                .to("http4://dummyhost")
                .removeHeaders("x-*")
                .end();

        from("direct:notAuthorized")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400"))
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
                .setBody(constant("Not Authorized."))
                .removeHeaders("PROXY_*")
                .removeHeaders("x-*")
                .end();

    }
}