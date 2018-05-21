package ro.dg.ioc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Loader {


    public Map<Class, Registration> loadConfiguration(String filename) throws IoCException{

        Map<Class, Registration> registrationMap = new HashMap<>();

        try {
            //final Path path = FileSystems.getDefault().getPath(filename);
            Path path = Paths.get(getClass().getClassLoader()
                    .getResource(filename).toURI());
            final String configContent = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            final ObjectMapper mapper = new ObjectMapper();
            final List<Registration> parsedReg = mapper.readValue(configContent, mapper.getTypeFactory()
                    .constructCollectionType(List.class, Registration.class));

            for (Registration reg: parsedReg){
                final Class<?> aClass = Class.forName(reg.getType());
                registrationMap.put(aClass, reg);
            }

        }catch (IOException | ClassNotFoundException | URISyntaxException e){
            throw  new IoCException(e);
        }

        return registrationMap;
    }
}
