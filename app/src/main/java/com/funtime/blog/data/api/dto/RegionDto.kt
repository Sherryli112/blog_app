package com.funtime.blog.data.api.dto

import com.google.gson.annotations.SerializedName

data class RegionDto(
    @SerializedName("displayName") val displayName: String?,
    @SerializedName("themes") val themes: List<ThemeDto>?
)

data class ThemeDto(
    @SerializedName("displayName") val displayName: String?,
    @SerializedName("categories") val categories: List<CategoryDto>?
)

data class CategoryDto(
    @SerializedName("displayName") val displayName: String?
)
