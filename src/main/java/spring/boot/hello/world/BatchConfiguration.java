package spring.boot.hello.world;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import spring.boot.hello.world.batch.DbPersonWriter;
import spring.boot.hello.world.batch.ToLowerCasePersonProcessor;
import spring.boot.hello.world.model.Person;

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

    @Value("${app.users-location}")
    Resource csvResource;

    @Bean
    public Job job() {
        return jobBuilderFactory.get("myJob")
                .incrementer(new RunIdIncrementer())
                .flow(csvToDataBaseStep())
                .end()
                .build();
    }

    private Step csvToDataBaseStep() {
        return stepBuilderFactory.get("csvToDatabaseStep")
                .<Person, Person>chunk(100)
                .reader(csvPersonReader())
                .processor(toLowerCasePersonProcessor)
                .writer(dbPersonWriter)
                .build();

    }

    public FlatFileItemReader csvPersonReader() {
        return new FlatFileItemReaderBuilder()
                .name("csvPersonReader")
                .resource(csvResource)
                .delimited()
                .names(new String[]{"firstName", "lastName"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                    setTargetType(Person.class);
                }})
                .build();

    }

}
