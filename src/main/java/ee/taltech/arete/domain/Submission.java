package ee.taltech.arete.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@ToString
@Entity
@Getter
@Setter
@Table(name = "submission")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    private String uniid;
    private String hash;

    private String testingPlatform;
    private String returnUrl;

    private String[] extra;


    public Submission(
            String uniid,
            String hash,
            String testingPlatform,
            String returnUrl,
            String[] extra) {
        this.uniid = uniid;
        this.hash = hash;
        this.testingPlatform = testingPlatform;
        this.returnUrl = returnUrl;
        this.extra = extra;
    }

    protected Submission() {
    }
}
