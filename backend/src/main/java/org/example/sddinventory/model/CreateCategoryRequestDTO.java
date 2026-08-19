package org.example.sddinventory.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCategoryRequestDTO {
    @NotBlank(message = "Category name cannot be blank")
    @Size(min = 1, max = 255, message = "Category name must be between 1 and 255 characters")
    private String name;

    public CreateCategoryRequestDTO() {
    }

    public CreateCategoryRequestDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
