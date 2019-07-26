package lucky.apollo.portal.constant;

/**
 * @Author luckylau
 * @Date 2019/7/18
 */
public class PermissionType {


    /**
     * system level permission
     */
    public static final String MANAGE_APP_MASTER = "ManageAppMaster";

    public static final String CREATE_APPLICATION = "CreateApplication";

    /**
     * APP level permission
     */

    public static final String CREATE_NAMESPACE = "CreateNamespace";


    /**
     * 分配用户权限的权限
     */
    public static final String ASSIGN_ROLE = "AssignRole";

    /**
     * namespace level permission
     */

    public static final String MODIFY_NAMESPACE = "ModifyNamespace";

    public static final String RELEASE_NAMESPACE = "ReleaseNamespace";

}