package com.example.rotravel

import android.annotation.SuppressLint
import android.app.Activity
import android.media.ExifInterface
import android.net.Uri
import android.os.CountDownTimer
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.VideoView
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.io.File
import java.io.Serializable

@SuppressLint("StaticFieldLeak")
object NearbyCommunication {
    private lateinit var connectionLifecycleCallback : ConnectionLifecycleCallback
    private lateinit var endpointDiscoveryCallback : EndpointDiscoveryCallback
    private lateinit var payloadCallback : PayloadCallback
    lateinit var mConnClient : ConnectionsClient
    lateinit var countDownTimer : CountDownTimer
    private var discover = false
    private var connInitiated = false
    private var ENDPOINT = ""
    var transferBackEndpoint = ""
    private var ready = false
    private var nrPhotos = 0
    var waitForEndpoints = -1
    private lateinit var transferBackPayload : Payload
    private var transferBackId : Long = -1
    private var payloads : HashMap<Long, Payload> = HashMap()
    //from RoTravel
    private lateinit var createdVideo : VideoView
    private var videoImgs : Array<Uri> = arrayOf<Uri>()
    private var toSendPayload : Array<Payload> = arrayOf()
    private var paths : Array<String> = arrayOf<String>()
    var endpointsMap : HashMap<String, Array<Payload>> = HashMap()
    var endpointsMutex : HashMap<String, Mutex> = HashMap()
    var transferBack : HashMap<String, PayloadDetails> = HashMap()
    val mutex : Mutex = Mutex()
    val TIMEOUT_DISCOVERY_MILLIS: Long = 15000
    val SECOND_MILLIS: Long = 1000
    var timedOut = false
    var initiated = false
    init {
//        initPayloadCallback()
//        advCallback()
//        discCallback()
//        startAdvertising()

    }
    fun doInit(act : Activity) {
        Log.i("doInit", "sper ca am facut call la init")
        mConnClient = Nearby.getConnectionsClient(act)
        initPayloadCallback()
        advCallback()
        discCallback()
        startAdvertising()
        //mutex.lock()

    }
    private fun startAdvertising() {
        var advOpt = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        mConnClient
            .startAdvertising("NearbyCommunication", "com.example.testnearby", connectionLifecycleCallback, advOpt)
            .addOnSuccessListener { Log.i("onSuccessListener", "yay I'm advertising") }
            .addOnFailureListener { Log.i("onFailureListener", "oops Advertising didn't work")}
    }

    private fun startDiscovery() {
        var descOpt = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        mConnClient
            .startDiscovery("com.example.testnearby", endpointDiscoveryCallback, descOpt)
            .addOnSuccessListener { Log.i("onSuccessListener", "yay I'm discoverying")}
            .addOnFailureListener { Log.i("onFailureListener", "oops Discovery didn't work")}
    }

