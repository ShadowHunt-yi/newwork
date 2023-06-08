package top.year21.computerstore;

import com.github.pagehelper.PageHelper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.Properties;

@SpringBootApplication
@MapperScan("top.year21.computerstore.mapper")    //指定当前项目中的Mapper接口路径，在项目启动的时候会自动加载所有的接口
public class ComputerstoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComputerstoreApplication.class, args);
    }

    @Bean
    public PageHelper pageHelper() {
        PageHelper pageHelper = new PageHelper();
        Properties properties = new Properties();
        properties.setProperty("offsetAsPageNum", "true");
        properties.setProperty("rowBoundsWithCount", "true");
        properties.setProperty("reasonable", "true");
        properties.setProperty("dialect", "mysql");
        pageHelper.setProperties(properties);
        return pageHelper;
    }
}
