package top.trumandu.patterns.chain;

/**
 * @author Truman.P.Du
 * @date 2022/08/03
 * @description 责任链模式
 */
public class Client {
    public static void main(String[] args) {
        Request request = new Request("hello chain");
        RequestHandle chain = new ParamValidationHandle(new CacheValidationHandle(new DataCheckHandle(null)));

        chain.handleRequest(request);

    }
}
