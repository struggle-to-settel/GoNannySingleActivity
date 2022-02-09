package com.au.gonannysingleactivity.webservices

data class KidsModel(
    val kidsName: String,
    val hourlyBehaviour: String,
    val allergies: Boolean,
    val indoorCamera: Boolean,
)

data class SendOtpResponse(
    var token: String,
    var message: String
)

data class VerifyOtpResponse(
    val message: String,
    val token: String,
    val user: User
)

data class ErrorBody(var error: String, var error_description: String)

data class User(
    val about: Any,
    val account_number: Any,
    val account_type: Any,
    val address: Any,
    val apple_id: Any,
    val city: Any,
    val country_code: String,
    val country_name: Any,
    val created_at: String,
    val description: Any,
    val designation: Any,
    val email: Any,
    val email_country: Any,
    val email_name: Any,
    val email_verify_token: Any,
    val fb_id: Any,
    val first_name: Any,
    val google_id: Any,
    val id: Int,
    val is_account_connected: Int,
    val is_active: Any,
    val is_agreement_signed: Int,
    val is_blocked: Any,
    val is_confirmed: Int,
    val is_deactivated: Int,
    val is_documents_exist: Int,
    val is_email_verified: Int,
    val is_phone_verified: Int,
    val last_name: Any,
    val last_seen: Any,
    val lat: Any,
    val lng: Any,
    val nanny_price: Any,
    val notifications: Int,
    val otp: Any,
    val otp_expired_at: Any,
    val password: Any,
    val phone_number: Long,
    val profile_image: Any,
    val rating: Int,
    val reset_pass_token: Any,
    val routing_number: Any,
    val ssn: Any,
    val state: Any,
    val stripe_account_id: Any,
    val stripe_city: Any,
    val stripe_country: Any,
    val stripe_customer_id: Any,
    val stripe_email: Any,
    val stripe_line_1: Any,
    val stripe_postal_code: Any,
    val stripe_state: Any,
    val type: Any,
    val unread_messages: Int,
    val unread_notifications: Int,
    val updated_at: String,
    val user_type: Int,
    val verification_image: Any,
    val working_hours: Any,
    val zip_code: Any
)

data class MessageResponse(
    val message: String
)

data class GetKidsResponse(
    val kids: List<Kid>
)

data class Kid(
    val allergies: Int,
    val allergy: String,
    val created_at: String,
    val hourly_behaviour: String,
    val id: Int,
    val indoor_camera: Int,
    val kid_first_name: String,
    val kid_last_name: Any,
    var is_selected: Boolean = false
)

data class UpdateUserProfileResponse(
    val message: String,
    val user: User
)

data class CreateBookingResponse(
    val message: String,
    val id: Int
)

data class GetPricingResponse(
    val address: String,
    val apartment_no: String,
    val app_commission: Double,
    val booking_date_time: String,
    val cancellation_charge: Any,
    val cancellation_reason: Any,
    val cancelled_at: Any,
    val cancelled_by: Any,
    val check_in: Any,
    val check_out: Any,
    val checkout_verification_code: Int,
    val city: String,
    val code_expired_at: Any,
    val code_generated_at: String,
    val completed_at: Any,
    val created_at: String,
    val deleted_at: Any,
    val distance: Any,
    val duration: Int,
    val duration_name: String,
    val end_booking_date_time: String,
    val end_time: String,
    val hours: Int,
    val id: Int,
    val is_paid: Int,
    val is_verified: Int,
    val landmark: String,
    val lat: Double,
    val lng: Double,
    val nanny_address: Any,
    val nanny_assigned_at: Any,
    val nanny_id: Any,
    val nanny_latitude: Any,
    val nanny_longitude: Any,
    val nanny_price: Double,
    val needed_date_time: String,
    val no_of_kids: Int,
    val not_available_time: Any,
    val parent_id: Int,
    val payment_method_id: Any,
    val price: Int,
    val refund_amount: Any,
    val start_time: String,
    val status: Int,
    val tax: Int,
    val time_zone: String,
    val total_charge: Int,
    val total_service_time: Any,
    val updated_at: Any,
    val verification_code: Int,
    val voucher: String
)

data class GetAddressesResponse(
    val adddresses: List<Adddresse>
)

data class Adddresse(
    val address: Any,
    val address_type: Int,
    val apartment_no: String,
    val city: String,
    val created_at: String,
    val deleted_at: Any,
    val id: Int,
    val landmark: Any,
    val lat: Double,
    val lng: Double,
    val state: String,
    val status: Int,
    val tags: Any,
    val updated_at: String,
    val user_id: Int
)

data class UploadImageResponse(
    val filename: String,
    val message: String
)

