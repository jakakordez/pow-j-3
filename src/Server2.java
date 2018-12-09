import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Server2 {
    public Server2(){
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8500), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpContext context = server.createContext("/");
        context.setHandler(new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String response = "Hi there!";
                exchange.sendResponseHeaders(200, response.getBytes().length);//response code and length
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        });
        server.start();

    }
}
