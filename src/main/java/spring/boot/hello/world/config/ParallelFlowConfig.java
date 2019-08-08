package spring.boot.hello.world.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;


@Configuration
public class ParallelFlowConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Tasklet tasklet() {
        return new CountingTasklet();
    }

    @Bean
    public Flow syncFlow() {
        return new FlowBuilder<Flow>("sync_flow")
                .start(stepBuilderFactory.get("sync_flow_step1")
                        .tasklet(tasklet())
                        .build())
                .next(stepBuilderFactory.get("sync_flow_step2")
                        .tasklet(tasklet())
                        //.taskExecutor(new SimpleAsyncTaskExecutor("async-"))
                        .build())
                .build();
    }

    @Bean
    public Flow parallelFlow1() {
        return new FlowBuilder<Flow>("async_flow_1")
                .start(stepBuilderFactory.get("async_flow_1_step_1")
                        .tasklet(tasklet()).build())
                .build();
    }

    @Bean
    public Flow parallelFlow2() {
        return new FlowBuilder<Flow>("async_flow_2")
                .start(stepBuilderFactory.get("async_flow_2_step_1")
                        .tasklet(tasklet())
                        .build())
                .next(stepBuilderFactory.get("async_flow_2_step_2")
                        .tasklet(tasklet())
                        .build())
                .build();
    }

    @Bean
    public Flow wrapperFlow(TaskExecutor jobTaskExecutor) {
        return new FlowBuilder<Flow>("wrapperFlow")
                .start(parallelFlow1())
                .split(jobTaskExecutor)
                .add(parallelFlow2())
                .build();
    }

    @Bean
    public Job parallelJob(TaskExecutor jobTaskExecutor) {
        return jobBuilderFactory.get("sync_async_investigation_test")
                .incrementer(new RunIdIncrementer())
                .start(syncFlow())
                .next(wrapperFlow(jobTaskExecutor))
                .end()
                .build();
    }

    public static class CountingTasklet implements Tasklet {
        private final Logger logger = LoggerFactory.getLogger(CountingTasklet.class);

        @Override
        public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
            logger.info("BEFORE {} has been executed on thread {}", chunkContext.getStepContext().getStepName(), Thread.currentThread().getName());
            Thread.sleep(5000);
            logger.info("AFTER {} has been executed on thread {}", chunkContext.getStepContext().getStepName(), Thread.currentThread().getName());
            return RepeatStatus.FINISHED;
        }
    }
}
