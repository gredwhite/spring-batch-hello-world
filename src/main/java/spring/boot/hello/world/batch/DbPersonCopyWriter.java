package spring.boot.hello.world.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Service;
import spring.boot.hello.world.jpa.PersonCopyRepository;
import spring.boot.hello.world.model.PersonCopy;

import java.util.List;

@Service
public class DbPersonCopyWriter implements ItemWriter<PersonCopy> {
    private final Logger logger = LoggerFactory.getLogger(DbPersonCopyWriter.class);
    private final PersonCopyRepository personRepository;

    public DbPersonCopyWriter(PersonCopyRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public void write(List<? extends PersonCopy> persons) throws Exception {
        logger.info(String.format("Save %s items", persons.size()));
        personRepository.saveAll(persons);
    }
}
