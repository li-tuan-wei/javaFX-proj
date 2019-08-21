package ldh.common.testui.util;

import javafx.scene.control.TreeItem;
import ldh.common.testui.constant.ParamCategory;
import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.dao.ParamDao;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.vo.SpringParam;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by ldh on 2018/3/20.
 */
public final class SpringInitFactory {

    private static SpringInitFactory springInitFactory = new SpringInitFactory();

    private ApplicationContext applicationContext = null;

    public static SpringInitFactory getInstance() {
        return springInitFactory;
    }

    public synchronized void initSpring(TreeItem<TreeNode> treeItem) throws SQLException {
        if (applicationContext == null) {
            ParamModel springPm = getSpringParamModel(treeItem);
            if (springPm == null) {
                System.out.println("error!:::: spring config not setting" );
                return;
            }
            SpringParam springParam = JsonUtil.toObject(VarUtil.replaceLine(springPm.getValue()), SpringParam.class);
            if (springParam.getXmlConfig() != null) {
                try {
                    UiUtil.showMessage("开始运行Spring");
                    String[] configs = springParam.getXmlConfig().split(",");
                    UiUtil.showProgress(0.1);
                    applicationContext = new ClassPathXmlApplicationContext(configs);
                    UiUtil.showMessage("运行Spring成功");
                    UiUtil.showProgress(1);
                    LogUtil.log("start spring success!!!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ParamModel getSpringParamModel(TreeItem<TreeNode> treeItem) {
        TreeItem<TreeNode> temp = treeItem;// treeItem.getParent();
//        while(temp.getValue().getTreeNodeType() != TreeNodeType.Root) {
//
//        }
        for (TreeItem<TreeNode> child : temp.getChildren()) {
            if (child.getValue().getTreeNodeType() != TreeNodeType.Param) continue;
            List<ParamModel> paramModelList = null;
            try {
                paramModelList = ParamDao.getByTreeNodeId(child.getValue().getId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            for (ParamModel pm : paramModelList) {
                if (pm.getParamCategory() == ParamCategory.Spring) {
                    return pm;
                }
            }
//                temp = temp.getParent();
        }
        return null;
    }

    public Object getBean(String resourceName) {
        return applicationContext.getBean(resourceName);
    }

    public Object getBean(Class clazz) {
        Service service = (Service) clazz.getAnnotation(Service.class);
        if (service != null && !service.value().equals("")) {
            return applicationContext.getBean(service.value(), clazz);
        }
        Component component = (Component) clazz.getAnnotation(Component.class);
        if (component != null && !component.value().equals("")) {
            return applicationContext.getBean(component.value(), clazz);
        }
        Repository repository = (Repository) clazz.getAnnotation(Repository.class);
        if (repository != null && !repository.value().equals("")) {
            return applicationContext.getBean(repository.value(), clazz);
        }
        Controller controller = (Controller) clazz.getAnnotation(Controller.class);
        if (controller != null && !controller.value().equals("")) {
            return applicationContext.getBean(controller.value(), clazz);
        }
        return applicationContext.getBean(clazz);
    }

    public void close() {
        if (applicationContext instanceof AbstractApplicationContext) {
            try {
                ((AbstractApplicationContext) applicationContext).close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
