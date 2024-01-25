package top.trumandu;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson2.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Truman.P.Du
 * @date 2024/01/16
 */
@SpringBootTest
public class ElasticClientTest {
    @Autowired
    ElasticSearchClient elasticLogClient;

    @Test
    public void testSendError() {
        try {
            elasticLogClient.index("test", JSONObject.parseObject("{\n" +
                    "  \"code\": 200,\n" +
                    "  \"message\": \"OK\"\n" +
                    "}"));
            Assert.state(true);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.state(false);
        }
    }
}
