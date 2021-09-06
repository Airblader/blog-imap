import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.table.catalog.Column;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.factories.DynamicTableSourceFactory;
import org.apache.flink.table.factories.FactoryUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class ImapTableSourceFactory implements DynamicTableSourceFactory {

    public static final ConfigOption<String> HOST = ConfigOptions.key("host").stringType().noDefaultValue();
    public static final ConfigOption<Integer> PORT = ConfigOptions.key("port").intType().noDefaultValue();
    public static final ConfigOption<String> USER = ConfigOptions.key("user").stringType().noDefaultValue();
    public static final ConfigOption<String> PASSWORD = ConfigOptions.key("password").stringType().noDefaultValue();

    @Override
    public String factoryIdentifier() {
        return "imap";
    }

    @Override
    public Set<ConfigOption<?>> requiredOptions() {
        final Set<ConfigOption<?>> options = new HashSet<>();
        options.add(HOST);
        options.add(USER);
        options.add(PASSWORD);
        return options;
    }

    @Override
    public Set<ConfigOption<?>> optionalOptions() {
        final Set<ConfigOption<?>> options = new HashSet<>();
        options.add(PORT);
        return options;
    }

    @Override
    public DynamicTableSource createDynamicTableSource(Context ctx) {
        final FactoryUtil.TableFactoryHelper factoryHelper = FactoryUtil.createTableFactoryHelper(this, ctx);
        factoryHelper.validate();

        final ImapSourceOptions options = ImapSourceOptions.builder()
            .host(factoryHelper.getOptions().get(HOST))
            .port(factoryHelper.getOptions().get(PORT))
            .user(factoryHelper.getOptions().get(USER))
            .password(factoryHelper.getOptions().get(PASSWORD))
            .build();

        final List<String> columnNames = ctx.getCatalogTable().getResolvedSchema().getColumns().stream()
            .filter(Column::isPhysical)
            .map(Column::getName)
            .collect(Collectors.toList());

        return new ImapTableSource(options, columnNames);
    }
}
