package top.trumandu.patterns.chain;

/**
 * @author Truman.P.Du
 * @date 2022/08/03
 * @description
 */
public class DataCheckHandle extends RequestHandle {

    public DataCheckHandle(RequestHandle nextHandle) {
        super(nextHandle);
    }

    @Override
    protected void execute(Request request) {
        System.out.println("3: data check!");
    }
}
