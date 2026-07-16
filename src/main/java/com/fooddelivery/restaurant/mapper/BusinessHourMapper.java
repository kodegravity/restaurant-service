package com.fooddelivery.restaurant.mapper;

import com.fooddelivery.restaurant.dto.request.BusinessHourRequest;
import com.fooddelivery.restaurant.dto.response.BusinessHourResponse;
import com.fooddelivery.restaurant.entity.BusinessHour;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BusinessHourMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurantId", source = "restaurantId")
    BusinessHour toEntity(BusinessHourRequest request, UUID restaurantId);

    BusinessHourResponse toResponse(BusinessHour businessHour);

    List<BusinessHourResponse> toResponseList(List<BusinessHour> businessHours);
}
