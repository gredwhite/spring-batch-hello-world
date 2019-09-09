package spring.boot.hello.process_cannot_access;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;

@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        ConfigurableApplicationContext context = SpringApplication.run(MyApplication.class, args);

        Path location = Path.of("D:\\chunk");
        JobParameters parameters = new JobParametersBuilder()
                .addString("files.location", location.toUri().resolve("*").toString()).toJobParameters();

        Job job = context.getBean("fileProcessingJob", Job.class);
        JobLauncher jobLauncher = context.getBean(JobLauncher.class);

        jobLauncher.run(job, parameters);
    }
}