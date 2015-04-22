# Rice Rest Services

##Installation
A collection of rest services for use with Kuali Rice.

See the META-INF/examples directory for setup code and config properties.

Include it in a project which uses rice by using the following Maven dependency:

```xml
    <dependency>
      <groupId>org.kuali.rice</groupId>
      <artifactId>rest-services</artifactId>
      <version>${rice.rest.version}</version>
      <exclusions>
        <exclusion>
          <groupId>org.kuali.org</groupId>
          <artifactId>rice-impl</artifactId>
        </exclusion>
      </exclusions>
    </dependency>
```

Include a rest servlet xml (see rest-servlet.xml file in META-INF/examples) with the following settings:
```xml
    <bean id="objectMapper"
          class="org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean"
          p:indentOutput="true" p:simpleDateFormat="yyyy-MM-dd'T'HH:mm:ssZ">
    </bean>

    <bean class="org.springframework.beans.factory.config.MethodInvokingFactoryBean"
          p:targetObject-ref="objectMapper" p:targetMethod="registerModule">
        <property name="arguments">
            <list>
                <bean class="com.fasterxml.jackson.datatype.joda.JodaModule"/>
            </list>
        </property>
    </bean>

    <mvc:annotation-driven>
        <mvc:argument-resolvers>
            <bean class="org.springframework.security.web.bind.support.AuthenticationPrincipalArgumentResolver"/>
        </mvc:argument-resolvers>

        <mvc:message-converters>
            <bean class="org.springframework.http.converter.StringHttpMessageConverter"/>
            <bean class="org.springframework.http.converter.ResourceHttpMessageConverter"/>
            <bean class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter">
                <property name="objectMapper" ref="objectMapper"/>
            </bean>
        </mvc:message-converters>
    </mvc:annotation-driven>
    <context:component-scan base-package="org.kuali.rice.rest"/>

    <import resource="classpath:META-INF/spring/oauth/oauth2-configuration.xml"/>
```

and servlet definitions/mapping in web.xml for your app:

```xml
  <servlet>
    <servlet-name>rest</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <load-on-startup>6</load-on-startup>
  </servlet>
```
```xml
  <servlet-mapping>
    <servlet-name>rest</servlet-name>
    <url-pattern>/rest/*</url-pattern>
  </servlet-mapping>
```

and the following filters for the oauth support:

```xml
    <filter>
        <filter-name>springSecurityFilterChain</filter-name>
        <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
        <init-param>
            <param-name>contextAttribute</param-name>
            <param-value>org.springframework.web.servlet.FrameworkServlet.CONTEXT.rest</param-value>
        </init-param>
    </filter>
```

```xml
    <filter-mapping>
        <filter-name>springSecurityFilterChain</filter-name>
        <url-pattern>/rest/*</url-pattern>
    </filter-mapping>
```

Oauth configuration that will be used by the rest services can be found at META-INF/spring/oauth/oauth2-configuration.xml.

##Oauth token
To obtain a token a post request must be made to this url with properties below posted:
```
http://localhost:8080/kr-dev/rest/oauth/token
```

with these posted properties:

```
grant_type:client_credentials
client_secret:<your_secret>
client_id:<your_client>
scope:access
```

This should be done by your server code to keep client secrets safe.  The token retrieved must then be used by
future requests (or re-requested to be used).  Using the token is done by setting *Authorization* header property
to:
```
bearer <token_value>
```

##Document Search service

###Find documents

Find documents with a filter
Find all docs with a documentTypeName that starts with "Document" and dateCreated ">=03/02/2015".
<br/>
Note the usage of the
:: for equals, the wildcard character \*, the use of \| for logical ANDing, the "\>=" for dateCreated, and url
encoding for special characters in the dateCreated filter (\>,=,/):
```
http://<hostname/projectpath>/rest/api/v1/documents?filter=documentTypeName::Document*|dateCreated::%3E%3D03%2F02%2F2015
```

Limit results & start at a specific index, this will give you next and previous page links as well!
Limit to 5 results and start at index 10:
```
http://<hostname/projectpath>/rest/api/v1/documents?limit=5&startIndex=10
```

All docs by principalId:
```
http://<hostname/projectpath>/rest/api/v1/documents?principalId=admin
```

###Get document

