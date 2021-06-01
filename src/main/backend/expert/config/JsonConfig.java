package expert.config;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedHashMap;

@Component
public class JsonConfig {
    @Bean
    public LinkedHashMap<String, String> aliasesMap() throws FileNotFoundException {
        //JsonReader reader = new JsonReader(new FileReader("D:\\Projects\\ExpertRestRELEASE\\src\\main\\resources\\static\\js\\assets\\json\\aliases.json"));
        //return new Gson().fromJson(reader, LinkedHashMap.class);
        return new LinkedHashMap<>();
    }

    @Bean
    public LinkedHashMap<String, String> groupsMap() throws FileNotFoundException {
        //JsonReader reader = new JsonReader(new FileReader("D:\\Projects\\ExpertRestRELEASE\\src\\main\\resources\\static\\js\\assets\\json\\groups.json"));
        //return new Gson().fromJson(reader, LinkedHashMap.class);
        return new LinkedHashMap<>();
    }
}
