import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.connector.source.ScanTableSource;
import org.apache.flink.table.connector.source.SourceFunctionProvider;

import java.util.List;

public class ImapTableSource implements ScanTableSource {
    private final ImapSourceOptions options;
    private final List<String> columnNames;

    public ImapTableSource(
        ImapSourceOptions options,
        List<String> columnNames
    ) {
        this.options = options;
        this.columnNames = columnNames;
    }


    @Override
    public ChangelogMode getChangelogMode() {
        return ChangelogMode.insertOnly();
    }

    @Override
    public ScanRuntimeProvider getScanRuntimeProvider(ScanContext ctx) {
        boolean bounded = false;
        final ImapSource source = new ImapSource(options, columnNames);
        return SourceFunctionProvider.of(source, bounded);
    }

    @Override
    public DynamicTableSource copy() {
        return new ImapTableSource(options, columnNames);
    }

    @Override
    public String asSummaryString() {
        return "IMAP Table Source";
    }
}
