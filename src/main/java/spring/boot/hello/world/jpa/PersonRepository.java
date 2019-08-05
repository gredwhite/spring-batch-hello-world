package spring.boot.hello.world.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import spring.boot.hello.world.model.Person;

@Repository
public interface PersonRepository extends CrudRepository<Person, Long> {
}
