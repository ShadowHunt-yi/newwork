package top.year21.computerstore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.year21.computerstore.controller.exception.*;
import top.year21.computerstore.service.IUserService;
import top.year21.computerstore.utils.JsonResult;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author hcxs1986
 * @version 1.0
 * @description: 处理文件上传和下载的Controller
 * @date 2022/7/12 19:55
 */
@RestController
@RequestMapping("/file")
public class FileController extends BaseController{
    @Autowired
    private IUserService userService;

    @Value("${server.ip}")
    private String ip;
    @Value("${server.port}")
    private String port;
    //@Value("${server.filePath}")
    //线上部署才需要使用这个参数，本地运行不需要
    private String filePath;

    //设置文件限制大小
    private static final Integer fileMaxSize = 10 * 1024 * 1024;

    //设置上传文件的类型
    private static final List<String> FILE_TYPE = new ArrayList<>();

    static {
        FILE_TYPE.add("image/jpeg");
        FILE_TYPE.add("image/png");
        FILE_TYPE.add("image/bmp");
        FILE_TYPE.add("image/gif");
    }

    /**
     * Description : 处理文件的上传
     * @date 2022/7/24
     * @param file 文件名
     * @param session 自动生成的session对象
     * @return top.year21.computerstore.utils.JsonResult<java.lang.Void>
     **/
    /*
    MultipartFile接口是SpringMVC提供的一个接口，这个接口为我们包装了获取文件类型的数据
    （任何类型的file都可以接受），Springboot它整合了SpringMVC，只需要在处理请求的方法参数列表上
    声明一个参数类型为MultipartFile的参数，然后Springboot会自动将文件当中的数据赋值给这个参数

     @RequestParam表示请求中的参数，将请求中的参数注入请求处理方法的某个参数上，如果名称不一致
     则可以使用@RequestParam注解进行标记和映射

     */
    @PostMapping
    public JsonResult<Void> userAvatarUpload(MultipartFile file,
                                             HttpSession session){

        //在保存文件之前对文件做检查
        //判断文件是否为空
        if (file.isEmpty()){
            throw new FileEmptyException("上传文件为空，上传失败");
        }

        //判断文件是否超过限制
        if (file.getSize() > fileMaxSize){
            throw new FileSizeException("文件过大，上传失败");
        }

        //判断上传的文件是否为图片类型 file.getContentType()获取的是这种形式 --> text/html
        if (!FILE_TYPE.contains(file.getContentType())){
            throw new FileTypeNotMatchException("文件类型不符，上传失败");
        }

        //获取上传文件的原始名字  eg. avatar01.png
        String originalFilename = file.getOriginalFilename();

        //获取文件的后缀名
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));//里面是找到最后点出现的位置

        //使用时间戳为文件定义新的名字，生成前缀名字
        String uuidName = UUID.randomUUID().toString();

        //定义文件最终的名字
        String fileName = uuidName + suffix;

        //获取项目在服务器上的真实位置并拼凑文件最终的保存位置
        //本地的写法
        String realPath = System.getProperty("user.dir") + "/src/main/resources/static/images/img/" + fileName;

        //线上服务器的写法
//        String realPath = filePath + fileName;
        /**
         * 这里其实有两个选择：①直接将文件写入硬盘 这里选择这种
         * ②先获取存储文件的文件夹的路径，判断存储文件的文件夹是否存在，不存在则创建
         * 最后再将文件名和文件夹路径进行拼凑出最终文件的保存路径
         **/
        //虚拟创建目标文件
        File destFile = new File(realPath);

        //获取目标文件的上级目录
        File parentFile = destFile.getParentFile();

        if (!parentFile.exists()){
            //代表文件的上级目录不存在，进行创建
            parentFile.mkdirs();
        }

        //选择第一种方法，直接写入目标位置
        try {
            file.transferTo(destFile);   //将file文件中的数据写入到dest文件中
        }catch (FileStatusException e) {
            throw new FileStatusException("文件状态异常，写入失败");
        }catch (IOException e) {
            throw new FileUploadIOException("服务器或数据库写入文件失败");
        }

        //获取uid值
        Integer uid = getUserIdFromSession(session);

        //将文件的下载路径写入数据库
        String fileAccessPath = "http://" + ip + ":" + port + "/file/down/" + fileName;

        userService.userUploadImg(fileAccessPath,uid);

        return new JsonResult<>(OK);
    }

    /**
     * Description : 处理文件的下载
     * @date 2022/7/12
     * @param fileName 下载的文件名
     * @return org.springframework.http.ResponseEntity<byte[]>
     **/
    @GetMapping("/down/{name}")
    public ResponseEntity<byte[]> fileUpload(@PathVariable("name") String fileName) throws IOException {
        //读取文件
        //本地读取路径的写法
        String downFilePath = System.getProperty("user.dir") + "/src/main/resources/static/images/img/" + fileName;
        //打包后线上读取路径的写法
//        File file = new File(filePath + fileName);
//        String downFilePath =  file.getAbsolutePath();

        if (downFilePath != null){
            //创建一个输入流读入需要下载的文件
            FileInputStream inputStream = new FileInputStream(new File(downFilePath));

            //创建一个和文件所需字节大小一致的byte字节数组
            byte[] fileBytes = new byte[inputStream.available()];

            //将读入的流写入字节数据
            inputStream.read(fileBytes);

            //创建HttpHeaders对象设置响应头信息
            HttpHeaders headers = new HttpHeaders();

            //设置要下载方式以及下载文件的名字
            //Content-Disposition 通知客户端以下载的方式接受数据
            //attachment;filename= 设置下载的文件的名字
            headers.add("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName,"UTF-8"));

            //设置响应状态码
            HttpStatus statusCode = HttpStatus.OK;

            //创建ResponseEntity对象
            ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(fileBytes, headers, statusCode);

            //关闭输入流
            inputStream.close();
            //将需要下载的文件以字节数组的方式响应出去
            return responseEntity;
        }
        return null;
    }
}
