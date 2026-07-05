package io.codeqube.restaurantservice.dto;

import jakarta.validation.constraints.*;

public class TableRequest {

    @NotNull(message = "Table number is required")
    @Min(value = 1, message = "Table number must be at least 1")
    private Integer tableNumber;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 20, message = "Capacity must not exceed 20")
    private Integer capacity;

    private String status;

    public Integer getTableNumber() { return tableNumber; }
    public void setTableNumber(Integer tableNumber) { this.tableNumber = tableNumber; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
