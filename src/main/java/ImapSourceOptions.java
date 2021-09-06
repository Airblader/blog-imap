import lombok.Data;
import lombok.experimental.SuperBuilder;
import javax.annotation.Nullable;
import java.io.Serializable;

@Data
@SuperBuilder(toBuilder = true)
public class ImapSourceOptions implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String host;
    private final @Nullable Integer port;
    private final @Nullable String user;
    private final @Nullable String password;
}
