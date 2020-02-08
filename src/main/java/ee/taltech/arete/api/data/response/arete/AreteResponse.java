package ee.taltech.arete.api.data.response.arete;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import ee.taltech.arete.api.data.response.hodor_studenttester.*;
import ee.taltech.arete.api.data.response.legacy.LegacyTestJobResult;
import ee.taltech.arete.api.data.response.legacy.LegacyTestingResult;
import ee.taltech.arete.domain.Submission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonClassDescription("Response sent to ReturnUrl")
public class AreteResponse {

    final String version = "arete_2.0";

    @JsonPropertyDescription("List of style, compilation and other errors")
    List<Error> errors = new ArrayList<>();

    @JsonPropertyDescription("List of student files")
    List<File> files = new ArrayList<>();

    @JsonPropertyDescription("List of test files")
    List<File> testFiles = new ArrayList<>();

    @JsonPropertyDescription("List of test suites which each contains unit-tests. Each test file produces an test suite")
    List<TestContext> testSuites = new ArrayList<>();

    @JsonPropertyDescription("Console outputs from docker")
    List<ConsoleOutput> consoleOutputs = new ArrayList<>();

    @JsonPropertyDescription("HTML result for student")
    String output;

    @JsonPropertyDescription("Number of tests")
    Integer totalCount = 0;

    @JsonPropertyDescription("Passed percentage")
    String totalGrade = "0";

    @JsonPropertyDescription("Number of passed tests")
    Integer totalPassedCount = 0;

    @JsonPropertyDescription("Docker image used for testing")
    String testingPlatform;

    @JsonPropertyDescription("git namespace")
    String root;

    @JsonPropertyDescription("URL or ssh for test repository")
    String gitTestRepo;

    @JsonPropertyDescription("URL or ssh for student repository")
    String gitStudentRepo;

    @JsonPropertyDescription("Style percentage")
    Integer style = 100;

    @JsonPropertyDescription("Slug ran for student. for example pr01_something")
    String slug;

    @JsonPropertyDescription("values that are returned the same way they were given in")
    JsonNode returnExtra;

    @JsonPropertyDescription("Commit hash from gitlab")
    String hash;

    @JsonPropertyDescription("Students uniid")
    String uniid;

    @JsonPropertyDescription("Testing timestamp")
    Long timestamp;

    @JsonPropertyDescription("Commit message for student repository")
    String commitMessage;

    @JsonPropertyDescription("Priority of job")
    Integer priority;

    @JsonPropertyDescription("No defaults. You can add (stylecheck) or something. It is sent to smaller tester. Look the possibilities from the small tester repository for more details.")
    private Set<String> dockerExtra;

    @JsonPropertyDescription("No defaults. You can add (noMail, noFiles, noTesterFiles, noStudentFiles, noStd, noFeedback, minimalFeedback)")
    private Set<String> systemExtra;

    @JsonPropertyDescription("Default docker timeout is 120 seconds")
    private Integer dockerTimeout;

    @JsonPropertyDescription("Whether the testing was successful or not")
    Boolean failed = false;

    public AreteResponse(String slug, Submission submission, String message) { //Failed submission
        Error error = new Error.ErrorBuilder().columnNo(0).lineNo(0).fileName("tester").message(message).build();
        this.fillFromSubmission(slug, submission);
        this.output = message;
        this.errors.add(error);
        this.failed = true;
    }

