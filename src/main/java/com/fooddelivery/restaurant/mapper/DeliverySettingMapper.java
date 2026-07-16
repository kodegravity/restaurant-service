package com.fooddelivery.restaurant.mapper;

import com.fooddelivery.restaurant.dto.request.UpdateDeliverySettingRequest;
import com.fooddelivery.restaurant.dto.response.DeliverySettingResponse;
import com.fooddelivery.restaurant.entity.DeliverySetting;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeliverySettingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateDeliverySettingRequest request, @MappingTarget DeliverySetting deliverySetting);

    DeliverySettingResponse toResponse(DeliverySetting deliverySetting);
}
