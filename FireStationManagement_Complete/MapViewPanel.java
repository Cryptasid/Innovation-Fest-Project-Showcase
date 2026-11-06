
import ca.weblite.webview.swing.WebViewComponent;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntConsumer;

/** Embeds the live database-backed incident map in the Swing dashboard. */
public class MapViewPanel extends JPanel {

    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final IntConsumer dispatchAction;

    private WebViewComponent webView;
    private volatile boolean pageReady;
    private final AtomicBoolean checkingReady = new AtomicBoolean(false);

    public MapViewPanel(IntConsumer dispatchAction) {
        this.dispatchAction = dispatchAction;

        setLayout(new BorderLayout());
        setBackground(UI.CARD);
        setBorder(BorderFactory.createLineBorder(UI.BORDER));

        try {
            MapServer.ensureStarted();

            webView = WebViewComponent.create();
            webView.setPreferredSize(new Dimension(900, 650));

            webView.addJavascriptCallback("fsmsDispatchIncident", raw -> {
            try {
                java.util.regex.Matcher matcher =
                        java.util.regex.Pattern.compile(
                            "\"args\"\\s*:\\s*\\[\\s*\"?(\\d+)\"?"
                        ).matcher(raw);

                if (!matcher.find()) {
                    System.err.println(
                            "Could not extract incident ID from callback: " + raw
                    );
                    return;
                }

                int incidentId = Integer.parseInt(matcher.group(1));

                SwingUtilities.invokeLater(() -> {
                    this.dispatchAction.accept(incidentId);
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                }
             });

            add(webView, BorderLayout.CENTER);

            webView.setUrl(MapServer.getMapUrl() + "?mode=view");

            // Wait until the map page has initialized before loading incidents.
            Timer readyTimer = new Timer(500, e -> checkPageReady());
            readyTimer.start();

        } catch (Throwable ex) {
            ex.printStackTrace();
            add(errorPanel(
                    "The embedded map could not start.\n" + ex.getMessage()
            ), BorderLayout.CENTER);
        }
    }

   

private void checkPageReady() {

    if (pageReady || webView == null) return;
    if (!checkingReady.compareAndSet(false, true)) return;

    webView.evalAsync(
            "return typeof window.renderIncidents === 'function';"
    ).whenComplete((result, error) -> {
        checkingReady.set(false);

        if (error != null) {
            System.err.println("MAP READY CHECK ERROR: " + error.getMessage());
            return;
        }

        System.out.println("MAP READY CHECK: " + result);

        if (result == null) return;

        if ("true".equalsIgnoreCase(result.trim())) {
            SwingUtilities.invokeLater(() -> {
                if (!pageReady) {
                    System.out.println("MAP INITIALIZED SUCCESSFULLY");
                    pageReady = true;
                    refreshIncidents();
                }
            });
        }
    });
}


    public void refreshIncidents() {
        if (!pageReady || webView == null) return;

        new SwingWorker<List<Incident>, Void>() {

            @Override
            protected List<Incident> doInBackground() throws SQLException {
                return incidentDAO.getAllGeolocated();
            }

            @Override
            protected void done() {
                try {
                    List<Incident> incidents = get();

                    System.out.println("INCIDENTS RETRIEVED FROM DATABASE: " + incidents.size());

                    String json = toJson(incidents);

                    System.out.println("INCIDENT MAP JSON: " + json);

                    if (webView != null && pageReady) {
                        webView.eval("renderIncidents(" + json + ");");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();

                    JOptionPane.showMessageDialog(
                            MapViewPanel.this,
                            "Unable to load incident coordinates from MySQL.\n"
                                    + ex.getMessage(),
                            "Incident Map",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private String toJson(List<Incident> incidents) {
        StringBuilder json = new StringBuilder("[");

        for (Incident incident : incidents) {
            if (!incident.hasCoordinates()) continue;

            if (json.length() > 1) json.append(',');

            json.append('{')
                    .append("\"id\":").append(incident.getId()).append(',')
                    .append("\"threat\":\"")
                    .append(jsonEscape(incident.getThreat())).append("\",")
                    .append("\"severity\":\"")
                    .append(jsonEscape(incident.getSeverity().toString())).append("\",")
                    .append("\"street\":\"")
                    .append(jsonEscape(incident.getStreet())).append("\",")
                    .append("\"city\":\"")
                    .append(jsonEscape(incident.getCity())).append("\",")
                    .append("\"status\":\"")
                    .append(jsonEscape(incident.getStatus().toString())).append("\",")
                    .append("\"date\":\"")
                    .append(jsonEscape(incident.getIncidentDate().toString())).append("\",")
                    .append("\"latitude\":").append(incident.getLatitude()).append(',')
                    .append("\"longitude\":").append(incident.getLongitude())
                    .append('}');
        }

        return json.append(']').toString();
    }

    private String jsonEscape(String value) {
        if (value == null) return "";

        StringBuilder out = new StringBuilder(value.length() + 16);

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }

        return out.toString();
    }

    private JPanel errorPanel(String message) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UI.CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JLabel label = new JLabel(
                "<html><div style='width:620px'>" +
                message.replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace("\n", "<br>") +
                "</div></html>"
        );

        label.setForeground(UI.TEXT);
        panel.add(label, BorderLayout.CENTER);

        return panel;
    }
}
