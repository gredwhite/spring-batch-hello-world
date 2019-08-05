package spring.boot.hello.world.batch;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Service;
import spring.boot.hello.world.model.Person;

@Service
public class ToLowerCasePersonProcessor implements ItemProcessor<Person, Person> {

    @Override
    public Person process(Person person) throws Exception {
        person.setFirstName(person.getFirstName().toLowerCase());
        person.setLastName(person.getLastName().toLowerCase());
        return person;
    }
}