data class SetUpIntentResponse(
    val application: Any,
    val cancellation_reason: Any,
    val client_secret: String,
    val created: Int,
    val customer: Any,
    val description: Any,
    val id: String,
    val last_setup_error: Any,
    val latest_attempt: Any,
    val livemode: Boolean,
    val mandate: Any,
    val metadata: Metadata,
    val next_action: Any,
    val `object`: String,
    val on_behalf_of: Any,
    val payment_method: Any,
    val payment_method_options: PaymentMethodOptions,
    val payment_method_types: List<String>,
    val single_use_mandate: Any,
    val status: String,
    val usage: String
)

class Metadata

data class PaymentMethodOptions(
    val card: Card
)

data class UsedHoursResponse(
    val bookings_data: BookingsData
)

data class BookingsData(
    val no_of_times: Int,
    val reviews_count: Int,
    val total_rating: Int,
    val total_users: Int,
    val used_hours: Int
)

data class ShowBookingsResponse(
    val bookings: List<Booking>,
    val count: Int
)

data class Booking(
    val address: String,
    val apartment_no: String,
    val app_commission: Double,
    val avg_rating: Int,
    val booking_date_time: String,
    val cancellation_charge: Any,
    val cancellation_reason: Any,
    val cancelled_at: Any,
    val cancelled_by: Any,
    val check_in: Any,
    val check_out: Any,
    val checkout_verification_code: Int,
    val city: Any,
    val code_expired_at: Any,
    val code_generated_at: String,
    val completed_at: Any,
    val created_at: String,
    val deleted_at: Any,
    val distance: Any,
    val duration: Int,
    val duration_name: String,
    val end_booking_date_time: String,
    val end_time: String,
    val hours: String,
    val id: Int,
    val is_paid: Int,
    val is_verified: Int,
    val landmark: String,
    val lat: Double,
    val lng: Double,
    val nanny_address: Any,
    val nanny_assigned_at: String,
    val nanny_first_name: String,
    val nanny_id: Int,
    val nanny_last_name: Any,
    val nanny_latitude: Any,
    val nanny_longitude: Any,
    val nanny_price: Double,
    val nanny_profile_image: Any,
    val needed_date_time: String,
    val no_of_kids: Int,
    val not_available_time: Any,
    val parent_first_name: String,
    val parent_id: Int,
    val parent_last_name: Any,
    val parent_profile_image: Any,
    val payment_method_id: String,
    val price: Int,
    val refund_amount: Any,
    val start_time: String,
    val status: Int,
    val tax: Int,
    val time_zone: String,
    val total_charge: Int,
    val total_service_time: Any,
    val updated_at: Any,
    val verification_code: Int,
    val voucher: String
)

data class BookingDetailResponse(
    val address: String,
    val apartment_no: String,
    val app_commission: Double,
    val avg_rating: Int,
    val booking_date: String,
    val booking_date_time: String,
    val can_chat: Int,
    val cancellation_charge: Any,
    val cancellation_reason: Any,
    val cancelled_at: Any,
    val cancelled_by: Any,
    val check_in: Any,
    val check_out: Any,
    val checkout_verification_code: Int,
    val city: Any,
    val code_expired_at: Any,
    val code_generated_at: String,
    val completed_at: Any,
    val created_at: String,
    val deleted_at: Any,
    val distance: Any,
    val duration: Int,
    val duration_name: String,
    val end_booking_date_time: String,
    val end_time: String,
    val hours: Double,
    val hours_left: String,
    val id: Int,
    val is_nanny_reviewed: Int,
    val is_paid: Int,
    val is_parent_reviewed: Int,
    val is_reviewed: Int,
    val is_verified: Int,
    val landmark: String,
    val lat: Double,
    val lng: Double,
    val nanny_about: String,
    val nanny_address: Any,
    val nanny_assigned_at: String,
    val nanny_avg_rating: Int,
    val nanny_first_name: String,
    val nanny_id: Int,
    val nanny_last_name: Any,
    val nanny_latitude: Any,
    val nanny_longitude: Any,
    val nanny_price: Double,
    val nanny_profile_image: String,
    val nanny_to_parent_rating: Any,
    val nanny_to_parent_review: Any,
    val needed: String,
    val needed_date_time: String,
    val no_of_kids: Int,
    val not_available_time: Any,
    val parent_about: Any,
    val parent_avg_rating: Int,
    val parent_first_name: String,
    val parent_id: Int,
    val parent_last_name: String,
    val parent_profile_image: Any,
    val parent_to_nanny_rating: Any,
    val parent_to_nanny_review: Any,
    val payment_method_id: String,
    val price: Int,
    val refund_amount: Any,
    val start_time: String,
    val status: Int,
    val tax: Int,
    val ten_hrs_cancel_booking_count: Int,
    val time_zone: String,
    val total: Int,
    val total_charge: Int,
    val total_service_time: String,
    val two_hrs_cancel_booking_count: Int,
    val updated_at: Any,
    val verification_code: Int,
    val voucher: String
)

