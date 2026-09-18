package com.example.fithub.data.remote.api

import com.example.fithub.core.RetrofitProvider
import android.util.Log

class OpenFoodFactsDataSource(
    private val api: OpenFoodFactsApi =
        RetrofitProvider.openFoodFacts.create(OpenFoodFactsApi::class.java)
) {
    suspend fun byBarcode(barcode: String): OffProduct? {
        val response = api.getProductByBarcode(barcode)

        Log.d(
            "OFF",
            "barcode=$barcode " +
                    "status=${response.status} " +
                    "status_verbose=${response.status_verbose} " +
                    "code=${response.code} " +
                    "name=${response.product?.productName} " +
                    "nameEn=${response.product?.productNameEn} " +
                    "nameZa=${response.product?.productNameZa} " +
                    "brand=${response.product?.brands}"
        )

        if (response.status != 1) return null
        return response.product
    }

    suspend fun search(terms: String): List<OffProduct> =
        api.searchProducts(terms).products.orEmpty()

    /**
     * Category browse via the search endpoint.
     * Uses OFF category tags (e.g. "fruits", "snacks") as a tag filter.
     */
    suspend fun byCategory(offTag: String): List<OffProduct> =
        api.searchByCategory(tag = offTag).products.orEmpty()
}