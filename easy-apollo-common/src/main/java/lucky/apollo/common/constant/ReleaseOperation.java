package lucky.apollo.common.constant;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
public class ReleaseOperation {
    public final static int NORMAL_RELEASE = 0;
    public final static int ROLLBACK = 1;
    public final static int GRAY_RELEASE = 2;
    public final static int APPLY_GRAY_RULES = 3;
    public final static int GRAY_RELEASE_MERGE_TO_MASTER = 4;
    public final static int MASTER_NORMAL_RELEASE_MERGE_TO_GRAY = 5;
    public final static int MATER_ROLLBACK_MERGE_TO_GRAY = 6;
    public final static int ABANDON_GRAY_RELEASE = 7;
    public final static int GRAY_RELEASE_DELETED_AFTER_MERGE = 8;
}