    public AreteResponse(String slug, Submission submission, hodorStudentTesterResponse response) { //Successful submission

        for (TestingResult result : response.getResults()) {

            if (result.getTotalCount() != null) {
                totalCount = result.getTotalCount();
            }

            if (result.getTotalGrade() != null) {
                totalGrade = result.getTotalGrade();
            }

            if (result.getTotalPassedCount() != null) {
                totalPassedCount = result.getTotalPassedCount();
            }

            if (result.getErrors() != null) {
                for (StyleError warning : result.getErrors()) {
                    Error areteWarning = new Error.ErrorBuilder()
                            .columnNo(warning.getColumnNo())
                            .lineNo(warning.getLineNo())
                            .fileName(warning.getFileName())
                            .message(warning.getMessage())
                            .kind("style error")
                            .build();
                    errors.add(areteWarning);
                    style = 0;
                }
            }

            if (result.getFiles() != null) {
                for (HodorFile file : result.getFiles()) {
                    File areteFile = new File.FileBuilder()
                            .path(file.getPath())
                            .contents(file.getContents())
                            .build();
                    if (!submission.getSystemExtra().contains("noFiles")) {
                        if (file.getIsTest()) {
                            if (!submission.getSystemExtra().contains("noTesterFiles")) {
                                testFiles.add(areteFile);
                            }
                        } else {
                            if (!submission.getSystemExtra().contains("noStudentFiles")) {
                                files.add(areteFile);
                            }
                        }
                    }
                }
            }

            if (result.getDiagnosticList() != null) {
                for (Diagnostic warning : result.getDiagnosticList()) {
                    Error areteWarning = new Error.ErrorBuilder()
                            .columnNo(warning.getColumnNo())
                            .lineNo(warning.getLineNo())
                            .fileName(warning.getFile())
                            .message(warning.getMessage())
                            .hint(warning.getHint())
                            .kind(warning.getKind() == null ? "Diagnostic error" : warning.getKind())
                            .build();
                    errors.add(areteWarning);
                    style = 0;
                }
            }

            if (result.getTestContexts() != null) {

                List<String> PASSED = Arrays.asList("success", "passed", "ok", "yes");
                List<String> SKIPPED = Arrays.asList("partial_success", "skipped");
                List<String> FAILED = Arrays.asList("not_run", "failure", "failed", "not_set", "unknown", "no");

                for (HodorTestContext context : result.getTestContexts()) {
                    List<UnitTest> unitTests = new ArrayList<>();
                    for (HodorUnitTest test : context.getUnitTests()) {

                        UnitTest.TestStatus status;
                        if (PASSED.contains(test.getStatus().toLowerCase())) {
                            status = UnitTest.TestStatus.PASSED;
                        } else if (FAILED.contains(test.getStatus().toLowerCase())) {
                            status = UnitTest.TestStatus.FAILED;
                        } else {
                            status = UnitTest.TestStatus.SKIPPED;
                        }

                        UnitTest unitTest = new UnitTest.UnitTestBuilder()
                                .exceptionClass(test.getExceptionClass())
                                .printExceptionMessage(test.getPrintExceptionMessage())
                                .exceptionMessage(test.getExceptionMessage())
                                .groupsDependedUpon(test.getGroupsDependedUpon())
                                .methodsDependedUpon(test.getMethodsDependedUpon())
                                .printStackTrace(test.getPrintStackTrace())
                                .stackTrace(test.getStackTrace())
                                .stdout(!submission.getSystemExtra().contains("noStd") ? test.getStdout() : null)
                                .stderr(!submission.getSystemExtra().contains("noStd") ? test.getStderr() : null)
                                .name(test.getName())
                                .timeElapsed(test.getTimeElapsed())
                                .weight(test.getWeight())
                                .status(status)
                                .build();

                        unitTests.add(unitTest);
                    }

                    TestContext testContext = new TestContext.TestContextBuilder()
                            .endDate(context.getEndDate())
                            .file(context.getFile())
                            .grade(context.getGrade())
                            .name(context.getName())
                            .passedCount(context.getPassedCount())
                            .startDate(context.getStartDate())
                            .endDate(context.getEndDate())
                            .weight(context.getWeight())
                            .unitTests(unitTests)
                            .build();

                    testSuites.add(testContext);
                }
            }
        }

        output = constructOutput(submission);
        fillFromSubmission(slug, submission);
    }

    public AreteResponse(String slug, Submission submission, LegacyTestJobResult response) {

        for (LegacyTestingResult result : response.getResults()) {
            TestContext textContext = new TestContext();
            textContext.name = result.getName();
            textContext.weight = 1;
            textContext.grade = result.getPercentage();
            this.totalGrade = String.valueOf(result.getPercentage());
            this.testSuites.add(textContext);
        }
        this.output = response.getOutput();

        if (!submission.getSystemExtra().contains("noFiles")) {
            this.files = response.getFiles();
        }

        if (response.getPercent() != null) {
            this.totalGrade = String.valueOf(response.getPercent());
        }

        fillFromSubmission(slug, submission);
    }

    private static void tr(StringBuilder output) {
        output.append("<tr style='border: 1px solid black;border-collapse: collapse;padding: 5px;text-align: left;'>");
    }

