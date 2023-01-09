package com.develmagic.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MsTeamsAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private static final String APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String METHOD_POST = "POST";
    private static final String ENV_ENABLED_FLAG  = "MSTEAMS_APPENDER_ENABLED";
    private static final String ENV_MSTEAMS_WEBHOOK_URL  = "MSTEAMS_APPENDER_WEBHOOK_URL";

    private static Layout<ILoggingEvent> defaultLayout =
            new LayoutBase<ILoggingEvent>() {
                public String doLayout(ILoggingEvent event) {
                    final StringBuffer sbuf = new StringBuffer();
                    final IThrowableProxy throwbleProxy =  event.getThrowableProxy();
                    if (throwbleProxy != null) {
                        sbuf.append("<br><pre>");
                        String throwableStr = ThrowableProxyUtil.asString(throwbleProxy);
                        sbuf.append(throwableStr.replace("\t", "   "));
                        sbuf.append(CoreConstants.LINE_SEPARATOR);
                        sbuf.append("</pre>");
                    }
                    return "**"
                            + event.getLoggerName()
                            + "** <br>"
                            + event.getFormattedMessage().replaceAll("\n", "\n\t")
                            + sbuf;
                }
            };

    private ObjectMapper objectMapper = new ObjectMapper();
    private String appName;
    private String webhookUri;
    private int timeout = 30_000;
    private Layout<ILoggingEvent> layout = defaultLayout;
    private boolean appenderConfigured = false;

    public MsTeamsAppender() {
        if (System.getenv(ENV_ENABLED_FLAG) == null || System.getenv(ENV_ENABLED_FLAG).equals("0")) {
            System.out.println("Logback greater MSTeams appender not enabled");
            return;
        }
        if (getWebhookUri() == null) {
            System.err.println("Logback greater MSTeams appender enabled, but no webhooh URI set.");
            System.err.println("Set it as environmental variable : MSTEAMS_APPENDER_WEBHOOK_URL");
            return;
        }
        webhookUri = getWebhookUri();
        appenderConfigured = true;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    @Override
    protected void append(final ILoggingEvent event) {
        if (!appenderConfigured) {
            return;
        }

        try {
            String[] parts = layout.doLayout(event).split("\n", 1);

            MsTeamsCard msTeamsCard = new MsTeamsCard();
            msTeamsCard.setContext("http://schema.org/extensions");
            msTeamsCard.setType("MessageCard");

            msTeamsCard.setThemeColor(decodeColor(event));
            msTeamsCard.setTitle(appName + " - " + event.getLevel().toString());
            msTeamsCard.setText(parts[0]);

            final byte[] bytes = objectMapper.writeValueAsBytes(msTeamsCard);

            postMessage(webhookUri, bytes);
        } catch (Exception ex) {
            ex.printStackTrace();
            addError("Error posting log to MS Teams: " + event, ex);
        }
    }

    private String decodeColor(ILoggingEvent evt) {
        switch (evt.getLevel().toString()) {
            case "INFO":
                return "0B6623"; // Forest Green
            case "WARN":
                return "F9A602"; // Gold
            case "ERROR":
                return "FF0800"; // Candy Apple Red
            default:
                return "0080FF"; // Azure Blue
        }
    }

    private void postMessage(String uri, byte[] bytes) throws IOException {
        final HttpURLConnection conn = (HttpURLConnection) new URL(uri).openConnection();
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        conn.setDoOutput(true);
        conn.setRequestMethod(METHOD_POST);

        conn.setFixedLengthStreamingMode(bytes.length);
        conn.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);

        final OutputStream os = conn.getOutputStream();
        os.write(bytes);

        os.flush();
        os.close();
    }


    private String getWebhookUri() {
        return System.getenv(ENV_MSTEAMS_WEBHOOK_URL);
    }
}