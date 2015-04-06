# Rice Rest Services

##Installation
A collection of rest services for use with Kuali Rice.

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

Include a rest servlet xml with the following settings:
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
      <mvc:message-converters>
        <bean class="org.springframework.http.converter.StringHttpMessageConverter"/>
        <bean
                class="org.springframework.http.converter.ResourceHttpMessageConverter"/>

        <bean
                class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter">
          <property name="objectMapper" ref="objectMapper"/>
        </bean>
      </mvc:message-converters>
    <mvc:annotation-driven />
  <context:component-scan base-package="org.kuali.rice.rest"/>
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

Swagger documentation (on your server) can be accessed at:
```
http://<hostname/projectpath>/swagger/riceRestApi.html
```






