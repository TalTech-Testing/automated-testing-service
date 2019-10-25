package ee.taltech.arete.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@ToString
@Entity
@Getter
@Setter
public class Submission {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private String uniid;

  private String testingPlatform;
  private String returnUrl;

  private String studentRepositoryHash;
  private String userRepositoryToken;

  private String testerRepositoryHash;
  private String testerRepositoryToken;

  public Submission(
      String uniid,
      String testingPlatform,
      String returnUrl,
      String studentRepositoryHash,
      String userRepositoryToken,
      String testerRepositoryHash,
      String testerRepositoryToken) {
    this.uniid = uniid;
    this.testingPlatform = testingPlatform;
    this.returnUrl = returnUrl;
    this.studentRepositoryHash = studentRepositoryHash;
    this.userRepositoryToken = userRepositoryToken;
    this.testerRepositoryHash = testerRepositoryHash;
    this.testerRepositoryToken = testerRepositoryToken;
  }

  protected Submission() {}
}
