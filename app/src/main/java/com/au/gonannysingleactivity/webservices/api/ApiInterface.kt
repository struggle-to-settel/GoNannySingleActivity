package com.au.gonannysingleactivity.webservices.api

import androidx.core.app.NotificationCompat
import com.au.gonannysingleactivity.webservices.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*
import kotlin.Metadata

interface ApiInterface {

    @POST("send/otp")
    fun sendOtp(@Body hashMap: HashMap<String, Any>): Call<SendOtpResponse>

    @POST("verify/otp")
    fun verifyOtp(@Body hashMap: HashMap<String, Any>): Call<VerifyOtpResponse>

    @POST("signup")
    fun signUp(@Body hashMap: HashMap<String, Any>): Call<VerifyOtpResponse>

    @POST("kid")
    fun addKidInfo(@Body hashMap: HashMap<String, Any>): Call<MessageResponse>

    @PUT("kid")
    fun updateKidInfo(@Body hashMap: HashMap<String, Any>): Call<MessageResponse>

    @GET("kids")
    fun getKidsList(): Call<GetKidsResponse>

    @HTTP(method = "DELETE", path = "kid", hasBody = true)
    fun deleteKid(@Query("kid_id") kid_id: Int): Call<MessageResponse>

    @POST("address")
    fun addAddress(@Body hashMap: HashMap<String, Any>): Call<MessageResponse>

    @GET("addresses")
    fun getAddresses(): Call<GetAddressesResponse>

    @PUT("profile")
    fun updateUserProfile(@Body hashMap: HashMap<String, Any>): Call<UpdateUserProfileResponse>

    @POST("login")
    fun login(@Body hashMap: HashMap<String, Any>): Call<VerifyOtpResponse>

    @POST("logout")
    fun logOut(@Body hashMap: HashMap<String, Any>): Call<MessageResponse>

    @POST("booking")
    fun createBooking(@Body hashMap: HashMap<String, Any>): Call<CreateBookingResponse>

    @GET("bookings/price-detail")
    fun getPricing(@Query("booking_id") booking_id: Int): Call<GetPricingResponse>

    @GET("profile")
    fun getProfile(): Call<User>

    @PUT("change/password")
    fun changePassword(@Body hashMap: HashMap<String, Any>): Call<MessageResponse>

    @PUT("verify/phone/email")
    fun verifyEmailPhone(@Body hashMap: HashMap<String, Any>): Call<MessageResponse>

    @POST("verify/phone")
    fun verifyPhone(@Body hashMap: HashMap<String, Any>): Call<VerifyOtpResponse>

    @POST("contact-us")
    fun contactUs(@Body hashMap: HashMap<String, Any>): Call<MessageResponse>

    @Multipart
    @POST("upload/image/do")
    fun uploadImage(@Part part: MultipartBody.Part): Call<UploadImageResponse>

    @GET("payments/setup-intent")
    fun setUpIntent(): Call<SetUpIntentResponse>

    @POST("payments/card")
    fun addCard(@Body hashMap: HashMap<String, Any>): Call<MessageResponse>

    @GET("bookings")
    fun showBookings(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("status") status: Int,
        @Query("user_type") user_type: Int
    ): Call<ShowBookingsResponse>

    @GET("bookings")
    fun showBookings(): Call<ShowBookingsResponse>

    @GET("notifications")
    fun getNotifications(@Query("page") page:Int,@Query("limit")limit:Int): Call<NotificationResponse>


    @GET("bookings/detail")
    fun getBookingDetails(@Query("id") id: Int): Call<BookingDetailResponse>

    @POST("bookings/cancel")
    fun cancelBooking(@Body hashMap: HashMap<String, Any>): Call<MessageResponse>

    @GET("nanny/profile")
    fun getNannyAbout(@Query("nanny_id") nanny_id: Int): Call<NannyAboutResponse>

    @GET("payments/cards")
    fun getCards(): Call<GetCardsResponse>

    @HTTP(method = "DELETE", path = "payments/card", hasBody = true)
    fun deleteCard(@Body hashMap: HashMap<String, Any>): Call<MessageResponse>

    @GET("faqs")
    fun getFaqs(): Call<GetFaqResponse>

    @GET("used/hours")
    fun getUsedHours(): Call<UsedHoursResponse>

    @PUT("address")
    fun editAddress(@Body hashMap: HashMap<String, Any>): Call<MessageResponse>

    @GET("chat/listing")
    fun getMessagesList(
        @Query("limit") limit: Int,
        @Query("page") page: Int,
        @Query("keyword") keyword: String
    ): Call<ChatListingResponse>


    @GET("chat/listing")
    fun getMessagesList(): Call<ChatListingResponse>

    @GET("chat/messages")
    fun getMessageList(@Query("booking_id") booking_id:Int,@Query("page") page:Int):Call<OneToOneChatData>

    @POST("bookings/payment")
    fun bookingPayment(@Body hashMap: HashMap<String, Any>):Call<MessageResponse>

    @POST("upload/nanny/photo")
    fun nannyUploadImages(@Body hashMap: HashMap<String, Any>):Call<MessageResponse>

    @GET("upload/nanny/photo")
    fun getNannyImages():Call<GetNannyImagesResponse>

    @GET("upload/nanny/photo")
    fun getNannyImages(@Query("page") page:Int,@Query("limit") limit:Int):Call<GetNannyImagesResponse>

    @HTTP(method = "DELETE", path = "upload/nanny/photo", hasBody = true)
    fun deleteNannyImage(@Query("image_id") image_id:Int):Call<MessageResponse>

    @PUT("upload/nanny/photo")
    fun editNannyImage(@Body hashMap: HashMap<String, Any>):Call<MessageResponse>

    @GET("terms-conditions")
    fun getTermsConditions(@Query("id") id:Int):Call<TermsConditionsResponse>

    @POST("nanny/document")
    fun addDocuments(@Body hashMap: HashMap<String, Any>):Call<MessageResponse>

    @POST("working/hours")
    fun addWorkingHours(@Body hashMap: HashMap<String, Any>):Call<MessageResponse>

    @GET("search/nanny")
    fun searchNanny(@Query("booking_id") booking_id: Int,
    @Query("payment_method") payment_method:String):Call<MessageResponse>

    @GET("nanny/document")
    fun getDocuments():Call<GetDocumentsResponse>

    @HTTP(method = "DELETE", path = "nanny/document", hasBody = true)
    fun deleteDocuments(@Query("document_id")documents_id:Int):Call<MessageResponse>

    @GET("working/hours")
    fun getWorkingHours():Call<GetWorkingHoursResponse>

    @PUT("working/hours")
    fun editWorkingHours(@Body hashMap: HashMap<String, Any>):Call<MessageResponse>

    @PUT("nanny/document")
    fun editDocument(@Body hashMap: HashMap<String, Any>):Call<MessageResponse>

    @HTTP(method = "DELETE",path = "working/hours",hasBody = true)
    fun deleteWorkingHours(@Body hashMap: HashMap<String, Any>):Call<MessageResponse>

}