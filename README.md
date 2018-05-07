Sample Spring Music
============

This is a sample spring boot application that is based off of the [spring music application](https://github.com/cloudfoundry-samples/spring-music) that has been enhanced to provide coding examples that illustrate some of the concepts explained in the [RESTful API Design Recipe](https://github.com/pivotalservices/modernization-cookbook-template/blob/master/content/recipes/rest-api-design-steps.md).  

This repo provides examples of the following concepts
1. Swagger API Documentation
1. Defining JSON Schema APIs
1. Error Handling Mechanism
1. Versioning Mechanism

## Swagger Documentation

The following example shows how swagger can be loaded from a yaml resource file.  Using this approach, the swagger content can be loaded into a swagger editor where the documentation can be enhanced and the the yaml exported back into the main resources folder.  In addition, since the swagger is all loaded from the yaml file, no swagger annotations need to be applied anywhere in the code.
``` java
@Configuration
@EnableSwagger2
@Import({Swagger2DocumentationConfiguration.class})
public class SwaggerConfig {

    @Value("classpath:/config/swagger.yml")
    private Resource swaggerResource;

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    public ServiceModelToSwagger2Mapper mapper() {
        return new  ServiceModelToSwagger2MapperImpl() {
            public Swagger mapDocumentation(Documentation from) {
                try {
                    String swaggerContent = StreamUtils.copyToString(SwaggerConfig.this.swaggerResource.getInputStream(), StandardCharsets.UTF_8);
                    return new SwaggerParser().parse(swaggerContent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
```

The following example shows an approach that allows multiple swagger resources to be aggregated together into a single swagger UI.
``` java
@Primary
@Bean
public SwaggerResourcesProvider swaggerResourcesProvider() {
    return () -> {
        SwaggerResource wsResource = new SwaggerResource();
        wsResource.setName("album-service");
        wsResource.setSwaggerVersion("2.0");
        wsResource.setLocation("/v2/api-docs");

        List<SwaggerResource> resources = new ArrayList<>();
        resources.add(wsResource);
        return resources;
    };
}
```

## JSON Schema

``` java
@RestController
@RequestMapping(value = "/schemas")
public class AlbumSchemaController {

    private static final Logger logger = LoggerFactory.getLogger(AlbumSchemaController.class);

    private final ObjectMapper objectMapper;

    private final JsonSchemaGenerator schemaGenerator;

    @Autowired
    public AlbumSchemaController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.schemaGenerator = new JsonSchemaGenerator(this.objectMapper);
    }

    @RequestMapping(value = "/album", method = RequestMethod.GET)
    public Object getAlbumSchema(UriComponentsBuilder builder) {
        logger.info("Getting album schema");

        ObjectNode schemaJsonNode = (ObjectNode)schemaGenerator.generateJsonSchema(Album.class);
        UriComponents uriComponents = builder.path("/schemas/album").build();
        schemaJsonNode.put("id", uriComponents.toUriString());

        return schemaJsonNode;
    }
}
```

## Error Handling

``` java
@ControllerAdvice
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {ApplicationException.class})
    public ResponseEntity<List<ApiError>> handleValidationFailures(ApplicationException ex) {
        return new ResponseEntity<>(ex.getErrors(), ex.getStatus());
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<List<ApiError>> handleError(Exception ex) {
        List<ApiError> errors = Arrays.asList(new ApiError("operation-failed", null, "an unexpected error occurred"));
        return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
}
```

## Versioning Mechanism

``` java
@ControllerAdvice
public class VersioningRequestBodyAdvice implements RequestBodyAdvice {

    private VersioningManager versioningManager;

    public VersioningRequestBodyAdvice(VersioningManager versioningManager) {
        this.versioningManager = versioningManager;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        return versioningManager.beforeBodyRead(inputMessage, targetType);
    }

}
```

``` java
@ControllerAdvice
public class VersioningResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private VersioningManager versioningManager;

    public VersioningResponseBodyAdvice(VersioningManager versioningManager) {
        this.versioningManager = versioningManager;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        return this.versioningManager.beforeBodyWrite(body, request.getHeaders());
    }

}
```
