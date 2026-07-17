package com.edutech.studify.config;

import com.edutech.studify.dto.response.GradeResponse;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Studify API",
                version = "1.0.0",
                description = "Student Management System REST API Documentation",
                contact = @Contact(
                        name = "Studify Team",
                        email = "support@studify.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Local Development Server"
                )
        },
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT authentication with Bearer token",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {

    /**
     * This customizer runs once when Swagger generates the API docs.
     * It loops through every single endpoint and attaches our standard global error responses.
     */
    @Bean
    public OpenApiCustomizer customOpenApiCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
            ApiResponses apiResponses = operation.getResponses();

            // Create a generic schema referencing our GlobalExceptionHandler ErrorResponse
            Schema<?> errorSchema = new Schema<>().$ref(GradeResponse.ErrorResponse.class.getSimpleName());
            MediaType mediaType = new MediaType().schema(errorSchema);
            Content content = new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, mediaType);

            // Add standard 400 Bad Request
            apiResponses.addApiResponse("400", new ApiResponse()
                    .description("Bad Request - Invalid input data or business rule violation")
                    .content(content));

            // Add standard 401 Unauthorized
            apiResponses.addApiResponse("401", new ApiResponse()
                    .description("Unauthorized - Invalid or missing JWT token")
                    .content(content));

            // Add standard 403 Forbidden
            apiResponses.addApiResponse("403", new ApiResponse()
                    .description("Forbidden - Insufficient permissions to access this resource")
                    .content(content));

            // Add standard 404 Not Found
            apiResponses.addApiResponse("404", new ApiResponse()
                    .description("Not Found - The requested resource does not exist")
                    .content(content));

            // Add standard 500 Internal Server Error
            apiResponses.addApiResponse("500", new ApiResponse()
                    .description("Internal Server Error - An unexpected server error occurred")
                    .content(content));
        }));
    }
}