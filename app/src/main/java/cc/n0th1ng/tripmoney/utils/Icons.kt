package cc.n0th1ng.tripmoney.utils

import androidx.annotation.DrawableRes
import com.composables.icons.materialsymbols.outlined.R


enum class Icons(@DrawableRes val resource: Int) {
    HOTEL(R.drawable.materialsymbols_ic_hotel_outlined),
    RESTAURANT(R.drawable.materialsymbols_ic_restaurant_outlined),
    TRANSPORT(R.drawable.materialsymbols_ic_local_taxi_outlined),
    FLIGHT(R.drawable.materialsymbols_ic_flight_outlined),
    ATTRACTION(R.drawable.materialsymbols_ic_theater_comedy_outlined),
    GROCERIES(R.drawable.materialsymbols_ic_grocery_outlined),
    COFFEE(R.drawable.materialsymbols_ic_local_cafe_outlined),
    GENERAL(R.drawable.materialsymbols_ic_shoppingmode_outlined),
    ENTERTAINMENT(R.drawable.materialsymbols_ic_theaters_outlined),
    LAUNDRY(R.drawable.materialsymbols_ic_local_laundry_service_outlined),
    INSURANCE(R.drawable.materialsymbols_ic_health_and_safety_outlined),
    SIM_DATA(R.drawable.materialsymbols_ic_sim_card_outlined),
    CAR_RENTAL(R.drawable.materialsymbols_ic_directions_car_outlined),
    FUEL(R.drawable.materialsymbols_ic_local_gas_station_outlined),
    TOURS(R.drawable.materialsymbols_ic_tour_outlined)
}