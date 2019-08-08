package spring.boot.hello.world.batch;

import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.Map;

public class MultiResourcePartitionerWrapper implements Partitioner {
    private final MultiResourcePartitioner multiResourcePartitioner = new MultiResourcePartitioner();
    private final ResourcePatternResolver resourcePatternResolver;
    private final String pathPattern;

    public MultiResourcePartitionerWrapper(ResourcePatternResolver resourcePatternResolver, String pathPattern) {
        this.resourcePatternResolver = resourcePatternResolver;
        this.pathPattern = pathPattern;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        try {
            Resource[] resources = resourcePatternResolver.getResources(pathPattern);
            multiResourcePartitioner.setResources(resources);
            return multiResourcePartitioner.partition(gridSize);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
