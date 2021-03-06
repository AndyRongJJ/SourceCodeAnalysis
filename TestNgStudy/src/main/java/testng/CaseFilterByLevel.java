package testng;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by andy on 2018/9/25.
 * 按名称过滤,只测试Item为High的用例
 */
public class CaseFilterByLevel implements IMethodInterceptor {
    private List<IMethodInstance> methodsToTest = null;
    //不用执行的
    public static int totalIgnored = 0;
    public static int totalRun = 0;
    public static int totalConfigured = 0;
    private static String lerver;

    static {
        Properties properties = new Properties();
        // 使用InPutStream流读取properties文件
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/data/case"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            properties.load(bufferedReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 获取key对应的value值
        lerver = properties.getProperty("lerver");
    }


    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext iTestContext) {
        if (methodsToTest == null) {
            SortedMap<String, IMethodInstance> sortedMap = new TreeMap<String, IMethodInstance>();
            List<String> ignoredMethods = new ArrayList<String>();
            if (getCaseLevel().equals("") || getCaseLevel().equals("all")) {
                System.out.println("Test Methods is : " + "all");

                return methods;
            } else {
                System.out.println("Test Methods is : " + getCaseLevel());
                for (Iterator<IMethodInstance> it = methods.iterator(); it.hasNext(); ) {
                    IMethodInstance iMethodInstance = it.next();
                    ITestNGMethod m = iMethodInstance.getMethod();
                    String methodName = m.getConstructorOrMethod().getName();
                    String className = m.getTestClass().getRealClass().getName();
                    totalConfigured += 1;
                    if (isQualified(iMethodInstance.getMethod())) {
                        String sortKey = className + "_" + methodName;
                        sortedMap.put(sortKey, iMethodInstance);
                        totalRun += 1;
                    } else {
                        ignoredMethods.add(methodName + "(" + className + ")");
                        totalIgnored += 1;
                    }
                }
                List<IMethodInstance> rtMethods = new ArrayList<IMethodInstance>(sortedMap.values());
                ProgressTrackerListener.totalRun = totalRun;
                System.out.println("Ignored Test Methods: " + ignoredMethods);
                methodsToTest = rtMethods;
            }
        }
        return methodsToTest;
    }

    //判断是否满足自定义级别
    private boolean isQualified(ITestNGMethod iTestNGMethod) {
        boolean isQualified = false;
        Method m = iTestNGMethod.getConstructorOrMethod().getMethod();
        if ((m.getAnnotation(TestDescription.class) == null)) {
            return isQualified;
        } else if (m.getAnnotation(TestDescription.class).item() == null) {
            return isQualified;
        } else if (m.getAnnotation(TestDescription.class).item().equals(getCaseLevel())) {
            isQualified = true;
            return isQualified;
        }
        return isQualified;
    }

    //定义执行级别
    private String getCaseLevel() {
        return lerver;
    }

}
