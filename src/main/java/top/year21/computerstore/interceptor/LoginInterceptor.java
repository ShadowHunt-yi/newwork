package top.year21.computerstore.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author hcxs1986
 * @version 1.0
 * @description: 自定义的拦截器类，拦截所有请求进行判断
 * @date 2022/7/11 19:40
 */
public class LoginInterceptor implements HandlerInterceptor {
    /**
     * Description : 检测全局session对象中是否由uid数据，有则放行，没有则拦截
     * @date 2022/7/11
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器（url+Controller：映射）
     * @return boolean 返回值为true表示放行当前的请求，如果返回值为false表示拦截当前的请求
     **/
    //在调用所有处理请求的方法之前被自动调用执行的方法
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取项目工程的session
        HttpSession session = request.getSession();

        if (session.getAttribute("uid") != null){ //说明此时已登录
            return true;
        }else{ //说明未登录
            //重定向至登录页面
            response.sendRedirect("/web/login.html");
            return false;
        }
    }

    //在ModelAndView对象返回之后被调用的方法
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    //在整个请求所有关联的自愿被执行完毕最后所执行的方法
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
