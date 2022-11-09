package com.gb.restaurant.push

enum class TYPE {
    OrderNew,OrderHold,Reservation,Inquiry;

    override fun toString(): String {
        return super.toString()
    }
}