Get a document with id "3252":
```
http://<hostname/projectpath>/rest/api/v1/documents/3252
```

##Json Results
###Single Item results
Single item results will include HAL links for their self reference and any related objects.

###List(paged) results
List results will contain "pageMetadata" about the page with what settings were applied (or automatically applied) and
how many items were applied.  Also HAL links are included to get the next/prev/first/self page using the same limits
and filters supplied. Items of the page are stored in the "content" variable.
For list results the json will look like:
```json
{
   "pageMetadata":{
      "startIndex":0,
      "limit":2,
      "itemsReturned":2,
      "itemsOmitted":0
   },
   "links":[
      {
         "rel":"self",
         "href":"http://localhost:8080/kr-dev/rest/api/v1/documents?limit=2&startIndex=0"
      },
      {
         "rel":"next",
         "href":"http://localhost:8080/kr-dev/rest/api/v1/documents?limit=2&startIndex=2"
      },
      {
         "rel":"first",
         "href":"http://localhost:8080/kr-dev/rest/api/v1/documents?limit=2&startIndex=0"
      }
   ],
   "content":[
      {
         "documentTypeName":"eDoc.Example1Doctype",
         "documentId":"3253",
         "status":"DISAPPROVED",
         "title":"Routing Document Type 'eDoc.Example1Doctype'",
         "applicationDocumentStatusDate":1426629499863,
         "documentTypeId":"2440",
         "dateCreated":1425336360000,
         "dateLastModified":1425336360000,
         "dateApproved":1426629499863,
         "dateFinalized":1426629499863,
         "applicationDocumentId":null,
         "initiatorPrincipalId":"admin",
         "routedByPrincipalId":null,
         "documentHandlerUrl":"${workflow.url}/EDocLite",
         "applicationDocumentStatus":null,
         "variables":{

         },
         "links":[
            {
               "rel":"self",
               "href":"http://localhost:8080/kr-dev/rest/api/v1/documents/3253"
            }
         ]
      },
      {
         "documentTypeName":"eDoc.Example1Doctype",
         "documentId":"3252",
         "status":"DISAPPROVED",
         "title":"Routing Document Type 'eDoc.Example1Doctype'",
         "applicationDocumentStatusDate":1426629499863,
         "documentTypeId":"2440",
         "dateCreated":1425336300000,
         "dateLastModified":1425336300000,
         "dateApproved":1426629499863,
         "dateFinalized":1426629499863,
         "applicationDocumentId":null,
         "initiatorPrincipalId":"admin",
         "routedByPrincipalId":null,
         "documentHandlerUrl":"${workflow.url}/EDocLite",
         "applicationDocumentStatus":null,
         "variables":{

         },
         "links":[
            {
               "rel":"self",
               "href":"http://localhost:8080/kr-dev/rest/api/v1/documents/3252"
            }
         ]
      }
   ]
}
```

##Swagger documentation

For swagger Rest documentation support ONLY on Spring 4.0+ (rice 2.5+):

```xml
    <dependency>
      <groupId>org.kuali.rice</groupId>
      <artifactId>rest-enhanced</artifactId>
      <version>${rice.rest.version}</version>
    </dependency>
    <dependency>
      <groupId>org.kuali.rice</groupId>
      <artifactId>rest-web</artifactId>
      <version>${rice.rest.version}</version>
      <type>war</type>
    </dependency>
```
And the War overlay:
```xml
     <overlay>
       <groupId>org.kuali.rice</groupId>
       <artifactId>rest-web</artifactId>
       <excludes>
         <exclude>**/web.xml</exclude>
         <exclude>META-INF</exclude>
       </excludes>
       <filtered>false</filtered>
     </overlay>
```

Swagger documentation (on your server) can be accessed at (a swagger user name and password from your config
properties must be used).  Click the oauth switch on method descriptions to verify access:
```
http://<hostname/projectpath>/swagger/riceRestApi.html
```

##Running tests

To run the tests, the RestSuiteTestData.sql file must be applied to the rice db you are testing with.  This file can
be found at rest-services/resources/sql/RestSuiteTestData.sql.

The tests can be found at in the org.kuali.rice.rest.test package in the rest-services modules.  These tests can only
be invoked manually and the test require that a Rice server using a the rest-services module to be currently running.
You must also modify the values in the TestConstants to match your own server's values to invoke these tests correctly.




