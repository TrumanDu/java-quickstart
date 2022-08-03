package top.trumandu.patterns.chain;

/**
 * @author Truman.P.Du
 * @date 2022/08/03
 * @description
 */
public class Request {
    private String msg;
    private boolean handled;


    public Request(String msg) {
        this.msg = msg;
    }

    public void markHandled() {
        this.handled = true;
    }

    public boolean isHandled() {
        return this.handled;
    }

}
