import com.squareup.okhttp.Response;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.Map;

public class Server extends NanoHTTPD {
WalletE we = new WalletE();
    public Server() throws IOException {
        super("0.0.0.0", 8080);

        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
    }

    boolean relay = true;
    long alarmTime = 1844326736;

    @Override
    public Response serve(IHTTPSession session) {
        relay = (System.currentTimeMillis()/1000) > alarmTime;
        if(session.getUri().startsWith("/relay")){
            System.out.println((System.currentTimeMillis()/1000) + "Relay "+relay);
            return newFixedLengthResponse((relay?"on":"aff")+"\n");
        }
        else if(session.getUri().startsWith("/button")){
            System.out.println("Button clicked");
            new Thread(() -> {
                we.Snooze();
            }).start();

            alarmTime = (System.currentTimeMillis()/1000)+30;
            return newFixedLengthResponse("ok\n");
        }
        else if(session.getUri().startsWith("/time")){
            String[] split = session.getUri().split("/");
            if(split.length > 2){
                alarmTime = Integer.parseInt(split[2]);
            }
            return newFixedLengthResponse((alarmTime)+"\n");
        }
        else return newFixedLengthResponse("error\n");
    }
}