package spring.boot.hello.world.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import spring.boot.hello.world.batch.*;
import spring.boot.hello.world.model.Person;
import spring.boot.hello.world.model.PersonCopy;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Arrays;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DbPersonWriter dbPersonWriter;

    @Autowired
    private ToLowerCasePersonProcessor toLowerCasePersonProcessor;

    @Autowired
    private MultiResourcePartitioner multiResourcePartitioner;

    @Autowired
    private FlatFileItemReader csvPersonReader;


//    @Value("${app.users-location}")
//    Resource[] resources;
//
//    @PostConstruct
//    public void init() {
//        System.out.println(resources);
//    }

    @Bean
    public Job job(Step databaseToDataBaseLowercaseSlaveStep) {
        return jobBuilderFactory.get("myJob")
                .incrementer(new RunIdIncrementer())
                .flow(csvToDbLowercaseStep())
                .next(databaseToDataBaseLowercaseSlaveStep)
                .end()
                .build();
    }

    private Step csvToDbLowercaseStep() {
        return stepBuilderFactory.get("csvToDbLowercaseStep")
                .partitioner("csvToDbLowercaseStep", multiResourcePartitioner)
                .gridSize(10)
                .step(csvToDataBaseSlaveStep())
                .taskExecutor(jobTaskExecutor())
                .build();
    }

    private Step csvToDataBaseSlaveStep() {
        return stepBuilderFactory.get("csvToDatabaseStep")
                .<Person, Person>chunk(50)
                .reader(csvPersonReader)
                .processor(toLowerCasePersonProcessor)
                .writer(dbPersonWriter)
                .build();

    }

    @Bean
    @StepScope
    public FlatFileItemReader csvPersonReader(@Value("#{stepExecutionContext[filePath]}") String filePath) {
        return new FlatFileItemReaderBuilder()
                .name("csvPersonReaderSplitted")
                .resource(new FileSystemResource(filePath))
                .delimited()
                .names(new String[]{"firstName", "lastName"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                    setTargetType(Person.class);
                }})
                .build();

    }

    @Bean
    public TaskExecutor jobTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        // there are 21 sites currently hence we have 21 threads
        taskExecutor.setMaxPoolSize(30);
        taskExecutor.setCorePoolSize(25);
        taskExecutor.setThreadGroupName("cust-job-exec-");
        taskExecutor.setThreadNamePrefix("cust-job-exec-");
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }

    @Bean
    public MultiResourcePartitioner multiResourcePartitioner(@Value("${app.users-location}") String csvFolderPath,
                                                             ResourcePatternResolver resourcePatternResolver) {
        Resource[] csvResources;
        try {
            csvResources = resourcePatternResolver.getResources(csvFolderPath);
        } catch (IOException e) {
            throw new RuntimeException("I/O problems when resolving the input file pattern.", e);
        }
        return new MultiResourcePartitioner(Arrays.asList(csvResources));
    }

    //second step
    @Bean
    public JdbcCursorItemReader<Person> sqlPersonReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Person>()
                .name("sqlPersonReader")
                .<Person>beanRowMapper(Person.class)
                .sql("select first_name, last_name from people")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Step databaseToDataBaseLowercaseSlaveStep(ToUpperCasePersonProcessor toUpperCasePersonProcessor,
                                                     JdbcCursorItemReader<Person> jdbcCursorItemReader,
                                                     DbPersonCopyWriter dbPersonCopyWriter) {
        return stepBuilderFactory.get("databaseToDataBaseLowercaseSlaveStep")
                .<Person, PersonCopy>chunk(50)
                .reader(jdbcCursorItemReader)
                .processor(toUpperCasePersonProcessor)
                .writer(dbPersonCopyWriter)
                .build();

    }


}
