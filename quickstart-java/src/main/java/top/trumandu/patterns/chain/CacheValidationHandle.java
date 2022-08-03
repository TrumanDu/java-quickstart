package top.trumandu.patterns.chain;

/**
 * @author Truman.P.Du
 * @date 2022/08/03
 * @description
 */
public class CacheValidationHandle extends RequestHandle {


    public CacheValidationHandle(RequestHandle nextHandle) {
        super(nextHandle);
    }

    @Override
    protected void execute(Request request) {
        System.out.println("2: cache validation!");
    }
}
