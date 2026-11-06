import ca.weblite.webview.swing.WebViewComponent;

import javax.swing.*;
import java.awt.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class MapLocationPicker extends JDialog {

    @FunctionalInterface
    public interface LocationConfirmed {
        void accept(double latitude, double longitude, String address);
    }

    private final LocationConfirmed callback;
    private boolean completed;
    private WebViewComponent webView;

    private MapLocationPicker(Window parent, LocationConfirmed callback) {
        super(parent, "Select Incident Location",
                Dialog.ModalityType.APPLICATION_MODAL);

        this.callback = callback;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(960, 690);
        setMinimumSize(new Dimension(760, 560));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        try {
            MapServer.ensureStarted();

            webView = WebViewComponent.create();
            webView.setPreferredSize(new Dimension(900, 650));

            webView.addJavascriptCallback("fsmsPickerConfirmed", raw -> {
                handlePickerConfirmation(raw);
            });

            add(webView, BorderLayout.CENTER);

            webView.setUrl(MapServer.getMapUrl() + "?mode=picker");

        } catch (Throwable ex) {
            ex.printStackTrace();
            add(errorPanel(
                    "The embedded map could not start.\n" + ex.getMessage()
            ), BorderLayout.CENTER);
        }
    }

    public static void open(Window parent, LocationConfirmed callback) {
        MapLocationPicker picker =
                new MapLocationPicker(parent, callback);

        picker.setVisible(true);
    }

    private void handlePickerConfirmation(String raw) {
    try {
        // WebView2 wraps callback arguments in a JSON message.
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile(
                    "\"args\"\\s*:\\s*\\[\\s*\"((?:\\\\.|[^\"\\\\])*)\""
                ).matcher(raw);

        if (!matcher.find()) {
            System.err.println("Could not extract picker callback arguments: " + raw);
            return;
        }

        String payload = matcher.group(1);

        String[] parts = payload.split("\\|", 3);

        if (parts.length != 3) {
            System.err.println("Invalid picker location data: " + payload);
            return;
        }

        double latitude = Double.parseDouble(parts[0]);
        double longitude = Double.parseDouble(parts[1]);

        String address = URLDecoder.decode(
                parts[2],
                StandardCharsets.UTF_8
        );

        if (!Double.isFinite(latitude)
                || !Double.isFinite(longitude)
                || latitude < -90 || latitude > 90
                || longitude < -180 || longitude > 180) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            if (completed || !isDisplayable()) return;

            completed = true;

            try {
                callback.accept(latitude, longitude, address);
            } finally {
                dispose();
            }
        });

    } catch (Exception ex) {
        ex.printStackTrace();
    }
}

    private JPanel errorPanel(String message) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UI.BG);

        JLabel label = new JLabel(
                "<html><div style='width:560px;text-align:center'>" +
                message.replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace("\n", "<br>") +
                "</div></html>",
                SwingConstants.CENTER
        );

        label.setForeground(UI.TEXT);
        panel.add(label, BorderLayout.CENTER);

        JButton close = UI.secondaryButton("Close");
        close.addActionListener(e -> dispose());

        JPanel south = new JPanel();
        south.setOpaque(false);
        south.add(close);

        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    @Override
    public void dispose() {
        if (webView != null) {
            webView.dispose();
            webView = null;
        }

        super.dispose();
    }
}