    private fun advCallback(){
        connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                mConnClient.acceptConnection(endpointId, payloadCallback);
            }

            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                when (result.getStatus().getStatusCode()) {
                    ConnectionsStatusCodes.STATUS_OK ->
                        // We're connected! Can now start sending and receiving data.
                    {
                        initiated = true
                        Log.i("OnConnectionResult", "connection accepted")
                        if (discover) {
                            ENDPOINT = endpointId
                            endpointsMap[endpointId] = arrayOf<Payload>()
                            //selectMultiple(endpointId)
//                            mConnClient.sendPayload(
//                                endpointId,
//                                Payload.fromBytes(toSendPayload.size.toString().toByteArray())
//                            )
                        }
                    }
                    ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED ->
                        // The connection was rejected by one or both sides
                        Log.i("OnConnectionResult", "connection rejected")
                    ConnectionsStatusCodes.STATUS_ERROR ->
                        // The connection broke before it was able to be accepted.
                        Log.i("OnConnectionResult", "connection err")
                    else ->
                        // Unknown status code
                        Log.i("OnConnectionResult", "unknown status")
                }
            }

            override fun onDisconnected(p0: String) {
            }
        }
    }

    private fun discCallback() {
        endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId : String, info : DiscoveredEndpointInfo)
            {
                // An endpoint was found. We request a connection to it.
                mConnClient
                    .requestConnection("NearbyCommunication", endpointId, connectionLifecycleCallback)
                    .addOnSuccessListener {
                        Log.i("onEndpointFound", "found possible conn") }
                    .addOnFailureListener {Log.i("onEndpointFound", "failed")}
            }

            override fun onEndpointLost(endpointId: String)
            {
                // A previously discovered endpoint has gone away.
            }
        }
    }

    private fun initPayloadCallback() {
        payloadCallback = object : PayloadCallback() {
            private var photoReady = false
            private var count = 0
            private var idx = 0
            override fun onPayloadReceived(endpointId : String, payload : Payload) {
                // This always gets the full data of the payload. Will be null if it's not a BYTES
                // payload. You can check the payload type with payload.getType().
//                var receivedBytes : ByteArray? = payload.asBytes()
//                text.text = String(receivedBytes!!)
                if(!discover) {
                    Log.i("onPayloadReceived", "count " + count + "  nr photos " + nrPhotos)
                    Log.i("onPayloadReceived", "file type " + payload.type)

                    if(payload.type == Payload.Type.FILE && count > 0) {
                        payloads[payload.id] = payload
                        count--
//                        var payloadFile : File? = payload.asFile()!!.asJavaFile()
//                        //payloadFile?.renameTo(File(payloadFile?.parent, endpointId + count))
//                        videoImgs[count] = Uri.fromFile(payloadFile)
//                        if(count == nrPhotos - 1)
//                            initFfmpeg()
                        Log.i("PayloadRceived", payload.id.toString())
                    }
                    if(nrPhotos == 0) {
                        var receivedBytes: ByteArray? = payload.asBytes()
                        nrPhotos = String(receivedBytes!!).toInt()
                        count = nrPhotos
                        Log.i("PayloadRceived", nrPhotos.toString())
                        mConnClient.sendPayload(
                            endpointId,
                            Payload.fromBytes("ready".toByteArray())
                        )
                        ready = true
                    }

                } else {
                    if(payload.type == Payload.Type.FILE) {
                        endpointsMutex[endpointId] = Mutex()
                        Log.i("ReceivePayload", "am primit de la " + endpointId + " payloadul " + payload.id)
                        transferBack[endpointId] = PayloadDetails(payload, payload.id)
//                        transferBackPayload = payload
//                        transferBackId = payload.id
                    } else {
                        var receivedBytes : ByteArray? = payload.asBytes()
                        Log.i("PayloadRceived", String(receivedBytes!!))
                        if(String(receivedBytes!!) == "ready")
                            sendPayload(endpointId)

                    }
                }
            }

            override fun onPayloadTransferUpdate(endpointId : String, update : PayloadTransferUpdate) {
                // Bytes payloads are sent as a single chunk, so you'll receive a SUCCESS update immediately
                // after the call to onPayloadReceived().
                Log.i("onPayloadTransferUpdate", update.status.toString())
                if(!discover && update.status == PayloadTransferUpdate.Status.SUCCESS) {
                    var payload = payloads[update.payloadId]
                    Log.i("onPayloadTransferUpdate", "dif sau nu de null " + (payload != null).toString())
                    if(payload != null) {
                        payloads.remove(update.payloadId)
                        var payloadFile = payload?.asFile()!!.asJavaFile()
                        var dest = File("/storage/emulated/0/Download", endpointId + "_" + idx + ".jpeg")
                        payloadFile?.renameTo(dest)
                        Log.i("onPayloadTransferUpdate", dest!!.absolutePath)
                        videoImgs += Uri.fromFile(dest)
                        idx++
                        if (payloads.isEmpty() && count == 0) {
                            Log.i("onPayloadTransferUpdate", "start processing")
                            GlobalScope.launch(Dispatchers.IO) {
                                Log.i("LaunchFfmpeg", "running on ${Thread.currentThread().name}")
                                initFfmpeg(endpointId)}

                        }
                    }
                }

                if(discover && update.status == PayloadTransferUpdate.Status.SUCCESS) {
                    if(transferBack[endpointId] != null) {
                        if (update.payloadId == transferBack[endpointId]!!.payloadId) {
                            transferBackPayload = transferBack[endpointId]!!.payload
                            if (transferBackPayload != null) {
                                var payloadFile = transferBackPayload?.asFile()!!.asJavaFile()
                                //                            transferBackEndpoint = endpointId
                                //                            mutex.unlock()
                                Log.i("onPayloadUpdate", transferBack.size.toString())
                                Log.i("onPayloadUpdate", "o sa scot " + endpointId + " payload " + update.payloadId)
                                Log.i("onPayloadUpdate", "am ramas cu " + transferBack.size.toString())
                                transferBack.remove(endpointId)
                                waitForEndpoints--
                                if (waitForEndpoints == 0) {
                                    mutex.unlock()
                                }
                                //endpointsMutex[endpointId]!!.unlock()

                                var dest = File("/storage/emulated/0/Download", "$endpointId.mp4")
                                payloadFile?.renameTo(dest)
                                Log.i("onPayloadTransferUpdate", dest!!.absolutePath)
                                //TODO: check if it works
                                mConnClient.stopDiscovery()
                                startAdvertising()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun sendPayload(endpointId : String) {
        //selectMultiple()
        Log.i("createAndSendPayload", "am intrat")
//        for (filePayload in toSendPayload) {
//            mConnClient.sendPayload(endpointId, filePayload)
//
//        }
        for (filePayload in endpointsMap[endpointId]!!) {
            mConnClient.sendPayload(endpointId, filePayload)

        }

        Log.i("createAndSendPayload", "am iesit")
    }

    fun doDiscover() {
        Log.i("doDiscover", "running on ${Thread.currentThread().name}")
        //toSendPayload = imgs

        mConnClient.stopAdvertising()
        discover = true
        startDiscovery()
    }

    private fun initFfmpeg(endpointId: String) {
        transpose(endpointId)
        val command = createCommand()
        var rc = FFmpeg.execute(command)
        if (rc == Config.RETURN_CODE_SUCCESS) {
            Log.i("initFfmpeg", "Command execution completed successfully.")
//            createdVideo.setVideoPath("/storage/emulated/0/TestVideos/output.mp4")
//            createdVideo.requestFocus()
//            createdVideo.start()
            var filePayload = Payload.fromFile(File("/storage/emulated/0/TestVideos/output.mp4"))
            mConnClient.sendPayload(endpointId, filePayload)
        } else if (rc == Config.RETURN_CODE_CANCEL) {
            Log.i("initFfmpeg", "Command execution cancelled by user.");
        } else {
            Log.i("initFfmpeg", String.format("Command execution failed with rc=%d and the output below.", rc));
            //Config.printLastCommandOutput(Log.INFO);
        }

    }

    private fun getOrientation(pathToImage : String) : String? {
        val exif = ExifInterface(pathToImage)
        var attr = exif.getAttribute(ExifInterface.TAG_ORIENTATION)
        if(attr != null) {
            Log.i("orientation", attr)
        }
        return attr
    }
    private fun pathForFile(endpointId: String, idx : Int) : String {
        return "/storage/emulated/0/Download/" + endpointId + "_" + idx + ".jpeg"
    }
    private fun transpose(endpointId: String) {
        var test : File = File("/storage/emulated/0/Rotated");
        if (!test.exists()) {
            test.mkdirs()
        }
        for(i in 0..videoImgs.size - 1) {
            //val path = getPath(this, videoImgs[i])
            val path = pathForFile(endpointId, i)
            paths += path
            if(getOrientation(path) == "6") {
                var comm = arrayOf<String>()
                comm += "-i"
                comm += path
                comm += "-vf"
                //scale=4640:3472,
                //val partial = "scale=4640:3472:force_original_aspect_ratio=decrease,pad=(ow-iw)/2:(oh-ih)/2"
                comm += ("transpose=dir=1")
                comm += "-y"
                comm += ("/storage/emulated/0/Rotated/output$i.jpeg")
                var rc = FFmpeg.execute(comm)
                if (rc == Config.RETURN_CODE_SUCCESS) {
                    Log.i("transpose", "Command execution completed successfully when transposing.")
                    videoImgs[i] = Uri.fromFile(File("/storage/emulated/0/Rotated/output$i.jpeg"))
                    paths[i] = "/storage/emulated/0/Rotated/output$i.jpeg"
                }
            }
        }
    }

    private fun createCommand() : Array<String>{
        var comm = arrayOf<String>()
        val count = videoImgs.size
        var idx = 1;
        var param = ""
        var path = ""
        comm += "-noautorotate"
        for (i in 0..count - 1) {
            comm += "-framerate"
            comm += "25"
            comm += "-t"
            comm += "2"
            comm += "-loop"
            comm += "1"
            path = paths[i]  //getPath(this, videoImgs[i])
            comm += "-i"
            comm += path
            param += "[" + i + "]"

        }
        var test : File = File("/storage/emulated/0/TestVideos");
        if (!test.exists()) {
            if (test.mkdirs())
                Log.e("FILE", "MADE IT!!")
            else
                Log.e("FILE", "DIDNT MAKE IT!!")
        }

        param +=  "concat=n=" + count + ":v=1,format=yuv420p[v]"
        comm += "-filter_complex"
        comm +=  param
        comm += "-map"
        comm += "[v]"
        comm += "-y"
        comm += "/storage/emulated/0/TestVideos/output.mp4"
        return comm
    }

    data class PayloadDetails(val payload : Payload, val payloadId: Long) : Serializable {}

}