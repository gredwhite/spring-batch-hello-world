package spring.boot.hello.world.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import spring.boot.hello.world.model.Person;

public class NotificationTasklet implements Tasklet {
    private final Logger logger = LoggerFactory.getLogger(NotificationTasklet.class);
    private final JdbcCursorItemReader<Person> jdbcCursorItemReader;

    public NotificationTasklet(JdbcCursorItemReader<Person> jdbcCursorItemReader) {
        this.jdbcCursorItemReader = jdbcCursorItemReader;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Person person = null;
        int i = 0;
        jdbcCursorItemReader.open(new ExecutionContext());
        while ((person = jdbcCursorItemReader.read()) != null) {
            //logger.info("Send {} email for user {}", i, person);
            if (i++ % 100 == 0) {
                Thread.sleep(100);
            }
        }
        logger.info("Sent {} emails", i);
        return RepeatStatus.FINISHED;
    }
}
