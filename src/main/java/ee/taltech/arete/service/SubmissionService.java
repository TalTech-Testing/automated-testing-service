package ee.taltech.arete.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ee.taltech.arete.domain.Submission;
import ee.taltech.arete.exception.RequestFormatException;
import ee.taltech.arete.repository.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class SubmissionService {

    private static final Logger LOG = LoggerFactory.getLogger(SubmissionService.class);
    @Autowired
    private ObjectMapper jacksonObjectMapper;
    @Autowired
    private SubmissionRepository SubmissionRepository;

    public ObjectNode getSubmissions() {
        LOG.info("Reading all Submissions from database.");
        ObjectNode objectNode = jacksonObjectMapper.createObjectNode();
        ArrayNode array = jacksonObjectMapper.valueToTree(SubmissionRepository.findAll());
        objectNode.set("courseNames", array);
        return objectNode;
    }

    public Submission getSubmissionByHash(String hash) {
        ArrayList<Submission> submissions = SubmissionRepository.findByHash(hash.toUpperCase());
        if (submissions.size() > 0) {
            LOG.info("Reading Submission hash " + submissions.get(0).getHash() + " from database.");
            return submissions.get(0);
        }
        LOG.error(String.format("Submission with hash %s was not found.", hash));
        throw new RequestFormatException(String.format("No Submission with hash: %s was not found", hash));
    }

}
