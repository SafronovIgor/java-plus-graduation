package ru.practicum.dto.locus;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class NewLocusDto {
    @NotNull
    @Size(min = 2, max = 128, message = "name must be more than 2 characters and less than 128")
    private String name;
    @NotNull
    private LocusType locusType;
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90.0")
    @DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90.0")
    private Float lat;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be greater than or equal to -180.0")
    @DecimalMax(value = "180.0", message = "Longitude must be less than or equal to 180.0")
    private Float lon;

    @NotNull(message = "Radius is required")
    @Positive(message = "Radius must be a positive number")
    private Float rad;

}
