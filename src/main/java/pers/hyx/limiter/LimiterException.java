package pers.hyx.limiter;

/**
 * 限流异常
 * @author heyouxin
 * @since 2021/8/3/0003 14:04
 */

public class LimiterException  extends RuntimeException {

    private String key;

    public LimiterException(String key) {
        super();
        this.key = key;
    }

    public LimiterException(String key, String message) {
        super(message);
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public Throwable fillInStackTrace() {
        return this;
    }

    public String toString() {
        return "LimiterException{key=" + this.key + "message=" + this.getMessage() + '}';
    }
}
