import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class JavaSchoolStarter {
    private DataBaseJava dataCollection;

    public JavaSchoolStarter() {
        dataCollection = new DataBaseJava();
    }

    public List<Map<String, Object>> execute(String request) throws Exception {
        return dataCollection.executeCommand(request);
    }
}