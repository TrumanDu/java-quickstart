package top.trumandu.patterns.chain;

/**
 * @author Truman.P.Du
 * @date 2022/08/03
 * @description
 */
public class ParamValidationHandle extends RequestHandle {


    public ParamValidationHandle(RequestHandle nextHandle) {
        super(nextHandle);
    }

    @Override
    protected void execute(Request request) {
        System.out.println("1: param validation!");
    }
}
