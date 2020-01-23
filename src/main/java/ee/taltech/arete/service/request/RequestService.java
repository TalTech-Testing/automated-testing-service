package ee.taltech.arete.service.request;

import ee.taltech.arete.api.data.response.arete.AreteResponse;
import ee.taltech.arete.domain.Submission;
import org.springframework.http.HttpEntity;

public interface RequestService {

    Submission testAsync(HttpEntity<String> httpEntity);

    AreteResponse testSync(HttpEntity<String> httpEntity);

    void waitingroom(HttpEntity<String> httpEntity, String hash);

    String updateImage(String image);

    String updateTests(HttpEntity<String> httpEntity);

}
