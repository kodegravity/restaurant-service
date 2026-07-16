package com.fooddelivery.restaurant.mapper;

import com.fooddelivery.restaurant.dto.response.CuisineResponse;
import com.fooddelivery.restaurant.entity.Cuisine;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CuisineMapper {

    CuisineResponse toResponse(Cuisine cuisine);

    List<CuisineResponse> toResponseList(List<Cuisine> cuisines);
}
