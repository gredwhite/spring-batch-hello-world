package spring.boot.hello.process_cannot_access;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.function.Function;

@EnableBatchProcessing
@Configuration
public class BatchConfig {

    @Bean
    public Job fileProcessingJob(
            MultiResourcePartitioner partitioner,
            Step slaveStep,
            StepBuilderFactory stepBuilderFactory,
            JobBuilderFactory jobBuilderFactory
    ) {
        return jobBuilderFactory
                .get("read-file-job")
                .start(stepBuilderFactory.get("master-step")
                        .partitioner("processChunk", partitioner)
                        .step(slaveStep)
                        .build())
                .build();
    }

    @Bean
    @JobScope
    public MultiResourcePartitioner filesPartitioner(
            ResourcePatternResolver resolver,
            @Value("#{jobParameters['files.location']}") String location
    ) throws IOException {
        var partitioner = new MultiResourcePartitioner();
        partitioner.setResources(resolver.getResources(location));
        return partitioner;
    }

    @Bean
    public Step slaveStep(
            FlatFileItemReader<String> reader,
            RemovingListener removingListener,
            StepBuilderFactory stepBuilderFactory
    ) {
        return stepBuilderFactory.get("processChunk")
                .<String, String>chunk(10)
                .reader(reader)
                .processor((Function<String, String>) s -> s) //empty
                .writer(items -> { //empty
                })
                .listener(removingListener)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<String> reader(@Value("#{stepExecutionContext['fileName']}") Resource resource) {
        var reader = new FlatFileItemReader<String>();
        reader.setName("my-reader");
        reader.setResource(resource);
        reader.setLineMapper((line, lineNumber) -> line);
        return reader;
    }
}
