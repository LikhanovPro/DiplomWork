package main.models;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostCommentsRepository extends CrudRepository<PostComments, Integer> {
}
