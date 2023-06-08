package top.year21.computerstore.controller;

import com.google.code.kaptcha.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.year21.computerstore.controller.exception.ValidCodeNotMatchException;
import top.year21.computerstore.entity.User;
import top.year21.computerstore.service.IUserService;
import top.year21.computerstore.utils.JsonResult;
import javax.servlet.http.HttpSession;


/**
 * @author hcxs1986
 * @version 1.0
 * @description: 处理用户请求的控制器
 * @date 2022/7/10 23:44
 */
@Slf4j
@RestController   //@Controller+@ResponseBody
@RequestMapping("/user")
public class UserController extends BaseController{

    @Autowired
    private IUserService userService;

    /*
    约定大于配置：开发思想来完成，省略大量的配置甚至注解来 编写
    1.接受数据的方式，请求处理方法的参数列闭包设置位pojo类型来接受前端的数据
    SpringBoot会将前端的url地址中的参数名和pojo类的属性名进行比较，如果这两个名称相同
    则直接将值注入到pojo类中对应的属性上

    2.接受数据的方式：请求处理方法的参数列表设置为非pojo类型
    SpringBoot会直接将请求的参数名和方法的参数名直接进行比较，如果名称
    相同则自动完成值的依赖注入
     */

    //用户注册
    @PostMapping
    public JsonResult<Void> userRegister(User user,HttpSession session,String code) { //没有返回数据就是void类型
        //从session取出验证码
        String validCode = (String) session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
        //判断验证码是否一致
        if (!validCode.equals(code)){
            throw new ValidCodeNotMatchException("验证码错误,请重试！");
        }
        //执行插入操作
        userService.userRegister(user);
        return new JsonResult<>(OK);
    }

    //用户登录
    @GetMapping
    public JsonResult<User> userLogin(User user, HttpSession session,String code){
        //将存储在session的kaptcha所生成的验证码取出
        String validCode = (String) session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
        //判断验证码是否一致
        if (!validCode.equals(code)){
            throw new ValidCodeNotMatchException("验证码错误,请重试！");
        }
        //执行登录操作
        User loginUser = userService.userLogin(user);

        /*使用session的目的：将登录返回的数据存储下来，每个页面都能拿到，这里的需求是每一个页面都会显示
        登录的用户名和id还有头像，所以将用户名和id保存到session中，将头像保存到cookie中就能在每个
        页面展示出用户名、id和头像
        */

        //分别将用户的session保存到服务端
        session.setAttribute("uid",loginUser.getUid());
        session.setAttribute("username",loginUser.getUsername());
        //优化一下传回前端的user数据，有些字段是不需要的。
        //只将用户名和uid进行回传
        User newUser = new User();
        newUser.setUsername(loginUser.getUsername());
        newUser.setUid(loginUser.getUid());
        newUser.setGender(loginUser.getGender());
        newUser.setPhone(loginUser.getPhone());
        newUser.setEmail(loginUser.getEmail());
        newUser.setAvatar(loginUser.getAvatar());

        return new JsonResult<>(OK,newUser);
    }

    //用户重置密码
    @PostMapping("/resetPassword")
    public JsonResult<Void> userResetPwd(@RequestParam("oldPassword") String oldPwd,
                                         @RequestParam("newPassword") String newPwd,
                                         HttpSession session){//session会自动传入
        userService.userResetPwd(oldPwd, newPwd, session);

        //在用户修改密码之后清除session中保存的密码
        session.setAttribute("uid",null);
        return new JsonResult<>(OK);
    }

    @GetMapping("/queryUser")
    public JsonResult<User> queryUserByUid(HttpSession session){
        Integer uid = getUserIdFromSession(session);

        User user = userService.queryUserByUid(uid);

        //将用户名、id、电话、邮箱、性别进行回传
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setUid(user.getUid());
        newUser.setGender(user.getGender());
        newUser.setPhone(user.getPhone());
        newUser.setEmail(user.getEmail());
        newUser.setAvatar(user.getAvatar());

        return new JsonResult<>(OK,newUser);
    }


    //用户个人信息更新
    @PostMapping("/updateInfo")
    public JsonResult<User> userInfoUpdate(String phone,String email,Integer gender,HttpSession session){
        //从session中取出用户名和uid
        String username = getUsernameFromSession(session);
        Integer uid = getUserIdFromSession(session);

        //更新数据
        userService.userUpdateInfo(phone, email, gender, username, uid);

        User user = userService.queryUserByUid(uid);

        //将用户名、id、电话、邮箱、性别进行回传
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setUid(user.getUid());
        newUser.setGender(user.getGender());
        newUser.setPhone(user.getPhone());
        newUser.setEmail(user.getEmail());
        newUser.setAvatar(user.getAvatar());

        return new JsonResult<>(OK,newUser);
    }

    //处理用户退出登录的请求
    @GetMapping("/exit")
    public JsonResult<Void> exitUserLoginStatus(HttpSession session){
        session.removeAttribute("username");
        session.removeAttribute("uid");
        return new JsonResult<>(OK);
    }
}
