package ee.taltech.arete.repository;

import ee.taltech.arete.domain.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    ArrayList<Submission> findByHash(@Param("hash") String hash);

}
