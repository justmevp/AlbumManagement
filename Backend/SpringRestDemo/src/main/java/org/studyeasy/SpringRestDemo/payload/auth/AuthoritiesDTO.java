package org.studyeasy.SpringRestDemo.payload.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class AuthoritiesDTO {
    @NotBlank
    @Schema(description = "Authorities", example = "User", requiredMode = RequiredMode.REQUIRED)
    private String authorties;

}
