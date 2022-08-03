package top.trumandu.patterns.chain;

import java.util.Objects;

/**
 * @author Truman.P.Du
 * @date 2022/08/03
 * @description
 */
public abstract class RequestHandle {

    public RequestHandle nextHandle;

    public RequestHandle(RequestHandle nextHandle) {
        this.nextHandle = nextHandle;
    }

    public void handleRequest(Request request) {
        if (request.isHandled()) {
            return;
        }
        execute(request);
        if (!request.isHandled() && Objects.nonNull(nextHandle)) {
            nextHandle.handleRequest(request);
        }
    }

    /**
     * 处理请求
     *
     * @param request
     */
    protected abstract void execute(Request request);
}
