import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.polar.androidblesdk.Backend
import com.polar.androidblesdk.DiabActivity
import okhttp3.*

class WsBack(private val url: String, private val context: Context, val email: String) : WebSocketListener() {
    private lateinit var webSocket: WebSocket

    fun start() {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, this)
    }

    fun sendMessage(text: String) {
        webSocket.send(text)
    }

    fun disconnect() {
        webSocket.close(1000, null)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        println("Connessione WebSocket aperta")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        println("Messaggio ricevuto: $text")
        // Visualizza il messaggio nel tuo UI

        val handler = Handler(Looper.getMainLooper())

        handler.post { Toast.makeText(context, text, Toast.LENGTH_SHORT).show() }
        val req = "{ \"notifica\": \"$text\", \"emailAddress\": \"$email\" }"
        do_post("http://192.168.0.105:3000/aggiungiNotifica", req)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        println("Connessione WebSocket chiusa")
        webSocket.close(NORMAL_CLOSURE_STATUS, null)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        println("Errore nella connessione WebSocket: ${t.message}")
    }

    companion object {
        private const val NORMAL_CLOSURE_STATUS = 1000
    }

    fun do_post(url: String, text: String) {
        val conn = Backend()
        Thread { conn.post_request(url, text) }.start()
    }
}