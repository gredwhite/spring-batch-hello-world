package spring.boot.hello.world.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MultiResourcePartitioner implements Partitioner {

    private final Logger logger = LoggerFactory.getLogger(MultiResourcePartitioner.class);
    public static final String FILE_PATH = "filePath";

    private static final String PARTITION_KEY = "partition";

    private final Collection<Resource> resources;


    public MultiResourcePartitioner(Collection<Resource> resources) {
        this.resources = resources;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> map = new HashMap<>(gridSize);
        int i = 0;
        for (Resource resource : resources) {
            ExecutionContext context = new ExecutionContext();
            context.putString(FILE_PATH, getPath(resource)); //Depends on what logic you want to use to split
            map.put(PARTITION_KEY + i++, context);
        }
        return map;
    }

    private String getPath(Resource resource) {
        try {
            return resource.getFile().getPath();
        } catch (IOException e) {
            logger.warn("Can't get file from from resource {}", resource);
            throw new RuntimeException(e);
        }
    }
}
