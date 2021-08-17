import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import top.trumandu.RedisTemplate;

/**
 * @author Truman.P.Du
 * @date 2021/07/30
 * @description
 */
public class RedisTemplateTest {
    private RedisTemplate redisTemplate;

    @Before
    public void init(){
         redisTemplate = new RedisTemplate.RedisBuilder("192.168.0.0.1:6379").password("aaaaaa").keyPrefix("test").builder();
    }

    @Test
    public void testGet(){
        redisTemplate.set("test","",60);
        Assert.assertEquals("",redisTemplate.get("test"));
        Assert.assertEquals(null,redisTemplate.get("truman"));
    }
}
