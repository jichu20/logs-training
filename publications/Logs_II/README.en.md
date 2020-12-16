# Traceability in distributed services part II

In the [latest article](https://jichu20.medium.com/traceability-in-distributed-services-bb3f67a67cb4), we viewed how to transport the value of any headers through our service so that we could have a unique identifier 'xTraceId' in all our services so that we could trace a request and link the logs that are generated in the different micro services that are part of our system.

In this post, we are going to integrate these changes into our server application so that we can see how the value of the xTraceId header is the same in the different services that our request passes through.

Then we will see how to modify the logback configuration to generate the traces in Json format and finally we will include the generation of traces for our own application.

All these changes will be based on the spring [Sleuth library](https://spring.io/projects/spring-cloud-sleuth).

## Integration of changes in the server

Remember that all the classes described in the previous article have been generated in a library, this will allow us to include the described functionality in all our microservices.

Then we will generate a new 'logger-server' application and include in our pom file, the reference to the new library

```xml

    <properties>
		<spring-cloud.version>Hoxton.SR8</spring-cloud.version>
	</properties>

	<dependencies>

		<dependency>
			<groupId>com.jichu20</groupId>
			<artifactId>logger-lib</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>
	</dependencies>
```

## Modifying the client ...

We will modify the client service so that the requests point to the new service.

```java
@Service
public class BookServiceImpl implements BookService {

    private RestTemplate restTemplate;

    @Autowired
    public BookServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BookDto getBook(String bookName) {

        ResponseEntity<BookDto> book = restTemplate.getForEntity("http://localhost:8081/book/TreasureIsland ", BookDto.class);

        return book.getBody();

    }
}
```

## Creating the server

In the server service, we will create a controller and a service.

@controller

```java

    @Autowired
    BookService bookService;

    @GetMapping("/{bookName}")
    public ResponseEntity<BookDto> getBook(@PathVariable("bookName") String bookName) throws InterruptedException {

        logger.info("Hello Sleuth - server");
        return new ResponseEntity<BookDto>(bookService.getBook(bookName), HttpStatus.ACCEPTED);

    }
```

@service

```java

     public BookDto getBook(String bookName) {

        return new BookDto(bookName, "resume of the book", "Robert Louis Stevenson", 2);

    }
```

The next step will be to create a client capable of invoking a service through spring's RestTemplate connector. Sleuth, allows you to use different connectors, but in this article we will focus on RestTemplate.

It is important that the RestTemplate component used is a bean injected in our context, that is, we cannot use the `new RestTemplate ()` statement since sleuth would not have the capacity to inject the necessary interceptors or filters, for this we generate a bean in the context of type RestTemplate

```java
@Configuration
public class LoggingConfiguration {

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
```

Next we will execute a request against the client to view the traces in the two services

This first test will generate the following request

![basic request](_assets/basic_request.png)

As we can see, in the headers sent, the B3 headers are included with the corresponding information.

## Generating new headers

Now is the time to generate new headers, specifically we will generate the `xTraceId` header whose value will be a random UUID.

In the first time we will generate a new filter capable of identifying if the header has been filled in from a client and therefore we propagate its value or if, on the contrary, it is not informed and we will have to generate a new value for it.

```java

@Component
public class TraceFilter extends GenericFilterBean {

    private final Tracer tracer;

    TraceFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        Span currentSpan = this.tracer.currentSpan();

        if (currentSpan == null) {
            chain.doFilter(request, response);
            return;
        }

        // get header value
        String xTraceId = ((HttpServletRequest) request).getHeader(Constant.X_TRACE_ID);

        if (StringUtils.isBlank(xTraceId)) {
            xTraceId = UUID.randomUUID().toString();
            // set value to header
            ExtraFieldPropagation.set(currentSpan.context(), Constant.X_TRACE_ID, xTraceId);
        }

        // set header to response
        ((HttpServletResponse) response).addHeader(Constant.X_TRACE_ID, xTraceId);
        currentSpan.tag(Constant.X_TRACE_ID, xTraceId);

		MDC.put(Constant.X_TRACE_ID, xTraceId);

        chain.doFilter(request, response);
    }
}
```

Now the request we make will be like this:

![request with traceId](_assets/requestWithTraceId.png)

And the response of our service will also return the information.

![response with traceId](_assets/responseWithTraceId.png)

Pay special attention to the sentence `MDC.put(Constant.X_TRACE_ID, xTraceId);`, This allows us to have the information when writing the logs of our application.

## Configuring logback

It is the moment to modify our logback configuration for the generation of logs in Json format, later we can use other processes to send these logs to centralized systems.

To make out this configuration we will use the [logsthas](https://github.com/logstash/logstash-logback-encoder)library, which provides different appenders, layouts, ... for logback. For this we will add the dependency.

```xml
        <!-- logstash -->
		<dependency>
			<groupId>net.logstash.logback</groupId>
			<artifactId>logstash-logback-encoder</artifactId>
			<version>6.4</version>
		</dependency>
```

Now we will add a new appender to our configuration

```xml
    <springProperty scope="context" name="springAppName" source="spring.application.name"/>

    <appender name="json-appender" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp>
                    <fieldName>creationDate</fieldName>
                    <pattern>[UNIX_TIMESTAMP_AS_NUMBER]</pattern>
                </timestamp>
                <pattern>
                    <pattern>
        			{
        				"level": "%level",
  						"message": "%msg",
						"spanId": "%X{X-B3-SpanId:-}",
						"traceId": "%X{xTraceId:-}",
						"properties": {
							"app-name": "${springAppName:-}",
							"class": "%logger{40}",
							"thread": "%thread"
  						}
        			}
                    </pattern>
                </pattern>

            </providers>
        </encoder>
    </appender>
```

When we run our application, we can see that the traces now have a json format

![request with traceId](_assets/trazasJson.png)

## Extra

### Converters in the appender

In our appender, we can add converters, for example, we may want to get the date in nanosecond format.

For this we will generate a new class with the name `NanoSecondsConverter.java` with the following code

```java

import java.util.concurrent.TimeUnit;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class NanoSecondsConverter extends ClassicConverter {

    long start = System.nanoTime();

    @Override
    public String convert(ILoggingEvent event) {
        return Long.toString(TimeUnit.MILLISECONDS.toNanos(event.getTimeStamp()));
    }
}
```

and we will modify the appender to include a new field that contains the date in nano seconds format.

```xml
    <!-- custom converters -->
    <conversionRule conversionWord="nanos" converterClass="com.jichu20.loggerlib.converter.NanoSecondsConverter" />

    ... ... ...
    "creationDateNano": "#asLong{%-19nanos}",
    ... ... ...
```

### Interceptors

We also have the possibility of adding interceptors to our restTemplate so that it shows both the requests made and the responses received.

To do this, we will include the `RequestResponseLoggingInterceptor.java` class in our project and we will modify the definition of the restTemplate bean as follows.

```java

    @Bean
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        if (restTemplate.getInterceptors() == null) {
            restTemplate.setInterceptors(new ArrayList<>());
        }

        restTemplate.getInterceptors().add(new RequestResponseLoggingInterceptor());

        return restTemplate;

    }

```

We also have the possibility of adding interceptors to our restTemplate so that it shows both the requests made and the responses received.

To do this, we will include the `RequestResponseLoggingInterceptor.java` class in our project and we will modify the definition of the restTemplate bean as follows.

Request

```json
{
  "creationDate": 1608033541998,
  "level": "INFO",
  "message": "===========================request begin================================================\nURI         : http://localhost:8081/book/elbarcodelpirata\nMethod      : GET\nHeaders     : [Accept:\"application/json, application/*+json\", Content-Length:\"0\", X-B3-TraceId:\"2087a978b522188d\", X-B3-SpanId:\"acb69697a19b720b\", X-B3-ParentSpanId:\"2087a978b522188d\", X-B3-Sampled:\"1\", xtraceid:\"0407a46e-7c55-4613-a264-b2a320255c77\"]\n==========================request end================================================",
  "spanId": "",
  "traceId": "0407a46e-7c55-4613-a264-b2a320255c77",
  "properties": {
    "app-name": "client-logger",
    "class": "c.j.l.i.RequestResponseLoggingInterceptor",
    "thread": "http-nio-8080-exec-1"
  },
  "creationDateNano": 1608033541998000000
}
```

Response

```json
{
  "creationDate": 1608033542219,
  "level": "INFO",
  "message": "============================response begin==========================================\nStatus code  : 202 ACCEPTED\nStatus text  : \nHeaders      : [xTraceId:\"0407a46e-7c55-4613-a264-b2a320255c77\", Content-Type:\"application/json\", Transfer-Encoding:\"chunked\", Date:\"Tue, 15 Dec 2020 11:59:02 GMT\", Keep-Alive:\"timeout=60\", Connection:\"keep-alive\"]\nResponse body: {\"name\":\"elbarcodelpirata\",\"resume\":\"resume of the book\",\"author\":\"Robert Louis Stevenson\",\"numPAges\":2}=======================response end=================================================",
  "spanId": "",
  "traceId": "0407a46e-7c55-4613-a264-b2a320255c77",
  "properties": {
    "app-name": "client-logger",
    "class": "c.j.l.i.RequestResponseLoggingInterceptor",
    "thread": "http-nio-8080-exec-1"
  },
  "creationDateNano": 1608033542219000000
}
```

You can download the source code of the projects in the following github link
