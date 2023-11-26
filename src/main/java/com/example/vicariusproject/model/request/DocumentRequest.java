package com.example.vicariusproject.model.request;

import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DocumentRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String text;
}
