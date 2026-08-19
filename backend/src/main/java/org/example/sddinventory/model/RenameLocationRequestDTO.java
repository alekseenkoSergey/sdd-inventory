package org.example.sddinventory.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RenameLocationRequestDTO {
    @NotBlank(message = "Location name cannot be empty or whitespace-only")
    @Size(min = 1, max = 255, message = "Location name must be between 1 and 255 characters")
    private String name;

    public RenameLocationRequestDTO() {
    }

    public RenameLocationRequestDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