    private static void td(StringBuilder output) {
        output.append("<td style='color:#D5DDE5;background:#393939;border: 1px solid black;border-collapse: collapse;padding: 5px;text-align: left;'>");
    }

    private static void td(StringBuilder output, boolean bright) {
        String hex;

        if (bright) {
            hex = "8FBC8F";
        } else {
            hex = "393939";
        }

        output.append("<td style='color:#D5DDE5;background:#").append(hex).append(";border: 1px solid black;border-collapse: collapse;padding: 5px;text-align: left;'>");
    }

    private static void td_hex(StringBuilder output, String hex) {
        output.append("<td style='color:#D5DDE5;background:#").append(hex).append(";border: 1px solid black;border-collapse: collapse;padding: 5px;text-align: left;'>");
    }

    private static void td_extra(StringBuilder output, String extra) {
        output.append("<td style='color:#D5DDE5;background:#393939;border: 1px solid black;border-collapse: collapse;padding: 5px;text-align: left;' ").append(extra).append(">");
    }

    private static void th(StringBuilder output) {
        output.append("<th style='color:#D5DDE5;background:#1b1e24;border: 1px solid black;border-collapse: collapse;padding: 5px;text-align: left;'>");
    }

    private static String getRandomQuote() {
        String[] quotes = new String[]{"Quote by Kevin Kruse: \"Life isn’t about getting and having, it’s about giving and being.\"", "Quote by Napoleon Hill: \"Whatever the mind of man can conceive and believe, it can achieve.\"", "Quote by Albert Einstein: \"Strive not to be a success, but rather to be of value.\"", "Quote by Robert Frost: \"Two roads diverged in a wood, and I—I took the one less traveled by, And that has made all the difference.\"", "Quote by Florence Nightingale: \"I attribute my success to this: I never gave or took any excuse.\"", "Quote by Wayne Gretzky: \"You miss 100% of the shots you don’t take.\"", "Quote by Michael Jordan: \"I’ve missed more than 9000 shots in my career. I’ve lost almost 300 games. 26 times I’ve been trusted to take the game winning shot and missed. I’ve failed over and over and over again in my life. And that is why I succeed.\"", "Quote by Amelia Earhart: \"The most difficult thing is the decision to act, the rest is merely tenacity.\"", "Quote by Babe Ruth: \"Every strike brings me closer to the next home run.\"", "Quote by W. Clement Stone: \"Definiteness of purpose is the starting point of all achievement.\"", "Quote by Kevin Kruse: \"We must balance conspicuous consumption with conscious capitalism.\"", "Quote by John Lennon: \"Life is what happens to you while you’re busy making other plans.\"", "Quote by Earl Nightingale: \"We become what we think about.\"", "Quote by Mark Twain: \"14.Twenty years from now you will be more disappointed by the things that you didn’t do than by the ones you did do, so throw off the bowlines, sail away from safe harbor, catch the trade winds in your sails.  Explore, Dream, Discover.\"", "Quote by Charles Swindoll: \"15.Life is 10% what happens to me and 90% of how I react to it.\"", "Quote by Alice Walker: \"The most common way people give up their power is by thinking they don’t have any.\"", "Quote by Buddha: \"The mind is everything. What you think you become.\"", "Quote by Chinese Proverb: \"The best time to plant a tree was 20 years ago. The second best time is now.\"", "Quote by Socrates: \"An unexamined life is not worth living.\"", "Quote by Woody Allen: \"Eighty percent of success is showing up.\"", "Quote by Steve Jobs: \"Your time is limited, so don’t waste it living someone else’s life.\"", "Quote by Vince Lombardi: \"Winning isn’t everything, but wanting to win is.\"", "Quote by Stephen Covey: \"I am not a product of my circumstances. I am a product of my decisions.\"", "Quote by Pablo Picasso: \"Every child is an artist.  The problem is how to remain an artist once he grows up.\"", "Quote by Christopher Columbus: \"You can never cross the ocean until you have the courage to lose sight of the shore.\"", "Quote by Maya Angelou: \"I’ve learned that people will forget what you said, people will forget what you did, but people will never forget how you made them feel.\"", "Quote by Jim Rohn: \"Either you run the day, or the day runs you.\"", "Quote by Henry Ford: \"Whether you think you can or you think you can’t, you’re right.\"", "Quote by Mark Twain: \"The two most important days in your life are the day you are born and the day you find out why.\"", "Quote by Johann Wolfgang von Goethe: \"Whatever you can do, or dream you can, begin it.  Boldness has genius, power and magic in it.\"", "Quote by Frank Sinatra: \"The best revenge is massive success.\"", "Quote by Zig Ziglar: \"People often say that motivation doesn’t last. Well, neither does bathing.  That’s why we recommend it daily.\"", "Quote by Anais Nin: \"Life shrinks or expands in proportion to one’s courage.\"", "Quote by Vincent Van Gogh: \"If you hear a voice within you say “you cannot paint,” then by all means paint and that voice will be silenced.\"", "Quote by Aristotle: \"There is only one way to avoid criticism: do nothing, say nothing, and be nothing.\"", "Quote by Jesus: \"Ask and it will be given to you; search, and you will find; knock and the door will be opened for you.\"", "Quote by Ralph Waldo Emerson: \"The only person you are destined to become is the person you decide to be.\"", "Quote by Henry David Thoreau: \"Go confidently in the direction of your dreams.  Live the life you have imagined.\"", "Quote by Erma Bombeck: \"When I stand before God at the end of my life, I would hope that I would not have a single bit of talent left and could say, I used everything you gave me.\"", "Quote by Booker T. Washington: \"Few things can help an individual more than to place responsibility on him, and to let him know that you trust him.\"", "Quote by  Ancient Indian Proverb: \"Certain things catch your eye, but pursue only those that capture the heart.\"", "Quote by Theodore Roosevelt: \"Believe you can and you’re halfway there.\"", "Quote by George Addair: \"Everything you’ve ever wanted is on the other side of fear.\"", "Quote by Plato: \"We can easily forgive a child who is afraid of the dark; the real tragedy of life is when men are afraid of the light.\"", "Quote by Maimonides: \"Teach thy tongue to say, “I do not know,” and thous shalt progress.\"", "Quote by Arthur Ashe: \"Start where you are. Use what you have.  Do what you can.\"", "Quote by John Lennon: \"When I was 5 years old, my mother always told me that happiness was the key to life.  When I went to school, they asked me what I wanted to be when I grew up.  I wrote down ‘happy’.  They told me I didn’t understand the assignment, and I told them they didn’t understand life.\"", "Quote by Japanese Proverb: \"Fall seven times and stand up eight.\"", "Quote by Helen Keller: \"When one door of happiness closes, another opens, but often we look so long at the closed door that we do not see the one that has been opened for us.\"", "Quote by Confucius: \"Everything has beauty, but not everyone can see.\"", "Quote by Anne Frank: \"How wonderful it is that nobody need wait a single moment before starting to improve the world.\"", "Quote by Lao Tzu: \"When I let go of what I am, I become what I might be.\"", "Quote by Maya Angelou: \"Life is not measured by the number of breaths we take, but by the moments that take our breath away.\"", "Quote by Dalai Lama: \"Happiness is not something readymade.  It comes from your own actions.\"", "Quote by Sheryl Sandberg: \"If you’re offered a seat on a rocket ship, don’t ask what seat! Just get on.\"", "Quote by Aristotle: \"First, have a definite, clear practical ideal; a goal, an objective. Second, have the necessary means to achieve your ends; wisdom, money, materials, and methods. Third, adjust all your means to that end.\"", "Quote by Latin Proverb: \"If the wind will not serve, take to the oars.\"", "Quote by Unknown: \"You can’t fall if you don’t climb.  But there’s no joy in living your whole life on the ground.\"", "Quote by Marie Curie: \"We must believe that we are gifted for something, and that this thing, at whatever cost, must be attained.\"", "Quote by Les Brown: \"Too many of us are not living our dreams because we are living our fears.\"", "Quote by Joshua J. Marine: \"Challenges are what make life interesting and overcoming them is what makes life meaningful.\"", "Quote by Booker T. Washington: \"If you want to lift yourself up, lift up someone else.\"", "Quote by Leonardo da Vinci: \"I have been impressed with the urgency of doing. Knowing is not enough; we must apply. Being willing is not enough; we must do.\"", "Quote by Jamie Paolinetti: \"Limitations live only in our minds.  But if we use our imaginations, our possibilities become limitless.\"", "Quote by Erica Jong: \"You take your life in your own hands, and what happens? A terrible thing, no one to blame.\"", "Quote by Bob Dylan: \"What’s money? A man is a success if he gets up in the morning and goes to bed at night and in between does what he wants to do.\"", "Quote by Benjamin Franklin: \"I didn’t fail the test. I just found 100 ways to do it wrong.\"", "Quote by Bill Cosby: \"In order to succeed, your desire for success should be greater than your fear of failure.\"", "Quote by  Albert Einstein: \"A person who never made a mistake never tried anything new.\"", "Quote by Chinese Proverb: \"The person who says it cannot be done should not interrupt the person who is doing it.\"", "Quote by Roger Staubach: \"There are no traffic jams along the extra mile.\"", "Quote by George Eliot: \"It is never too late to be what you might have been.\"", "Quote by Oprah Winfrey: \"You become what you believe.\"", "Quote by Vincent van Gogh: \"I would rather die of passion than of boredom.\"", "Quote by Unknown: \"A truly rich man is one whose children run into his arms when his hands are empty.\"", "Quote by Ann Landers: \"It is not what you do for your children, but what you have taught them to do for themselves, that will make them successful human beings.\"", "Quote by Abigail Van Buren: \"If you want your children to turn out well, spend twice as much time with them, and half as much money.\"", "Quote by Farrah Gray: \"Build your own dreams, or someone else will hire you to build theirs.\"", "Quote by Jesse Owens: \"The battles that count aren’t the ones for gold medals. The struggles within yourself–the invisible battles inside all of us–that’s where it’s at.\"", "Quote by Sir Claus Moser: \"Education costs money.  But then so does ignorance.\"", "Quote by Rosa Parks: \"I have learned over the years that when one’s mind is made up, this diminishes fear.\"", "Quote by Confucius: \"It does not matter how slowly you go as long as you do not stop.\"", "Quote by Oprah Winfrey: \"If you look at what you have in life, you’ll always have more. If you look at what you don’t have in life, you’ll never have enough.\"", "Quote by Dalai Lama: \"Remember that not getting what you want is sometimes a wonderful stroke of luck.\"", "Quote by Maya Angelou: \"You can’t use up creativity.  The more you use, the more you have.\"", "Quote by Norman Vaughan: \"Dream big and dare to fail.\"", "Quote by Martin Luther King Jr.: \"Our lives begin to end the day we become silent about things that matter.\"", "Quote by Teddy Roosevelt: \"Do what you can, where you are, with what you have.\"", "Quote by Tony Robbins: \"If you do what you’ve always done, you’ll get what you’ve always gotten.\"", "Quote by Gloria Steinem: \"Dreaming, after all, is a form of planning.\"", "Quote by Mae Jemison: \"It’s your place in the world; it’s your life. Go on and do all you can with it, and make it the life you want to live.\"", "Quote by Beverly Sills: \"You may be disappointed if you fail, but you are doomed if you don’t try.\"", "Quote by Eleanor Roosevelt: \"Remember no one can make you feel inferior without your consent.\"", "Quote by Grandma Moses: \"Life is what we make it, always has been, always will be.\"", "Quote by Ayn Rand: \"The question isn’t who is going to let me; it’s who is going to stop me.\"", "Quote by Henry Ford: \"When everything seems to be going against you, remember that the airplane takes off against the wind, not with it.\"", "Quote by Abraham Lincoln: \"It’s not the years in your life that count. It’s the life in your years.\"", "Quote by Norman Vincent Peale: \"Change your thoughts and you change your world.\"", "Quote by Benjamin Franklin: \"Either write something worth reading or do something worth writing.\"", "Quote by –Audrey Hepburn: \"Nothing is impossible, the word itself says, “I’m possible!”\"", "Quote by Steve Jobs: \"The only way to do great work is to love what you do.\"", "Quote by Zig Ziglar: \"If you can dream it, you can achieve it.\""};
        Random random = new Random();
        return quotes[random.nextInt(quotes.length)];
    }

