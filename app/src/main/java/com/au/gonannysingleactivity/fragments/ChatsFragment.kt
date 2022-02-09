package com.au.gonannysingleactivity.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.adapters.ChatsAdapter
import com.au.gonannysingleactivity.objects.Constants.BASE_IMAGE_URL
import com.au.gonannysingleactivity.objects.Constants.PATH_FOR_SOCKET
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.ApplicationGlobal.Companion.fromNotification
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getFormattedTime
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.utils.PagingScrollListener
import com.au.gonannysingleactivity.webservices.*
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_chat.ivBack
import kotlinx.android.synthetic.main.fragment_show_booking_details.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ChatsFragment : BaseFragment(), View.OnClickListener {

    private var list: MutableList<Messages> = mutableListOf()

    private lateinit var socket: Socket
    private lateinit var chatsAdapter: ChatsAdapter
    private lateinit var lm: LinearLayoutManager
    private lateinit var calendar:Calendar

    private var page: Int = 0
    private var isLoading: Boolean = false
    private var notHaveData: Boolean = false

    private var bookingId: Int? = null
    private var nannyProfileImage:String = ""
    private var nannyName:String = ""
    private var nannyId: Int? = null

    private var sentFromHere:Boolean = false

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_chat
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fromNotification = false
        requireArguments().apply {
            bookingId = getInt("booking_id")
            nannyId = getInt("nanny_id")
            nannyProfileImage = getString("nanny_profile_image","")
            nannyName = getString("nanny_name","")
        }
        try {
            socket = IO.socket(PATH_FOR_SOCKET)
            socket.connect()
        } catch (e: Exception) {
        }

        socket.on("msgToServer", onMessageReceived)

        val chat = Message(
            0,
            "",
            bookingId!!,
            System.currentTimeMillis().toString(),
            nannyId!!,
            ApplicationGlobal.userObject!!.id,
            getFormattedTime(System.currentTimeMillis()),
            ApplicationGlobal.accessToken
        )

        socket.emit("init", JSONObject(Gson().toJson(chat)))

        lm = LinearLayoutManager(requireContext())
        lm.stackFromEnd = true
        chatsAdapter = ChatsAdapter(list)
        calendar = Calendar.getInstance()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSend.setOnClickListener(this)
        ivBack.setOnClickListener(this)

        Glide.with(requireContext()).load("$BASE_IMAGE_URL$nannyProfileImage").apply(
            RequestOptions.circleCropTransform()
        ).into(ivProfile)
        tvName.text = nannyName

        rvChat.apply {
            layoutManager = lm
            addOnScrollListener(object : PagingScrollListener(lm) {

                override fun isLoading(): Boolean {
                    return isLoading
                }

                override fun loadMore() {
                    isLoading = true
                    page++
                    loadChats()
                }

                override fun notHaveData(): Boolean {
                    return notHaveData
                }
            })
            adapter = chatsAdapter
        }
        loadChats()
    }

    override fun onPause() {
        super.onPause()
        rvChat.clearOnScrollListeners()
    }

    private fun loadChats() {
        showProgressBar(requireContext())
        RetrofitInstance.create().getMessageList(bookingId!!, page)
            .enqueue(object : Callback<OneToOneChatData> {

                override fun onFailure(call: Call<OneToOneChatData>, t: Throwable) {
                    hideProgressBar()
                    t.message!!.showToast(requireContext())
                }

                override fun onResponse(
                    call: Call<OneToOneChatData>,
                    response: Response<OneToOneChatData>
                ) {
                    response.apply {
                        if (isSuccessful) {
                            notHaveData = body()!!.count < 10
                            if (body()!!.count > 0) {
                                val startSize = list.count()
                                list.addAll(body()!!.messages)
                                chatsAdapter.notifyItemRangeInserted(startSize, list.count())
                                lm.scrollToPosition(list.count()-1)
                                hideProgressBar()
                            }
                            hideProgressBar()
                        } else {
                            hideProgressBar()
                            getErrorMessage(errorBody()!!, requireContext(), code())
                        }
                    }
                }

            })
    }

    private val onMessageReceived: Emitter.Listener = Emitter.Listener {
        requireActivity().runOnUiThread {

            val chat: Messages = Gson().fromJson(it[0].toString(), Messages::class.java)

            val count = list.count()

            if(sentFromHere) {
                chat.from_me = 1
                sentFromHere = false
            } else {
                chat.from_me = 0
            }
            list.add(chat)
            chatsAdapter.notifyItemInserted(count)
            rvChat.scrollToPosition(count)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSend -> {
                val text = etChat.text.toString().trim()
                if (text.isNotBlank()) {
                    val chat = Message(
                        0,
                        text,
                        bookingId!!,
                        System.currentTimeMillis().toString(),
                        nannyId!!,
                        ApplicationGlobal.userObject!!.id,
                        getFormattedTime(System.currentTimeMillis()),
                        ApplicationGlobal.accessToken
                    )
                    sentFromHere = true
                    socket.emit("msgToServer",JSONObject(Gson().toJson(chat)))
                    etChat.setText("")
                }
            }
            R.id.ivBack->requireActivity().onBackPressed()
        }
    }

    companion object{
        fun newInstance(bookingId:Int,nannyId:Int,nannyImage:String,nannyName:String) = ChatsFragment().apply {
            arguments = Bundle().apply {
                putInt("booking_id",bookingId)
                putInt("nanny_id",nannyId)
                putString("nanny_profile_image",nannyImage)
                putString("nanny_name",nannyName)
            }
        }
    }
}