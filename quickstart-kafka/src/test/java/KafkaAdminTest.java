import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import top.trumandu.KafkaAdminTemplate;

import java.io.IOException;

/**
 * @author Truman.P.Du
 * @date 2021/09/10
 * @description
 */
public class KafkaAdminTest {
    private KafkaAdminTemplate kafkaAdminTemplate;

    @Before
    public void init() {
        kafkaAdminTemplate = new KafkaAdminTemplate("localhost:9092");
    }

    @Test
    public void test() {
    }

    @After
    public void close() {
        if (kafkaAdminTemplate != null) {
            try {
                kafkaAdminTemplate.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