    private void fillFromSubmission(String slug, Submission submission) {

        if (!submission.getSystemExtra().contains("noStd")) {
            consoleOutputs.add(new ConsoleOutput.ConsoleOutputBuilder().content(submission.getResult()).build());
        }

        this.returnExtra = submission.getReturnExtra();
        this.slug = slug;
        this.hash = submission.getHash();
        this.uniid = submission.getUniid();
        this.timestamp = submission.getTimestamp();
        this.commitMessage = submission.getCommitMessage();
        this.testingPlatform = submission.getTestingPlatform();
        this.root = submission.getCourse();
        this.gitStudentRepo = submission.getGitStudentRepo();
        this.gitTestRepo = submission.getGitTestSource();
        this.priority = submission.getPriority();
        this.dockerTimeout = submission.getDockerTimeout();
        this.dockerExtra = submission.getDockerExtra();
        this.systemExtra = submission.getSystemExtra();
    }

    private String constructOutput(Submission submission) {
        StringBuilder output = new StringBuilder();

        if (submission.getUniid() != null) {
            output.append(String.format("<h2>Testing results for %s</h2>", submission.getUniid()));
        } else {
            output.append("<h2>Testing results</h2>");
        }
        output.append(String.format("<p>Submission hash: %s</p>", submission.getHash()));
        if (!submission.getSystemExtra().contains("noFun")) {
            output.append(String.format("<br><p>%s</p><br><br>", getRandomQuote()));
        }

        long totalSize = 0;
        long totalPassed = 0;

        long totalWeight = 0;
        long totalPassedWeight = 0;

        errorTable(output);


        for (TestContext context : testSuites) {

            if (context.unitTests.size() != 0) {

                output.append("<br>");
                long size = context.unitTests.size();
                totalSize += size;

                long passed = context.unitTests.stream().filter(x -> x.status.equals(UnitTest.TestStatus.PASSED)).count();
                totalPassed += passed;

                long weights = context.unitTests.stream()
                        .map(x -> x.weight)
                        .mapToInt(Integer::intValue)
                        .sum();
                totalWeight += weights;

                testsTable(submission, output, context, size == passed);

                output.append(String.format("<p>Number of tests: %s</p>", size));

                output.append(String.format("<p>Passed tests: %s</p>", passed));

                output.append(String.format("<p>Total weight: %s</p>", weights));

                long passedWeights = context.unitTests.stream()
                        .filter(x -> x.status.equals(UnitTest.TestStatus.PASSED))
                        .map(x -> x.weight)
                        .mapToInt(Integer::intValue)
                        .sum();
                totalPassedWeight += passedWeights;
                output.append(String.format("<p>Passed weight: %s</p>", passedWeights));

                String totalGrade = String.format("%s", Math.round((float) passedWeights / (float) weights * 100 * 100.0) / 100.0);
                output.append(String.format("<p>Percentage: %s%s</p>", totalGrade, "%"));
            }
        }

        this.totalCount = Math.toIntExact(totalSize);
        this.totalPassedCount = Math.toIntExact(totalPassed);

        output.append("<br>");
        output.append("<br>");
        output.append("<h2>Overall</h2>");

        output.append(String.format("<p>Total number of tests: %s</p>", totalSize));
        output.append(String.format("<p>Total passed tests: %s</p>", totalPassed));
        output.append(String.format("<p>Total weight: %s</p>", totalWeight));
        output.append(String.format("<p>Total Passed weight: %s</p>", totalPassedWeight));

        String totalGrade = String.format("%s", Math.round((float) totalPassedWeight / (float) totalWeight * 100 * 100.0) / 100.0);
        this.totalGrade = totalGrade;
        output.append(String.format("<p>Total Percentage: %s%s</p>", totalGrade, "%"));

        output.append("<br>");
        output.append("<br>");

        Date date = new Date(submission.getTimestamp());
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("Europe/Tallinn"));
        String formatted = format.format(date);
        output.append(String.format("<p>Timestamp: %s</p>", formatted));
        return output.toString();
    }

    private void TestsHeader(StringBuilder output, String headerName) {
        th(output);
        output.append(headerName);
        output.append("</th>");

        th(output);
        output.append("Result");
        output.append("</th>");

        th(output);
        output.append("Time (ms)");
        output.append("</th>");

        th(output);
        output.append("Weight");
        output.append("</th>");
    }

    private void errorTable(StringBuilder output) {
        output.append("<br>");
        output.append("<table style='width:100%;border: 1px solid black;border-collapse: collapse;' id='errors'>");

        tr(output);

        th(output);
        output.append("File");
        output.append("</th>");

        th(output);
        output.append("Line");
        output.append("</th>");

        th(output);
        output.append("Column");
        output.append("</th>");

        th(output);
        output.append("Error");
        output.append("</th>");

        output.append("</tr>");

        for (Error error : errors) {

            tr(output);
            String[] name;
            if (error.fileName != null) {
                name = error.fileName.split("/");
            } else {
                name = new String[]{"null"};
            }

            String hex;
            if (error.kind == null || error.kind.toLowerCase().contains("style") || error.kind.toLowerCase().contains("diagnostic")) {
                hex = "393939";
            } else {
                hex = "8b0000";
            }

            td_hex(output, hex);
            output.append(name[name.length - 1]);
            output.append("</td>");

            td_hex(output, hex);
            output.append(error.lineNo);
            output.append("</td>");

            td_hex(output, hex);
            output.append(error.columnNo);
            output.append("</td>");

            td_hex(output, hex);
            output.append(error.message);
            output.append("</td>");

            output.append("</tr>");

            if (error.hint != null && !error.hint.equals("")) {
                tr(output);

                td(output);
                output.append("Hint");
                output.append("</td>");

                td_extra(output, "colspan='3'");
                output.append(error.hint.replace("\n", ""));
                output.append("</td>");
                output.append("</tr>");
            }
        }

        output.append("</table>");

        output.append("<br>");
        output.append(String.format("Style percentage: %s%s", style, "%"));
        output.append("<br>");
    }

    private void testsTable(Submission submission, StringBuilder output, TestContext context, boolean bright) {
        output.append("<br>");
        output.append("<table style='width:100%;border: 1px solid black;border-collapse: collapse;'>");

        String filename = context.file;
        if (filename == null) {
            filename = "test_file";
        }
        String[] file = filename.split("[/\\\\]");
        TestsHeader(output, file[file.length - 1]);

        context.unitTests.sort((o1, o2) -> {
            if (o1.status.equals(o2.status)) {
                return 0;
            }

            List<UnitTest.TestStatus> results = Arrays.asList(UnitTest.TestStatus.PASSED, UnitTest.TestStatus.SKIPPED, UnitTest.TestStatus.FAILED);

            int place1 = results.indexOf(o1.status);
            int place2 = results.indexOf(o2.status);
            return place1 < place2 ? -1 : 1;
        });

        for (UnitTest test : context.unitTests) {

            tr(output);
            td(output, bright);

            if (bright) {
                output.append(String.format("<p style='color:black;'>%s</p>", test.name));
            } else {
                output.append(test.name);
            }

            if (!submission.getSystemExtra().contains("noFeedback") && test.getPrintExceptionMessage() != null && test.getPrintExceptionMessage() && test.getExceptionMessage() != null) {

                output.append(String.format("<br><a style='color:red;'>%s</a>: ", test.getExceptionClass().equals("") ? "Exception" : test.getExceptionClass()));
                if (test.getExceptionMessage() == null || test.getExceptionMessage().equals("") || test.getExceptionClass().toLowerCase().contains("assert") && submission.getSystemExtra().contains("minimalFeedback")) {
                    output.append(" ... ");
                } else {
                    output.append(test.getExceptionMessage());
                }
            }

            if (!submission.getSystemExtra().contains("noFeedback") && test.getPrintStackTrace() != null && test.getPrintStackTrace() && test.getStackTrace() != null) {

                output.append(String.format("<br>%s:%s", "Stacktrace", test.getStackTrace()));

            }

            output.append("</td>");

            td(output, bright);

            if (test.status.equals(UnitTest.TestStatus.PASSED)) {
                output.append(String.format("<p style='color:greenyellow;'>%s</p>", test.status));
            } else if (test.status.equals(UnitTest.TestStatus.SKIPPED)) {
                output.append(String.format("<p style='color:yellow;'>%s</p>", test.status));
            } else if (test.status.equals(UnitTest.TestStatus.FAILED)) {
                output.append(String.format("<p style='color:red;'>%s</p>", test.status));
            }

            output.append("</td>");

            td(output, bright);
            if (bright) {
                output.append(String.format("<p style='color:black;'>%s</p>", test.timeElapsed));
            } else {
                output.append(test.timeElapsed);
            }
            output.append("</td>");

            td(output, bright);
            if (bright) {
                output.append(String.format("<p style='color:black;'>%s</p>", test.weight));
            } else {
                output.append(test.weight);
            }
            output.append("</td>");

            output.append("</tr>");

        }
        output.append("</table>");
    }

}
