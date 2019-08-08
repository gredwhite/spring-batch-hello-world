package spring.boot.hello.world.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import spring.boot.hello.world.batch.DbPersonWriter;
import spring.boot.hello.world.batch.FileSplitterTasklet;
import spring.boot.hello.world.batch.MultiResourcePartitionerWrapper;
import spring.boot.hello.world.batch.ToLowerCasePersonProcessor;
import spring.boot.hello.world.model.Person;

import java.io.IOException;
import java.net.MalformedURLException;

@Configuration
public class SingleFilePartitionedJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private ToLowerCasePersonProcessor toLowerCasePersonProcessor;

    @Autowired
    private DbPersonWriter dbPersonWriter;

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;


    @Value("${app.file-to-split}")
    private Resource resource;


    @Bean
    public Job splitFileProcessingJob() throws IOException {
        return jobBuilderFactory.get("splitFileProcessingJob")
                .incrementer(new RunIdIncrementer())
                .flow(splitFileIntoPiecesStep())
                .next(csvToDbLowercaseMasterStep())
                .end()
                .build();
    }

    private Step splitFileIntoPiecesStep() throws IOException {
        return stepBuilderFactory.get("splitFile")
                .tasklet(new FileSplitterTasklet(resource.getFile()))
                .build();
    }

    @Bean
    public Step csvToDbLowercaseMasterStep() throws IOException {
        MultiResourcePartitionerWrapper partitioner = new MultiResourcePartitionerWrapper(resourcePatternResolver, "split/*.csv");
        return stepBuilderFactory.get("csvReaderMasterStep")
                .partitioner("csvReaderMasterStep", partitioner)
                .gridSize(10)
                .step(csvToDataBaseSlaveStep())
                .taskExecutor(jobTaskExecutorSplitted())
                .build();
    }

    @Bean
    public Step csvToDataBaseSlaveStep() throws MalformedURLException {
        return stepBuilderFactory.get("csvToDatabaseStep")
                .<Person, Person>chunk(50)
                .reader(csvPersonReaderSplitted(null))
                .processor(toLowerCasePersonProcessor)
                .writer(dbPersonWriter)
                .build();

    }

    @Bean
    @StepScope
    public FlatFileItemReader csvPersonReaderSplitted(@Value("#{stepExecutionContext[fileName]}") String fileName) throws MalformedURLException {
        return new FlatFileItemReaderBuilder()
                .name("csvPersonReaderSplitted")
                .resource(new UrlResource(fileName))
                .delimited()
                .names(new String[]{"firstName", "lastName"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                    setTargetType(Person.class);
                }})
                .build();

    }

    @Bean
    public TaskExecutor jobTaskExecutorSplitted() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(30);
        taskExecutor.setCorePoolSize(25);
        taskExecutor.setThreadNamePrefix("cust-job-exec2-");
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }
}