data class NotificationResponse(
    val count: Int,
    val notifications: List<Notification>
)

data class Notification(
    val booking_id: Int,
    val created_at: String,
    val from_user_id: Int,
    val is_read: Int,
    val message: String,
    val sender_profile_image: Any,
    val time_since: String,
    val title: String,
    val type: Int
)

data class NannyAboutResponse(
    val nanny_data: NannyData
)

data class NannyData(
    val about: String,
    val avg_rating: Int,
    val first_name: String,
    val last_name: Any,
    val nanny_id: Int,
    val nanny_photos: List<NannyPhoto>,
    val profile_image: String
)

data class NannyPhoto(
    val image: String,
    val image_caption: String
)

data class GetCardsResponse(
    val cards: List<Card>
)

data class Card(
    val request_three_d_secure: String,
    val billing_details: BillingDetails,
    val card: CardX,
    val created: Int,
    val customer: String,
    val id: String,
    val livemode: Boolean,
    val metadata: Metadata,
    val `object`: String,
    val type: String
)

data class BillingDetails(
    val address: Address,
    val email: Any,
    val name: String,
    val phone: Any
)

data class CardX(
    val brand: String,
    val checks: Checks,
    val country: String,
    val exp_month: Int,
    val exp_year: Int,
    val fingerprint: String,
    val funding: String,
    val generated_from: Any,
    val last4: String,
    val networks: Networks,
    val three_d_secure_usage: ThreeDSecureUsage,
    val wallet: Any
)


data class Address(
    val city: Any,
    val country: Any,
    val line1: Any,
    val line2: Any,
    val postal_code: String,
    val state: Any
)

data class Checks(
    val address_line1_check: Any,
    val address_postal_code_check: String,
    val cvc_check: String
)

data class Networks(
    val available: List<String>,
    val preferred: Any
)

data class ThreeDSecureUsage(
    val supported: Boolean
)

data class GetFaqResponse(
    val faqs: List<Faq>
)

data class Faq(
    val answer: String,
    val created_at: String,
    val id: Int,
    val question: String,
    val updated_at: Any
)

data class Message(
    var id: Int,
    var message: String,
    var booking_id: Int,
    var local_identifier: String,
    var to_user_id: Int,
    var from_user_id: Int,
    var created_at: String,
    var token: String
)

data class ChatList(
    val count: Int,
    val messages: ArrayList<Message>
)

data class ChatListingResponse(
    val chats: List<Chat>,
    val count: Int
)

data class Chat(
    val booking_id: Int,
    val chat_id: Any,
    val conversation_id: String,
    val country_code: String,
    val created_at: String,
    val deleted_by: Any,
    val deleted_for_me: Int,
    val email: String,
    val first_name: String,
    var from_me: Int,
    val from_user_id: Int,
    val id: Int,
    val is_read: Int,
    val last_name: Any,
    val local_identifier: String,
    val media: Any,
    val message: String,
    val participant: String,
    val phone_number: Long,
    val profile_image: String,
    val sender_id: Int,
    val to_user_id: Int,
    val unread_count: Int
)

data class OneToOneChatData(
    val count: Int,
    val messages: List<Messages>
)

data class Messages(
    val booking_id: Int,
    val chat_id: Any,
    val conversation_id: String,
    val created_at: String,
    val deleted_by: Any,
    val deleted_for_me: Int,
    var from_me: Int,
    val from_user_email: String,
    val from_user_first_name: String,
    val from_user_id: Int,
    val from_user_last_name: String,
    val from_user_profile_image: String,
    val id: Int,
    val is_read: Int,
    val local_identifier: String,
    val media: Any,
    val message: String,
    val to_user_email: String,
    val to_user_first_name: String,
    val to_user_id: Int,
    val to_user_last_name: Any,
    val to_user_profile_image: String
)


data class NannyUploadImage(
    val image: String,
    val image_caption: String
)

data class GetNannyImagesResponse(
    val nanny_images: List<NannyImage>,
    val total_photos: Int
)

data class NannyImage(
    val id: Int,
    val image: String,
    val image_caption: String
)

data class PostDocumentsList(
    val doc_image: String,
    val doc_name: String,
    val doc_type: Int,
    val state: String
)

data class TermsConditionsResponse(
    val content: Content
)

data class Content(
    val content: String,
    val id: Int,
    val image: String,
    val title: String
)

data class WorkingHourList(
    val working_hours_type: Int,
    val working_hours: String
)

data class GetDocumentsResponse(
    val documents: List<Document>
)

data class Document(
    var doc_image: String,
    val doc_name: String,
    val doc_type: Int,
    val id: Int,
    val nanny_id: Int,
    val state: Any
)

data class GetWorkingHoursResponse(
    val working_hours: List<WorkingHour>
)

data class WorkingHour(
    val id: Int,
    val nanny_id: Int,
    val working_hours: String,
    val working_hours_type: Int
)