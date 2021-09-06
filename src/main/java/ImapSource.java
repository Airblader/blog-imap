import com.sun.mail.imap.IMAPFolder;
import jakarta.mail.*;
import jakarta.mail.event.MessageCountAdapter;
import jakarta.mail.event.MessageCountEvent;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.data.TimestampData;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ImapSource extends RichSourceFunction<RowData> {
    private final ImapSourceOptions options;
    private final List<String> columnNames;

    private transient Store store;
    private transient IMAPFolder folder;

    private transient volatile boolean running = false;

    public ImapSource(
        ImapSourceOptions options,
        List<String> columnNames
    ) {
        this.options = options;
        this.columnNames = columnNames.stream()
            .map(String::toUpperCase)
            .collect(Collectors.toList());
    }

    @Override
    public void run(SourceFunction.SourceContext<RowData> ctx) throws Exception {
        connect();
        running = true;

        folder.addMessageCountListener(new MessageCountAdapter() {
            @Override
            public void messagesAdded(MessageCountEvent e) {
                collectMessages(ctx, e.getMessages());
            }
        });

        while (running) {
            // Trigger some IMAP request to force the server to send a notification
            folder.getMessageCount();
            Thread.sleep(250);
        }
    }

    @Override
    public void cancel() {
        running = false;
    }

    @Override
    public void close() throws Exception {
        if (folder != null) {
            folder.close();
        }

        if (store != null) {
            store.close();
        }
    }

    private void collectMessages(SourceFunction.SourceContext<RowData> ctx, Message[] messages) {
        for (Message message : messages) {
            try {
                collectMessage(ctx, message);
            } catch (MessagingException ignored) {}
        }
    }

    private void collectMessage(SourceFunction.SourceContext<RowData> ctx, Message message)
        throws MessagingException {
        final GenericRowData row = new GenericRowData(columnNames.size());

        for (int i = 0; i < columnNames.size(); i++) {
            switch (columnNames.get(i)) {
                case "SUBJECT":
                    row.setField(i, StringData.fromString(message.getSubject()));
                    break;
                case "SENT":
                    row.setField(i, TimestampData.fromInstant(message.getSentDate().toInstant()));
                    break;
                case "RECEIVED":
                    row.setField(i, TimestampData.fromInstant(message.getReceivedDate().toInstant()));
                    break;
                // ...
            }
        }

        ctx.collect(row);
    }

    private void connect() throws Exception {
        final Session session = Session.getInstance(getSessionProperties(), null);
        store = session.getStore();
        store.connect(options.getUser(), options.getPassword());

        final Folder genericFolder = store.getFolder("INBOX");
        folder = (IMAPFolder) genericFolder;

        if (!folder.isOpen()) {
            folder.open(Folder.READ_ONLY);
        }
    }

    private Properties getSessionProperties() {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imap");
        props.put("mail.imap.auth", true);
        props.put("mail.imap.host", options.getHost());
        if (options.getPort() != null) {
            props.put("mail.imap.port", options.getPort());
        }

        return props;
    }
}
