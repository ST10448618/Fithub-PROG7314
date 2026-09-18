package com.example.fithub.data.remote.mapper

import com.example.fithub.core.CategoryMapper
import com.example.fithub.data.remote.api.OffProduct
import com.example.fithub.domain.model.Food
import com.example.fithub.domain.model.FoodSource
import com.example.fithub.util.IdGenerator

object FoodMapper {

    fun fromOff(product: OffProduct, source: FoodSource): Food? {
        val barcode = product.code?.takeIf { it.isNotBlank() } ?: return null

        // Try every name field OFF provides, in order of preference.
        // Falls back to a safe placeholder so the entry is never lost.
        val name = sequenceOf(
            product.productNameEn,
            product.productName,
            product.productNameZa,
            product.genericName,
            product.brands?.let { "$it (product)" }
        )
            .firstOrNull { !it.isNullOrBlank() }
            ?.trim()
            ?: "Unknown product"

        val n = product.nutriments

        // Calories — try kcal first, then kJ→kcal conversion, then per-serving
        val caloriesPer100g: Double? = when {
            n?.energyKcal100g != null -> n.energyKcal100g
            n?.energyKj100g != null -> n.energyKj100g / 4.184
            n?.energyLegacy100g != null -> n.energyLegacy100g / 4.184
            n?.energyKcalServing != null && (product.servingQuantity ?: 0.0) > 0.0 ->
                n.energyKcalServing * 100.0 / product.servingQuantity!!
            else -> null
        }

        return Food(
            id = IdGenerator.foodIdFromBarcode(barcode),
            name = name,
            brand = product.brands?.substringBefore(",")?.trim(),
            imageUrl = product.imageFrontUrl ?: product.imageUrl,
            category = CategoryMapper.mapOffCategories(product.categoriesTags),
            caloriesPer100g = caloriesPer100g,
            proteinPer100g = n?.proteins100g,
            carbsPer100g = n?.carbs100g,
            fatPer100g = n?.fat100g,
            fiberPer100g = n?.fiber100g,
            sugarPer100g = n?.sugars100g,
            sodiumPer100g = n?.sodium100g?.times(1000),
            servingSizeG = product.servingQuantity,
            servingLabel = product.servingSize,
            source = source,
            description = null
        )
    }
}