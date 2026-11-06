import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

/** Serves the embedded map page from the same localhost origin allowed by the Maps key. */
public final class MapServer {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8765;
    private static final String MAP_KEY_PLACEHOLDER =
            "window.FSMS_GOOGLE_MAPS_API_KEY = 'AIzaSyBB6i13SDb8bXIsVxRMV1I1wfLv5l2gndA';\n";
    private static HttpServer server;
    private static Path projectRoot;

    private MapServer() { }

    public static synchronized void ensureStarted() throws IOException {
        if (server != null) return;
        projectRoot = resolveProjectRoot();
        HttpServer created = HttpServer.create(new InetSocketAddress(HOST, PORT), 0);
        created.createContext("/map.html", exchange -> serveFile(exchange, projectRoot.resolve("map.html"), "text/html; charset=utf-8"));
        created.createContext("/google-maps-config.js", MapServer::serveConfig);
        created.setExecutor(Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "fsms-map-http");
            thread.setDaemon(true);
            return thread;
        }));
        created.start();
        server = created;
        Runtime.getRuntime().addShutdownHook(new Thread(MapServer::stop, "fsms-map-server-shutdown"));
    }

    public static String getMapUrl() {
        return "http://127.0.0.1:" + PORT + "/map.html";
    }

    private static Path resolveProjectRoot() throws IOException {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        if (Files.isRegularFile(current.resolve("map.html"))) return current;

        try {
            Path codeLocation = Paths.get(MapServer.class.getProtectionDomain().getCodeSource()
                    .getLocation().toURI()).toAbsolutePath().normalize();
            if (Files.isDirectory(codeLocation) && Files.isRegularFile(codeLocation.resolve("map.html"))) {
                return codeLocation;
            }
            Path parent = codeLocation.getParent();
            if (parent != null && Files.isRegularFile(parent.resolve("map.html"))) return parent;
        } catch (Exception ignored) {
            // The current working directory is the documented VS Code launch location.
        }
        throw new IOException("map.html was not found. Open the FireStationManagement project folder in VS Code and run MainForm from the project root.");
    }

    private static void serveConfig(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "text/plain; charset=utf-8", "Method not allowed");
            return;
        }
        Path localConfig = projectRoot.resolve("config").resolve("google-maps-config.js");
        String contents = Files.isRegularFile(localConfig)
                ? Files.readString(localConfig, StandardCharsets.UTF_8)
                : MAP_KEY_PLACEHOLDER;
        exchange.getResponseHeaders().set("Cache-Control", "no-store, max-age=0");
        send(exchange, 200, "application/javascript; charset=utf-8", contents);
    }

    private static void serveFile(HttpExchange exchange, Path file, String contentType) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "text/plain; charset=utf-8", "Method not allowed");
            return;
        }
        if (!Files.isRegularFile(file)) {
            send(exchange, 404, "text/plain; charset=utf-8", "Map page not found: " + file.getFileName());
            return;
        }
        exchange.getResponseHeaders().set("Cache-Control", "no-store, max-age=0");
        send(exchange, 200, contentType, Files.readString(file, StandardCharsets.UTF_8));
    }

    private static void send(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    public static synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }
}
